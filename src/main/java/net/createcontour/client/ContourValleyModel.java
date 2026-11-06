package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourValleyBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Valley render: two 2px planes sheared down from the outer u
 * edges (y 14..16) to the center line (y 6..8). Sheets and the
 * u-edge lips are interior emitDouble quads; the trapezoid end
 * caps join the run-boundary cull lists. The two planes abut at
 * u=8 so no center cap is emitted.
 */
public class ContourValleyModel extends ContourModel {

	public ContourValleyModel(BakedModel original,
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
		int w = state.getValue(ContourValleyBlock.AXIS).ordinal();
		int u = w == 0 ? 2 : 0;
		Direction.Axis wAxis = Direction.Axis.values()[w];
		Direction.Axis uAxis = Direction.Axis.values()[u];
		List<BakedQuad> out = new ArrayList<>();

		for (int half = 0; half < 2; half++) {
			// outer edge at u=0 (left) or u=16 (right), center at u=8
			float uOut = half == 0 ? 0f : 16f;
			float uIn = 8f;
			float yOut = 14f, yIn = 6f; // bottom edge; top edge +2

			// run end caps (trapezoids on the w boundary faces)
			for (int end = 0; end < 2; end++) {
				Direction cap = Direction.fromAxisAndDirection(wAxis,
					end == 0 ? Direction.AxisDirection.NEGATIVE
						: Direction.AxisDirection.POSITIVE);
				if (side != cap)
					continue;
				float we = end == 0 ? 0f : 16f;
				for (ShapeQuads.SrcQuad src : srcFor.apply(cap))
					out.add(ShapeQuads.emitFace(cap, new Vector3f[]{
						v(u, w, uOut, yOut, we), v(u, w, uOut, yOut + 2f, we),
						v(u, w, uIn, yIn + 2f, we), v(u, w, uIn, yIn, we)}, src));
			}
			// outer u-edge lip on the u boundary face
			Direction lip = Direction.fromAxisAndDirection(uAxis,
				half == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side == lip) {
				for (ShapeQuads.SrcQuad src : srcFor.apply(lip))
					out.add(ShapeQuads.emitFace(lip, new Vector3f[]{
						v(u, w, uOut, yOut, 0f), v(u, w, uOut, yOut + 2f, 0f),
						v(u, w, uOut, yOut + 2f, 16f), v(u, w, uOut, yOut, 16f)},
						src));
			}
			if (side == null) {
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
					// top sheet
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						v(u, w, uOut, yOut + 2f, 0f), v(u, w, uIn, yIn + 2f, 0f),
						v(u, w, uIn, yIn + 2f, 16f), v(u, w, uOut, yOut + 2f, 16f)});
					// bottom sheet
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						v(u, w, uOut, yOut, 0f), v(u, w, uIn, yIn, 0f),
						v(u, w, uIn, yIn, 16f), v(u, w, uOut, yOut, 16f)});
				}
			}
		}
		return out;
	}

	private static Vector3f v(int u, int w, float uc, float y, float wc) {
		float[] c = new float[3];
		c[u] = uc;
		c[1] = y;
		c[w] = wc;
		return new Vector3f(c[0], c[1], c[2]);
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_VALLEY.get().defaultBlockState();
	}
}
