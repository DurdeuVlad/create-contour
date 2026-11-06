package net.createcontour.block;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourTrimBlock.Edge;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * S-curve cyma moulding (ogee) spanning an 8px corner square on
 * any of the cube's 12 edges - the classic crown/base transition.
 * The solid keeps the corner under a smoothstep curve from
 * (a=0,b=8) to (a=8,b=0); collision is a 4-band staircase that
 * never under-fills the curve. Placement picks the edge nearest
 * the click; the wrench cycles edges.
 */
public class ContourOgeeBlock extends ContourBlock {

	public static final net.minecraft.world.level.block.state.properties.EnumProperty<Edge>
		EDGE = ContourTrimBlock.EDGE;

	public ContourOgeeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(EDGE, Edge.DN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EDGE));
	}

	/**
	 * {aLo, aHi, bExtent} bands: the S-curve b = 8*(1-ss(a/8)) falls
	 * monotonically, so each band's max b is its left edge value
	 * (8, 6.75, 4, 1.25). The union never under-fills the profile.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Edge e = state.getValue(EDGE);
		float[][] bands = {{0f, 2f, 8f}, {2f, 4f, 6.75f},
			{4f, 6f, 4f}, {6f, 8f, 1.25f}};
		List<float[]> out = new ArrayList<>(4);
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			boolean aPos = e.a.getAxisDirection()
				== Direction.AxisDirection.POSITIVE;
			setSpan(b, e.a, aPos ? 16 - bd[1] : bd[0], bd[1] - bd[0]);
			setSpan(b, e.b, e.b.getAxisDirection()
				== Direction.AxisDirection.POSITIVE ? 16 - bd[2] : 0, bd[2]);
			out.add(b);
		}
		return out;
	}

	private static void setSpan(float[] b, Direction d, float lo, float len) {
		int i = d.getAxis().ordinal();
		b[i] = lo;
		b[i + 3] = lo + len;
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
		// Same click-quadrant edge pick as trim/quarter_round.
		double[] frac = {
			ctx.getClickLocation().x - ctx.getClickedPos().getX(),
			ctx.getClickLocation().y - ctx.getClickedPos().getY(),
			ctx.getClickLocation().z - ctx.getClickedPos().getZ()};
		Direction.Axis[] axes = {Direction.Axis.X, Direction.Axis.Y,
			Direction.Axis.Z};
		java.util.Arrays.sort(axes, java.util.Comparator.comparingDouble(
			ax -> Math.min(frac[ax.ordinal()], 1 - frac[ax.ordinal()])));
		Direction d1 = Direction.fromAxisAndDirection(axes[0],
			frac[axes[0].ordinal()] < 0.5
				? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
		Direction d2 = Direction.fromAxisAndDirection(axes[1],
			frac[axes[1].ordinal()] < 0.5
				? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
		return withWater(defaultBlockState().setValue(EDGE, Edge.of(d1, d2)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Edge e = state.getValue(EDGE);
		return state.setValue(EDGE,
			Edge.of(rotation.rotate(e.a), rotation.rotate(e.b)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Edge e = state.getValue(EDGE);
		return state.setValue(EDGE,
			Edge.of(ContourQuarterRoundBlock.mirrorDir(mirror, e.a),
				ContourQuarterRoundBlock.mirrorDir(mirror, e.b)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(EDGE);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
