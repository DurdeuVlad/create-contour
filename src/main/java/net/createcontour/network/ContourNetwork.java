package net.createcontour.network;

import net.createcontour.item.ContourBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** Server-authoritative packet registration and codex template emission. */
@EventBusSubscriber(modid = "create_contour", bus = EventBusSubscriber.Bus.MOD)
public final class ContourNetwork {

	private ContourNetwork() {
	}

	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		event.registrar("1").playToServer(CodexSelectPayload.TYPE,
			CodexSelectPayload.STREAM_CODEC, ContourNetwork::handleCodexSelect);
		event.registrar("1").playToServer(SignTextPayload.TYPE,
			SignTextPayload.STREAM_CODEC, ContourNetwork::handleSignText);
		event.registrar("1").playToServer(PlacerSelectPayload.TYPE,
			PlacerSelectPayload.STREAM_CODEC, ContourNetwork::handlePlacerSelect);
	}

	/**
	 * Builds the codex output stack: TEMPLATE semantics only - optional
	 * material tag, paid 0, default facing. Shared with GameTests so the
	 * no-credit invariant is verified against this exact code path.
	 */
	/**
	 * Codex grants are always blank shape items - paint lives on the
	 * world block only. The material argument is retained for payload
	 * compatibility but never written onto the stack.
	 */
	public static ItemStack buildCodexTemplate(ItemStack stack,
		@Nullable BlockState material) {
		return stack;
	}

	private static void handleCodexSelect(CodexSelectPayload payload,
		IPayloadContext context) {
		context.enqueueWork(() -> {
			Player player = context.player();
			Item item = BuiltInRegistries.ITEM.get(payload.shape());
			if (!(item instanceof ContourBlockItem))
				return; // unknown or tampered shape id: emit nothing
			ItemStack out = buildCodexTemplate(new ItemStack(item), null);
			if (!player.getInventory().add(out))
				player.drop(out, false);
		});
	}

	private static void handlePlacerSelect(PlacerSelectPayload payload,
		IPayloadContext context) {
		context.enqueueWork(() -> {
			Player player = context.player();
			for (var hand : new net.minecraft.world.InteractionHand[]{
				net.minecraft.world.InteractionHand.MAIN_HAND,
				net.minecraft.world.InteractionHand.OFF_HAND}) {
				ItemStack held = player.getItemInHand(hand);
				if (held.getItem() instanceof net.createcontour.item.ContourPlacerItem) {
					net.createcontour.item.ContourPlacerItem
						.setShape(held, payload.shape());
					return; // invalid ids are rejected inside setShape
				}
			}
		});
	}

	private static void handleSignText(SignTextPayload payload,
		IPayloadContext context) {
		context.enqueueWork(() -> {
			Player player = context.player();
			BlockPos pos = payload.pos();
			// reach + distance gate: the edit UI can only target a sign
			// the player actually clicked
			if (pos.distSqr(player.blockPosition()) > 64)
				return;
			if (!(player.level().getBlockEntity(pos)
				instanceof net.createcontour.blockentity.ContourSignBlockEntity be))
				return;
			if (be.isFrozen())
				return;
			be.setLines(payload.lines().toArray(new String[0]));
		});
	}
}
