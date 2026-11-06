package net.createcontour.client;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Generic model for pure-AABB shapes whose block exposes a box list. */
public class ContourBoxModel extends ContourModel {

	private final Function<BlockState, List<float[]>> boxes;
	private final Supplier<? extends Block> block;

	public ContourBoxModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial, Function<BlockState, List<float[]>> boxes,
		Supplier<? extends Block> block) {
		super(original, boundState, boundMaterial);
		this.boxes = boxes;
		this.block = block;
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return boxes.apply(state);
	}

	@Override
	protected BlockState neutralState() {
		return block.get().defaultBlockState();
	}
}
