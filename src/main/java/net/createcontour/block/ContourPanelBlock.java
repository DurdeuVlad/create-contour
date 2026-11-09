package net.createcontour.block;

import java.util.ArrayList;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Positional thin panel (2px): 6 face positions, 3 centered planes,
 * 24 edge strips (lies on face A hugging the edge shared with adjacent
 * face B), and 6 diagonal planes through opposite edge pairs. One panel
 * per block space (ADR-013; true multipart is a later family).
 *
 * Placement maps the clicked face + hit point: the outer quarter near
 * an edge places the edge strip, the centered region places the mid
 * plane, otherwise the face plate. The wrench cycles all 39 positions
 * in enum order (planes -> mids -> edges -> diagonals).
 */
public class ContourPanelBlock extends ContourBlock {

	public enum PanelPos implements StringRepresentable {
		FACE_UP, FACE_DOWN, FACE_NORTH, FACE_SOUTH, FACE_EAST, FACE_WEST,
		MID_X, MID_Y, MID_Z,
		EDGE_UP_NORTH, EDGE_UP_SOUTH, EDGE_UP_EAST, EDGE_UP_WEST,
		EDGE_DOWN_NORTH, EDGE_DOWN_SOUTH, EDGE_DOWN_EAST, EDGE_DOWN_WEST,
		EDGE_NORTH_UP, EDGE_NORTH_DOWN, EDGE_NORTH_EAST, EDGE_NORTH_WEST,
		EDGE_SOUTH_UP, EDGE_SOUTH_DOWN, EDGE_SOUTH_EAST, EDGE_SOUTH_WEST,
		EDGE_EAST_UP, EDGE_EAST_DOWN, EDGE_EAST_NORTH, EDGE_EAST_SOUTH,
		EDGE_WEST_UP, EDGE_WEST_DOWN, EDGE_WEST_NORTH, EDGE_WEST_SOUTH,
		DIAG_Y0, DIAG_Y1, DIAG_X0, DIAG_X1, DIAG_Z0, DIAG_Z1;

		private final String name;

		PanelPos() {
			this.name = name().toLowerCase(java.util.Locale.ROOT);
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public boolean isDiagonal() {
			return ordinal() >= DIAG_Y0.ordinal();
		}
	}

	public static final EnumProperty<PanelPos> POSITION =
		EnumProperty.create("position", PanelPos.class);

	private static final VoxelShape[] SHAPES = new VoxelShape[PanelPos.values().length];

	static {
		for (PanelPos p : PanelPos.values())
			SHAPES[p.ordinal()] = buildShape(p);
	}

