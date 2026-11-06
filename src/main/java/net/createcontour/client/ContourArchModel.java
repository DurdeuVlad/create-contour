package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourArchBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Semicircular arch: 3px jambs up to y=8 plus a 3px arc band
 * (rOut=8, rIn=5, center at y=8) across the top. Boundary faces
 * (jamb faces, bottom strips, annular end faces) join their cull
 * lists; jamb inner faces and the curved band are interior.
 */
public class ContourArchModel extends ContourModel {

	public ContourArchModel(BakedModel original, @Nullable BlockState boundState,
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

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		boolean x = state.getValue(ContourArchBlock.AXIS) == Direction.Axis.X;
		int s = x ? 0 : 2;  // span axis
		int d = x ? 2 : 0;  // depth axis
		List<BakedQuad> out = new ArrayList<>();

		// eU = span (cos), eV = up (sin), eD = depth
		float[] eU = new float[3], eV = {0, 1, 0}, eD = new float[3];
		eU[s] = 1f;
		eD[d] = 1f;
		float[] c0 = {8f, 8f, 8f}, c1 = {8f, 8f, 8f};
		c0[d] = 0f;   // south/west plane center
		c1[d] = 16f;  // north/east plane center

		// jamb boundary faces: outer faces on the two span ends and
		// the bottom; end faces + arc sector on the two depth faces
		for (int jamb = 0; jamb < 2; jamb++) {
			float s0 = jamb == 0 ? 0f : 13f;
			float s1 = s0 + 3f;
			// outer span face
			Direction outer = dirFor(s, jamb == 1);
			if (side == outer)
				for (ShapeQuads.SrcQuad src : srcFor.apply(outer))
					out.add(ShapeQuads.emitFace(outer,
						ShapeQuads.faceCorners(outer, 0, 16, 0, 8,
							jamb == 0 ? 0f : 16f), src));
			// inner span face (interior, double-sided)
			if (side == null) {
				float w = jamb == 0 ? 3f : 13f;
				Vector3f[] q = {vv(s, d, w, 0, 0), vv(s, d, w, 0, 16),
					vv(s, d, w, 8, 16), vv(s, d, w, 8, 0)};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					ShapeQuads.emitDouble(out, src, q);
			}
			// bottom strips on the down face
			if (side == Direction.DOWN)
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.DOWN))
					out.add(ShapeQuads.emitFace(Direction.DOWN,
						ShapeQuads.faceCorners(Direction.DOWN,
							s == 0 ? s0 : 0, s == 0 ? s1 : 16,
							s == 0 ? 0 : s0, s == 0 ? 16 : s1, 0f), src));
			// depth faces: jamb rect + annular sector (in-plane U on
			// a depth face is always the span axis)
			for (int end = 0; end < 2; end++) {
				float w = end == 0 ? 0f : 16f;
				Direction df = dirFor(d, end == 1);
				if (side != df)
					continue;
				for (ShapeQuads.SrcQuad src : srcFor.apply(df))
					out.add(ShapeQuads.emitFace(df,
						ShapeQuads.faceCorners(df, s0, s1, 0, 8, w), src));
			}
		}
		// annular sectors on the two depth faces
		for (int end = 0; end < 2; end++) {
			Direction df = dirFor(d, end == 1);
			if (side == df)
				ShapeQuads.emitAnnularSector(out, srcFor, df,
					end == 0 ? c0 : c1, eU, eV, 5f, 8f,
					0, Math.PI, 8);
		}
		// curved band: outer+inner arc surfaces + radial spring caps
		// (d measured from the center, so -8..8 spans the block)
		if (side == null)
			ShapeQuads.emitArcBand(out, srcFor, new float[]{8f, 8f, 8f},
				eU, eV, eD, 8f, 5f, 0, Math.PI, 8, -8f, 8f);
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
		return ContourRegistries.CONTOUR_ARCH.get().defaultBlockState();
	}
}
