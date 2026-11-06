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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Quarter-torus pipe elbow on all 12 edges: a 3-thick tube (matching
 * contour_tube's cross-section) swept a quarter turn between two
 * adjacent faces. Legs exit both faces centered at 8px; the bend
 * center sits 5px inward of the legs' intersection. Collision is
 * three boxes: both leg straights plus the bend rect, which never
 * under-fills the annular quarter.
 */
public class ContourElbowBlock extends ContourBlock {

	public static final EnumProperty<ContourTrimBlock.Edge> EDGE =
		ContourTrimBlock.EDGE;

	public ContourElbowBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(EDGE, ContourTrimBlock.Edge.DN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EDGE));
	}

	/** Bend circle center coordinate on a face axis (8 + 5*s). */
	public static float centerCoord(Direction d) {
		return d.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? 13f : 3f;
	}

	public static List<float[]> boxesFor(BlockState state) {
		ContourTrimBlock.Edge e = state.getValue(EDGE);
		Direction a = e.a, b = e.b;
		int u = a.getAxis().ordinal(), v = b.getAxis().ordinal();
		int w = e.runAxis().ordinal();
		float sA = a.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? 1f : -1f;
		float sB = b.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? 1f : -1f;
		float cu = 8f + 5f * sA, cv = 8f + 5f * sB;
		float uE = sA > 0 ? 16f : 0f, vE = sB > 0 ? 16f : 0f;
		List<float[]> out = new java.util.ArrayList<>(3);
		// leg A: straight tube from face u=uE to the arc tangent at C_u
		float[] legA = {0, 0, 0, 16, 16, 16};
		legA[u] = Math.min(cu, uE);
		legA[u + 3] = Math.max(cu, uE);
		legA[v] = 5f;
		legA[v + 3] = 11f;
		legA[w] = 5f;
		legA[w + 3] = 11f;
		out.add(legA);
		// leg B: straight tube from face v=vE to the arc tangent at C_v
		float[] legB = {0, 0, 0, 16, 16, 16};
		legB[v] = Math.min(cv, vE);
		legB[v + 3] = Math.max(cv, vE);
		legB[u] = 5f;
		legB[u + 3] = 11f;
		legB[w] = 5f;
		legB[w + 3] = 11f;
		out.add(legB);
		// bend rect: quarter-annulus cover from C to C - 8s on both axes
		float[] bend = {0, 0, 0, 16, 16, 16};
		bend[u] = Math.min(cu, cu - 8f * sA);
		bend[u + 3] = Math.max(cu, cu - 8f * sA);
		bend[v] = Math.min(cv, cv - 8f * sB);
		bend[v + 3] = Math.max(cv, cv - 8f * sB);
		bend[w] = 5f;
		bend[w + 3] = 11f;
		out.add(bend);
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
		// Edge = the two axes closest to an edge at the click point.
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
		return withWater(defaultBlockState()
			.setValue(EDGE, ContourTrimBlock.Edge.of(d1, d2)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(EDGE, ContourTrimBlock.Edge.of(
			rotation.rotate(state.getValue(EDGE).a),
			rotation.rotate(state.getValue(EDGE).b)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(EDGE, ContourTrimBlock.Edge.of(
			mirror.mirror(state.getValue(EDGE).a),
			mirror.mirror(state.getValue(EDGE).b)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		ContourTrimBlock.Edge[] vals = ContourTrimBlock.Edge.values();
		return state.setValue(EDGE,
			vals[(state.getValue(EDGE).ordinal() + 1) % vals.length]);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
