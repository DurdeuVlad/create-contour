package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Parametric layer plate: LAYERS 1..8 at 2px per layer. Placement picks
 * thickness from the hit height; the wrench cycles 1..8 in place. One
 * paid unit per placed block regardless of thickness (ADR-013).
 */
public class ContourLayersBlock extends ContourBlock {

	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);

	private static final VoxelShape[] SHAPES = new VoxelShape[9];
	static {
		for (int i = 1; i <= 8; i++)
			SHAPES[i] = Block.box(0, 0, 0, 16, i * 2, 16);
	}

	public ContourLayersBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(LAYERS, 4));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(LAYERS));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[state.getValue(LAYERS)];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return state.getValue(LAYERS) < 8;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		// Stacking QoL: clicking an existing layer plate grows it one
		// step (snow-style) instead of re-deriving thickness.
		BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
		if (existing.is(this))
			return withWater(existing.setValue(LAYERS,
				Math.min(8, existing.getValue(LAYERS) + 1)), ctx);
		double fy = ctx.getClickLocation().y - ctx.getClickedPos().getY();
		int layers = ctx.getClickedFace() == net.minecraft.core.Direction.UP ? 8
			: Mth.clamp((int) Math.ceil(fy * 8), 1, 8);
		return withWater(defaultBlockState().setValue(LAYERS, layers), ctx);
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
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(LAYERS);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
