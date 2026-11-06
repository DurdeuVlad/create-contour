package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourTrussBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Truss render: bottom chord, king post, and web posts are plain
 * AABBs (emitBoxes); the two sloped top chords are sheared 2px
 * prisms with vertical cross-sections - end caps on the u run
 * boundary faces, four tilted sides as interior emitDouble
 * parallelograms. Apex caps are omitted: the two chords abut
 * exactly at u=8.
 */
public class ContourTrussModel extends ContourModel {

	public ContourTrussModel(BakedModel original,
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
		Direction f = state.getValue(ContourTrussBlock.FACING);
		int n = f.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		Direction.Axis uAxis = Direction.Axis.values()[u];
		List<BakedQuad> out = new ArrayList<>();

		// straight members: bottom chord, king post, web posts
		List<float[]> straight = new ArrayList<>();
		float[] bc = {0, 0, 0, 16, 16, 16};
		bc[1] = 1f; bc[4] = 3f; bc[n] = 7f; bc[n + 3] = 9f;
		straight.add(bc);
		straight.add(member(u, n, 7f, 9f, 3f, 13f));
		straight.add(member(u, n, 3.5f, 5.5f, 3f, 9f));
		straight.add(member(u, n, 10.5f, 12.5f, 3f, 9f));
		out.addAll(ShapeQuads.emitBoxes(straight, side, srcFor));

		// sloped chords: left u 0..8 rising, right u 8..16 falling
		for (int half = 0; half < 2; half++) {
			float uLo = half == 0 ? 0f : 8f;
			float uHi = half == 0 ? 8f : 16f;
			// y at the outer (boundary) end and at the apex
			float yOut = 3f, yApex = 13f;
			float uOut = half == 0 ? uLo : uHi;
			float uIn = half == 0 ? uHi : uLo;
			// boundary end cap on the outer run face
			Direction cap = Direction.fromAxisAndDirection(uAxis,
				half == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side == cap) {
				for (ShapeQuads.SrcQuad src : srcFor.apply(cap))
					out.add(ShapeQuads.emitFace(cap, new Vector3f[]{
						v(u, n, uOut, yOut, 7f), v(u, n, uOut, yOut, 9f),
						v(u, n, uOut, yOut + 2f, 9f), v(u, n, uOut, yOut + 2f, 7f)},
						src));
			}
			if (side == null) {
				// four tilted sides: bottom/top edges + two n faces
				float[][] edges = {
					{0f, 7f, 0f, 9f},   // bottom edge extruded across n
					{2f, 9f, 2f, 7f},   // top edge
					{0f, 9f, 2f, 9f},   // n=9 face
					{2f, 7f, 0f, 7f},   // n=7 face
				};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					for (float[] e : edges)
						ShapeQuads.emitDouble(out, src, new Vector3f[]{
							v(u, n, uOut, yOut + e[0], e[1]),
							v(u, n, uIn, yApex + e[0], e[1]),
							v(u, n, uIn, yApex + e[2], e[3]),
							v(u, n, uOut, yOut + e[2], e[3])});
			}
		}
		return out;
	}

	private static float[] member(int u, int n, float u0, float u1,
		float y0, float y1) {
		float[] b = {0, 0, 0, 16, 16, 16};
		b[u] = u0; b[u + 3] = u1;
		b[1] = y0; b[4] = y1;
		b[n] = 7f; b[n + 3] = 9f;
		return b;
	}

	private static Vector3f v(int u, int n, float uc, float y, float nc) {
		float[] c = new float[3];
		c[u] = uc;
		c[1] = y;
		c[n] = nc;
		return new Vector3f(c[0], c[1], c[2]);
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_TRUSS.get().defaultBlockState();
	}
}
