package net.createcontour.item;

import java.util.List;
import net.createcontour.block.ContourBlock;
import net.createcontour.client.ContourPlacerClient;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

/**
 * Contour Placer: the shape "place gun". Consumes contour_blank
 * feedstock - left-click on a face places the configured shape,
 * right-click on a Contour shape swoops it back into one blank,
 * sneak+right-click opens the back-mounted shape selector. The
 * configured shape id lives in `contour_shape` custom data.
 */
public class ContourPlacerItem extends Item {

	private static final String SHAPE_KEY = "contour_shape";

	public ContourPlacerItem(Properties properties) {
		super(properties);
	}

	// --- selection state (item-stack local, server authored) ---

	@Nullable
	public static ResourceLocation selectedShapeId(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null)
			return null;
		CompoundTag tag = data.copyTag();
		if (!tag.contains(SHAPE_KEY))
			return null;
		ResourceLocation id = ResourceLocation.tryParse(tag.getString(SHAPE_KEY));
		return id != null && BuiltInRegistries.ITEM
			.get(id) instanceof ContourBlockItem ? id : null;
	}

	/**
	 * The block the gun would place: stored selection when valid,
	 * otherwise the default contour slab. Always a ContourBlock.
	 */
	public static Block selectedBlock(ItemStack stack) {
		ResourceLocation id = selectedShapeId(stack);
		if (id != null && BuiltInRegistries.ITEM.get(id)
			instanceof ContourBlockItem cbi)
			return cbi.getBlock();
		return ContourRegistries.CONTOUR_SLAB.get();
	}

	/** Writes the selection onto the gun stack; rejects non-shape ids. */
	public static boolean setShape(ItemStack stack, ResourceLocation shapeId) {
		if (!(BuiltInRegistries.ITEM.get(shapeId) instanceof ContourBlockItem))
			return false;
		CustomData.update(DataComponents.CUSTOM_DATA, stack,
			t -> t.putString(SHAPE_KEY, shapeId.toString()));
		return true;
	}

	// --- gestures ---

	/** First inventory slot holding contour_blank feedstock, or -1. */
	private static int blankSlot(Player player) {
		for (int i = 0; i < player.getInventory().items.size(); i++)
			if (player.getInventory().items.get(i)
				.is(ContourRegistries.CONTOUR_BLANK_ITEM.get()))
				return i;
		return -1;
	}

	/**
	 * Left-click placement: resolves the placement cell from the clicked
	 * face, places the configured shape with normal placement orientation,
	 * and consumes one blank in non-creative play.
	 */
	public static InteractionResult tryPlace(Level level, Player player,
		InteractionHand hand, BlockPos pos, @Nullable Direction face) {
		ItemStack gun = player.getItemInHand(hand);
		if (!(gun.getItem() instanceof ContourPlacerItem) || face == null)
			return InteractionResult.PASS;
		Block block = selectedBlock(gun);
		Vec3 hit = Vec3.atCenterOf(pos).add(
			face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
		BlockPlaceContext ctx = new BlockPlaceContext(player, hand, gun,
			new BlockHitResult(hit, face, pos, false));
		if (!ctx.canPlace())
			return InteractionResult.FAIL;
		BlockState state = block.getStateForPlacement(ctx);
		if (state == null)
			return InteractionResult.FAIL;
		BlockPos placePos = ctx.getClickedPos();
		if (!state.canSurvive(level, placePos)
			|| !level.isUnobstructed(state, placePos, CollisionContext.of(player)))
			return InteractionResult.FAIL;
		int slot = player.getAbilities().instabuild ? -1 : blankSlot(player);
		if (!player.getAbilities().instabuild && slot < 0)
			return InteractionResult.FAIL; // out of feedstock
		if (!level.setBlock(placePos, state, Block.UPDATE_ALL))
			return InteractionResult.FAIL;
		block.setPlacedBy(level, placePos, state, player, gun);
		SoundType snd = state.getSoundType(level, placePos, player);
		level.playSound(player, placePos, snd.getPlaceSound(), SoundSource.BLOCKS,
			(snd.getVolume() + 1f) / 2f, snd.getPitch() * 0.8f);
		if (slot >= 0)
			player.getInventory().items.get(slot).shrink(1);
		return InteractionResult.SUCCESS;
	}

	/**
	 * Right-click reclaim: a Contour shape is lifted whole - no drops -
	 * and converted back into one contour_blank. PASS on anything else.
	 */
	public static InteractionResult trySwoop(Level level, Player player,
		BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof ContourBlock contour))
			return InteractionResult.PASS;
		java.util.List<ItemStack> drops = new java.util.ArrayList<>();
		if (level.getBlockEntity(pos) instanceof net.createcontour.blockentity.ContourBlockEntity be
			&& !be.isFrozen())
			// Swoop follows the shared drop rule (blank shape + paid material
			// refund); every shape item converts to ammo feedstock.
			for (ItemStack d : contour.ledgerDrops(state, be))
				drops.add(d.getItem() instanceof ContourBlockItem
					? new ItemStack(ContourRegistries.CONTOUR_BLANK_ITEM.get(), d.getCount())
					: d);
		if (drops.isEmpty())
			drops.add(new ItemStack(ContourRegistries.CONTOUR_BLANK_ITEM.get()));
		SoundType snd = state.getSoundType(level, pos, player);
		level.removeBlock(pos, false);
		level.playSound(player, pos, snd.getBreakSound(), SoundSource.BLOCKS,
			(snd.getVolume() + 1f) / 2f, snd.getPitch() * 0.8f);
		for (ItemStack stack : drops)
			if (!player.getInventory().add(stack))
				player.drop(stack, false);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		if (player == null)
			return InteractionResult.PASS;
		if (player.isShiftKeyDown()) {
			if (level.isClientSide && FMLEnvironment.dist == Dist.CLIENT)
				ContourPlacerClient.openPlacerScreen();
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		InteractionResult r = level.isClientSide
			? trySwoopPreview(level, context.getClickedPos())
			: trySwoop(level, player, context.getClickedPos());
		return r;
	}

	private static InteractionResult trySwoopPreview(Level level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() instanceof ContourBlock
			? InteractionResult.sidedSuccess(true) : InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player,
		InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			if (level.isClientSide && FMLEnvironment.dist == Dist.CLIENT)
				ContourPlacerClient.openPlacerScreen();
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
		List<Component> tooltip, TooltipFlag flag) {
		ResourceLocation id = selectedShapeId(stack);
		tooltip.add(Component.translatable("item.create_contour.contour_placer.shape",
			id == null ? Component.translatable("block.create_contour.contour_slab")
				: Component.translatable(
					"block." + id.getNamespace() + "." + id.getPath())));
		tooltip.add(Component.translatable(
			"item.create_contour.contour_placer.tooltip"));
	}
}
