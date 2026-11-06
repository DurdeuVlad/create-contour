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
 * Quoins: alternating long/short corner stones wrapping any of the
 * 12 cube edges. Long quoins reach 8px onto each face (L-pieces),
 * short quoins fill only the 4px corner cube - the classic
 * alternating masonry corner. Completes the dressed-stone kit
 * with pilasters, cornices, and dentils.
 */
public class ContourQuoinBlock extends ContourBlock {

	public static final EnumProperty<ContourTrimBlock.Edge> EDGE =
		ContourTrimBlock.EDGE;

	public ContourQuoinBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(EDGE, ContourTrimBlock.Edge.DN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EDGE));
	}

	/**
	 * u = a-axis, v = b-axis (corner at the u/v edge), w = run axis.
	 * Long quoins at w 0..4 and 8..12: L = {u 0..4, v 0..8} +
	 * {u 4..8, v 0..4}. Short quoins at w 4..8 and 12..16: corner
	 * cube only.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		ContourTrimBlock.Edge e = state.getValue(EDGE);
		Direction a = e.a, b = e.b;
		int u = a.getAxis().ordinal(), v = b.getAxis().ordinal();
		int w = e.runAxis().ordinal();
		boolean aPos = a.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean bPos = b.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<float[]> out = new java.util.ArrayList<>(6);
		for (int seg = 0; seg < 4; seg++) {
			float w0 = seg * 4f, w1 = w0 + 4f;
			boolean longQ = seg % 2 == 0;
			// L leg on face a: u 4px thick, v reaching 8px
			float[] la = {0, 0, 0, 16, 16, 16};
			la[u] = aPos ? 12f : 0f;
			la[u + 3] = aPos ? 16f : 4f;
			la[v] = bPos ? (longQ ? 8f : 12f) : 0f;
			la[v + 3] = bPos ? 16f : (longQ ? 8f : 4f);
			la[w] = w0;
			la[w + 3] = w1;
			out.add(la);
			if (longQ) {
				// L leg on face b: v 4px, u tail 4..8
				float[] lb = {0, 0, 0, 16, 16, 16};
				lb[u] = aPos ? 8f : 4f;
				lb[u + 3] = aPos ? 12f : 8f;
				lb[v] = bPos ? 12f : 0f;
				lb[v + 3] = bPos ? 16f : 4f;
				lb[w] = w0;
				lb[w + 3] = w1;
				out.add(lb);
			}
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
