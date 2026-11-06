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
 * Balustrade: a flat railing segment for balconies, terraces, and
 * level edges (the handrail covers sloped stair runs). FACING is
 * the run direction; geometry is symmetric so opposite facings
 * look identical. Top rail, foot rail, three balusters, and half
 * end posts that pair into full posts between adjacent segments.
 */
public class ContourBalustradeBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourBalustradeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	public static List<float[]> boxesFor(BlockState state) {
		int u = state.getValue(FACING).getAxis().ordinal();
		int v = u == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(7);
		// top rail and foot rail span the full run
		float[][] rails = {{0, 12, 6.5f, 16, 15, 9.5f},
			{0, 1, 7, 16, 3.5f, 9}};
		for (float[] r : rails) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = r[0];
			b[u + 3] = r[3];
			b[1] = r[1];
			b[4] = r[4];
			b[v] = r[2];
			b[v + 3] = r[5];
			out.add(b);
		}
		// three balusters between foot and top rail
		float[][] balusters = {{2, 4}, {7, 9}, {12, 14}};
		for (float[] ba : balusters) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = ba[0];
			b[u + 3] = ba[1];
			b[1] = 3.5f;
			b[4] = 12f;
			b[v] = 7f;
			b[v + 3] = 9f;
			out.add(b);
		}
		// half end posts - pair into a full post between segments
		for (int e = 0; e < 2; e++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = e == 0 ? 0f : 14.5f;
			b[u + 3] = e == 0 ? 1.5f : 16f;
			b[1] = 0f;
			b[4] = 15f;
			b[v] = 7f;
			b[v + 3] = 9f;
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
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection()), ctx);
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
