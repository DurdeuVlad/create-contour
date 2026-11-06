package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourOculusBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Oculus: the free-floating annular ring - annular faces on both
 * sides of the 3px band plus the outer r8 rim and inner r4 bore
 * cylinders, every surface interior and emitted double-sided.
 */
public class ContourOculusModel extends ContourModel {

	private static final int SEG = 14;

	public ContourOculusModel(BakedModel original,
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
		int n = state.getValue(ContourOculusBlock.AXIS).ordinal();
		int au = (n + 1) % 3, av = (n + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();
		if (side != null)
			return out;

		java.util.function.BiFunction<Double, float[], Vector3f> pt =
			(t, nr) -> {
				float[] c = {8f, 8f, 8f};
				c[n] = nr[0];
				c[au] = (float) (8 + nr[1] * Math.cos(t));
				c[av] = (float) (8 + nr[1] * Math.sin(t));
				return new Vector3f(c[0], c[1], c[2]);
			};
		for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
			for (int i = 0; i < SEG; i++) {
				double ta = Math.PI * 2 * i / SEG,
					tb = Math.PI * 2 * (i + 1) / SEG;
				// annular faces on both sides of the band
				for (float nn : new float[]{6.5f, 9.5f})
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(ta, new float[]{nn, 4f}),
						pt.apply(tb, new float[]{nn, 4f}),
						pt.apply(tb, new float[]{nn, 8f}),
						pt.apply(ta, new float[]{nn, 8f})});
				// outer rim and inner bore cylinders
				for (float r : new float[]{8f, 4f})
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(ta, new float[]{6.5f, r}),
						pt.apply(tb, new float[]{6.5f, r}),
						pt.apply(tb, new float[]{9.5f, r}),
						pt.apply(ta, new float[]{9.5f, r})});
			}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_OCULUS.get().defaultBlockState();
	}
}
