package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 45-degree wedge ramp. FACING names the horizontal direction the ramp
 * ascends toward (high edge against that side); HALF TOP inverts the
 * wedge into the top slice. Collision is a stepped 4-slice approximation
 * of the triangle (documented, like the diagonal panel). The wrench
 * cycles the 8 states (facing then half).
 */
public class ContourSlopeBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	private static final VoxelShape[] SHAPES = new VoxelShape[8];

	static {
		for (Direction f : Direction.Plane.HORIZONTAL)
			for (Half h : Half.values())
				SHAPES[index(f, h)] = build(f, h);
	}

	private static int index(Direction f, Half h) {
		return f.get2DDataValue() * 2 + (h == Half.TOP ? 1 : 0);
	}

	private static VoxelShape build(Direction f, Half h) {
		return build(f, h, 16f);
	}

	/**
	 * Stepped approximation: 4 slices across the run, height dropping
	 * height/4 per slice (BOTTOM) or rising (TOP), rotated to face f.
	 */
	static VoxelShape build(Direction f, Half h, float height) {
		VoxelShape s = Shapes.empty();
		float step = height / 4f;
		for (int i = 0; i < 4; i++) {
			float lo = i * 4f, hi = lo + 4f;
			float y0, y1;
			if (h == Half.BOTTOM) { // solid below the ramp line
				y0 = 0;
				y1 = height - i * step;
			} else { // TOP: solid above the ramp line
				y0 = 16f - (i + 1) * step;
				y1 = 16f;
			}
			s = Shapes.or(s, switch (f) {
				case NORTH -> Block.box(0, y0, lo, 16, y1, hi);
				case SOUTH -> Block.box(0, y0, 16 - hi, 16, y1, 16 - lo);
				case EAST -> Block.box(16 - hi, y0, 0, 16 - lo, y1, 16);
				default -> Block.box(lo, y0, 0, hi, y1, 16);
			});
		}
		return s.optimize();
	}

	public ContourSlopeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(HALF));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[index(state.getValue(FACING), state.getValue(HALF))];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Half half = ctx.getClickedFace() == Direction.DOWN
			|| (ctx.getClickedFace().getAxis().isHorizontal()
				&& ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5)
			? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection())
			.setValue(HALF, half), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		Direction f = state.getValue(FACING);
		Half h = state.getValue(HALF);
		if (h == Half.BOTTOM)
			return state.setValue(HALF, Half.TOP);
		Direction[] order = {Direction.NORTH, Direction.EAST, Direction.SOUTH,
			Direction.WEST};
		Direction nf = order[(java.util.Arrays.asList(order).indexOf(f) + 1) % 4];
		return state.setValue(FACING, nf).setValue(HALF, Half.BOTTOM);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
