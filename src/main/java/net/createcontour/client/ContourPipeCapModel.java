package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourPipeCapBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Pipe cap: the r3 stub tube swept from the mouth face to the
 * shoulder, the r3 -> r4 shoulder ring, the r4 plug cylinder, and
 * the r4 plug cap disk, all interior surfaces emitted
 * double-sided; the open mouth is a full r3 disk on the facing
 * cull list, matching tee/elbow mouths.
 */
public class ContourPipeCapModel extends ContourModel {

	private static final int SEG = 14;

	public ContourPipeCapModel(BakedModel original,
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
		Direction f = state.getValue(ContourPipeCapBlock.FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int au = (n + 1) % 3, av = (n + 2) % 3;
		float nMouth = pos ? 16f : 0f, nShoulder = pos ? 6f : 10f;
		float nOuter = pos ? 4f : 12f;
		List<BakedQuad> out = new ArrayList<>();

		// open mouth disk on the facing cull list
		if (side == f) {
			float[] c = {8f, 8f, 8f};
			c[n] = nMouth;
			ShapeQuads.emitDiskFan(out, srcFor, f, c, 3f, 0,
				Math.PI * 2, SEG);
		}
		if (side == null) {
			java.util.function.BiFunction<Float, Double, Vector3f> pt =
				(nn, t) -> {
					float[] c = {8f, 8f, 8f};
					c[n] = nn;
					c[au] = (float) (8 + 3 * Math.cos(t));
					c[av] = (float) (8 + 3 * Math.sin(t));
					return new Vector3f(c[0], c[1], c[2]);
				};
			java.util.function.BiFunction<Float, Double, Vector3f> pt4 =
				(nn, t) -> {
					float[] c = {8f, 8f, 8f};
					c[n] = nn;
					c[au] = (float) (8 + 4 * Math.cos(t));
					c[av] = (float) (8 + 4 * Math.sin(t));
					return new Vector3f(c[0], c[1], c[2]);
				};
			float[] mid = {8f, 8f, 8f};
			mid[n] = nOuter;
			Vector3f center = new Vector3f(mid[0], mid[1], mid[2]);
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				for (int i = 0; i < SEG; i++) {
					double ta = Math.PI * 2 * i / SEG,
						tb = Math.PI * 2 * (i + 1) / SEG;
					// stub cylinder r3 mouth -> shoulder
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(nMouth, ta), pt.apply(nMouth, tb),
						pt.apply(nShoulder, tb), pt.apply(nShoulder, ta)});
					// shoulder ring r3 -> r4
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(nShoulder, ta), pt.apply(nShoulder, tb),
						pt4.apply(nShoulder, tb), pt4.apply(nShoulder, ta)});
					// plug cylinder r4 shoulder -> outer face
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt4.apply(nShoulder, ta), pt4.apply(nShoulder, tb),
						pt4.apply(nOuter, tb), pt4.apply(nOuter, ta)});
					// plug cap disk r4 on the outer face (sliver tri)
					ShapeQuads.emitDouble(out, src, ShapeQuads.tri(
						center, pt4.apply(nOuter, ta),
						pt4.apply(nOuter, tb)));
				}
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_PIPE_CAP.get().defaultBlockState();
	}
}
