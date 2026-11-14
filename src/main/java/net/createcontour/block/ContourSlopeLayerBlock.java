package net.createcontour.block;

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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Issue #21 - stackable ramp slices. One block = SIZE bottom eighths of the
 * 45-degree slope defined by {@link ContourSlopeBlock}: at run distance d
 * (measured from the FACING wall, the same convention as contour_slope) the
 * shape fills y <= min(2*SIZE, 16-d). Each added slice is the next 2px wedge
 * stratum, so slices stack only bottom-up in slope-consistent order and a
 * SIZE=8 stack has exactly the contour_slope BOTTOM silhouette (documented
 * equivalence - the block id is never silently converted).
 *
 * <p>Stacking gate: {@link #canBeReplaced} additionally requires the implied
 * incoming facing ({@code ctx.getHorizontalDirection()}, the same rule a
 * fresh placement applies) to equal the stack's FACING. A wrong-facing click
 * is rejected so it can never grow the stack out of order; it falls through
 * to the normal adjacent cell instead.
 *
 * <p>Ledger: {@link #hostUnits} returns SIZE, i.e. a per-slice material
 * ledger of one paid unit per slice (each slice is 1/8 of a full slope's
 * units). Painting prices the whole block at SIZE units, drops refund the
 * stored ledger and break drops return SIZE slice items (proportional).
 */
public class ContourSlopeLayerBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty SIZE = IntegerProperty.create("size", 1, 8);

	// [facing.get2DDataValue() * 8 + (SIZE - 1)]
	private static final VoxelShape[] SHAPES = new VoxelShape[4 * 8];

	static {
		for (Direction f : Direction.Plane.HORIZONTAL)
			for (int size = 1; size <= 8; size++)
				SHAPES[f.get2DDataValue() * 8 + (size - 1)] = build(f, size);
	}

	public ContourSlopeLayerBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH)
			.setValue(SIZE, 8));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, SIZE));
	}

	/** One ledger unit per slice: paint cost, refunds and drops scale with SIZE. */
	@Override
	public int hostUnits(BlockState state) {
		return state.getValue(SIZE);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[state.getValue(FACING).get2DDataValue() * 8
			+ state.getValue(SIZE) - 1];
	}

	/**
	 * Eight 2px columns along the run; column j tops at
	 * min(2*SIZE, 16-2j), the same upper-bound staircase convention as
	 * {@link ContourSlopeBlock#build} but at 2px pitch instead of 4px.
	 */
	private static VoxelShape build(Direction f, int size) {
		VoxelShape s = Shapes.empty();
		float h = size * 2f;
		for (int j = 0; j < 8; j++) {
			float lo = j * 2f, hi = lo + 2f;
			float y1 = Math.min(h, 16f - lo);
			s = Shapes.or(s, switch (f) {
				case NORTH -> Block.box(0, 0, lo, 16, y1, hi);
				case SOUTH -> Block.box(0, 0, 16 - hi, 16, y1, 16 - lo);
				case EAST -> Block.box(16 - hi, 0, 0, 16 - lo, y1, 16);
				default -> Block.box(lo, 0, 0, hi, y1, 16);
			});
		}
		return s.optimize();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		BlockState existing = ctx.getLevel().getBlockState(pos);
		if (existing.is(this))
			// Slice growth keeps the stack's facing: each +1 adds the next
			// 2px wedge stratum at the top, extending the ramp upward in
			// the existing FACING direction.
			return withWater(existing.setValue(SIZE,
				Math.min(8, existing.getValue(SIZE) + 1)), ctx);
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection())
			.setValue(SIZE, 1), ctx);
	}

	/**
	 * Merge gate - a slice may join the stack only when it extends the ramp
	 * in slope-consistent order:
	 * <ul>
	 * <li>same shape item and below the SIZE=8 cap (layers rule),</li>
	 * <li>same paint (blank slices merge only into neutral stacks; a
	 * painted stack accepts a blank slice only when the offhand/look
	 * material matches - the shared sameMaterial rule),</li>
	 * <li>the implied incoming facing equals the stack's FACING. A
	 * wrong-facing click would insert a slice that does not continue the
	 * ramp, so the merge is rejected and placement falls through to the
	 * adjacent cell - inconsistent geometry is never silently grown.</li>
	 * </ul>
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return state.getValue(SIZE) < 8
			&& ctx.getItemInHand().is(asItem())
			&& ctx.getHorizontalDirection() == state.getValue(FACING)
			&& sameMaterial(ctx, ctx.getClickedPos());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// Facing cycle only - SIZE is an economic resource and is never
		// minted by the wrench.
		return state.cycle(FACING);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}

	@Override
	protected BlockState carryOrientation(BlockState state, Direction facing, boolean top) {
		return state.setValue(FACING, facing);
	}
}
