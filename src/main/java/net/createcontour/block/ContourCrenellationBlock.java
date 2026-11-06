package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Castle battlement: a solid base band plus a front parapet lip and
 * three merlons (4px wide, 2px crenels). FACING is the outward
 * direction the parapet defends; HALF=TOP crowns a wall, HALF=BOTTOM
 * hangs downward as a machicolation. Render shares boxesFor with
 * collision via ContourBoxModel.
 */
public class ContourCrenellationBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	public ContourCrenellationBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH).setValue(HALF, Half.TOP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(HALF));
	}

	/** Base + parapet lip + 3 merlons; mirrored vertically for BOTTOM. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int v = f.getAxis().ordinal();
		int u = f.getAxis() == Direction.Axis.X ? 2 : 0;
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean top = state.getValue(HALF) == Half.TOP;
		List<float[]> out = new java.util.ArrayList<>(5);
		// base band: full block, half height
		float[] base = {0, 0, 0, 16, 16, 16};
		base[1] = top ? 0f : 8f;
		base[4] = top ? 8f : 16f;
		out.add(base);
		// continuous parapet lip on the defended edge
		float[] lip = {0, 0, 0, 16, 16, 16};
		lip[v] = pos ? 13f : 0f;
		lip[v + 3] = pos ? 16f : 3f;
		lip[1] = top ? 8f : 6f;
		lip[4] = top ? 10f : 8f;
		out.add(lip);
		// three merlons: 4px on, 2px crenel
		for (int m = 0; m < 3; m++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = m == 0 ? 0f : m == 1 ? 6f : 12f;
			b[u + 3] = m == 0 ? 4f : m == 1 ? 10f : 16f;
			b[v] = pos ? 13f : 0f;
			b[v + 3] = pos ? 16f : 3f;
			b[1] = top ? 10f : 2f;
			b[4] = top ? 14f : 6f;
			out.add(b);
		}
		return out;
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

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getClickedFace().getAxis() == Direction.Axis.Y
			? ctx.getHorizontalDirection()
			: ctx.getClickedFace();
		double fy = ctx.getClickLocation().y - ctx.getClickedPos().getY();
		Half h = ctx.getClickedFace() == Direction.DOWN
			|| (ctx.getClickedFace().getAxis() != Direction.Axis.Y && fy > 0.5)
			? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState().setValue(FACING, f)
			.setValue(HALF, h), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		if (state.getValue(FACING) != Direction.WEST)
			return state.cycle(FACING);
		return state.setValue(FACING, Direction.NORTH).setValue(HALF,
			state.getValue(HALF) == Half.TOP ? Half.BOTTOM : Half.TOP);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
