package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Edge trim (moulding): an L-cross-section piece wrapping one of the
 * cube's 12 edges - two 4px-wide, 3px-thick plates meeting at the edge.
 * Skirting boards, crown moulding, corner edging. Placement picks the
 * edge nearest the click; the wrench cycles all 12 edges.
 */
public class ContourTrimBlock extends ContourBlock {

	public enum Edge implements StringRepresentable {
		DN("dn", Direction.DOWN, Direction.NORTH),
		DS("ds", Direction.DOWN, Direction.SOUTH),
		DE("de", Direction.DOWN, Direction.EAST),
		DW("dw", Direction.DOWN, Direction.WEST),
		UN("un", Direction.UP, Direction.NORTH),
		US("us", Direction.UP, Direction.SOUTH),
		UE("ue", Direction.UP, Direction.EAST),
		UW("uw", Direction.UP, Direction.WEST),
		NE("ne", Direction.NORTH, Direction.EAST),
		NW("nw", Direction.NORTH, Direction.WEST),
		SE("se", Direction.SOUTH, Direction.EAST),
		SW("sw", Direction.SOUTH, Direction.WEST);
		private final String name;
		public final Direction a, b;
		Edge(String name, Direction a, Direction b) {
			this.name = name;
			this.a = a;
			this.b = b;
		}
		@Override
		public String getSerializedName() {
			return name;
		}
		/** The axis this edge runs along. */
		public Direction.Axis runAxis() {
			for (Direction.Axis ax : Direction.Axis.values())
				if (a.getAxis() != ax && b.getAxis() != ax)
					return ax;
			return Direction.Axis.Y;
		}
		public static Edge of(Direction a, Direction b) {
			for (Edge e : values())
				if ((e.a == a && e.b == b) || (e.a == b && e.b == a))
					return e;
			return DN;
		}
	}

	public static final EnumProperty<Edge> EDGE =
		EnumProperty.create("edge", Edge.class);

	public ContourTrimBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(EDGE, Edge.DN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EDGE));
	}

	/**
	 * 4px-wide, 3px-thick plate hugging `face` at the `near` edge:
	 * the plate touches `face`'s plane and extends 4px from `near`'s edge.
	 */
	private static float[] plate(Direction face, Direction near) {
		float[] b = {0, 0, 0, 16, 16, 16};
		Direction.Axis fa = face.getAxis();
		if (face.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
			b[3 + fa.ordinal()] = 3;       // from low edge
		else
			b[fa.ordinal()] = 13;          // from high edge
		Direction.Axis na = near.getAxis();
		if (near.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
			b[3 + na.ordinal()] = 4;
		else
			b[na.ordinal()] = 12;
		return b;
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		Edge e = state.getValue(EDGE);
		return List.of(plate(e.a, e.b), plate(e.b, e.a));
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
		return withWater(defaultBlockState().setValue(EDGE, Edge.of(d1, d2)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state,
		net.minecraft.world.level.block.Rotation rotation) {
		Edge e = state.getValue(EDGE);
		return state.setValue(EDGE, Edge.of(rotation.rotate(e.a), rotation.rotate(e.b)));
	}

	@Override
	public BlockState mirror(BlockState state,
		net.minecraft.world.level.block.Mirror mirror) {
		// Mirror each edge direction independently - getRotation(e.a)
		// alone cannot mirror the other axis (vertical edges NE/SE/SW/NW
		// would come out unchanged under an x/z flip).
		Edge e = state.getValue(EDGE);
		return state.setValue(EDGE, Edge.of(
			ContourQuarterRoundBlock.mirrorDir(mirror, e.a),
			ContourQuarterRoundBlock.mirrorDir(mirror, e.b)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		Edge[] v = Edge.values();
		return state.setValue(EDGE, v[(state.getValue(EDGE).ordinal() + 1) % v.length]);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
