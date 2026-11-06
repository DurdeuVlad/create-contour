package net.createcontour.item;

import java.util.List;
import net.createcontour.client.CodexClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Contour Codex: opens the shape/material selection GUI. The emitted
 * selection is ALWAYS a TEMPLATE shape item (paid 0) - the codex is a
 * convenience, never a credit source (ADR-009/ADR-013).
 */
public class ContourCodexItem extends Item {

	public ContourCodexItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player,
		InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide && FMLEnvironment.dist == Dist.CLIENT)
			CodexClient.openCodexScreen();
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context,
		List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(
			"item.create_contour.contour_codex.tooltip"));
	}
}
