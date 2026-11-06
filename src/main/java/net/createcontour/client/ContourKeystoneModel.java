package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourKeystoneBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Keystone render: a trapezoidal prism u 6..10 at y=0 flaring to
 * 3..13 at y=16, panel n 5..11. Narrow bottom and wide top caps
 * join boundary cull lists; the two sloped u-ends and flat
 * n-faces are interior emitDouble quads.
 */
public class ContourKeystoneModel extends ContourModel {

	public ContourKeystoneModel(BakedModel original,
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
		int n = state.getValue(ContourKeystoneBlock.FACING)
			.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		float l0 = 6f, h0 = 10f;   // u range at y=0
		float l1 = 3f, h1 = 13f;   // u range at y=16
		List<BakedQuad> out = new ArrayList<>();

		if (side == Direction.DOWN) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.DOWN))
				out.add(ShapeQuads.emitFace(Direction.DOWN, new Vector3f[]{
					v(u, n, l0, 0f, 5f), v(u, n, h0, 0f, 5f),
					v(u, n, h0, 0f, 11f), v(u, n, l0, 0f, 11f)}, src));
		}
		if (side == Direction.UP) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
				out.add(ShapeQuads.emitFace(Direction.UP, new Vector3f[]{
					v(u, n, l1, 16f, 5f), v(u, n, h1, 16f, 5f),
					v(u, n, h1, 16f, 11f), v(u, n, l1, 16f, 11f)}, src));
		}
		if (side == null) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				// sloped u ends
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, l0, 0f, 5f), v(u, n, l0, 0f, 11f),
					v(u, n, l1, 16f, 11f), v(u, n, l1, 16f, 5f)});
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, h0, 0f, 5f), v(u, n, h0, 0f, 11f),
					v(u, n, h1, 16f, 11f), v(u, n, h1, 16f, 5f)});
				// flat n faces (trapezoids)
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, l0, 0f, 5f), v(u, n, h0, 0f, 5f),
					v(u, n, h1, 16f, 5f), v(u, n, l1, 16f, 5f)});
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, l0, 0f, 11f), v(u, n, h0, 0f, 11f),
					v(u, n, h1, 16f, 11f), v(u, n, l1, 16f, 11f)});
			}
		}
		return out;
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
		return ContourRegistries.CONTOUR_KEYSTONE.get().defaultBlockState();
	}
}
