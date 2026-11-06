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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Half-round vertical column projecting 8px from a wall face - the
 * classical pilaster. FACING is the bulge direction (horizontal);
 * the flat chord sits on the wall. Collision is stepped columns
 * under the half-dome - it never fills past the curve.
 */
public class ContourPilasterBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourPilasterBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Stepped half-dome collision: circle of r8 centered on the wall
	 * plane; five in-plane columns whose depths cover the arc, so the
	 * union never under-fills the visible half-dome.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		// column spans on the in-plane axis, depth = max arc depth per span
		float[][] xr = {{0f, 2f}, {2f, 5f}, {5f, 11f}, {11f, 14f}, {14f, 16f}};
		float[] d = {5.3f, 7.5f, 8f, 7.5f, 5.3f};
		List<float[]> out = new java.util.ArrayList<>(5);
		for (int i = 0; i < 5; i++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			int u = a == 0 ? 2 : 0; // in-plane horizontal axis
			b[u] = xr[i][0];
			b[u + 3] = xr[i][1];
			if (pos) {
				b[a] = 0;
				b[a + 3] = d[i];
			} else {
				b[a] = 16 - d[i];
				b[a + 3] = 16;
			}
			b[1] = 0;
			b[4] = 16;
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
		return withWater(defaultBlockState().setValue(FACING, f), ctx);
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
		return state.cycle(FACING);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