	private static VoxelShape buildShape(PanelPos p) {
		if (p.isDiagonal())
			return diagShape(p);
		List<float[]> boxes = boxesFor(p);
		VoxelShape shape = Shapes.empty();
		for (float[] b : boxes)
			shape = Shapes.or(shape, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return shape;
	}

	/** Stepped 5-slice approximation for the 45-degree plate. */
	private static VoxelShape diagShape(PanelPos p) {
		VoxelShape shape = Shapes.empty();
		for (int i = 0; i < 5; i++) {
			float s = i * 3.2f, e = s + 3.2f;
			float lo = Math.max(0, s - 1.5f), hi = Math.min(16, e + 1.5f - 3.2f);
			shape = Shapes.or(shape, switch (p) {
				case DIAG_Y0 -> Block.box(lo, 0, s, hi + 3.2f > 16 ? 16 : hi + 3.2f, 16, e);
				case DIAG_Y1 -> Block.box(16 - Math.min(16, hi + 3.2f), 0, s, 16 - lo, 16, e);
				case DIAG_X0 -> Block.box(s, lo, 0, e, hi + 3.2f > 16 ? 16 : hi + 3.2f, 16);
				case DIAG_X1 -> Block.box(s, 16 - Math.min(16, hi + 3.2f), 0, e, 16 - lo, 16);
				case DIAG_Z0 -> Block.box(s, 0, lo, e, 16, hi + 3.2f > 16 ? 16 : hi + 3.2f);
				default -> Block.box(s, 0, 16 - Math.min(16, hi + 3.2f), e, 16, 16 - lo);
			});
		}
		return shape;
	}

	/**
	 * Pixel-space AABBs per position. Diagonals contribute their two
	 * corner columns (the band faces are emitted by the model; collision
	 * uses the stepped approximation in diagShape).
	 */
	public static List<float[]> boxesFor(PanelPos p) {
		List<float[]> out = new ArrayList<>();
		switch (p) {
			case DIAG_Y0, DIAG_X0, DIAG_Z0 -> {
				out.add(new float[]{0, 0, 0, 1.5f, 16, 1.5f});
				out.add(new float[]{14.5f, 0, 14.5f, 16, 16, 16});
				fixDiagColumns(out, p);
			}
			case DIAG_Y1, DIAG_X1, DIAG_Z1 -> {
				out.add(new float[]{14.5f, 0, 0, 16, 16, 1.5f});
				out.add(new float[]{0, 0, 14.5f, 1.5f, 16, 16});
				fixDiagColumns(out, p);
			}
			default -> {
			}
		}
		if (!out.isEmpty())
			return out;
		switch (p) {
			case FACE_UP -> out.add(new float[]{0, 14, 0, 16, 16, 16});
			case FACE_DOWN -> out.add(new float[]{0, 0, 0, 16, 2, 16});
			case FACE_NORTH -> out.add(new float[]{0, 0, 0, 16, 16, 2});
			case FACE_SOUTH -> out.add(new float[]{0, 0, 14, 16, 16, 16});
			case FACE_EAST -> out.add(new float[]{14, 0, 0, 16, 16, 16});
			case FACE_WEST -> out.add(new float[]{0, 0, 0, 2, 16, 16});
			case MID_X -> out.add(new float[]{7, 0, 0, 9, 16, 16});
			case MID_Y -> out.add(new float[]{0, 7, 0, 16, 9, 16});
			case MID_Z -> out.add(new float[]{0, 0, 7, 16, 16, 9});
			default -> {
				if (p.name().startsWith("EDGE_")) {
					String[] parts = p.name().substring(5).split("_");
					Direction lie = Direction.byName(parts[0].toLowerCase(java.util.Locale.ROOT));
					Direction hug = Direction.byName(parts[1].toLowerCase(java.util.Locale.ROOT));
					float[] b = {0, 0, 0, 16, 16, 16};
					setBand(b, lie, 2f);   // thickness axis
					setBand(b, hug, 8f);   // width axis hugging the edge
					out.add(b);
				}
			}
		}
		return out;
	}

	/**
	 * Corner columns sit at opposite ends of the (a1,a2) diagonal,
	 * extruded along a3: for X/Z bands the written template is in the
	 * Y-extruded orientation and gets axis-permuted here.
	 */
	private static void fixDiagColumns(List<float[]> out, PanelPos p) {
		if (p == PanelPos.DIAG_X0 || p == PanelPos.DIAG_X1) {
			// written (x,z) band along y -> need (y,z) band along x: swap x<->y
			for (float[] b : out) {
				float t = b[0]; b[0] = b[1]; b[1] = t;
				t = b[3]; b[3] = b[4]; b[4] = t;
			}
		} else if (p == PanelPos.DIAG_Z0 || p == PanelPos.DIAG_Z1) {
			// need (x,y) band along z: swap y<->z
			for (float[] b : out) {
				float t = b[1]; b[1] = b[2]; b[2] = t;
				t = b[4]; b[4] = b[5]; b[5] = t;
			}
		}
	}

	/** Sets the axis range of box b to the 0..w band on face d's side. */
	private static void setBand(float[] b, Direction d, float w) {
		int i = axisIndex(d.getAxis());
		if (d.getAxisDirection() == Direction.AxisDirection.POSITIVE)
			b[i] = 16f - w;
		else
			b[i + 3] = w;
	}

	private static int axisIndex(Direction.Axis a) {
		return a == Direction.Axis.X ? 0 : a == Direction.Axis.Y ? 1 : 2;
	}

	public ContourPanelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(POSITION, PanelPos.FACE_NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POSITION));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = SHAPES[state.getValue(POSITION).ordinal()];
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			for (ContourBlockEntity.PanelOccupant occ : be.getPanelOccupants())
				for (float[] b : PanelSlots.occupantBoxes(occ.slot(), occ.kind()))
					s = Shapes.or(s,
						Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/** Merge eligibility: family item + disjoint slot claim (PanelSlots). */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return PanelSlots.canMergeReplace(state, ctx);
	}

	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		net.minecraft.world.item.ItemStack stack, BlockState state,
		net.minecraft.world.level.Level level, BlockPos pos,
		net.minecraft.world.entity.player.Player player,
		net.minecraft.world.InteractionHand hand,
		net.minecraft.world.phys.BlockHitResult hit) {
		return usePanelOccupantItemOn(stack, state, level, pos, player, hand,
			hit);
	}

