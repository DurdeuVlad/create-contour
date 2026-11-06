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
 * Pilot: the locomotive cowcatcher - a stepped wedge of three
 * rows that drop and narrow toward FACING, plus a low skid plate
 * under the tip. FACING is the direction the plow pushes. Pure
 * box geometry, so render and collision share boxesFor.
 */
public class ContourPilotBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourPilotBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Four boxes measured from the facing face: skid at the bottom
	 * tip, then three wedge rows rising and widening backward.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(4);
		out.add(box(n, pos, 0f, 6f, u, 5f, 11f, 1f, 2f));   // skid
		out.add(box(n, pos, 0f, 3f, u, 5f, 11f, 2f, 6f));   // tip row
		out.add(box(n, pos, 3f, 6f, u, 4f, 12f, 6f, 10f));  // mid row
		out.add(box(n, pos, 6f, 9f, u, 3f, 13f, 10f, 14f)); // top row
		return out;
	}

	private static float[] box(int n, boolean pos, float d0, float d1,
		int u, float u0, float u1, float y0, float y1) {
		float[] b = {0, 0, 0, 16, 16, 16};
		b[n] = pos ? 16f - d1 : d0;
		b[n + 3] = pos ? 16f - d0 : d1;
		b[u] = u0;
		b[u + 3] = u1;
		b[1] = y0;
		b[4] = y1;
		return b;
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
		Direction f = ctx.getClickedFace().getAxis().isHorizontal()
			? ctx.getClickedFace() : ctx.getHorizontalDirection();
		return withWater(defaultBlockState().setValue(FACING, f), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
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
