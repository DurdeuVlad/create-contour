package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourSmokestackBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Smokestack: the flared funnel - skirt, barrel, crown flare and
 * rim lathed along the run axis, open at the top: a r4.5 inner
 * bore drops to a cavity floor and the rim is a flat annulus
 * between bore and outer radius. Base disk on the run-negative
 * boundary face.
 */
public class ContourSmokestackModel extends ContourModel {

	private static final int SEG = 14;

	// (d, r) stations of the outer profile along the run axis
	private static final float[] YS = {0f, 1f, 2f, 3f, 8f, 9f, 11f,
		13f, 14f, 15.5f};
	private static final float[] RS = {5.5f, 5.5f, 4.5f, 4f, 4f, 4.3f,
		5.2f, 6.5f, 7f, 7f};

	public ContourSmokestackModel(BakedModel original,
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
		int run = state.getValue(ContourSmokestackBlock.AXIS).ordinal();
		int au = (run + 1) % 3, av = (run + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();

		Direction foot = Direction.fromAxisAndDirection(
			Direction.Axis.values()[run], Direction.AxisDirection.NEGATIVE);
		if (side == foot) {
			float[] c = {8f, 8f, 8f};
			c[run] = 0f;
			ShapeQuads.emitDiskFan(out, srcFor, foot, c, 5.5f, 0,
				Math.PI * 2, SEG);
		}
		if (side == null) {
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[au] = 1f;
			eV[av] = 1f;
			eD[run] = 1f;
			float[] c = {8f, 8f, 8f};
			c[run] = 0f;
			ShapeQuads.emitLathe(out, srcFor, c, eU, eV, eD, YS, RS, SEG);

			// open top: r4.5 bore down to the cavity floor at d9,
			// floor disk, and the r4.5 -> r7 rim annulus at d15.5
			java.util.function.BiFunction<Float, Double, Vector3f> pt =
				(nn, t) -> {
					float[] q = {8f, 8f, 8f};
					q[run] = nn;
					q[au] = (float) (8 + 4.5 * Math.cos(t));
					q[av] = (float) (8 + 4.5 * Math.sin(t));
					return new Vector3f(q[0], q[1], q[2]);
				};
			java.util.function.BiFunction<Float, Double, Vector3f> pt7 =
				(nn, t) -> {
					float[] q = {8f, 8f, 8f};
					q[run] = nn;
					q[au] = (float) (8 + 7 * Math.cos(t));
					q[av] = (float) (8 + 7 * Math.sin(t));
					return new Vector3f(q[0], q[1], q[2]);
				};
			float[] mid = {8f, 8f, 8f};
			mid[run] = 9f;
			Vector3f center = new Vector3f(mid[0], mid[1], mid[2]);
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
				for (int i = 0; i < SEG; i++) {
					double ta = Math.PI * 2 * i / SEG,
						tb = Math.PI * 2 * (i + 1) / SEG;
					// inner bore cylinder r4.5: floor -> rim
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(9f, ta), pt.apply(9f, tb),
						pt.apply(15.5f, tb), pt.apply(15.5f, ta)});
					// cavity floor disk at d9 (sliver tri)
					ShapeQuads.emitDouble(out, src, ShapeQuads.tri(
						center, pt.apply(9f, ta), pt.apply(9f, tb)));
					// rim annulus r4.5 -> r7 at d15.5
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(15.5f, ta), pt.apply(15.5f, tb),
						pt7.apply(15.5f, tb), pt7.apply(15.5f, ta)});
				}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_SMOKESTACK.get().defaultBlockState();
	}
}
