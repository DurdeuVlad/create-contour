package net.createcontour.client;

import java.util.List;
import java.util.function.Supplier;
import net.createcontour.block.ContourHalfLayerBlock;
import net.createcontour.block.ContourLayersBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Layers: a single box scaled by the LAYERS property (pxPerLayer per layer). */
public class ContourLayersModel extends ContourModel {

	private final float pxPerLayer;
	private final Supplier<Block> host;
	private final boolean halfStep;

	public ContourLayersModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		this(original, boundState, boundMaterial, 2f,
			() -> ContourRegistries.CONTOUR_LAYERS.get(), false);
	}

	public ContourLayersModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial, float pxPerLayer,
		Supplier<Block> host, boolean halfStep) {
		super(original, boundState, boundMaterial);
		this.pxPerLayer = pxPerLayer;
		this.host = host;
		this.halfStep = halfStep;
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0; // boxesFor owns geometry
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		int layers = state.getValue(halfStep
			? ContourHalfLayerBlock.LAYERS : ContourLayersBlock.LAYERS);
		float h = layers * pxPerLayer;
		return List.of(new float[]{0, 0, 0, 16, h, 16});
	}

	@Override
	protected BlockState neutralState() {
		return host.get().defaultBlockState();
	}
}
