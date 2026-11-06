package net.createcontour.block;

import java.util.List;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.content.decoration.copycat.WaterloggedCopycatBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialPolicy;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Shared base for Create: Contour shapes. Extends Create's
 * WaterloggedCopycatBlock; material acceptance is narrowed by
 * MaterialSafety; economics/gestures are owned here (the base defaults -
 * wrench strips material, onRemove pops loose material - would violate
 * the paid-ledger design, so the accounting surface is fully owned).
 */
public abstract class ContourBlock extends WaterloggedCopycatBlock {

	protected ContourBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public Class<CopycatBlockEntity> getBlockEntityClass() {
		return CopycatBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CopycatBlockEntity> getBlockEntityType() {
		return ContourRegistries.CONTOUR_BE.get();
	}

	@Override
	public boolean canConnectTexturesToward(net.minecraft.world.level.BlockAndTintGetter level,
		BlockPos pos, BlockPos otherPos, BlockState state) {
		return false;
	}

	/** Acceptance: real registered non-air non-copycat non-entity block. */
	@Override
	public BlockState getAcceptedBlockState(Level pLevel, BlockPos pPos,
		ItemStack pItem, Direction pFace) {
		if (!(pItem.getItem() instanceof BlockItem blockItem))
			return null;
		BlockState candidate = blockItem.getBlock().defaultBlockState();
		return MaterialSafety.isAccepted(candidate) ? candidate : null;
	}

	/** Host shape units carried by this block state (1 or 2 for doubles). */
	public int hostUnits(BlockState state) {
		return 1;
	}

	// --- placement: suppress inherited offhand paint; MaterialPolicy owns it ---

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
		LivingEntity placer, ItemStack stack) {
		// Intentionally NOT calling CopycatBlock.setPlacedBy: its offhand
		// material application charges through the base private ledger,
		// bypassing MaterialPolicy. Painting at placement time is applied
		// authoritatively by ContourBlockItem.place() after setBlock.
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			be.setCanonical(canonicalFacing(state), isTop(state));
	}

	protected Direction canonicalFacing(BlockState state) {
		return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
			? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
	}

	protected boolean isTop(BlockState state) {
		if (state.hasProperty(BlockStateProperties.SLAB_TYPE))
			return state.getValue(BlockStateProperties.SLAB_TYPE) == net.minecraft.world.level.block.state.properties.SlabType.TOP;
		if (state.hasProperty(BlockStateProperties.HALF))
			return state.getValue(BlockStateProperties.HALF) == net.minecraft.world.level.block.state.properties.Half.TOP;
		return false;
	}

	// --- gestures ---

