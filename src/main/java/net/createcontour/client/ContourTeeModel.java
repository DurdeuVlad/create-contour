package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourTeeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Pipe tee: the main r3 tube swept full length along the run axis,
 * plus the branch tube from its face to the block center, both
 * emitted double-sided, with full-disk mouths on all three faces.
 */
public class ContourTeeModel extends ContourModel {

	private static final int SEG = 14;

	public ContourTeeModel(BakedModel original,
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

	private static Vector3f tubePoint(int ax, int[] perp, float e,
		double phi) {
		float[] c = new float[3];
		c[ax] = e;
		c[perp[0]] = (float) (8 + 3 * Math.cos(phi));
		c[perp[1]] = (float) (8 + 3 * Math.sin(phi));
		return new Vector3f(c[0], c[1], c[2]);
	}

	private static void cylinder(List<BakedQuad> out, ShapeQuads.SrcQuad src,
		int ax, int[] perp, float e0, float e1) {
		for (int j = 0; j < SEG; j++) {
			double pa = Math.PI * 2 * j / SEG,
				pb = Math.PI * 2 * (j + 1) / SEG;
			ShapeQuads.emitDouble(out, src, new Vector3f[]{
				tubePoint(ax, perp, e0, pa),
				tubePoint(ax, perp, e0, pb),
				tubePoint(ax, perp, e1, pb),
				tubePoint(ax, perp, e1, pa)});
		}
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		ContourTeeBlock.Tee t = state.getValue(ContourTeeBlock.TEE);
		int u = t.run.ordinal(), v = t.branch.getAxis().ordinal();
		int w = 3 - u - v;
		boolean pos = t.branch.getAxisDirection()
			== Direction.AxisDirection.POSITIVE;
		List<BakedQuad> out = new ArrayList<>();

		if (side == null) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				cylinder(out, src, u, new int[]{v, w}, 0f, 16f);
				cylinder(out, src, v, new int[]{u, w},
					pos ? 8f : 0f, pos ? 16f : 8f);
			}
		}
		// mouths: both run ends + the branch face, centered at 8
		for (Direction f : new Direction[]{
			Direction.fromAxisAndDirection(t.run,
				Direction.AxisDirection.NEGATIVE),
			Direction.fromAxisAndDirection(t.run,
				Direction.AxisDirection.POSITIVE),
			t.branch}) {
			if (side != f)
				continue;
			float[] c = {8f, 8f, 8f};
			c[f.getAxis().ordinal()] =
				f.getAxisDirection() == Direction.AxisDirection.POSITIVE
					? 16f : 0f;
			ShapeQuads.emitDiskFan(out, srcFor, f, c, 3f, 0,
				Math.PI * 2, 10);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_TEE.get().defaultBlockState();
	}
}
