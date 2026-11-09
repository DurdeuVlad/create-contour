package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourPanelBlock;
import net.createcontour.block.ContourPanelBlock.PanelPos;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Positional panel: AABB box lists for face/edge/mid positions; diagonal
 * positions contribute their two corner-column boxes via boxesFor plus
 * the two 45-degree band faces emitted here (double-sided).
 */
public class ContourPanelModel extends ContourModel {

	private static final float T = 1.5f;

	public ContourPanelModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return ContourPanelBlock.boxesFor(state.getValue(ContourPanelBlock.POSITION));
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_PANEL.get().defaultBlockState();
	}

	/**
	 * Extra face-quadrant occupants (issue #12) emitted after the bound
	 * state's quads, same ModelData lists as ContourMultipartModel.
	 */
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state,
		@Nullable Direction side, net.minecraft.util.RandomSource rand,
		net.neoforged.neoforge.client.model.data.ModelData data,
		@Nullable net.minecraft.client.renderer.RenderType renderType) {
		List<BakedQuad> quads = new ArrayList<>(
			super.getQuads(state, side, rand, data, renderType));
		quads.addAll(ContourMultipartModel.emitExtras(this, data, side, rand,
			renderType));
		return quads;
	}

	private static Vector3f v(int a1, int a2, int a3, float c1, float c2, float c3) {
		float[] f = new float[3];
		f[a1] = c1;
		f[a2] = c2;
		f[a3] = c3;
		return new Vector3f(f[0], f[1], f[2]);
	}

	private static Vector3f[] rev(Vector3f[] in) {
		return new Vector3f[]{in[3], in[2], in[1], in[0]};
	}

	private static Direction.Axis axisOf(int i) {
		return i == 0 ? Direction.Axis.X : i == 1 ? Direction.Axis.Y : Direction.Axis.Z;
	}

	/** The two diagonal band faces (both windings each) for DIAG states. */
	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		PanelPos p = state.getValue(ContourPanelBlock.POSITION);
		if (p == null || !p.isDiagonal() || side != null)
			return List.of();
		int a1, a2, a3;
		boolean v1;
		switch (p) {
			case DIAG_Y0, DIAG_Y1 -> { a1 = 0; a2 = 2; a3 = 1; v1 = p == PanelPos.DIAG_Y1; }
			case DIAG_X0, DIAG_X1 -> { a1 = 1; a2 = 2; a3 = 0; v1 = p == PanelPos.DIAG_X1; }
			default -> { a1 = 0; a2 = 1; a3 = 2; v1 = p == PanelPos.DIAG_Z1; }
		}
		Direction posA1 = Direction.fromAxisAndDirection(
			axisOf(a1), Direction.AxisDirection.POSITIVE);
		Direction negA1 = posA1.getOpposite();
		Vector3f[] plus, minus;
		if (!v1) { // a1 - a2 = +-T
			plus = new Vector3f[]{v(a1, a2, a3, T, 0, 0), v(a1, a2, a3, T, 0, 16),
				v(a1, a2, a3, 16, 16 - T, 16), v(a1, a2, a3, 16, 16 - T, 0)};
			minus = new Vector3f[]{v(a1, a2, a3, 0, T, 0), v(a1, a2, a3, 0, T, 16),
				v(a1, a2, a3, 16 - T, 16, 16), v(a1, a2, a3, 16 - T, 16, 0)};
		} else { // a1 + a2 = 16 +- T
			plus = new Vector3f[]{v(a1, a2, a3, T, 16, 0), v(a1, a2, a3, T, 16, 16),
				v(a1, a2, a3, 16, T, 16), v(a1, a2, a3, 16, T, 0)};
			minus = new Vector3f[]{v(a1, a2, a3, 0, 16 - T, 0), v(a1, a2, a3, 0, 16 - T, 16),
				v(a1, a2, a3, 16 - T, 0, 16), v(a1, a2, a3, 16 - T, 0, 0)};
		}
		List<BakedQuad> out = new ArrayList<>(8);
		for (ShapeQuads.SrcQuad src : srcFor.apply(posA1)) {
			out.add(ShapeQuads.emitFace(posA1, plus, src));
			out.add(ShapeQuads.emitFace(negA1, rev(plus), src));
		}
		for (ShapeQuads.SrcQuad src : srcFor.apply(negA1)) {
			out.add(ShapeQuads.emitFace(negA1, minus, src));
			out.add(ShapeQuads.emitFace(posA1, rev(minus), src));
		}
		return out;
	}
}
