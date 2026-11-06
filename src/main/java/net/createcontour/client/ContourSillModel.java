package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourSillBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Sill render: a sheared ledge prism u 1..15 x n wall..tip with a
 * flat bottom and sloped top. Bottom and wall cap join boundary
 * cull lists; the slope, tip, and two u-ends are interior
 * emitDouble faces.
 */
public class ContourSillModel extends ContourModel {

	public ContourSillModel(BakedModel original,
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
		Direction f = state.getValue(ContourSillBlock.FACING);
		int n = f.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float nW = pos ? 16f : 0f;   // wall end; tip stays at n=8
		float nT = 8f;
		float yW = 4f, yT = 2.5f;
		List<BakedQuad> out = new ArrayList<>();

		// flat bottom on the DOWN boundary cull list
		if (side == Direction.DOWN) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.DOWN))
				out.add(ShapeQuads.emitFace(Direction.DOWN, new Vector3f[]{
					v(u, n, 1f, 0f, nW), v(u, n, 15f, 0f, nW),
					v(u, n, 15f, 0f, nT), v(u, n, 1f, 0f, nT)}, src));
		}
		// wall cap on the facing boundary cull list
		if (side == f) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(f))
				out.add(ShapeQuads.emitFace(f, new Vector3f[]{
					v(u, n, 1f, 0f, nW), v(u, n, 15f, 0f, nW),
					v(u, n, 15f, yW, nW), v(u, n, 1f, yW, nW)}, src));
		}
		if (side == null) {
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				// sloped top
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, 1f, yW, nW), v(u, n, 15f, yW, nW),
					v(u, n, 15f, yT, nT), v(u, n, 1f, yT, nT)});
				// drip tip cap
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, 1f, 0f, nT), v(u, n, 15f, 0f, nT),
					v(u, n, 15f, yT, nT), v(u, n, 1f, yT, nT)});
				// u end trapezoids
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, 1f, 0f, nW), v(u, n, 1f, 0f, nT),
					v(u, n, 1f, yT, nT), v(u, n, 1f, yW, nW)});
				ShapeQuads.emitDouble(out, src, new Vector3f[]{
					v(u, n, 15f, 0f, nW), v(u, n, 15f, 0f, nT),
					v(u, n, 15f, yT, nT), v(u, n, 15f, yW, nW)});
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
		return ContourRegistries.CONTOUR_SILL.get().defaultBlockState();
	}
}
