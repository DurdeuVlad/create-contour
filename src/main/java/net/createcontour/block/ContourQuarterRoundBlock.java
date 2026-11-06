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
 * Quarter-cylinder moulding wrapping any of the cube's 12 edges
 * (cove/rounded corner): the two faces meeting at the edge are fully
 * covered legs, the curved face is an interior arc of radius 16px.
 * Collision is a 4-step staircase that never under-fills the arc.
 * Placement picks the edge nearest the click; the wrench cycles edges.
 */
public class ContourQuarterRoundBlock extends ContourBlock {

	public static final net.minecraft.world.level.block.state.properties.EnumProperty<Edge>
		EDGE = ContourTrimBlock.EDGE;

	public ContourQuarterRoundBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(EDGE, Edge.DN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EDGE));
	}

	/**
	 * Stepped collision: 4 boxes that never under-fill the arc. Step i
	 * covers b-inward distance [i*4,(i+1)*4] with a-inward extent
	 * sqrt(256-(4i)^2) px (16, 15.5, 13.9, 12).
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Edge e = state.getValue(EDGE);
		float[] h = {16f, 15.5f, 13.9f, 12f};
		List<float[]> out = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			setSpan(b, e.a, e.a.getAxisDirection()
				== Direction.AxisDirection.POSITIVE ? 16 - h[i] : 0, h[i]);
			setSpan(b, e.b, e.b.getAxisDirection()
				== Direction.AxisDirection.POSITIVE ? 16 - (i + 1) * 4f : i * 4f, 4f);
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
		// Same click-quadrant edge pick as trim.
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

	static Direction mirrorDir(Mirror m, Direction d) {
		return m.getRotation(d).rotate(d);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Edge e = state.getValue(EDGE);
		return state.setValue(EDGE,
			Edge.of(mirrorDir(mirror, e.a), mirrorDir(mirror, e.b)));
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
