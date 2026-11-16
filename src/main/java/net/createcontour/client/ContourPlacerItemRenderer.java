package net.createcontour.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.createcontour.item.ContourPlacerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

/**
 * Issue #32: the placer gun renders its selected shape as a small icon
 * sitting on the model's angled back screen (replacing the old
 * crosshair HUD decal - the icon lives ON the gun now). ItemRenderer
 * only dispatches to {@link IClientItemExtensions#getCustomRenderer()}
 * when the item's baked model reports {@code isCustomRenderer()}, so
 * {@link ScreenModel} wraps the inventory model and flips that flag;
 * {@link #renderByItem} then draws the wrapped quads through the same
 * pass/render-type loop the vanilla item path uses, plus the shape
 * icon on the tilted screen plane. Original implementation.
 */
public final class ContourPlacerItemRenderer
	extends BlockEntityWithoutLevelRenderer {

	/**
	 * Screen plane (contour_placer.json): element [5.75,11,14.0] ->
	 * [10.25,14.5,14.45] rotated -45 deg about X at origin
	 * [8,11,14.25]. The south face center post-rotation is
	 * (8,12.379,13.154) in model px, i.e. (0,0.2737,0.3221) in item
	 * space (px/16 - 0.5); the face normal is (0,0.7071,0.7071), so a
	 * -45 deg X rotation maps local +Z onto it.
	 */
	private static final float SCREEN_Y = (12.379f - 8f) / 16f;
	private static final float SCREEN_Z = (13.154f - 8f) / 16f;
	/** Lift the icon slightly off the screen surface (no z-fighting). */
	private static final float ICON_LIFT = 0.03f;
	/** Icon footprint: ~3.4 px of screen surface (icon ~0.625 units
	 *  before this scale, from the block GUI transform). */
	private static final float ICON_SCALE = 0.33f;

	public ContourPlacerItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
			Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context,
		PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		BakedModel model = itemRenderer.getModel(stack, mc.level,
			mc.player, 0);
		BakedModel base = model instanceof ScreenModel screen
			? screen.delegate() : model;
		if (!base.isCustomRenderer())
			renderBase(itemRenderer, base, stack, pose, buffer, light,
				overlay);
		renderScreenIcon(mc, itemRenderer, stack, pose, buffer, light,
			overlay);
	}

	/**
	 * Draws the wrapped gun model through the same render-pass /
	 * render-type loop {@link ItemRenderer#render} uses for ordinary
	 * models (the incoming pose already carries the display transform,
	 * so only the quad emission remains).
	 */
	private static void renderBase(ItemRenderer itemRenderer,
		BakedModel model, ItemStack stack, PoseStack pose,
		MultiBufferSource buffer, int light, int overlay) {
		for (BakedModel pass : model.getRenderPasses(stack, true))
			for (RenderType type : pass.getRenderTypes(stack, true)) {
				VertexConsumer vc = ItemRenderer.getFoilBufferDirect(
					buffer, type, true, stack.hasFoil());
				itemRenderer.renderModelLists(pass, stack, light, overlay,
					pose, vc);
			}
	}

	/** The selected shape icon, flat on the screen's tilted plane. */
	private static void renderScreenIcon(Minecraft mc,
		ItemRenderer itemRenderer, ItemStack gun, PoseStack pose,
		MultiBufferSource buffer, int light, int overlay) {
		ItemStack shape = new ItemStack(
			ContourPlacerItem.selectedBlock(gun));
		if (shape.isEmpty())
			return;
		pose.pushPose();
		pose.translate(0, SCREEN_Y, SCREEN_Z);
		pose.mulPose(Axis.XP.rotationDegrees(-45f));
		pose.translate(0, 0, ICON_LIFT);
		pose.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);
		itemRenderer.renderStatic(shape, ItemDisplayContext.GUI, light,
			overlay, pose, buffer, mc.level, 0);
		pose.popPose();
	}

	/**
	 * Identity wrapper that only flips {@link #isCustomRenderer()} so
	 * {@link ItemRenderer} hands this item to our
	 * {@link BlockEntityWithoutLevelRenderer}.
	 */
	public static final class ScreenModel
		extends BakedModelWrapper<BakedModel> {

		public ScreenModel(BakedModel original) {
			super(original);
		}

		public BakedModel delegate() {
			return originalModel;
		}

		@Override
		public boolean isCustomRenderer() {
			return true;
		}
	}
}
