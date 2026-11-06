package net.createcontour.client;

import java.util.List;
import net.createcontour.block.ContourConnectiveBlock;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Connective shapes (wall/pane/fence): the block's own box list is the
 * single geometry source for both collision and render.
 */
public class ContourConnectiveModel extends ContourModel {

	private final ContourConnectiveBlock block;

	public ContourConnectiveModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial, ContourConnectiveBlock block) {
		super(original, boundState, boundMaterial);
		this.block = block;
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return block.boxesFor(state);
	}

	@Override
	protected BlockState neutralState() {
		return block.defaultBlockState();
	}
}
