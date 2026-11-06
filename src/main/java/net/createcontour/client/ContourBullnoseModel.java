package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourBullnoseBlock;
import net.createcontour.block.ContourTrimBlock.Edge;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Convex r6 quarter-round on a cube edge: strips on the two rounded
 * faces (kept part), full far faces, L-rect + quarter-disk end caps,
 * and the convex arc surface tangent to both faces (interior,
 * double-sided).
 */
public class ContourBullnoseModel extends ContourModel {

	private static final float R = 6f;

	public ContourBullnoseModel(BakedModel original, @Nullable BlockState boundState,
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

	// face-local u/v -> world axis for a face on axis fa
	private static int uAxis(int fa) {
		return fa == 0 ? 2 : 0;
	}

	private static int vAxis(int fa) {
		return fa == 0 ? 1 : (fa == 1 ? 2 : 1);
	}

	/** Emit a rect on `face` spanning axis p [p0,p1] x axis q [q0,q1]. */
	private static void rect(List<BakedQuad> out,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor,
		Direction face, int p, float p0, float p1, int q, float q0,
		float q1, float w) {
		int fa = face.getAxis().ordinal();
		float u0, u1, v0, v1;
		if (uAxis(fa) == p) {
			u0 = p0; u1 = p1; v0 = q0; v1 = q1;
		} else {
			u0 = q0; u1 = q1; v0 = p0; v1 = p1;
		}
		for (ShapeQuads.SrcQuad src : srcFor.apply(face))
			out.add(ShapeQuads.emitFace(face,
				ShapeQuads.faceCorners(face, u0, u1, v0, v1, w), src));
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Edge e = state.getValue(ContourBullnoseBlock.EDGE);
		Direction A = e.a, B = e.b;
		boolean aPos = A.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean bPos = B.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int aAx = A.getAxis().ordinal(), bAx = B.getAxis().ordinal();
		int r = e.runAxis().ordinal();
		List<BakedQuad> out = new ArrayList<>();

		// kept spans in block coords: du/dv inward distance >= 6
		float[] aKeep = aPos ? new float[]{0, 10} : new float[]{6, 16};
		float[] bKeep = bPos ? new float[]{0, 10} : new float[]{6, 16};
		// tangent-adjacent arms for end caps
		float[] aCut = aPos ? new float[]{10, 16} : new float[]{0, 6};
		float[] bCut = bPos ? new float[]{10, 16} : new float[]{0, 6};

		if (side == A) // strip on face a: dv >= 6, run full
			rect(out, srcFor, A, bAx, bKeep[0], bKeep[1], r, 0, 16,
				aPos ? 16 : 0);
		if (side == B)
			rect(out, srcFor, B, aAx, aKeep[0], aKeep[1], r, 0, 16,
				bPos ? 16 : 0);
		if (side == A.getOpposite() || side == B.getOpposite()) {
			Direction f = side;
			for (ShapeQuads.SrcQuad src : srcFor.apply(f))
				out.add(ShapeQuads.emitFace(f,
					ShapeQuads.faceCorners(f, 0, 16, 0, 16,
						f.getAxisDirection() == Direction.AxisDirection.POSITIVE
							? 16 : 0),
					src));
		}
		// end caps on both run ends
		for (Direction end : new Direction[]{
			Direction.fromAxisAndDirection(Direction.Axis.values()[r],
				Direction.AxisDirection.NEGATIVE),
			Direction.fromAxisAndDirection(Direction.Axis.values()[r],
				Direction.AxisDirection.POSITIVE)}) {
			if (side != end)
				continue;
			float w = end.getAxisDirection() == Direction.AxisDirection.POSITIVE
				? 16 : 0;
			// L: a-keep strip full-b + b-keep strip on a-cut
			rect(out, srcFor, end, aAx, aKeep[0], aKeep[1], bAx, 0, 16, w);
			rect(out, srcFor, end, aAx, aCut[0], aCut[1], bAx, bKeep[0],
				bKeep[1], w);
			// quarter-disk fan in the corner square
			float[] cc = {8f, 8f, 8f};
			cc[aAx] = aPos ? 10 : 6;
			cc[bAx] = bPos ? 10 : 6;
			cc[r] = w;
			int au = (r + 1) % 3, av = (r + 2) % 3;
			float cornerAu = (au == aAx ? (aPos ? 16 : 0)
				: (bPos ? 16 : 0)) - cc[au];
			float cornerAv = (av == aAx ? (aPos ? 16 : 0)
				: (bPos ? 16 : 0)) - cc[av];
			double aC = Math.atan2(cornerAv, cornerAu);
			ShapeQuads.emitDiskFan(out, srcFor, end, cc, R,
				aC - Math.PI / 4, aC + Math.PI / 4, 6);
		}
		// convex arc surface tangent to both faces (interior)
		if (side == null) {
			float[] c = {8f, 8f, 8f}, eU = new float[3],
				eV = new float[3], eD = new float[3];
			c[aAx] = aPos ? 10 : 6;
			c[bAx] = bPos ? 10 : 6;
			eU[aAx] = aPos ? -1f : 1f; // inward
			eV[bAx] = bPos ? -1f : 1f;
			eD[r] = 1f;
			ShapeQuads.emitArc(out, srcFor, c, eU, eV, eD, R,
				Math.PI, Math.PI * 1.5, 4, -8f, 8f);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_BULLNOSE.get().defaultBlockState();
	}
}
