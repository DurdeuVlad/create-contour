package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourCrossBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Pipe cross: two r3 tubes swept full length on the two non-closed
 * axes, both emitted double-sided, with full-disk mouths on all
 * four open boundary faces.
 */
public class ContourCrossModel extends ContourModel {

	private static final int SEG = 14;

	public ContourCrossModel(BakedModel original,
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
		int closed = state.getValue(ContourCrossBlock.AXIS).ordinal();
		List<BakedQuad> out = new ArrayList<>();

		if (side == null) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
				for (int a = 0; a < 3; a++) {
					if (a == closed)
						continue;
					cylinder(out, src, a,
						new int[]{(a + 1) % 3, (a + 2) % 3}, 0f, 16f);
				}
		}
		// mouths on the four open boundary faces, centered at 8
		for (Direction.Axis ax : Direction.Axis.values()) {
			if (ax.ordinal() == closed)
				continue;
			for (Direction.AxisDirection d : Direction.AxisDirection.values()) {
				Direction f = Direction.fromAxisAndDirection(ax, d);
				if (side != f)
					continue;
				float[] c = {8f, 8f, 8f};
				c[ax.ordinal()] = d == Direction.AxisDirection.POSITIVE
					? 16f : 0f;
				ShapeQuads.emitDiskFan(out, srcFor, f, c, 3f, 0,
					Math.PI * 2, 10);
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_CROSS.get().defaultBlockState();
	}
}
