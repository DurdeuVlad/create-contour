package net.createcontour.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.createcontour.block.ContourBoardBlock;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.item.ContourPlacerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Placement preview (issue #32). While the local player holds a Contour
 * shape item or the placer gun and the aim ray lands on a block, an
 * arrow decal on the clicked face shows the resulting placement
 * orientation and a translucent "shadow" previews how the block will
 * sit. The preview resolves through the SAME placement contexts the
 * real gestures use - ContourPlaceContext for shape items, the gun's
 * own face-center BlockPlaceContext for the placer - so merge/grow
 * logic (layers, slope layers, boards, slab double) stays faithful.
 * Original implementation; nothing here is derived from another mod.
 */
@EventBusSubscriber(modid = "create_contour", value = Dist.CLIENT)
public final class PlacementPreviewRenderer {

	private static final ResourceLocation ARROW_TEXTURE = ResourceLocation
		.fromNamespaceAndPath("create_contour",
			"textures/misc/placement_arrow.png");
	/** Decal lift above the clicked face (z-fight margin). */
	private static final float FACE_OFFSET = 0.01f;
	/** Arrow decal half-extent in blocks. */
	private static final double ARROW_HALF = 0.28;
	private static final double MIN_PROJ_LEN2 = 1.0E-4;
	private static boolean warned;

	/**
	 * Ghost fill render type: mirrors vanilla debug_filled_box but
	 * writes color only - no depth - so the arrow decal and the crisp
	 * outline drawn afterwards are never occluded by the translucent
	 * volume itself.
	 */
	private static final RenderType GHOST_FILL = RenderType.create(
		"create_contour_ghost_fill",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.QUADS,
		1536,
		false,
		true,
		RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
			.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(
				RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(RenderStateShard.COLOR_WRITE)
			.setCullState(RenderStateShard.NO_CULL)
			.createCompositeState(false));

	private PlacementPreviewRenderer() {
	}

	/** What placement would produce, plus where the ray landed. */
	private record Preview(BlockState state, BlockPos placePos,
		BlockHitResult hit) {
	}

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		if (level == null || player == null || mc.screen != null
			|| mc.options.hideGui)
			return;
		if (!(mc.hitResult instanceof BlockHitResult hit)
			|| hit.getType() != HitResult.Type.BLOCK)
			return;
		try {
			Preview preview = resolve(level, player, hit);
			if (preview != null)
				render(event, mc, level, player, preview);
		} catch (RuntimeException e) {
			// A preview must never break the render loop - log once.
			if (!warned) {
				warned = true;
				com.mojang.logging.LogUtils.getLogger()
					.warn("[contour] placement preview failed", e);
			}
		}
	}

	// --- resolution (mirrors the real placement gestures) ---

	@Nullable
	private static Preview resolve(ClientLevel level, LocalPlayer player,
		BlockHitResult hit) {
		ItemStack main = player.getMainHandItem();
		// The gun places on LEFT click - main hand only.
		if (main.getItem() instanceof ContourPlacerItem)
			return forGun(level, player, main, hit);
		if (main.getItem() instanceof ContourBlockItem cbi)
			return forShapeItem(level, player, main,
				InteractionHand.MAIN_HAND, hit, cbi);
		ItemStack off = player.getOffhandItem();
		if (off.getItem() instanceof ContourBlockItem cbi)
			return forShapeItem(level, player, off,
				InteractionHand.OFF_HAND, hit, cbi);
		return null;
	}

	/** Shape item path: identical to ContourBlockItem.useOn -> place. */
	private static Preview forShapeItem(ClientLevel level,
		LocalPlayer player, ItemStack held, InteractionHand hand,
		BlockHitResult hit, ContourBlockItem item) {
		BlockPlaceContext ctx = new ContourBlockItem.ContourPlaceContext(
			new UseOnContext(level, player, hand, held, hit));
		return gate(level, player, ctx,
			item.getBlock().getStateForPlacement(ctx), hit);
	}

	/**
	 * Gun path: ContourPlacerItem.tryPlace builds a BlockPlaceContext
	 * with the hit relocated to the clicked face's center and the GUN
	 * stack in hand (so item-sensitive merge gates resolve exactly as
	 * the real placement does).
	 */
	private static Preview forGun(ClientLevel level, LocalPlayer player,
		ItemStack gun, BlockHitResult hit) {
		Block block = ContourPlacerItem.selectedBlock(gun);
		Direction face = hit.getDirection();
		BlockPos pos = hit.getBlockPos();
		Vec3 hitVec = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5,
			face.getStepY() * 0.5, face.getStepZ() * 0.5);
		BlockPlaceContext ctx = new BlockPlaceContext(player,
			InteractionHand.MAIN_HAND, gun,
			new BlockHitResult(hitVec, face, pos, false));
		return gate(level, player, ctx, block.getStateForPlacement(ctx),
			hit);
	}

	/**
	 * Same gates BlockItem.place applies: canPlace, non-null state,
	 * canSurvive and unobstructed - the preview only draws when the
	 * real placement would succeed.
	 */
	@Nullable
	private static Preview gate(ClientLevel level, LocalPlayer player,
		BlockPlaceContext ctx, @Nullable BlockState state,
		BlockHitResult hit) {
		if (!ctx.canPlace() || state == null)
			return null;
		BlockPos placePos = ctx.getClickedPos();
		if (!state.canSurvive(level, placePos)
			|| !level.isUnobstructed(state, placePos,
				CollisionContext.of(player)))
			return null;
		return new Preview(state, placePos, hit);
	}

	// --- rendering ---

	private static void render(RenderLevelStageEvent event, Minecraft mc,
		ClientLevel level, LocalPlayer player, Preview p) {
		MultiBufferSource.BufferSource buffers = mc.renderBuffers()
			.bufferSource();
		Vec3 cam = event.getCamera().getPosition();
		PoseStack pose = event.getPoseStack();
		pose.pushPose();
		pose.translate(-cam.x, -cam.y, -cam.z);
		VoxelShape shape = previewShape(level, player, p);
		// Order matters: fills first (no depth writes), then the face
		// decal, then the crisp outline on top.
		if (shape != null && !shape.isEmpty())
			renderGhostFill(pose, buffers, p, shape);
		renderArrow(pose, buffers, p);
		if (shape != null && !shape.isEmpty())
			renderGhostOutline(pose, buffers, p, shape);
		pose.popPose();
	}

	@Nullable
	private static VoxelShape previewShape(ClientLevel level,
		LocalPlayer player, Preview p) {
		try {
			return p.state().getShape(level, p.placePos(),
				CollisionContext.of(player));
		} catch (RuntimeException e) {
			return null;
		}
	}

	/** Feature 2a: translucent fill of the would-be shape. */
	private static void renderGhostFill(PoseStack pose,
		MultiBufferSource.BufferSource buffers, Preview p,
		VoxelShape shape) {
		VertexConsumer fill = buffers.getBuffer(GHOST_FILL);
		for (AABB box : shape.toAabbs())
			fillBox(fill, pose, box.move(p.placePos()),
				0.55f, 0.75f, 1.0f, 0.25f);
		buffers.endBatch(GHOST_FILL);
	}

	/** Feature 2b: crisp white outline of the would-be shape. */
	private static void renderGhostOutline(PoseStack pose,
		MultiBufferSource.BufferSource buffers, Preview p,
		VoxelShape shape) {
		VertexConsumer lines = buffers.getBuffer(RenderType.lines());
		for (AABB box : shape.toAabbs()) {
			AABB world = box.move(p.placePos());
			LevelRenderer.renderLineBox(pose, lines, world,
				1.0f, 1.0f, 1.0f, 0.9f);
		}
		buffers.endBatch(RenderType.lines());
	}

	/** Emits the six faces of {@code box} as POSITION_COLOR quads. */
	private static void fillBox(VertexConsumer vc, PoseStack pose,
		AABB box, float r, float g, float b, float a) {
		float x0 = (float) box.minX, y0 = (float) box.minY,
			z0 = (float) box.minZ;
		float x1 = (float) box.maxX, y1 = (float) box.maxY,
			z1 = (float) box.maxZ;
		PoseStack.Pose last = pose.last();
		// bottom / top
		quad(vc, last, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1,
			r, g, b, a);
		quad(vc, last, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1,
			r, g, b, a);
		// north / south
		quad(vc, last, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0,
			r, g, b, a);
		quad(vc, last, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1,
			r, g, b, a);
		// west / east
		quad(vc, last, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0,
			r, g, b, a);
		quad(vc, last, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0,
			r, g, b, a);
	}

	private static void quad(VertexConsumer vc, PoseStack.Pose pose,
		float ax, float ay, float az, float bx, float by, float bz,
		float cx, float cy, float cz, float dx, float dy, float dz,
		float r, float g, float b, float a) {
		vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a);
		vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a);
		vc.addVertex(pose, cx, cy, cz).setColor(r, g, b, a);
		vc.addVertex(pose, dx, dy, dz).setColor(r, g, b, a);
	}

	/** Feature 1: textured chevron decal on the clicked face. */
	private static void renderArrow(PoseStack pose,
		MultiBufferSource.BufferSource buffers, Preview p) {
		Direction face = p.hit().getDirection();
		Vec3 normal = new Vec3(face.getStepX(), face.getStepY(),
			face.getStepZ());
		Vec3 dir = arrowDirection(p, normal);
		if (dir == null)
			return;
		// In-plane perpendicular across the arrow's width.
		Vec3 side = normal.cross(dir).normalize();
		Vec3 center = clampToFace(
			p.hit().getLocation().add(normal.scale(FACE_OFFSET)),
			p.hit().getBlockPos(), face);
		Vec3 along = dir.scale(ARROW_HALF);
		Vec3 across = side.scale(ARROW_HALF);
		// rendertype_text: texture * vertexColor * lightmap - the
		// entity translucent types would instead dim the decal by
		// directional shading on the face normal. Full-bright UV2
		// makes the quad sample the texture unlit, so the aim
		// marker stays legible in the dark; drawn after the ghost
		// fills (which write no depth) it always reads on top.
		RenderType type = RenderType.text(ARROW_TEXTURE);
		VertexConsumer vc = buffers.getBuffer(type);
		int light = LightTexture.FULL_BRIGHT;
		PoseStack.Pose last = pose.last();
		// Texture "up" (v=0) points along `dir` - the chevron tip.
		quadVertex(vc, last, center.subtract(across).add(along), 0, 0,
			light);
		quadVertex(vc, last, center.add(across).add(along), 1, 0,
			light);
		quadVertex(vc, last, center.add(across).subtract(along), 1, 1,
			light);
		quadVertex(vc, last, center.subtract(across).subtract(along), 0, 1,
			light);
		buffers.endBatch(type);
	}

	/** POSITION_COLOR_TEX_LIGHTMAP vertex: position+color+uv+light. */
	private static void quadVertex(VertexConsumer vc, PoseStack.Pose pose,
		Vec3 pos, float u, float v, int light) {
		vc.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
			.setColor(255, 255, 255, 255)
			.setUv(u, v)
			.setLight(light);
	}

	/**
	 * The direction the arrow should point, in the clicked face's
	 * plane: the placed state's primary facing projected onto the
	 * face, else the click->placement-cell line projected, else the
	 * click->face-center line (also the degenerate-center fallback).
	 */
	@Nullable
	private static Vec3 arrowDirection(Preview p, Vec3 normal) {
		BlockState state = p.state();
		Vec3 dir = null;
		if (state.hasProperty(BlockStateProperties.FACING))
			dir = Vec3.atLowerCornerOf(
				state.getValue(BlockStateProperties.FACING).getNormal());
		else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			dir = Vec3.atLowerCornerOf(state
				.getValue(BlockStateProperties.HORIZONTAL_FACING)
				.getNormal());
		else if (state.getBlock() instanceof ContourBoardBlock)
			// Board sheets mount on the clicked plane: the mounted
			// face's inward normal is perpendicular to the decal, so
			// the projected fallbacks below aim at the sheet itself.
			dir = normal;
		if (dir != null) {
			Vec3 proj = dir.subtract(normal.scale(dir.dot(normal)));
			if (proj.lengthSqr() > MIN_PROJ_LEN2)
				return proj.normalize();
		}
		Vec3 toCell = Vec3.atCenterOf(p.placePos())
			.subtract(p.hit().getLocation());
		Vec3 proj = toCell.subtract(normal.scale(toCell.dot(normal)));
		if (proj.lengthSqr() > MIN_PROJ_LEN2)
			return proj.normalize();
		Vec3 faceCenter = Vec3.atLowerCornerOf(p.hit().getBlockPos())
			.add(0.5, 0.5, 0.5).add(normal.scale(0.5));
		Vec3 toCenter = faceCenter.subtract(p.hit().getLocation());
		proj = toCenter.subtract(normal.scale(toCenter.dot(normal)));
		if (proj.lengthSqr() > MIN_PROJ_LEN2)
			return proj.normalize();
		// Dead-center click: fall back to a canonical tangent so the
		// decal still shows.
		return normal.y != 0 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
	}

	/**
	 * Keep the decal centered on the clicked block face (the quad may
	 * still overhang a rim, but its center stays on the face).
	 */
	private static Vec3 clampToFace(Vec3 c, BlockPos cell, Direction face) {
		double min = ARROW_HALF * 0.5;
		double x = clampAxis(c.x, cell.getX(), min,
			face.getAxis() != Direction.Axis.X);
		double y = clampAxis(c.y, cell.getY(), min,
			face.getAxis() != Direction.Axis.Y);
		double z = clampAxis(c.z, cell.getZ(), min,
			face.getAxis() != Direction.Axis.Z);
		return new Vec3(x, y, z);
	}

	private static double clampAxis(double v, int cell, double margin,
		boolean tangential) {
		if (!tangential)
			return v;
		return Math.max(cell + margin, Math.min(cell + 1 - margin, v));
	}
}
