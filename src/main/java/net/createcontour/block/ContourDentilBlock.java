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
 * Dentil course: a backing band with four evenly spaced teeth
 * hanging below it, mounted proud of a wall face. The tooth-row
 * moulding that finishes classical cornices - the last standard
 * moulding profile after cove, scotia, ogee, bullnose, and cornice.
 * FACING is the wall normal; HALF TOP hangs from the top edge,
 * BOTTOM rises from the bottom edge.
 */
public class ContourDentilBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<net.minecraft.world.level.block.state.properties.Half> HALF =
		BlockStateProperties.HALF;

	public ContourDentilBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH)
			.setValue(HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(HALF));
	}

	/**
	 * u = run axis, n = facing normal (4px proud of the wall,
	 * centered n 6..10). Backing band 4px + four teeth 4px.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(FACING).getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		boolean top = state.getValue(HALF)
			== net.minecraft.world.level.block.state.properties.Half.TOP;
		float bandY0 = top ? 12f : 0f, bandY1 = top ? 16f : 4f;
		float toothY0 = top ? 8f : 4f, toothY1 = top ? 12f : 8f;
		float n0 = 6f, n1 = 10f; // centered panel, like arrow_slit
		List<float[]> out = new java.util.ArrayList<>(5);
		float[] band = {0, 0, 0, 16, 16, 16};
		band[u] = 0f;
		band[u + 3] = 16f;
		band[1] = bandY0;
		band[4] = bandY1;
		band[n] = n0;
		band[n + 3] = n1;
		out.add(band);
		float[][] teeth = {{0.5f, 3.5f}, {4.5f, 7.5f}, {8.5f, 11.5f},
			{12.5f, 15.5f}};
		for (float[] t : teeth) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = t[0];
			b[u + 3] = t[1];
			b[1] = toothY0;
			b[4] = toothY1;
			b[n] = n0;
			b[n + 3] = n1;
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
		BlockState f = state.cycle(FACING);
		return f.getValue(FACING) == state.getValue(FACING)
			? f.cycle(HALF) : f;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
