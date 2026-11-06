package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourFlangeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Pipe flange: annular ring face on the boundary plane (r3 bore,
 * r7 rim) via emitAnnularSector, the matching interior ring and the
 * outer/inner cylinder walls emitted double-sided, plus four bolt
 * stud boxes. Closed loop - no radial seam caps.
 */
public class ContourFlangeModel extends ContourModel {

	private static final int SEG = 12;

	public ContourFlangeModel(BakedModel original,
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
		Direction f = state.getValue(ContourFlangeBlock.FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int au = (n + 1) % 3, av = (n + 2) % 3;
		float nOut = pos ? 16f : 0f, nIn = pos ? 14f : 2f;
		List<BakedQuad> out = new ArrayList<>();

		float[] eU = new float[3], eV = new float[3];
		eU[au] = 1f;
		eV[av] = 1f;
		// boundary ring face (joins the facing cull list)
		if (side == f) {
			float[] c = {8f, 8f, 8f};
			c[n] = nOut;
			ShapeQuads.emitAnnularSector(out, srcFor, f, c, eU, eV,
				3f, 7f, 0, Math.PI * 2, SEG);
		}
		if (side == null) {
			java.util.function.BiFunction<Double, Float, Vector3f> ring =
				(t, r) -> {
					float[] c = {8f, 8f, 8f};
					c[n] = nIn;
					c[au] = (float) (8 + r * Math.cos(t));
					c[av] = (float) (8 + r * Math.sin(t));
					return new Vector3f(c[0], c[1], c[2]);
				};
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				for (int i = 0; i < SEG; i++) {
					double ta = Math.PI * 2 * i / SEG,
						tb = Math.PI * 2 * (i + 1) / SEG;
					// interior annular ring at nIn
					ShapeQuads.emitDouble(out, src, new Vector3f[]{
						ring.apply(ta, 3f), ring.apply(tb, 3f),
						ring.apply(tb, 7f), ring.apply(ta, 7f)});
					// outer and inner cylinder walls
					for (float r : new float[]{7f, 3f}) {
						Vector3f[] q = new Vector3f[4];
						double[][] dc = {
							{Math.cos(ta), Math.sin(ta), nIn},
							{Math.cos(tb), Math.sin(tb), nIn},
							{Math.cos(tb), Math.sin(tb), nOut},
							{Math.cos(ta), Math.sin(ta), nOut}};
						for (int k = 0; k < 4; k++) {
							float[] c = {8f, 8f, 8f};
							c[au] = (float) (8 + r * dc[k][0]);
							c[av] = (float) (8 + r * dc[k][1]);
							c[n] = (float) dc[k][2];
							q[k] = new Vector3f(c[0], c[1], c[2]);
						}
						ShapeQuads.emitDouble(out, src, q);
					}
				}
			}
		}
		// bolt studs (shared boxes -> emitBoxes)
		out.addAll(ShapeQuads.emitBoxes(
			ContourFlangeBlock.studBoxes(state), side, srcFor));
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_FLANGE.get().defaultBlockState();
	}
}
