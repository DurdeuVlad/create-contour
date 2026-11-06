package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Two-block openable door: 3px plate per half. Closed, the plate sits on
 * the FACING side of each cell; open, it rests against the hinge side.
 * The material ledger lives ONLY on the lower half's block entity - the
 * upper half mirrors material for rendering but never holds paid credit
 * and never drops items, so the double-block structure cannot mint or
 * lose ledger. Redstone powers both halves; clicks on either half
 * delegate material work to the lower half.
 */
public class ContourDoorBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF =
		BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final EnumProperty<DoorHingeSide> HINGE =
		BlockStateProperties.DOOR_HINGE;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ContourDoorBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH)
			.setValue(HALF, DoubleBlockHalf.LOWER)
			.setValue(HINGE, DoorHingeSide.LEFT)
			.setValue(OPEN, false).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(
			builder.add(FACING).add(HALF).add(HINGE).add(OPEN).add(POWERED));
	}

	/** Side the plate occupies when OPEN (hinge side, from the front). */
	public static Direction openSide(Direction facing, DoorHingeSide hinge) {
		return hinge == DoorHingeSide.RIGHT
			? facing.getCounterClockWise() : facing.getClockWise();
	}

	private static float[] plate(Direction side) {
		return switch (side) {
			case NORTH -> new float[]{0, 0, 0, 16, 16, 3};
			case SOUTH -> new float[]{0, 0, 13, 16, 16, 16};
			case EAST -> new float[]{13, 0, 0, 16, 16, 16};
			default -> new float[]{0, 0, 0, 3, 16, 16};
		};
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		return List.of(state.getValue(OPEN)
			? plate(openSide(state.getValue(FACING), state.getValue(HINGE)))
			: plate(state.getValue(FACING)));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	private BlockPos sibling(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
	}

	private BlockPos lower(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		if (!ctx.getLevel().getBlockState(pos.above()).canBeReplaced(ctx))
			return null;
		Direction facing = ctx.getHorizontalDirection();
		// Hinge from the click's position along the perpendicular axis.
		double t = facing.getAxis() == Direction.Axis.X
			? ctx.getClickLocation().z - pos.getZ()
			: ctx.getClickLocation().x - pos.getX();
		DoorHingeSide hinge = t > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
		return withWater(defaultBlockState().setValue(FACING, facing)
			.setValue(HINGE, hinge), ctx);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
		net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		BlockPos above = pos.above();
		level.setBlock(above, state.setValue(HALF, DoubleBlockHalf.UPPER)
			.setValue(WATERLOGGED,
				level.getFluidState(above).getType() == net.minecraft.world.level.material.Fluids.WATER),
			Block.UPDATE_ALL);
		syncUpperFromLower(level, pos);
	}

	/** Copy render material (NOT paid credit) from the lower BE upward. */
	private void syncUpperFromLower(Level level, BlockPos lowerPos) {
		if (level.getBlockEntity(lowerPos) instanceof ContourBlockEntity lbe
			&& level.getBlockEntity(lowerPos.above()) instanceof ContourBlockEntity ube) {
			ube.setMaterial(lbe.getMaterial());
			ube.setCanonical(lbe.getFacing(), lbe.isTop());
		}
	}

	/**
	 * All item interaction resolves against the lower half so material,
	 * sampling and prepaid credit land on the single ledger-bearing BE.
	 * Non-material clicks toggle both halves.
	 */
	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		ItemStack stack, BlockState state, Level level, BlockPos pos,
		Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos lp = pos.below();
			var r = super.useItemOn(stack, level.getBlockState(lp), level, lp,
				player, hand,
				new BlockHitResult(Vec3.atCenterOf(lp), hit.getDirection(), lp, false));
			if (r != net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
				syncUpperFromLower(level, lp);
				return r;
			}
		} else {
			var r = super.useItemOn(stack, state, level, pos, player, hand, hit);
			if (r != net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
				syncUpperFromLower(level, pos);
				return r;
			}
		}
		if (!canManualOpen())
			return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.isClientSide)
			return net.minecraft.world.ItemInteractionResult.SUCCESS;
		setOpen(level, lower(pos, state), !state.getValue(OPEN));
		return net.minecraft.world.ItemInteractionResult.CONSUME;
	}

	/** Wooden doors respond to hands; the iron variant is redstone-only. */
	protected boolean canManualOpen() {
		return true;
	}

	private void setOpen(Level level, BlockPos lowerPos, boolean open) {
		BlockState ls = level.getBlockState(lowerPos);
		BlockState us = level.getBlockState(lowerPos.above());
		if (ls.is(this) && us.is(this)) {
			level.setBlock(lowerPos, ls.setValue(OPEN, open),
				Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
			level.setBlock(lowerPos.above(), us.setValue(OPEN, open),
				Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
			level.levelEvent(null, open ? 1007 : 1013, lowerPos, 0);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos,
		Block block, BlockPos fromPos, boolean isMoving) {
		BlockPos lp = lower(pos, state);
		boolean powered = level.hasNeighborSignal(pos)
			|| level.hasNeighborSignal(sibling(pos, state));
		BlockState ls = level.getBlockState(lp);
		BlockState us = level.getBlockState(lp.above());
		if (ls.is(this) && us.is(this)
			&& (powered != ls.getValue(OPEN) || powered != ls.getValue(POWERED))) {
			level.setBlock(lp, ls.setValue(OPEN, powered).setValue(POWERED, powered),
				Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
			level.setBlock(lp.above(),
				us.setValue(OPEN, powered).setValue(POWERED, powered),
				Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
			level.levelEvent(null, powered ? 1007 : 1013, lp, 0);
		}
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER
			|| level.getBlockState(pos.below())
				.isFaceSturdy(level, pos.below(), Direction.UP);
	}

	/**
	 * Orphan halves collapse exactly like a vanilla door: the survivor
	 * resolves to air, and updateOrDestroy's destroyBlock fires drops
	 * once (lower only - the upper half never drops).
	 */
	@Override
	public BlockState updateShape(BlockState state, Direction dir,
		BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		if (dir.getAxis() == Direction.Axis.Y
			&& (half == DoubleBlockHalf.LOWER) == (dir == Direction.UP)) {
			if (!neighbor.is(this) || neighbor.getValue(HALF) == half)
				return Blocks.AIR.defaultBlockState();
			// Mirror interactive flags so states never diverge.
			return state.setValue(OPEN, neighbor.getValue(OPEN))
				.setValue(POWERED, neighbor.getValue(POWERED));
		}
		if (half == DoubleBlockHalf.LOWER && dir == Direction.DOWN
			&& !state.canSurvive(level, pos))
			return Blocks.AIR.defaultBlockState();
		return super.updateShape(state, dir, neighbor, level, pos, fromPos);
	}

	/**
	 * Breaking the UPPER half in survival must still pay out the lower
	 * half's ledger - destroy the lower part with drops first (creative:
	 * without drops, matching vanilla's prevent-creative-drop rule).
	 */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos,
		BlockState state, Player player) {
		if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos below = pos.below();
			BlockState ls = level.getBlockState(below);
			if (ls.is(this) && ls.getValue(HALF) == DoubleBlockHalf.LOWER)
				level.destroyBlock(below, !player.isCreative(), player);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	/** The upper half is render-only: no item, no ledger split. */
	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER
			? List.of() : super.getDrops(state, params);
	}

	@Override
	public int hostUnits(BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? 0 : 1;
	}

	/** Sneak-wrench on either half picks up the whole door from the ledger. */
	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			Level level = context.getLevel();
			BlockPos lp = context.getClickedPos().below();
			BlockState ls = level.getBlockState(lp);
			if (ls.is(this)) {
				InteractionResult r = super.onSneakWrenched(ls,
					new UseOnContext(level, context.getPlayer(), context.getHand(),
						context.getItemInHand(),
						new BlockHitResult(Vec3.atCenterOf(lp), Direction.UP, lp, false)));
				if (r == InteractionResult.SUCCESS)
					level.removeBlock(context.getClickedPos(), false);
				return r;
			}
		}
		return super.onSneakWrenched(state, context);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)))
			.setValue(HINGE, state.getValue(HINGE) == DoorHingeSide.LEFT
				? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(FACING);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		InteractionResult r = super.onWrenched(state, context);
		if (r == InteractionResult.sidedSuccess(context.getLevel().isClientSide)
			&& !context.getLevel().isClientSide) {
			// Keep the sibling's facing aligned with the wrenched half.
			BlockPos pos = context.getClickedPos();
			BlockPos sp = sibling(pos, context.getLevel().getBlockState(pos));
			BlockState ss = context.getLevel().getBlockState(sp);
			BlockState cs = context.getLevel().getBlockState(pos);
			if (ss.is(this) && cs.is(this))
				context.getLevel().setBlock(sp, ss.setValue(FACING, cs.getValue(FACING)),
					Block.UPDATE_ALL);
		}
		return r;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
