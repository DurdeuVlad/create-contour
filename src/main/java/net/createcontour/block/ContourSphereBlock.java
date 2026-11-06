package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Radius-8 full sphere - finials, balls and ornaments. Orientation-
 * free: the ball is tangent to all six faces at single points, so
 * it never overlaps a neighbor. Collision is a 5-band stepped cover
 * (same convention as the dome: widest cross-section per band).
 */
public class ContourSphereBlock extends ContourBlock {

	public ContourSphereBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	/** Stepped r8 sphere: 5 y-bands, each at its widest cross-section. */
	public static List<float[]> boxesFor(BlockState state) {
		float[] y = {0f, 2f, 4f, 12f, 14f, 16f};
		float[] w = {5.29f, 6.93f, 8f, 6.93f, 5.29f};
		List<float[]> out = new java.util.ArrayList<>(5);
		for (int i = 0; i < 5; i++)
			out.add(new float[]{8 - w[i], y[i], 8 - w[i],
				8 + w[i], y[i + 1], 8 + w[i]});
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
		return withWater(defaultBlockState(), ctx);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
