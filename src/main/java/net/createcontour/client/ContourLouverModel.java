package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourLouverBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Louver vent: the mount plate goes through emitBoxes (boundary
 * faces join cull lists); each slat is a tilted parallelogram
 * spanning the width axis between the outward edge (lower) and a
 * 3px-deeper edge (higher), emitted double-sided.
 */
public class ContourLouverModel extends ContourModel {

	public ContourLouverModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of();
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Direction f = state.getValue(ContourLouverBlock.FACING);
		int fi = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int p = fi == 1 ? 2 : 1;
		int w = 3 - fi - p;
		List<BakedQuad> out = new ArrayList<>();

		// mount plate: a lone box - emitBoxes handles cull/interior
		float[] plate = {0, 0, 0, 16, 16, 16};
		plate[fi] = pos ? 0f : 14.5f;
		plate[fi + 3] = pos ? 1.5f : 16f;
		out.addAll(ShapeQuads.emitBoxes(List.of(plate), side, srcFor));

		if (side == null) {
			float outEdge = pos ? 16f : 0f;
			float deepEdge = pos ? 16f - ContourLouverBlock.SLAT_DEPTH
				: ContourLouverBlock.SLAT_DEPTH;
			for (int i = 0; i < ContourLouverBlock.SLATS; i++) {
				float lo = ContourLouverBlock.SLAT_LO
					+ i * ContourLouverBlock.SLAT_STEP;
				float hi = lo + ContourLouverBlock.SLAT_TALL;
				Vector3f[] q = {v(fi, w, p, outEdge, 0f, lo),
					v(fi, w, p, outEdge, 16f, lo),
					v(fi, w, p, deepEdge, 16f, hi),
					v(fi, w, p, deepEdge, 0f, hi)};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					ShapeQuads.emitDouble(out, src, q);
			}
		}
		return out;
	}

	private static Vector3f v(int fi, int wi, int pi, float f, float w, float p) {
		float[] c = new float[3];
		c[fi] = f;
		c[wi] = w;
		c[pi] = p;
		return new Vector3f(c[0], c[1], c[2]);
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_LOUVER.get().defaultBlockState();
	}
}
