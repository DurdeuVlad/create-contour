package net.createcontour.block;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import java.util.List;
import net.createcontour.blockentity.ContourPaintable;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialPolicy;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

/**
 * True kinetic Contour cogwheel (issue #23): a real Create cogwheel -
 * Create's {@link CogWheelBlock} supplies ICogWheel semantics, AXIS,
 * waterlog, canSurvive, encasing, wrench axis-cycling and the kinetic
 * propagation that RotationPropagator performs via IRotate/ICogWheel on
 * the block plus {@link KineticBlockEntity} on the BE. This class adds
 * the owned Contour surface: paint gestures through MaterialPolicy, the
 * paid-ledger drops, and our own kinetic BE type.
 *
 * Geometry: the baked model emits NO static quads in-world; the painted
 * mesh is drawn rotating by ContourCogwheelVisual (instanced) or
 * ContourCogwheelRenderer (baked fallback) - never a static fake.
 */
public class ContourCogwheelBlock extends CogWheelBlock {

	public ContourCogwheelBlock(Properties pProperties) {
		super(false, pProperties); // small cog
	}

	@Override
	public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
		return ContourRegistries.CONTOUR_COGWHEEL_BE.get();
	}

	/** Acceptance: real registered non-air non-copycat non-entity block. */
	public BlockState getAcceptedBlockState(Level level, BlockPos pos,
		ItemStack item, Direction face) {
		if (!(item.getItem() instanceof BlockItem blockItem))
			return null;
		BlockState candidate = blockItem.getBlock().defaultBlockState();
		return MaterialSafety.isAccepted(candidate) ? candidate : null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
		LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.getBlockEntity(pos) instanceof ContourPaintable be)
			be.setCanonical(Direction.NORTH, false);
	}

	/**
	 * Encasing first (Create-native, no-ops on a small cog), then the
	 * owned material-paint path - ContourBlockItems fall through to
	 * normal adjacent placement like every other shape.
	 */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
		Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		ItemInteractionResult encase =
			super.useItemOn(stack, state, level, pos, player, hand, hit);
		if (encase.consumesAction())
			return encase;
		return hostPaintItemOn(stack, state, level, pos, player, hand, hit);
	}

	/**
	 * The owned material-paint path (same body as
	 * ContourBlock.hostPaintItemOn, against the ContourPaintable BE).
	 */
	protected ItemInteractionResult hostPaintItemOn(ItemStack stack,
		BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() instanceof ContourBlockItem)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!level.isClientSide) {
			BlockState accepted =
				getAcceptedBlockState(level, pos, stack, hit.getDirection());
			if (accepted != null
				&& level.getBlockEntity(pos) instanceof ContourPaintable be) {
				if (MaterialPolicy.paint(level, be, player, accepted))
					return ItemInteractionResult.sidedSuccess(false);
				return ItemInteractionResult.FAIL;
			}
		} else if (getAcceptedBlockState(level, pos, stack,
			hit.getDirection()) != null) {
			return ItemInteractionResult.sidedSuccess(true);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/**
	 * Drop rule, identical to ContourBlock's ledger: one blank shape item
	 * per host unit plus one material item per paid unit. Frozen hosts
	 * export the blank only.
	 */
	public List<ItemStack> ledgerDrops(BlockState state, ContourPaintable be) {
		java.util.ArrayList<ItemStack> drops = new java.util.ArrayList<>();
		for (int i = 0; i < be.hostUnits(); i++)
			drops.add(new ItemStack(asItem()));
		BlockState material = be.getMaterial();
		int paid = be.getPaidUnits();
		if (paid > 0 && material != null && !MaterialSafety.isNeutral(material))
			drops.add(new ItemStack(material.getBlock().asItem(), paid));
		return drops;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockEntity entity =
			params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (entity instanceof ContourPaintable be && !be.isFrozen())
			return ledgerDrops(state, be);
		java.util.ArrayList<ItemStack> drops = new java.util.ArrayList<>();
		drops.add(new ItemStack(asItem()));
		return drops;
	}
}
