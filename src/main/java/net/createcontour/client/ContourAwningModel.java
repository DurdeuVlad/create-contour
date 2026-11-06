package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourAwningBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Awning: the sheared canopy sheet - top and bottom faces plus
 * the wall and outer end strips, all interior and emitted
 * double-sided; the two u-edge cross-section strips join the
 * side-boundary cull lists.
 */
public class ContourAwningModel extends ContourModel {

	public ContourAwningModel(BakedModel original,
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
		Direction f = state.getValue(ContourAwningBlock.FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = n == 0 ? 2 : 0;
		List<BakedQuad> out = new ArrayList<>();

		// plate cross-section in (n_dist, y): top yTop(n), bottom -1.2
		float yA = ContourAwningBlock.yTop(0f), yB = ContourAwningBlock.yTop(8f);
		java.util.function.BiFunction<float[], Float, Vector3f> pt =
			(du, nn) -> {
				float[] c = {8f, 8f, 8f};
				c[n] = pos ? 16f - nn : nn;
				c[1] = du[1];
				c[u] = du[0];
				return new Vector3f(c[0], c[1], c[2]);
			};

		// u-edge cross-section strips on the u boundary cull lists
		for (Direction.AxisDirection d : Direction.AxisDirection.values()) {
			Direction face = Direction.fromAxisAndDirection(
				u == 0 ? Direction.Axis.X : Direction.Axis.Z, d);
			if (side != face)
				continue;
			float uu = d == Direction.AxisDirection.POSITIVE ? 16f : 0f;
			for (ShapeQuads.SrcQuad src : srcFor.apply(face))
				out.add(ShapeQuads.emitFace(face, new Vector3f[]{
					pt.apply(new float[]{uu, yA}, 0f),
					pt.apply(new float[]{uu, yB}, 8f),
					pt.apply(new float[]{uu, yB - 1.2f}, 8f),
					pt.apply(new float[]{uu, yA - 1.2f}, 0f)}, src));
		}
		if (side == null) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				// top and bottom sheared faces
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					pt.apply(new float[]{0f, yA}, 0f),
					pt.apply(new float[]{16f, yA}, 0f),
					pt.apply(new float[]{16f, yB}, 8f),
					pt.apply(new float[]{0f, yB}, 8f)});
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					pt.apply(new float[]{0f, yA - 1.2f}, 0f),
					pt.apply(new float[]{16f, yA - 1.2f}, 0f),
					pt.apply(new float[]{16f, yB - 1.2f}, 8f),
					pt.apply(new float[]{0f, yB - 1.2f}, 8f)});
				// wall attach strip (n=0) and outer lip strip (n=8)
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					pt.apply(new float[]{0f, yA - 1.2f}, 0f),
					pt.apply(new float[]{16f, yA - 1.2f}, 0f),
					pt.apply(new float[]{16f, yA}, 0f),
					pt.apply(new float[]{0f, yA}, 0f)});
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					pt.apply(new float[]{0f, yB - 1.2f}, 8f),
					pt.apply(new float[]{16f, yB - 1.2f}, 8f),
					pt.apply(new float[]{16f, yB}, 8f),
					pt.apply(new float[]{0f, yB}, 8f)});
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_AWNING.get().defaultBlockState();
	}
}
