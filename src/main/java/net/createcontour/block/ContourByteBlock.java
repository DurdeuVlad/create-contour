package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
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
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Quarter-block mini cube: one 8px octant per block space. Placement
 * hugs the clicked face at the clicked quadrant; the wrench cycles the
 * eight octants. (Multi-byte-per-cell is a deferred multipart family.)
 */
public class ContourByteBlock extends ContourBlock {

	public enum Octant implements StringRepresentable {
		X0Y0Z0("x0y0z0", 0, 0, 0), X1Y0Z0("x1y0z0", 1, 0, 0),
		X0Y1Z0("x0y1z0", 0, 1, 0), X1Y1Z0("x1y1z0", 1, 1, 0),
		X0Y0Z1("x0y0z1", 0, 0, 1), X1Y0Z1("x1y0z1", 1, 0, 1),
		X0Y1Z1("x0y1z1", 0, 1, 1), X1Y1Z1("x1y1z1", 1, 1, 1);

		private final String name;
		public final int x, y, z;

		Octant(String name, int x, int y, int z) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final EnumProperty<Octant> OCTANT = EnumProperty.create("octant", Octant.class);

	public ContourByteBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(OCTANT, Octant.X0Y0Z0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(OCTANT));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		Octant o = state.getValue(OCTANT);
		VoxelShape shape = Block.box(o.x * 8, o.y * 8, o.z * 8,
			o.x * 8 + 8, o.y * 8 + 8, o.z * 8 + 8);
		// issue #22: union EVERY extra occupant's boxes (any kind), not
		// just same-kind ones.
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			for (float[] b : be.extraFootprintBoxes())
				shape = net.minecraft.world.phys.shapes.Shapes.or(shape,
					Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return shape;
	}

	/** Octant index (0-7, matching Octant.ordinal) from a hit position. */
	public static int octantAt(Vec3 hit, BlockPos cell) {
		int ox = hit.x - cell.getX() > 0.5 ? 1 : 0;
		int oy = hit.y - cell.getY() > 0.5 ? 1 : 0;
		int oz = hit.z - cell.getZ() > 0.5 ? 1 : 0;
		for (Octant o : Octant.values())
			if (o.x == ox && o.y == oy && o.z == oz)
				return o.ordinal();
		return 0;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		// issue #22: universal geometric merge gate - any Contour
		// occupant item whose claim is legal (intra-kind lattice +
		// cross-kind disjointness) replaces into this cell.
		return ContourBlockEntity.canMergeInto(ctx);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/**
	 * Octant nearest the click: the face axis snaps to the side adjacent
	 * to the clicked block; the other two axes come from the hit point's
	 * in-face coordinates.
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();
		double fx = hit.x - p.getX(), fy = hit.y - p.getY(), fz = hit.z - p.getZ();
		int ox = face.getAxis() == Direction.Axis.X ? (face.getStepX() < 0 ? 1 : 0) : (fx > 0.5 ? 1 : 0);
		int oy = face.getAxis() == Direction.Axis.Y ? (face.getStepY() < 0 ? 1 : 0) : (fy > 0.5 ? 1 : 0);
		int oz = face.getAxis() == Direction.Axis.Z ? (face.getStepZ() < 0 ? 1 : 0) : (fz > 0.5 ? 1 : 0);
		for (Octant o : Octant.values())
			if (o.x == ox && o.y == oy && o.z == oz)
				return withWater(defaultBlockState().setValue(OCTANT, o), ctx);
		return withWater(defaultBlockState(), ctx);
	}

	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		net.minecraft.world.item.ItemStack stack, BlockState state,
		net.minecraft.world.level.Level level, BlockPos pos,
		net.minecraft.world.entity.player.Player player,
		net.minecraft.world.InteractionHand hand,
		net.minecraft.world.phys.BlockHitResult hit) {
		// issue #22: unified per-occupant paint targeting; a footprint
		// miss falls through to the host (occupant #0) paint path.
		return useOccupantItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// Occupant #0 cycles to the next FREE octant - landing on an
		// extra occupant's octant would collapse two units into one, and
		// (issue #22) the destination must also be geometrically
		// disjoint from every different-kind occupant.
		if (be == null)
			return state.cycle(OCTANT);
		java.util.Set<Integer> occupied = be.occupiedOctants();
		Octant o = state.getValue(OCTANT);
		for (int i = 0; i < 7; i++) {
			o = Octant.values()[(o.ordinal() + 1) % Octant.values().length];
			if (!occupied.contains(o.ordinal())
				&& be.crossKindClear(ContourBlockEntity.OccupantKind.BYTE,
					ContourBlockEntity.occupantBoxes(
						ContourBlockEntity.OccupantKind.BYTE, o.ordinal())))
				return state.setValue(OCTANT, o);
		}
		return state; // all other octants claimed: nothing to cycle to
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
