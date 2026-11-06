package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourGothicArchBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Pointed arch: full-height side walls plus a pointed opening whose
 * reveals are two circle arcs (r8.9, centers near the opposite
 * spring line at y=8, meeting at apex 8,16). Boundary faces join
 * their cull lists; the gable silhouette above each arc is emitted
 * as a stepped strip of planar quads on the depth face.
 */
public class ContourGothicArchModel extends ContourModel {

	// left arc center u=11.9, right u=4.1, r=8.9, spring y=8
	private static final double R = 8.9;
	private static final double C_L = 11.9, C_R = 4.1;
	private static final float[] US = {3f, 4.25f, 5.5f, 6.75f, 8f,
		9.25f, 10.5f, 11.75f, 13f};

	public ContourGothicArchModel(BakedModel original,
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

	private static float arcY(double u) {
		double c = u <= 8 ? C_L : C_R;
		return 8f + (float) Math.sqrt(Math.max(0, R * R - (u - c) * (u - c)));
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		boolean x = state.getValue(ContourGothicArchBlock.AXIS)
			== Direction.Axis.X;
		int s = x ? 0 : 2;  // span axis
		int d = x ? 2 : 0;  // depth axis
		List<BakedQuad> out = new ArrayList<>();

		// span boundary faces: solid wall edges, full height
		for (int side_i = 0; side_i < 2; side_i++) {
			Direction outer = dirFor(s, side_i == 1);
			if (side == outer)
				for (ShapeQuads.SrcQuad src : srcFor.apply(outer))
					out.add(ShapeQuads.emitFace(outer,
						ShapeQuads.faceCorners(outer, 0, 16, 0, 16,
							side_i == 0 ? 0f : 16f), src));
			// inner wall faces (interior, buried parts harmless)
			if (side == null) {
				float w = side_i == 0 ? 3f : 13f;
				Vector3f[] q = {vv(s, d, w, 0, 0), vv(s, d, w, 0, 16),
					vv(s, d, w, 16, 16), vv(s, d, w, 16, 0)};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					ShapeQuads.emitDouble(out, src, q);
			}
		}
		// top and bottom strips where the walls meet the faces
		for (Direction cap : new Direction[]{Direction.DOWN, Direction.UP}) {
			if (side != cap)
				continue;
			float y = cap == Direction.DOWN ? 0f : 16f;
			for (int w = 0; w < 2; w++)
				for (ShapeQuads.SrcQuad src : srcFor.apply(cap)) {
					float a0 = w == 0 ? 0f : 13f, a1 = w == 0 ? 3f : 16f;
					out.add(ShapeQuads.emitFace(cap,
						ShapeQuads.faceCorners(cap,
							s == 0 ? a0 : 0, s == 0 ? a1 : 16,
							s == 0 ? 0 : a0, s == 0 ? 16 : a1, y), src));
				}
		}
		// depth gable faces: wall rects + arc-top strips
		for (int end = 0; end < 2; end++) {
			float w = end == 0 ? 0f : 16f;
			Direction df = dirFor(d, end == 1);
			if (side != df)
				continue;
			for (ShapeQuads.SrcQuad src : srcFor.apply(df)) {
				for (int i = 0; i < 2; i++) {
					float a0 = i == 0 ? 0f : 13f, a1 = i == 0 ? 3f : 16f;
					out.add(ShapeQuads.emitFace(df,
						ShapeQuads.faceCorners(df, a0, a1, 0, 16, w), src));
				}
				// strips between each arc and the top edge; where an arc
				// sample meets y=16 the strip collapses to a triangle
				for (int i = 0; i < US.length - 1; i++) {
					float u0 = US[i], u1 = US[i + 1];
					float y0 = arcY(u0), y1 = arcY(u1);
					boolean a0top = y0 >= 16f - 1e-3, a1top = y1 >= 16f - 1e-3;
					if (a0top && a1top)
						continue;
					if (a1top)
						out.add(ShapeQuads.emitFace(df, ShapeQuads.tri(
							vv(s, d, u0, y0, w), vv(s, d, u1, 16, w),
							vv(s, d, u0, 16, w)), src));
					else if (a0top)
						out.add(ShapeQuads.emitFace(df, ShapeQuads.tri(
							vv(s, d, u0, 16, w), vv(s, d, u1, y1, w),
							vv(s, d, u1, 16, w)), src));
					else
						out.add(ShapeQuads.emitFace(df, new Vector3f[]{
							vv(s, d, u0, y0, w), vv(s, d, u1, y1, w),
							vv(s, d, u1, 16, w), vv(s, d, u0, 16, w)}, src));
				}
			}
		}
		// reveals: left arc springs at (3,8) center 11.9 sweep pi->2.025;
		// right arc center 4.1 sweep 0->1.117
		if (side == null) {
			float[] eU = new float[3], eV = {0, 1, 0}, eD = new float[3];
			eU[s] = 1f;
			eD[d] = 1f;
			float[] cL = {0, 8f, 0}, cR = {0, 8f, 0};
			cL[s] = (float) C_L;
			cR[s] = (float) C_R;
			ShapeQuads.emitArc(out, srcFor, cL, eU, eV, eD, (float) R,
				2.0246, Math.PI, 7, 0f, 16f);
			ShapeQuads.emitArc(out, srcFor, cR, eU, eV, eD, (float) R,
				0, 1.117, 7, 0f, 16f);
		}
		return out;
	}

	private static Direction dirFor(int axis, boolean pos) {
		return Direction.fromAxisAndDirection(
			axis == 0 ? Direction.Axis.X : Direction.Axis.Z,
			pos ? Direction.AxisDirection.POSITIVE
				: Direction.AxisDirection.NEGATIVE);
	}

	private static Vector3f vv(int s, int d, float cs, float y, float cd) {
		float[] f = new float[3];
		f[s] = cs;
		f[1] = y;
		f[d] = cd;
		return new Vector3f(f[0], f[1], f[2]);
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_GOTHIC_ARCH.get().defaultBlockState();
	}
}
