package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourQuarterRoundBlock;
import net.createcontour.block.ContourTrimBlock.Edge;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Quarter-cylinder moulding: two full leg faces (the faces meeting at
 * the edge), an 8-facet interior arc of radius 16px emitted
 * double-sided, and quarter-disk fans capping the two run-axis faces.
 */
public class ContourQuarterRoundModel extends ContourModel {

	private static final int SEGMENTS = 8;

	public ContourQuarterRoundModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial) {
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

	private static Vector3f pt(float[] c) {
		return new Vector3f(c[0], c[1], c[2]);
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Edge e = state.getValue(ContourQuarterRoundBlock.EDGE);
		List<BakedQuad> out = new ArrayList<>();

		// corner C at the edge; inward unit vectors along each leg axis
		float[] c0 = {0, 0, 0};
		c0[e.a.getAxis().ordinal()] = e.a.getAxisDirection()
			== Direction.AxisDirection.POSITIVE ? 16f : 0f;
		c0[e.b.getAxis().ordinal()] = e.b.getAxisDirection()
			== Direction.AxisDirection.POSITIVE ? 16f : 0f;
		int run = e.runAxis().ordinal();
		int axA = e.a.getAxis().ordinal(), axB = e.b.getAxis().ordinal();
		float sa = e.a.getAxisDirection()
			== Direction.AxisDirection.POSITIVE ? -1f : 1f; // inward
		float sb = e.b.getAxisDirection()
			== Direction.AxisDirection.POSITIVE ? -1f : 1f;

		// arc sample i: C + 16*(cos(t)*ib + sin(t)*ia)
		float[][] arc = new float[SEGMENTS + 1][3];
		for (int i = 0; i <= SEGMENTS; i++) {
			double t = Math.PI * 0.5 * i / SEGMENTS;
			arc[i][axA] = c0[axA] + 16f * (float) Math.sin(t) * sa;
			arc[i][axB] = c0[axB] + 16f * (float) Math.cos(t) * sb;
		}

		// full leg faces on the two faces meeting at the edge
		for (Direction leg : new Direction[]{e.a, e.b}) {
			if (side != leg)
				continue;
			float plane = leg.getAxisDirection()
				== Direction.AxisDirection.POSITIVE ? 16f : 0f;
			int li = leg.getAxis().ordinal();
			int u = (li + 1) % 3, w = (li + 2) % 3;
			Vector3f[] c = {
				new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
				new Vector3f(0, 0, 0), new Vector3f(0, 0, 0)};
			float[][] cc = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
			for (int k = 0; k < 4; k++) {
				cc[k][li] = plane;
				cc[k][u] = (k == 1 || k == 2) ? 16f : 0f;
				cc[k][w] = (k >= 2) ? 16f : 0f;
				c[k] = pt(cc[k]);
			}
			for (ShapeQuads.SrcQuad src : srcFor.apply(leg))
				out.add(ShapeQuads.emitFace(leg, c, src));
		}

		// cap fans on the two run-axis faces
		for (int cap = 0; cap < 2; cap++) {
			float s0 = cap == 0 ? 0f : 16f;
			Direction capDir = Direction.fromAxisAndDirection(e.runAxis(),
				cap == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side != capDir)
				continue;
			float[] cc = c0.clone();
			cc[run] = s0;
			for (int i = 0; i < SEGMENTS; i++) {
				float[] p1 = arc[i].clone(), p2 = arc[i + 1].clone();
				p1[run] = s0;
				p2[run] = s0;
				Vector3f[] tri = ShapeQuads.tri(pt(cc), pt(p1), pt(p2));
				for (ShapeQuads.SrcQuad src : srcFor.apply(capDir))
					out.add(ShapeQuads.emitFace(capDir, tri, src));
			}
		}

		// interior arc facets, double-sided like the slope ramp
		if (side == null) {
			for (int i = 0; i < SEGMENTS; i++) {
				float[] p1 = arc[i].clone(), p2 = arc[i].clone();
				float[] p3 = arc[i + 1].clone(), p4 = arc[i + 1].clone();
				p1[run] = 0; p2[run] = 16; p3[run] = 16; p4[run] = 0;
				Vector3f[] facet = {pt(p1), pt(p2), pt(p3), pt(p4)};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
					out.add(ShapeQuads.emitFace(Direction.UP, facet, src));
					out.add(ShapeQuads.emitFace(Direction.DOWN,
						new Vector3f[]{facet[3], facet[2], facet[1], facet[0]}, src));
				}
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_QUARTER_ROUND.get().defaultBlockState();
	}
}
