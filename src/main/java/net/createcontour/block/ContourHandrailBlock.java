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
 * Stair handrail: a 2px square rail climbing 8px over the run at the
 * stair pitch (26.57 degrees, matching contour_slope), plus posts
 * standing from the block floor to the rail at both ends. FACING is
 * the direction the rail ascends toward; adjacent same-facing rails
 * form a continuous handrail over a stair run.
 */
public class ContourHandrailBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	/** rail centerline: y = 5.5 + 0.5*run */
	public static final float RAIL_Y0 = 5.5f, RAIL_SLOPE = 0.5f;

	public ContourHandrailBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Four stepped rail cover columns plus a low and a high post.
	 * u = run axis (ascends toward +u), v = depth axis.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int u = f.getAxis().ordinal();
		int v = u == 0 ? 2 : 0;
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<float[]> out = new java.util.ArrayList<>(6);
		// rail cover: 4px bands, y covering centerline +/-1.5
		float[][] bands = {{0, 4, 4, 9}, {4, 8, 6, 11}, {8, 12, 8, 13},
			{12, 16, 10, 15}};
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			float lo = pos ? bd[0] : 16f - bd[1];
			float hi = pos ? bd[1] : 16f - bd[0];
			b[u] = lo;
			b[u + 3] = hi;
			b[v] = 7f;
			b[v + 3] = 9f;
			b[1] = bd[2];
			b[4] = bd[3];
			out.add(b);
		}
		// posts under the rail ends
		float[][] posts = {{1, 3, 0, 5.5f}, {13, 15, 0, 12.5f}};
		for (float[] pd : posts) {
			float[] b = {0, 0, 0, 16, 16, 16};
			float lo = pos ? pd[0] : 16f - pd[1];
			float hi = pos ? pd[1] : 16f - pd[0];
			b[u] = lo;
			b[u + 3] = hi;
			b[v] = 7f;
			b[v + 3] = 9f;
			b[1] = pd[2];
			b[4] = pd[3];
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