	/**
	 * useItemOn is fully owned (the base version would charge the private
	 * ledger and ignore consume config / paid refunds).
	 *
	 * Shape items never carry paint, so the only interaction here is a
	 * held material BlockItem painting this shape via MaterialPolicy.
	 * ContourBlockItems fall through to normal adjacent placement.
	 */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
		Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() instanceof ContourBlockItem)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!level.isClientSide) {
			BlockState accepted = getAcceptedBlockState(level, pos, stack, hit.getDirection());
			if (accepted != null && level.getBlockEntity(pos) instanceof ContourBlockEntity be) {
				if (MaterialPolicy.paint(level, be, player, accepted))
					return ItemInteractionResult.sidedSuccess(false);
				return ItemInteractionResult.FAIL;
			}
		} else if (getAcceptedBlockState(level, pos, stack, hit.getDirection()) != null) {
			return ItemInteractionResult.sidedSuccess(true);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	// --- wrench: cycle family / sneak-wrench pick up ---

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (level.isClientSide)
			return InteractionResult.sidedSuccess(true); // client never mutates
		if (!(level.getBlockEntity(pos) instanceof ContourBlockEntity be) || be.isFrozen())
			return InteractionResult.PASS;
		if (hostUnits(state) > 1)
			return InteractionResult.FAIL; // doubled shapes never cycle
		BlockState newState = wrenchTarget(state, be);
		if (newState == null)
			return InteractionResult.PASS;
		// Atomic: snapshot the full serialized BE, setBlock, copy into the
		// new BE; rollback restores block AND owned fields.
		CompoundTag snap = be.saveWithFullMetadata(level.registryAccess());
		if (!level.setBlock(pos, newState, Block.UPDATE_ALL)) {
			level.setBlock(pos, state, Block.UPDATE_ALL);
			return InteractionResult.FAIL;
		}
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity nbe) {
			nbe.loadWithComponents(snap, level.registryAccess());
			// owned refresh seam: NBT load does not fire afterOwnedMutation,
			// so re-set material to refresh aux light/model data
			nbe.setMaterial(nbe.getMaterial());
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		// Rollback: restore the original block AND its serialized fields.
		level.setBlock(pos, state, Block.UPDATE_ALL);
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity old) {
			old.loadWithComponents(snap, level.registryAccess());
			old.setMaterial(old.getMaterial()); // same owned refresh
		}
		return InteractionResult.FAIL;
	}

	/** Destination state carrying canonical orientation (if supported). */
	protected BlockState carryOrientation(BlockState state, Direction facing, boolean top) {
		return state;
	}

	/**
	 * What the wrench turns this state into: the family-chain default
	 * carries orientation into nextInFamily's default state, or null for
	 * no wrench action. Same-block property cycles (thickness, axis,
	 * position) override this and return a state of THIS block - the
	 * material/paid BE is preserved by the shared atomic swap.
	 */
	@org.jetbrains.annotations.Nullable
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		Block next = nextInFamily();
		if (next == null)
			return null;
		// Orientation is carried by the DESTINATION's rule (each shape
		// knows which of its own properties represents facing/top).
		return ((ContourBlock) next).carryOrientation(next.defaultBlockState()
			.setValue(WATERLOGGED, state.getValue(WATERLOGGED)),
			be.getFacing(), be.isTop());
	}

	protected abstract Block nextInFamily();

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (level.isClientSide)
			return InteractionResult.sidedSuccess(true);
		if (!(level.getBlockEntity(pos) instanceof ContourBlockEntity be))
			return InteractionResult.PASS;
		if (be.isFrozen())
			return InteractionResult.FAIL; // unknown future payload: no pickup yet
		List<ItemStack> drops = ledgerDrops(state, be);
		// Only hand out items AFTER the world removal is confirmed.
		if (!level.removeBlock(pos, false))
			return InteractionResult.FAIL;
		for (ItemStack stack : drops)
			if (player == null || !player.getInventory().add(stack))
				popResource(level, pos, stack);
		return InteractionResult.SUCCESS;
	}

	/**
	 * Drop rule: shape items are ALWAYS blank - paint lives on the
	 * world block only. Each host unit drops its blank shape item,
	 * and every paid unit additionally refunds one material item.
	 */
	public List<ItemStack> ledgerDrops(BlockState state, ContourBlockEntity be) {
		java.util.ArrayList<ItemStack> drops = new java.util.ArrayList<>();
		for (int i = 0; i < hostUnits(state); i++)
			drops.add(new ItemStack(asItem()));
		BlockState material = be.getMaterial();
		int paid = be.getPaidUnits();
		if (paid > 0 && material != null && !MaterialSafety.isNeutral(material))
			drops.add(new ItemStack(material.getBlock().asItem(), paid));
		return drops;
	}

	// --- drops: blank shape items + material refund (see ledgerDrops) ---

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (entity instanceof ContourBlockEntity be && !be.isFrozen())
			return ledgerDrops(state, be);
		java.util.ArrayList<ItemStack> drops = new java.util.ArrayList<>();
		drops.add(new ItemStack(asItem(), hostUnits(state)));
		return drops;
	}

	/**
	 * Removal goes through IBE.onRemove (SmartBlockEntity.destroy +
	 * removeBlockEntity) WITHOUT CopycatBlock.onRemove's loose material
	 * pop - drops with ledger come from the loot path, not onRemove.
	 */
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos,
		BlockState newState, boolean movedByPiston) {
		IBE.onRemove(state, level, pos, newState);
	}

	/**
	 * Pick-block returns the blank shape item - items never carry
	 * paint, so a pick only ever yields the neutral frame.
	 */
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target,
		LevelReader level, BlockPos pos, Player player) {
		return new ItemStack(asItem());
	}
}
