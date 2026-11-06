package net.createcontour.client;

import net.createcontour.block.ContourStairsBlock;
import net.createcontour.block.ShapeGrid;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Stairs occupancy: shared ShapeGrid.cellMask (same grid as collision). */
public class ContourStairsModel extends ContourModel {

	public ContourStairsModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return ShapeGrid.cellMask(state.getValue(ContourStairsBlock.HALF),
			state.getValue(ContourStairsBlock.FACING),
			state.getValue(ContourStairsBlock.STAIRS_SHAPE));
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_STAIRS.get().defaultBlockState();
	}
}
