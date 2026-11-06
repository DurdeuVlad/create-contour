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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Directional layer plate: FACING is the face the slice hugs (the
 * clicked face's opposite), LAYERS 1..8 at 2px per layer measured inward
 * from that face. Covers what `contour_layers` cannot: vertical wall
 * slices and ceiling plates. The wrench cycles thickness; one paid unit
 * per placed block regardless of thickness (ADR-013).
 */
public class ContourSliceBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;
	public static final IntegerProperty LAYERS =
		IntegerProperty.create("layers", 1, 8);

	public ContourSliceBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.DOWN).setValue(LAYERS, 4));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(LAYERS));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		int t = state.getValue(LAYERS) * 2;
		return List.of(switch (state.getValue(FACING)) {
			case DOWN -> new float[]{0, 0, 0, 16, t, 16};
			case UP -> new float[]{0, 16 - t, 0, 16, 16, 16};
			case NORTH -> new float[]{0, 0, 0, 16, 16, t};
			case SOUTH -> new float[]{0, 0, 16 - t, 16, 16, 16};
			case WEST -> new float[]{0, 0, 0, t, 16, 16};
			default -> new float[]{16 - t, 0, 0, 16, 16, 16};
		});
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
		return state.getValue(LAYERS) < 8;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		// Stacking QoL: clicking an existing slice grows it one step
		// (snow-style) along its own facing instead of re-deriving.
		BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
		if (existing.is(this))
			return withWater(existing.setValue(LAYERS,
				Math.min(8, existing.getValue(LAYERS) + 1)), ctx);
		// Click depth along the face normal is always the cell boundary,
		// so thickness comes from sneak (thin) vs normal (half); the
		// wrench fine-tunes afterward.
		int layers = ctx.isSecondaryUseActive() ? 1 : 4;
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getClickedFace().getOpposite())
			.setValue(LAYERS, layers), ctx);
	}

	/**
	 * Merge eligibility: same item + room to grow + same FULL paint.
	 * Paint mismatches fall through to the adjacent cell like the slab.
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return state.getValue(LAYERS) < 8
			&& ctx.getItemInHand().is(asItem())
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
		return state.cycle(LAYERS);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
