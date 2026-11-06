package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourElbowBlock;
import net.createcontour.block.ContourTrimBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Quarter-torus elbow: a 3-thick tube swept a quarter turn through
 * the bend plane (bend radius 5, tube radius 3), emitted double-
 * sided, plus full-disk mouths on the two faces the edge connects.
 * Mouths sit centered at 8px so they line up with contour_tube.
 */
public class ContourElbowModel extends ContourModel {

	public ContourElbowModel(BakedModel original,
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
		ContourTrimBlock.Edge e = state.getValue(ContourElbowBlock.EDGE);
		Direction a = e.a, b = e.b;
		int u = a.getAxis().ordinal(), v = b.getAxis().ordinal();
		int w = e.runAxis().ordinal();
		float sA = a.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? 1f : -1f;
		float sB = b.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? 1f : -1f;
		float cu = ContourElbowBlock.centerCoord(a);
		float cv = ContourElbowBlock.centerCoord(b);
		List<BakedQuad> out = new ArrayList<>();

		if (side == null) {
			// straight leg stubs: cylinder from each face to the tangent
			for (int leg = 0; leg < 2; leg++) {
				int ax = leg == 0 ? u : v;
				float s = leg == 0 ? sA : sB;
				float uE = s > 0 ? 16f : 0f;
				float cA = leg == 0 ? cu : cv;
				int[] perp = leg == 0 ? new int[]{v, w}
					: new int[]{u, w};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					for (int j = 0; j < 14; j++) {
						double pa = Math.PI * 2 * j / 14,
							pb = Math.PI * 2 * (j + 1) / 14;
						Vector3f[] q = new Vector3f[4];
						for (int k = 0; k < 4; k++) {
							double p = (k == 0 || k == 3) ? pa : pb;
							float ee = (k < 2) ? uE : cA;
							float[] cc = new float[3];
							cc[ax] = ee;
							cc[perp[0]] = (float) (8 + 3 * Math.cos(p));
							cc[perp[1]] = (float) (8 + 3 * Math.sin(p));
							q[k] = new Vector3f(cc[0], cc[1], cc[2]);
						}
						ShapeQuads.emitDouble(out, src, q);
					}
			}
			java.util.function.BiFunction<Double, Double, Vector3f> pt =
				(t, ph) -> {
					double ring = 5 + 3 * Math.cos(ph);
					float[] c = new float[3];
					c[u] = (float) (cu - ring * sA * Math.sin(t));
					c[v] = (float) (cv - ring * sB * Math.cos(t));
					c[w] = (float) (8 + 3 * Math.sin(ph));
					return new Vector3f(c[0], c[1], c[2]);
				};
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
				for (int i = 0; i < 6; i++)
					for (int j = 0; j < 14; j++) {
						double ta = Math.PI / 2 * i / 6,
							tb = Math.PI / 2 * (i + 1) / 6;
						double pa = Math.PI * 2 * j / 14,
							pb = Math.PI * 2 * (j + 1) / 14;
						ShapeQuads.emitDouble(out, src, new Vector3f[]{
							pt.apply(ta, pa), pt.apply(ta, pb),
							pt.apply(tb, pb), pt.apply(tb, pa)});
					}
		}
		// mouth disks on the two connected faces, centered at 8
		for (int leg = 0; leg < 2; leg++) {
			Direction f = leg == 0 ? a : b;
			if (side != f)
				continue;
			float[] c = {8f, 8f, 8f};
			c[leg == 0 ? u : v] = leg == 0
				? (sA > 0 ? 16f : 0f) : (sB > 0 ? 16f : 0f);
			ShapeQuads.emitDiskFan(out, srcFor, f, c, 3f, 0,
				Math.PI * 2, 10);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_ELBOW.get().defaultBlockState();
	}
}
