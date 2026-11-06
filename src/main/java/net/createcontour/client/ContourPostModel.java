package net.createcontour.client;

import java.util.List;
import net.createcontour.block.ContourPostBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Post: centered 8px column on the bound axis. */
public class ContourPostModel extends ContourModel {

	public ContourPostModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of(switch (state.getValue(ContourPostBlock.AXIS)) {
			case X -> new float[]{0, 4, 4, 16, 12, 12};
			case Z -> new float[]{4, 4, 0, 12, 12, 16};
			default -> new float[]{4, 0, 4, 12, 16, 12};
		});
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_POST.get().defaultBlockState()
			.setValue(ContourPostBlock.AXIS, Direction.Axis.Y);
	}
}
