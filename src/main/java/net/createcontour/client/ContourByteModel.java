package net.createcontour.client;

import java.util.List;
import net.createcontour.block.ContourByteBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Byte: the 8px octant cube at the bound position. */
public class ContourByteModel extends ContourModel {

	public ContourByteModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		ContourByteBlock.Octant o = state.getValue(ContourByteBlock.OCTANT);
		float x0 = o.x * 8f, y0 = o.y * 8f, z0 = o.z * 8f;
		return List.of(new float[]{x0, y0, z0, x0 + 8, y0 + 8, z0 + 8});
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_BYTE.get().defaultBlockState();
	}
}
