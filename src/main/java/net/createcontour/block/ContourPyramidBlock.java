package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pyramid cap: four sloped faces meeting at a center apex, reaching
 * full height. Height field is 2*min(dist to each edge). Post caps,
 * turret tops, roof finials. HALF TOP inverts it (hanging spike).
 * Render emits true triangles; collision is a documented
 * quarter-stepped approximation (quarter-max sampling).
 */
public class ContourPyramidBlock extends ContourBlock {

	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	private static final VoxelShape[] SHAPES = new VoxelShape[2];

	static {
		for (Half h : Half.values())
			SHAPES[h.ordinal()] = build(h);
	}

	/** Height at (x,z): rises 2px per pixel inward, apex at center. */
	private static float heightAt(float x, float z) {
		return Math.min(Math.min(x, 16 - x), Math.min(z, 16 - z)) * 2f;
	}

	private static VoxelShape build(Half h) {
		VoxelShape s = Shapes.empty();
		for (int qx = 0; qx < 2; qx++)
			for (int qz = 0; qz < 2; qz++) {
				float x0 = qx * 8f, z0 = qz * 8f;
				// sample at the quarter's center-most corner (quarter-max)
				float y = heightAt(x0 + 8f, z0 + 8f);
				s = Shapes.or(s, h == Half.BOTTOM
					? Block.box(x0, 0, z0, x0 + 8, y, z0 + 8)
					: Block.box(x0, 16 - y, z0, x0 + 8, 16, z0 + 8));
			}
		return s.optimize();
	}

	public ContourPyramidBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(HALF));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[state.getValue(HALF).ordinal()];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Half half = ctx.getClickedFace() == Direction.DOWN
			|| (ctx.getClickedFace().getAxis().isHorizontal()
				&& ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5)
			? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState().setValue(HALF, half), ctx);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(HALF);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
