package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.createcontour.block.ContourTrimBlock;
import net.createcontour.block.ContourTrimBlock.Edge;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Edge moulding with an arbitrary corner profile: the solid keeps
 * the cube corner under a curve sampled as (aInward, bInward) px
 * pairs from the edge corner, running from face A (first point,
 * aInward=0) to face B (last point, bInward=0). Emits leg strips
 * on the two faces meeting at the edge, a corner-fan cap on both
 * run ends, and the swept profile surface as interior
 * double-sided facets. Covers scotia (arc), ogee (sigmoid) and
 * any future profile-only moulding.
 */
public class ContourEdgeProfileModel extends ContourModel {

	private final float[][] prof;
	private final Supplier<Block> neutral;

	public ContourEdgeProfileModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial,
		float[][] prof, Supplier<Block> neutral) {
		super(original, boundState, boundMaterial);
		this.prof = prof;
		this.neutral = neutral;
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of(); // every face is emitted by extraQuads
	}

	private static Vector3f pt(float[] c) {
		return new Vector3f(c[0], c[1], c[2]);
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Edge e = state.getValue(ContourTrimBlock.EDGE);
		boolean aPos = e.a.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		boolean bPos = e.b.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int axA = e.a.getAxis().ordinal(), axB = e.b.getAxis().ordinal();
		int run = e.runAxis().ordinal();
		List<BakedQuad> out = new ArrayList<>();

		// corner point C at the edge; inward signs along each leg axis
		float[] c0 = {0, 0, 0};
		c0[axA] = aPos ? 16f : 0f;
		c0[axB] = bPos ? 16f : 0f;
		float sa = aPos ? -1f : 1f, sb = bPos ? -1f : 1f;

		// absolute profile samples (run coord patched per use)
		float[][] P = new float[prof.length][3];
		for (int i = 0; i < prof.length; i++) {
			P[i][axA] = c0[axA] + prof[i][0] * sa;
			P[i][axB] = c0[axB] + prof[i][1] * sb;
		}
		float bLeg = prof[0][1];                 // leg extent on face A
		float aLeg = prof[prof.length - 1][0];   // leg extent on face B

		// leg strips on the two faces meeting at the edge
		if (side == e.a)
			strip(out, srcFor, e.a, axB, c0[axB], c0[axB] + bLeg * sb,
				aPos ? 16f : 0f);
		if (side == e.b)
			strip(out, srcFor, e.b, axA, c0[axA], c0[axA] + aLeg * sa,
				bPos ? 16f : 0f);

		// corner-fan caps on the two run ends
		for (int cap = 0; cap < 2; cap++) {
			float s0 = cap == 0 ? 0f : 16f;
			Direction capDir = Direction.fromAxisAndDirection(e.runAxis(),
				cap == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side != capDir)
				continue;
			float[] cc = c0.clone();
			cc[run] = s0;
			for (int i = 0; i + 1 < prof.length; i++) {
				float[] p1 = P[i].clone(), p2 = P[i + 1].clone();
				p1[run] = s0;
				p2[run] = s0;
				Vector3f[] tri = ShapeQuads.tri(pt(cc), pt(p1), pt(p2));
				for (ShapeQuads.SrcQuad src : srcFor.apply(capDir))
					out.add(ShapeQuads.emitFace(capDir, tri, src));
			}
		}

		// swept profile surface, interior double-sided
		if (side == null)
			for (int i = 0; i + 1 < prof.length; i++) {
				float[] p1 = P[i].clone(), p2 = P[i].clone();
				float[] p3 = P[i + 1].clone(), p4 = P[i + 1].clone();
				p1[run] = 0; p2[run] = 16; p3[run] = 16; p4[run] = 0;
				Vector3f[] facet = {pt(p1), pt(p2), pt(p3), pt(p4)};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					ShapeQuads.emitDouble(out, src, facet);
			}
		return out;
	}

	/** Strip on `face` spanning `spanAxis` [s0,s1] x the third axis [0,16]. */
	private static void strip(List<BakedQuad> out,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor,
		Direction face, int spanAxis, float s0, float s1, float w) {
		int fa = face.getAxis().ordinal();
		int other = 3 - fa - spanAxis;
		float[][] cc = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
		for (int k = 0; k < 4; k++) {
			cc[k][fa] = w;
			cc[k][spanAxis] = (k == 1 || k == 2) ? s1 : s0;
			cc[k][other] = (k >= 2) ? 16f : 0f;
		}
		Vector3f[] q = {pt(cc[0]), pt(cc[1]), pt(cc[2]), pt(cc[3])};
		for (ShapeQuads.SrcQuad src : srcFor.apply(face))
			out.add(ShapeQuads.emitFace(face, q, src));
	}

	@Override
	protected BlockState neutralState() {
		return neutral.get().defaultBlockState();
	}
}
