package net.createcontour.client;

import java.util.List;
import net.createcontour.block.ContourLayersBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Layers: a single box scaled by the LAYERS property (2px per layer). */
public class ContourLayersModel extends ContourModel {

	public ContourLayersModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0; // boxesFor owns geometry
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		float h = state.getValue(ContourLayersBlock.LAYERS) * 2f;
		return List.of(new float[]{0, 0, 0, 16, h, 16});
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_LAYERS.get().defaultBlockState();
	}
}
