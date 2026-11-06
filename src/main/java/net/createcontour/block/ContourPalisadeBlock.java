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
 * Palisade: a wall run of pointed logs - four posts with two-stage
 * tips plus two back rails, in a wall-parallel panel. FACING is the
 * outward normal. The early-fort counterpart of the portcullis
 * and arrow slit; adjacent segments form a continuous stockade.
 */
public class ContourPalisadeBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourPalisadeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * u = wall-run axis, n = facing normal. Posts at 3px wide with
	 * 1px gaps, stepped tips above y14, back rails slightly proud
	 * of the post backs.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(FACING).getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(14);
		float[][] posts = {{1, 4}, {5, 8}, {9, 12}, {13, 16}};
		for (float[] p : posts) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = p[0];
			b[u + 3] = p[1];
			b[1] = 0f;
			b[4] = 14f;
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
			out.add(b);
			// two-stage point
			float[] t1 = {0, 0, 0, 16, 16, 16};
			t1[u] = p[0] + 0.5f;
			t1[u + 3] = p[1] - 0.5f;
			t1[1] = 14f;
			t1[4] = 15f;
			t1[n] = 7f;
			t1[n + 3] = 9f;
			out.add(t1);
			float[] t2 = {0, 0, 0, 16, 16, 16};
			t2[u] = p[0] + 1f;
			t2[u + 3] = p[1] - 1f;
			t2[1] = 15f;
			t2[4] = 16f;
			t2[n] = 7.5f;
			t2[n + 3] = 8.5f;
			out.add(t2);
		}
		for (float y = 4f; y <= 10f; y += 6f) {
			float[] r = {0, 0, 0, 16, 16, 16};
			r[u] = 0f;
			r[u + 3] = 16f;
			r[1] = y;
			r[4] = y + 2f;
			r[n] = 9.5f;
			r[n + 3] = 11f;
			out.add(r);
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
		return state.cycle(FACING);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
