package net.createcontour.client;

import net.createcontour.block.ContourSlabBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

/** Slab occupancy: full layer at y=0 (BOTTOM) or y=1 (TOP), all cells for DOUBLE. */
public class ContourSlabModel extends ContourModel {

	public ContourSlabModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return switch (state.getValue(ContourSlabBlock.TYPE)) {
			case DOUBLE -> 0b11111111;
			case TOP -> 0b11001100;
			default -> 0b00110011;
		};
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_SLAB.get().defaultBlockState()
			.setValue(ContourSlabBlock.TYPE, SlabType.BOTTOM);
	}
}
