package net.createcontour.block;

import java.util.List;
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
 * Four-step crown moulding at wall-ceiling joints: FACING is the
 * projection direction (the wall the cornice hangs off), HALF crowns
 * at the ceiling (TOP) or skirts an eave/ledge (BOTTOM). Steps run
 * 10/8/6/2 px tall over 4px depth bands. Pure AABB - render shares
 * the collision box list.
 */
public class ContourCorniceBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	public ContourCorniceBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH).setValue(HALF, Half.TOP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(HALF));
	}

	/** depth band (from wall) -> vertical extent measured from the cap */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean top = state.getValue(HALF) == Half.TOP;
		// {depth_lo, depth_hi, height} - heights step down with depth
		float[][] bands = {{0f, 4f, 10f}, {4f, 8f, 8f}, {8f, 12f, 6f},
			{12f, 16f, 2f}};
		List<float[]> out = new java.util.ArrayList<>(4);
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			if (pos) {
				b[a] = bd[0];
				b[a + 3] = bd[1];
			} else {
				b[a] = 16 - bd[1];
				b[a + 3] = 16 - bd[0];
			}
			if (top) {
				b[1] = 16 - bd[2];
				b[4] = 16;
			} else {
				b[1] = 0;
				b[4] = bd[2];
			}
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
		return s.optimize();
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
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
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
