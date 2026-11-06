package net.createcontour.client;

import net.createcontour.block.ContourVerticalSlabBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Vertical slab occupancy: the half-column of cells on the FACING side. */
public class ContourVerticalSlabModel extends ContourModel {

	public ContourVerticalSlabModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		Direction f = state.getValue(ContourVerticalSlabBlock.FACING);
		boolean lo = f.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
		return switch (f.getAxis()) {
			case Z -> lo ? 0b00001111 : 0b11110000;   // cells z=0 / z=1
			default -> lo ? 0b01010101 : 0b10101010;  // cells x=0 / x=1
		};
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_VERTICAL_SLAB.get().defaultBlockState()
			.setValue(ContourVerticalSlabBlock.FACING, Direction.NORTH);
	}
}
