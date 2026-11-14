package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Parametric layer plate: LAYERS 1..8 at 2px per layer, the issue-#20
 * SIZE property (kept under the `layers` name for parity with
 * contour_slice / contour_half_layer / contour_corner_slice).
 * Placement picks thickness from the hit height; clicking an existing
 * plate grows it one layer snow-style to 8 = full block.
 *
 * Economics (ADR-018): each layer is one host unit - drops return one
 * blank layer item per layer present. Material is billed in whole
 * items at floor(LAYERS/8): a full 8-layer block costs one material
 * item to paint and refunds one on break; sub-full stacks paint free
 * (fractional items cannot be charged, so the remainder rounds to
 * zero - ledger stays conservative, never a source). The wrench
 * removes the TOP layer: it drops one blank frame and preserves the
 * paid ledger (real charges stay refundable on break; regrowing an
 * already-billed cell is free because placement billing only ever
 * charges the unpaid delta); at LAYERS=1 the wrench does nothing
 * (sneak-wrench picks the block up).
 */
public class ContourLayersBlock extends ContourBlock {

	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);

	private static final VoxelShape[] SHAPES = new VoxelShape[9];
	static {
		for (int i = 1; i <= 8; i++)
			SHAPES[i] = Block.box(0, 0, 0, 16, i * 2, 16);
	}

	public ContourLayersBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(LAYERS, 4));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(LAYERS));
	}

	/** One host unit per layer: drops and requirements scale with size. */
	@Override
	public int hostUnits(BlockState state) {
		return state.getValue(LAYERS);
	}

	/**
	 * Whole material items billed for paint: floor(LAYERS/8). Sizes 1-7
	 * bill zero (free fractional paint); size 8 bills one item.
	 */
	@Override
	public int materialUnits(BlockState state) {
		return state.getValue(LAYERS) / 8;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[state.getValue(LAYERS)];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return state.getValue(LAYERS) < 8;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		// Stacking QoL: clicking an existing layer plate grows it one
		// step (snow-style) instead of re-deriving thickness.
		BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
		if (existing.is(this))
			return withWater(existing.setValue(LAYERS,
				Math.min(8, existing.getValue(LAYERS) + 1)), ctx);
		double fy = ctx.getClickLocation().y - ctx.getClickedPos().getY();
		int layers = ctx.getClickedFace() == net.minecraft.core.Direction.UP ? 8
			: Mth.clamp((int) Math.ceil(fy * 8), 1, 8);
		return withWater(defaultBlockState().setValue(LAYERS, layers), ctx);
	}

	/**
	 * Merge eligibility: same item + room to grow + same FULL paint.
	 * Paint mismatches fall through to the adjacent cell like the slab.
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return state.getValue(LAYERS) < 8
			&& ctx.getItemInHand().is(asItem())
			&& sameMaterial(ctx, ctx.getClickedPos());
	}

	/**
	 * Wrench = remove the TOP layer (ADR-018): decrements LAYERS through
	 * the shared atomic swap and drops one blank frame item. The paid
	 * ledger is preserved untouched - paid units are real historical
	 * charges and stay refundable on the eventual break; shrinking below
	 * a billed boundary does not void them (and placement billing only
	 * ever charges the unpaid delta, so regrowing to 8 is free once the
	 * whole-eighth was paid). At LAYERS=1 nothing is removed - PASS so
	 * sneak-wrench stays the pickup gesture.
	 */
	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		int size = state.getValue(LAYERS);
		if (size <= 1)
			return InteractionResult.PASS;
		if (level.isClientSide)
			return InteractionResult.sidedSuccess(true);
		if (!(level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			|| be.isFrozen())
			return InteractionResult.PASS;
		BlockState newState = state.setValue(LAYERS, size - 1);
		CompoundTag snap = be.saveWithFullMetadata(level.registryAccess());
		if (!level.setBlock(pos, newState, Block.UPDATE_ALL)) {
			level.setBlock(pos, state, Block.UPDATE_ALL);
			return InteractionResult.FAIL;
		}
		if (!(level.getBlockEntity(pos) instanceof ContourBlockEntity nbe)) {
			// Rollback: restore the original block AND serialized fields.
			level.setBlock(pos, state, Block.UPDATE_ALL);
			if (level.getBlockEntity(pos) instanceof ContourBlockEntity old) {
				old.loadWithComponents(snap, level.registryAccess());
				old.setMaterial(old.getMaterial());
			}
			return InteractionResult.FAIL;
		}
		nbe.loadWithComponents(snap, level.registryAccess());
		// Owned refresh seam: NBT load does not fire afterOwnedMutation.
		nbe.setMaterial(nbe.getMaterial());
		giveOrDrop(level, pos, player, new ItemStack(asItem()));
		return InteractionResult.sidedSuccess(false);
	}

	private static void giveOrDrop(Level level, BlockPos pos, Player player,
		ItemStack stack) {
		if (stack.isEmpty())
			return;
		if (player == null || !player.getInventory().add(stack))
			popResource(level, pos, stack);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
