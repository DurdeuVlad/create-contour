package net.createcontour.item;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Left-click binding for the place gun. Vanilla spends LMB on
 * breaking; while a placer is held the click is repurposed - the
 * event is cancelled on both sides (the gun never breaks) and the
 * server performs the placement.
 */
@EventBusSubscriber(modid = "create_contour")
public final class ContourPlacerEvents {

	private ContourPlacerEvents() {
	}

	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (!(event.getItemStack().getItem() instanceof ContourPlacerItem))
			return;
		event.setCanceled(true);
		if (!event.getLevel().isClientSide())
			ContourPlacerItem.tryPlace(event.getLevel(), event.getEntity(),
				event.getHand(), event.getPos(), event.getFace());
	}
}
