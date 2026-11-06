package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourHalfTimberBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Half timber: the perimeter frame emitted from the shared box
 * list, plus the diagonal brace as a sheared prism - two broad
 * faces, two edge strips, and two end caps, all interior and
 * emitted double-sided. The stepped brace-cover collision boxes
 * are collision only and never rendered.
 */
public class ContourHalfTimberModel extends ContourModel {

	public ContourHalfTimberModel(BakedModel original,
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
		Direction f = state.getValue(ContourHalfTimberBlock.FACING);
		int n = f.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		boolean rise = state.getValue(ContourHalfTimberBlock.DIAG);
		List<BakedQuad> out = new ArrayList<>();

		// frame rails/stiles (boundary-aware shared boxes)
		out.addAll(ShapeQuads.emitBoxes(
			ContourHalfTimberBlock.frameBoxes(state), side, srcFor));

		if (side == null) {
			float ya = rise ? 3f : 13f, yb = rise ? 13f : 3f;
			// band corners in (u, y): end points offset +-1 perpendicular
			float[][] ends = {
				{3f - 1f, ya + 1f, 3f + 1f, ya - 1f},
				{13f - 1f, yb + 1f, 13f + 1f, yb - 1f}};
			java.util.function.BiFunction<float[], Float, Vector3f> pt =
				(uu, nn) -> {
					float[] c = {8f, 8f, 8f};
					c[u] = uu[0];
					c[1] = uu[1];
					c[n] = nn;
					return new Vector3f(c[0], c[1], c[2]);
				};
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				for (float nn : new float[]{6.5f, 9.5f}) {
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(new float[]{ends[0][0], ends[0][1]}, nn),
						pt.apply(new float[]{ends[0][2], ends[0][3]}, nn),
						pt.apply(new float[]{ends[1][2], ends[1][3]}, nn),
						pt.apply(new float[]{ends[1][0], ends[1][1]}, nn)});
				}
				// edge strips along both long sides
				for (int k = 0; k < 2; k++) {
					float[] ea = {ends[0][k * 2], ends[0][k * 2 + 1]};
					float[] eb = {ends[1][k * 2], ends[1][k * 2 + 1]};
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(ea, 6.5f), pt.apply(eb, 6.5f),
						pt.apply(eb, 9.5f), pt.apply(ea, 9.5f)});
				}
				// end caps perpendicular to the band
				for (int e = 0; e < 2; e++) {
					float[] ca = {ends[e][0], ends[e][1]};
					float[] cb = {ends[e][2], ends[e][3]};
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						pt.apply(ca, 6.5f), pt.apply(cb, 6.5f),
						pt.apply(cb, 9.5f), pt.apply(ca, 9.5f)});
				}
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_HALF_TIMBER.get().defaultBlockState();
	}
}
