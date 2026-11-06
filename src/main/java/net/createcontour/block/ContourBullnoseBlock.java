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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Convex round-off of one sharp edge on all 12 edges: the r6 quarter
 * arc removes the corner where faces a and b meet - countertop
 * nosing, rounded wall corners, softened stair edges. Complement of
 * the concave quarter_round. Collision is a 5-box staircase whose
 * union covers the kept region and never under-fills the arc.
 */
public class ContourBullnoseBlock extends ContourBlock {

	public static final EnumProperty<Edge> EDGE = ContourTrimBlock.EDGE;
	private static final float R = 6f;

	public ContourBullnoseBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(EDGE, Edge.DN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EDGE));
	}

	/**
	 * Stepped cover of cube-minus-rounded-corner: du/dv measure inward
	 * distance from faces a/b. Kept: du>=6 or dv>=6 or inside the r6
	 * circle at (6,6). Four du-bands at the band-far-edge depth plus
	 * one full box du>=4.5.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Edge e = state.getValue(EDGE);
		boolean aPos = e.a.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean bPos = e.b.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int aAx = e.a.getAxis().ordinal(), bAx = e.b.getAxis().ordinal();
		int r = e.runAxis().ordinal();
		List<float[]> out = new ArrayList<>(5);
		// du >= 4.5: everything kept (dv_min(4.5) = 0.19 ~ 0)
		float[] keep = {0, 0, 0, 16, 16, 16};
		setSpan(keep, aAx, aPos ? 0f : 4.5f, aPos ? 11.5f : 16f);
		out.add(keep);
		// du bands {du_hi, dv_min}: cover the kept corner square
		float[][] bands = {{0.75f, 3.10f}, {1.5f, 2.03f}, {3f, 0.80f},
			{4.5f, 0.19f}};
		float duLo = 0f;
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			// coord = pos ? 16 - du : du
			setSpan(b, aAx, aPos ? 16 - bd[0] : duLo,
				aPos ? 16 - duLo : bd[0]);
			setSpan(b, bAx, bPos ? 0f : bd[1], bPos ? 16 - bd[1] : 16f);
			b[r] = 0;
			b[r + 3] = 16;
			out.add(b);
			duLo = bd[0];
		}
		return out;
	}

	private static void setSpan(float[] b, int ax, float lo, float hi) {
		b[ax] = lo;
		b[ax + 3] = hi;
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
		// edge nearest the click, like the trim/quarter_round pick
		var hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		double fx = hit.x - p.getX(), fy = hit.y - p.getY(),
			fz = hit.z - p.getZ();
		double[] frac = {fx, fy, fz};
		Direction.Axis clicked = ctx.getClickedFace().getAxis();
		Direction.Axis[] axes = {Direction.Axis.X, Direction.Axis.Y,
			Direction.Axis.Z};
		Direction.Axis a1 = null, a2 = null;
		for (Direction.Axis ax : axes)
			if (ax != clicked) {
				if (a1 == null)
					a1 = ax;
				else
					a2 = ax;
			}
		Direction d1 = Direction.fromAxisAndDirection(a1,
			frac[a1.ordinal()] < 0.5
				? Direction.AxisDirection.NEGATIVE
				: Direction.AxisDirection.POSITIVE);
		Direction d2 = Direction.fromAxisAndDirection(a2,
			frac[a2.ordinal()] < 0.5
				? Direction.AxisDirection.NEGATIVE
				: Direction.AxisDirection.POSITIVE);
		return withWater(defaultBlockState().setValue(EDGE, Edge.of(d1, d2)),
			ctx);
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
