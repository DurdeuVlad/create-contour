package net.createcontour.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Half-height 45-degree wedge (8px rise): gentle ramps, driveways,
 * low-pitch roof edges. Same FACING/HALF contract as the full slope;
 * collision reuses the stepped 4-slice approximation at half height.
 */
public class ContourHalfSlopeBlock extends ContourSlopeBlock {

	private static final VoxelShape[] SHAPES = new VoxelShape[8];

	static {
		for (Direction f : Direction.Plane.HORIZONTAL)
			for (Half h : Half.values())
				SHAPES[f.get2DDataValue() * 2 + (h == Half.TOP ? 1 : 0)] =
					build(f, h, 8f);
	}

	public ContourHalfSlopeBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[state.getValue(FACING).get2DDataValue() * 2
			+ (state.getValue(HALF) == Half.TOP ? 1 : 0)];
	}
}
