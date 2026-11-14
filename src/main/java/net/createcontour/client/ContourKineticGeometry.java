package net.createcontour.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.content.decoration.copycat.CopycatModel;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.NeoForgeSinglePosVirtualBlockGetter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.createcontour.material.MaterialSafety;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * Painted-mesh geometry providers for the kinetic cogwheel (issue #23).
 * Both render paths share the same source: the wrapped blockstate model,
 * asked for its {@link ContourCogwheelModel#MESH_PROPERTY} path with the
 * BE's material supplied through ModelData - identical to how chunk
 * meshing would emit, but captured into a reusable mesh instead.
 *
 * - {@link #meshModel}: a Flywheel {@link Model} produced by Create's
 *   own {@link BlockModelBuilder} + virtual getter, consumed by the
 *   instanced RotatingInstance (same path Create uses for its partials).
 * - {@link #buffers}: per-RenderType {@link SuperByteBuffer}s produced
 *   by emitting the model's BakedQuads into a vanilla {@link BufferBuilder}
 *   - the documented baked fallback when Flywheel visuals are off.
 *
 * Caches are keyed on (axis, material) and bounded; materials in a world
 * are few and the neutral entry is shared.
 */
public final class ContourKineticGeometry {

	private ContourKineticGeometry() {
	}

	private record Key(Direction.Axis axis, @Nullable BlockState material) {
	}

	/** One render-layer slice of the baked fallback buffer. */
	public record BufferEntry(RenderType type, SuperByteBuffer buffer) {
	}

	private static final int CACHE_LIMIT = 128;

	private static final Map<Key, Model> MODELS = new LinkedHashMap<>(16,
		0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Key, Model> e) {
			return size() > CACHE_LIMIT;
		}
	};

	private static final Map<Key, List<BufferEntry>> BUFFERS =
		new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(
				Map.Entry<Key, List<BufferEntry>> e) {
				return size() > CACHE_LIMIT;
			}
		};

	private static Direction.Axis axisOf(BlockState state) {
		return state.hasProperty(BlockStateProperties.AXIS)
			? state.getValue(BlockStateProperties.AXIS) : Direction.Axis.Y;
	}

	@Nullable
	private static BlockState norm(@Nullable BlockState material) {
		if (material == null)
			return null;
		BlockState n = MaterialSafety.normalize(material);
		return MaterialSafety.isAccepted(n) && !MaterialSafety.isNeutral(n)
			? MaterialSafety.sanitize(n) : null;
	}

	/**
	 * The ModelData handed to the mesh path: MESH marker plus the BE's
	 * material (Create's canonical copycat key, so the stock Contour
	 * emission treats it exactly like a world paint).
	 */
	public static ModelData meshData(@Nullable BlockState material) {
		return ModelData.builder()
			.with(ContourCogwheelModel.MESH_PROPERTY, true)
			.with(CopycatModel.MATERIAL_PROPERTY,
				norm(material) != null ? norm(material)
					: MaterialSafety.neutral())
			.build();
	}

	/**
	 * Flywheel {@link Model} for the instanced visual: the blockstate
	 * model's painted MESH emission captured by Flywheel's own
	 * BlockModelBuilder over a single-position virtual getter. The axis
	 * comes baked into the mesh (the virtual state carries it), so the
	 * instance needs no rotateToFace - only the kinetic spin.
	 */
	public static synchronized Model meshModel(BlockState state,
		@Nullable BlockState material) {
		Key key = new Key(axisOf(state), norm(material));
		Model model = MODELS.get(key);
		if (model == null) {
			var getter = NeoForgeSinglePosVirtualBlockGetter.createFullDark()
				.pos(BlockPos.ZERO)
				.blockState(state)
				.modelData(meshData(material));
			model = new BlockModelBuilder(getter, List.of(BlockPos.ZERO))
				.build();
			MODELS.put(key, model);
		}
		return model;
	}

	/**
	 * Baked fallback buffers, one per chunk render layer of the painted
	 * model (solid/cutout/translucent), built once per (axis, material).
	 */
	public static synchronized List<BufferEntry> buffers(BlockState state,
		@Nullable BlockState material) {
		Key key = new Key(axisOf(state), norm(material));
		return BUFFERS.computeIfAbsent(key,
			k -> buildBuffers(state, material));
	}

	private static List<BufferEntry> buildBuffers(BlockState state,
		@Nullable BlockState material) {
		Minecraft mc = Minecraft.getInstance();
		BakedModel model = mc.getBlockRenderer().getBlockModel(state);
		ModelData data = meshData(material);
		RandomSource rand = RandomSource.create(42L);
		List<BufferEntry> out = new ArrayList<>();
		for (RenderType rt : model.getRenderTypes(state, rand, data)) {
			BufferBuilder bb = new BufferBuilder(new ByteBufferBuilder(8192),
				VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			PoseStack.Pose pose = new PoseStack().last();
			for (BakedQuad q : model.getQuads(state, null, rand, data, rt))
				bb.putBulkData(pose, q, 1f, 1f, 1f, 1f,
					net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY);
			for (Direction side : Direction.values())
				for (BakedQuad q : model.getQuads(state, side, rand, data, rt))
					bb.putBulkData(pose, q, 1f, 1f, 1f, 1f,
						net.minecraft.client.renderer.LightTexture.FULL_BRIGHT,
						OverlayTexture.NO_OVERLAY);
			MeshData mesh = bb.build();
			if (mesh != null)
				out.add(new BufferEntry(rt,
					SuperBufferFactory.getInstance().create(mesh)));
		}
		return List.copyOf(out);
	}
}
