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

	public record ByteExtra(int octant, BlockState material) {
	}

	public static final net.neoforged.neoforge.client.model.data.ModelProperty<
		java.util.List<ByteExtra>> BYTE_EXTRAS =
			new net.neoforged.neoforge.client.model.data.ModelProperty<>();

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		ContourByteBlock.Octant o = state.getValue(ContourByteBlock.OCTANT);
		float x0 = o.x * 8f, y0 = o.y * 8f, z0 = o.z * 8f;
		return List.of(new float[]{x0, y0, z0, x0 + 8, y0 + 8, z0 + 8});
	}

	@Override
	public List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(
		@Nullable BlockState state, @Nullable net.minecraft.core.Direction side,
		net.minecraft.util.RandomSource rand,
		net.neoforged.neoforge.client.model.data.ModelData data,
		@Nullable net.minecraft.client.renderer.RenderType renderType) {
		List<net.minecraft.client.renderer.block.model.BakedQuad> quads =
			new java.util.ArrayList<>(super.getQuads(state, side, rand, data,
				renderType));
		java.util.List<ByteExtra> extras = data == null ? null
			: data.get(BYTE_EXTRAS);
		if (extras == null || extras.isEmpty())
			return quads;
		for (ByteExtra e : extras) {
			ContourByteBlock.Octant o =
				ContourByteBlock.Octant.values()[e.octant()];
			float x0 = o.x * 8f, y0 = o.y * 8f, z0 = o.z * 8f;
			quads.addAll(ShapeQuads.emitBoxes(
				List.of(new float[]{x0, y0, z0, x0 + 8, y0 + 8, z0 + 8}),
				side,
				d -> sourcesFor(e.material(), d, rand,
					net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
					renderType)));
		}
		return quads;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_BYTE.get().defaultBlockState();
	}
}
