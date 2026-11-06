package net.createcontour.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Paintable stairs. Vanilla geometry/state semantics: FACING is the side
 * the steps ascend toward (tall half occupies the FACING side), HALF +
 * five STAIR_SHAPE states. Corner connections follow the vanilla rule,
 * matching both Contour stairs and vanilla StairBlock neighbours with the
 * same half, regardless of painted material. Collision uses the same
 * 2x2x2 occupancy grid as the neutral models (render parity is asserted
 * against vanilla StairsBlock by GameTests).
 */
public class ContourStairsBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final EnumProperty<StairsShape> STAIRS_SHAPE = BlockStateProperties.STAIRS_SHAPE;

	/** [half ordinal][2D facing index][shape ordinal] occupancy-mask shapes. */
	private static final VoxelShape[][][] SHAPES = buildShapeTable();

	public ContourStairsBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH)
			.setValue(HALF, Half.BOTTOM)
			.setValue(STAIRS_SHAPE, StairsShape.STRAIGHT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, HALF, STAIRS_SHAPE));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[state.getValue(HALF).ordinal()]
			[state.getValue(FACING).get2DDataValue()]
			[state.getValue(STAIRS_SHAPE).ordinal()];
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction face = ctx.getClickedFace();
		BlockPos pos = ctx.getClickedPos();
		boolean top = face == Direction.DOWN
			|| (face != Direction.UP && ctx.getClickLocation().y - pos.getY() > 0.5);
		BlockState state = withWater(defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection())
			.setValue(HALF, top ? Half.TOP : Half.BOTTOM), ctx);
		return state.setValue(STAIRS_SHAPE, shapeFor(state, ctx.getLevel(), pos));
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction,
		BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
		return updated.setValue(STAIRS_SHAPE, shapeFor(updated, level, pos));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Direction facing = state.getValue(FACING);
		StairsShape shape = state.getValue(STAIRS_SHAPE);
		// Vanilla rule: a mirror across the facing axis swaps left/right
		// corner states AND rotates the whole state 180 deg.
		// Vanilla: LEFT_RIGHT mirror on a Z-facing stair swaps L/R corner
		// states + rotates 180; FRONT_BACK on an X-facing stair rotates
		// 180 but PRESERVES the shape value (no swap).
		if (mirror == Mirror.LEFT_RIGHT && facing.getAxis() == Direction.Axis.Z)
			return rotate(state.setValue(STAIRS_SHAPE, swapLeftRight(shape)),
				Rotation.CLOCKWISE_180);
		if (mirror == Mirror.FRONT_BACK && facing.getAxis() == Direction.Axis.X)
			return state.rotate(Rotation.CLOCKWISE_180)
				.setValue(STAIRS_SHAPE, swapOuterLeftRight(shape));
		return super.mirror(state, mirror);
	}

	private static StairsShape swapOuterLeftRight(StairsShape shape) {
		return switch (shape) {
			case OUTER_LEFT -> StairsShape.OUTER_RIGHT;
			case OUTER_RIGHT -> StairsShape.OUTER_LEFT;
			default -> shape;
		};
	}

	private static StairsShape swapLeftRight(StairsShape shape) {
		return switch (shape) {
			case INNER_LEFT -> StairsShape.INNER_RIGHT;
			case INNER_RIGHT -> StairsShape.INNER_LEFT;
			case OUTER_LEFT -> StairsShape.OUTER_RIGHT;
			case OUTER_RIGHT -> StairsShape.OUTER_LEFT;
			default -> shape;
		};
	}

	// --- corner connection rule (vanilla-compatible, material-independent) ---

	private static boolean isStairs(BlockState state) {
		return state.getBlock() instanceof StairBlock
			|| state.getBlock() instanceof ContourStairsBlock;
	}

	/**
	 * Vanilla getStairsShape semantics: a same-half perpendicular stair on
	 * the FACING side forms an OUTER corner; on the opposite side an INNER
	 * corner. `canTakeShape` validates the block on the candidate corner
	 * leg side (pos.relative(bd)) does not already claim the corner.
	 */
	private static StairsShape shapeFor(BlockState state, BlockGetter level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		Half half = state.getValue(HALF);

		BlockState front = level.getBlockState(pos.relative(facing));
		if (isStairs(front) && front.getValue(HALF) == half) {
			Direction fd = front.getValue(FACING);
			if (fd.getAxis() != facing.getAxis()
				&& differentShape(state, level.getBlockState(pos.relative(fd.getOpposite())))) {
				return fd == facing.getCounterClockWise()
					? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
			}
		}

		BlockState back = level.getBlockState(pos.relative(facing.getOpposite()));
		if (isStairs(back) && back.getValue(HALF) == half) {
			Direction bd = back.getValue(FACING);
			if (bd.getAxis() != facing.getAxis()
				&& differentShape(state, level.getBlockState(pos.relative(bd)))) {
				return bd == facing.getCounterClockWise()
					? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
			}
		}

		return StairsShape.STRAIGHT;
	}

	private static boolean differentShape(BlockState state, BlockState other) {
		return !isStairs(other)
			|| other.getValue(HALF) != state.getValue(HALF)
			|| other.getValue(FACING) != state.getValue(FACING);
	}

	private static VoxelShape[][][] buildShapeTable() {
		VoxelShape[][][] table = new VoxelShape[2][4][StairsShape.values().length];
		for (Half half : Half.values())
			for (Direction facing : Direction.Plane.HORIZONTAL)
				for (StairsShape shape : StairsShape.values())
					table[half.ordinal()][facing.get2DDataValue()][shape.ordinal()] =
						ShapeGrid.build(ShapeGrid.cellMask(half, facing, shape));
		return table;
	}

	@Override
	protected Block nextInFamily() {
		return net.createcontour.registry.ContourRegistries.CONTOUR_SLAB.get();
	}

	@Override
	protected BlockState carryOrientation(BlockState state, Direction facing, boolean top) {
		return state.setValue(FACING, facing)
			.setValue(HALF, top ? Half.TOP : Half.BOTTOM);
	}
}
