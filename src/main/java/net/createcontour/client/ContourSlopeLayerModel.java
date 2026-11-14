package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourSlopeLayerBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Issue #21 - stacked slope slices. SIZE n renders the bottom n eighths of
 * the 45-degree wedge: a plateau band (0..2n high on the FACING wall side)
 * plus a 45-degree ramp tail from the plateau edge down to the toe, i.e.
 * the same silhouette contract as {@link ContourSlopeBlock} clipped at
 * y <= 2*SIZE. The boundary faces (floor, wall band, trapezoid side caps,
 * 2px toe lip) join their cull lists; the plateau top and the diagonal
 * ramp are interior (the ramp is double-sided, matching contour_slope).
 * At SIZE=8 the emitted quads are exactly contour_slope's BOTTOM set
 * (documented equivalence - the block id stays distinct).
 */
public class ContourSlopeLayerModel extends ContourModel {

	public ContourSlopeLayerModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of(); // every face is emitted by extraQuads
	}

	private static Vector3f v(int a1, int a3, float c1, float y, float c3) {
		float[] f = new float[3];
		f[a1] = c1;
		f[1] = y;
		f[a3] = c3;
		return new Vector3f(f[0], f[1], f[2]);
	}

	private static Direction.Axis axisOf(int i) {
		return i == 0 ? Direction.Axis.X : i == 1 ? Direction.Axis.Y : Direction.Axis.Z;
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Direction f = state.getValue(ContourSlopeLayerBlock.FACING);
		float h = state.getValue(ContourSlopeLayerBlock.SIZE) * 2f;
		int a1 = f.getAxis() == Direction.Axis.X ? 0 : 2; // run axis
		int a3 = a1 == 0 ? 2 : 0;                        // span axis
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float w = pos ? 16f : 0f;   // a1 coordinate of the FACING wall
		float t = pos ? 0f : 16f;   // a1 coordinate of the toe (low edge)
		float bend = pos ? h : 16f - h; // a1 where the ramp meets the plateau
		List<BakedQuad> out = new ArrayList<>();

		// floor: full bottom boundary
		if (side == Direction.DOWN) {
			Vector3f[] c = ShapeQuads.faceCorners(Direction.DOWN, 0, 16, 0, 16, 0);
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.DOWN))
				out.add(ShapeQuads.emitFace(Direction.DOWN, c, src));
		}
		// wall band on the FACING side: h tall (full face at SIZE 8)
		if (side == f) {
			Vector3f[] c = ShapeQuads.faceCorners(f, 0, 16, 0, h, w);
			for (ShapeQuads.SrcQuad src : srcFor.apply(f))
				out.add(ShapeQuads.emitFace(f, c, src));
		}
		// 2px toe lip: the stepped collision keeps a last column 2px high
		// at the low boundary, so the render emits the matching band
		// (same rule as contour_slope's collision lip).
		Direction lip = f.getOpposite();
		if (side == lip) {
			float plane = lip.getAxisDirection() == Direction.AxisDirection.POSITIVE
				? 16f : 0f;
			Vector3f[] c = ShapeQuads.faceCorners(lip, 0, 16, 0, 2f, plane);
			for (ShapeQuads.SrcQuad src : srcFor.apply(lip))
				out.add(ShapeQuads.emitFace(lip, c, src));
		}
		// trapezoid caps on the two span faces: bottom run, ramp edge,
		// plateau top edge, wall edge; degenerates to the wedge triangle
		// at SIZE=8
		for (int cap = 0; cap < 2; cap++) {
			float s0 = cap == 0 ? 0f : 16f;
			Direction capDir = Direction.fromAxisAndDirection(axisOf(a3),
				cap == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side != capDir)
				continue;
			Vector3f p1 = v(a1, a3, w, 0, s0);   // wall bottom
			Vector3f p2 = v(a1, a3, t, 0, s0);   // toe bottom
			Vector3f p3 = v(a1, a3, bend, h, s0); // ramp/plateau joint
			Vector3f p4 = v(a1, a3, w, h, s0);   // wall top
			Vector3f[] q = h >= 16f
				? ShapeQuads.tri(p1, p2, p4)
				: new Vector3f[]{p1, p2, p3, p4};
			for (ShapeQuads.SrcQuad src : srcFor.apply(capDir))
				out.add(ShapeQuads.emitFace(capDir, q, src));
		}
		// interior surfaces: plateau top (UP) + the 45-degree ramp tail,
		// double-sided like contour_slope's ramp
		if (side == null) {
			if (h < 16f) {
				float lo = Math.min(w, bend), hi = Math.max(w, bend);
				Vector3f[] c = a1 == 0
					? ShapeQuads.faceCorners(Direction.UP, lo, hi, 0, 16, h)
					: ShapeQuads.faceCorners(Direction.UP, 0, 16, lo, hi, h);
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					out.add(ShapeQuads.emitFace(Direction.UP, c, src));
			}
			Vector3f[] ramp = {v(a1, a3, bend, h, 0), v(a1, a3, bend, h, 16),
				v(a1, a3, t, 0, 16), v(a1, a3, t, 0, 0)};
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				out.add(ShapeQuads.emitFace(Direction.UP, ramp, src));
				out.add(ShapeQuads.emitFace(Direction.DOWN,
					new Vector3f[]{ramp[3], ramp[2], ramp[1], ramp[0]}, src));
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_SLOPE_LAYER.get().defaultBlockState();
	}
}