	/**
	 * Click geometry -> position. In-face coords u/v of the hit point:
	 * outer 4px band -> edge strip on the clicked face hugging that edge;
	 * centered 4x4px -> middle plane normal to the clicked face;
	 * otherwise -> face plate on the clicked face.
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getClickedFace();
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		double u, v;
		Direction uMin, uMax, vMin, vMax;
		switch (f.getAxis()) {
			case X -> {
				u = hit.z - p.getZ(); v = hit.y - p.getY();
				uMin = Direction.NORTH; uMax = Direction.SOUTH;
				vMin = Direction.DOWN; vMax = Direction.UP;
			}
			case Y -> {
				u = hit.x - p.getX(); v = hit.z - p.getZ();
				uMin = Direction.WEST; uMax = Direction.EAST;
				vMin = Direction.NORTH; vMax = Direction.SOUTH;
			}
			default -> {
				u = hit.x - p.getX(); v = hit.y - p.getY();
				uMin = Direction.WEST; uMax = Direction.EAST;
				vMin = Direction.DOWN; vMax = Direction.UP;
			}
		}
		PanelPos pos;
		if (u >= 0.25 && u <= 0.75 && v >= 0.25 && v <= 0.75
			&& u >= 0.375 && u <= 0.625 && v >= 0.375 && v <= 0.625)
			pos = midFor(f.getAxis());
		else if (u < 0.25)
			pos = edgeOn(f, uMin);
		else if (u > 0.75)
			pos = edgeOn(f, uMax);
		else if (v < 0.25)
			pos = edgeOn(f, vMin);
		else if (v > 0.75)
			pos = edgeOn(f, vMax);
		else
			pos = faceFor(f);
		return withWater(defaultBlockState().setValue(POSITION, pos), ctx);
	}

	private static PanelPos midFor(Direction.Axis a) {
		return a == Direction.Axis.X ? PanelPos.MID_X
			: a == Direction.Axis.Y ? PanelPos.MID_Y : PanelPos.MID_Z;
	}

	private static PanelPos faceFor(Direction d) {
		return PanelPos.values()[d.ordinal()]; // FACE_* declared in ordinal order
	}

	private static PanelPos edgeOn(Direction lie, Direction hug) {
		String name = "EDGE_" + lie.name() + "_" + hug.name();
		for (PanelPos p : PanelPos.values())
			if (p.name().equals(name))
				return p;
		return faceFor(lie);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// Cycle positions, skipping any whose slot footprint collides
		// with OTHER occupants. Non-FACE positions claim all 24 slots,
		// so they stay reachable only while no extras exist.
		if (be == null)
			return state.cycle(POSITION);
		java.util.Set<Integer> others = be.occupiedPanelSlots();
		others.removeAll(currentSlots(state));
		PanelPos p = state.getValue(POSITION);
		for (int i = 0; i < PanelPos.values().length - 1; i++) {
			p = PanelPos.values()[(p.ordinal() + 1)
				% PanelPos.values().length];
			BlockState cand = state.setValue(POSITION, p);
			if (java.util.Collections.disjoint(others, currentSlots(cand)))
				return cand;
		}
		return state;
	}

	private static java.util.Set<Integer> currentSlots(BlockState state) {
		return PanelSlots.stateSlots(state);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
