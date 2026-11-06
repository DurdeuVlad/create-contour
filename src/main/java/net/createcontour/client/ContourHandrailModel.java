package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourHandrailBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Stair handrail: two AABB posts via emitBoxes plus a sheared 2px
 * rail - vertical 2px cross-section sliding up the stair pitch, so
 * the end caps stay axis-aligned and land on the run-face cull
 * lists. Rail sides are interior double-sided parallelograms.
 */
public class ContourHandrailModel extends ContourModel {

	public ContourHandrailModel(BakedModel original,
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

	private static float railY(float s) {
		return ContourHandrailBlock.RAIL_Y0
			+ ContourHandrailBlock.RAIL_SLOPE * s;
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Direction f = state.getValue(ContourHandrailBlock.FACING);
		int u = f.getAxis().ordinal();
		int v = u == 0 ? 2 : 0;
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<BakedQuad> out = new ArrayList<>();

		// posts: same AABBs as the collision list
		List<float[]> posts = new ArrayList<>();
		for (float[] b : ContourHandrailBlock.boxesFor(state))
			if (b[1] == 0f)
				posts.add(b);
		out.addAll(ShapeQuads.emitBoxes(posts, side, srcFor));

		// rail run positions in block coords (u0 is the low end)
		float uA = pos ? 0f : 16f, uB = pos ? 16f : 0f;
		float yA = railY(0f), yB = railY(16f);
		// end caps on the run boundary faces
		for (int end = 0; end < 2; end++) {
			Direction cap = Direction.fromAxisAndDirection(
				Direction.Axis.values()[u],
				end == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side != cap)
				continue;
			float ue = end == 0 ? uA : uB, ye = end == 0 ? yA : yB;
			for (ShapeQuads.SrcQuad src : srcFor.apply(cap)) {
				float[] c = {0, 0, 0};
				c[u] = ue;
				out.add(ShapeQuads.emitFace(cap, new Vector3f[]{
					v(u, v, ue, ye - 1f, 7f), v(u, v, ue, ye - 1f, 9f),
					v(u, v, ue, ye + 1f, 9f), v(u, v, ue, ye + 1f, 7f)},
					src));
			}
		}
		if (side == null) {
			// four tilted side faces: top, bottom, two depth sides
			float[][] edges = {
				{1f, 7f, 1f, 9f},   // top
				{-1f, 9f, -1f, 7f}, // bottom
				{1f, 9f, -1f, 9f},  // v=9 side
				{-1f, 7f, 1f, 7f},  // v=7 side
			};
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
				for (float[] e : edges)
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						v(u, v, uA, yA + e[0], e[1]),
						v(u, v, uB, yB + e[0], e[1]),
						v(u, v, uB, yB + e[2], e[3]),
						v(u, v, uA, yA + e[2], e[3])});
		}
		return out;
	}

	private static Vector3f v(int u, int vv, float uc, float y, float vc) {
		float[] c = new float[3];
		c[u] = uc;
		c[1] = y;
		c[vv] = vc;
		return new Vector3f(c[0], c[1], c[2]);
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_HANDRAIL.get().defaultBlockState();
	}
}
