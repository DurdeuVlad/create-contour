package net.createcontour.client;

import net.createcontour.item.ContourPlacerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Contour Placer HUD decal: while the gun is held, the configured
 * shape is drawn next to the crosshair so the current selection is
 * readable in first person. The gun's own angled screen is the
 * in-model view; this layer is the always-visible companion readout.
 */
@EventBusSubscriber(modid = "create_contour", value = Dist.CLIENT,
	bus = EventBusSubscriber.Bus.MOD)
public final class ContourPlacerRenderer {

	private static final ResourceLocation LAYER = ResourceLocation
		.fromNamespaceAndPath("create_contour", "placer_selection");

	private ContourPlacerRenderer() {
	}

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.CROSSHAIR, LAYER,
			(guiGraphics, deltaTracker) -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null || mc.options.hideGui)
					return;
				ItemStack held = mc.player.getMainHandItem();
				if (!(held.getItem() instanceof ContourPlacerItem))
					return;
				ItemStack shape = new ItemStack(
					ContourPlacerItem.selectedBlock(held));
				int x = guiGraphics.guiWidth() / 2 + 10;
				int y = guiGraphics.guiHeight() / 2 - 8;
				guiGraphics.renderItem(shape, x, y);
			});
	}
}
