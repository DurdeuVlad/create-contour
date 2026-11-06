package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourSlopeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * 45-degree wedge. All six face roles are emitted as arbitrary quads:
 * the full boundary faces (floor+wall for BOTTOM, ceiling+wall for TOP)
 * join that side's cull list; the two triangle end caps join their cull
 * lists; the diagonal ramp surface is interior and double-sided.
 * Triangles are emitted as epsilon-duplicated-vertex quads so every
 * quad keeps nonzero area.
 */
public class ContourSlopeModel extends ContourModel {

	private final float height;

	public ContourSlopeModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		this(original, boundState, boundMaterial, 16f);
	}

	public ContourSlopeModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial, float height) {
		super(original, boundState, boundMaterial);
		this.height = height;
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of(); // every face is emitted by extraQuads
	}

	private static Vector3f v(int a1, int a3, float c1, float y, float c3) {
		float[] f = new float[3];
		f[a1] = c1;
		f[1] = y;
		f[a3] = c3;
		return new Vector3f(f[0], f[1], f[2]);
	}

	private static Direction.Axis axisOf(int i) {
		return i == 0 ? Direction.Axis.X : i == 1 ? Direction.Axis.Y : Direction.Axis.Z;
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Direction f = state.getValue(ContourSlopeBlock.FACING);
		boolean top = state.getValue(ContourSlopeBlock.HALF) == Half.TOP;
		int a1 = f.getAxis() == Direction.Axis.X ? 0 : 2; // run axis
		int a3 = a1 == 0 ? 2 : 0;                        // span axis
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float ch = pos ? 16f : 0f; // a1 coordinate of the high edge
		float cl = pos ? 0f : 16f; // a1 coordinate of the zero-height edge
		// wedge vertical extent; block boundary planes stay at 0/16
		float yLo = top ? 16f - height : 0f;
		float yHi = top ? 16f : height;
		List<BakedQuad> out = new ArrayList<>();

		// full horizontal boundary: floor for BOTTOM, ceiling for TOP
		Direction horiz = top ? Direction.UP : Direction.DOWN;
		if (side == horiz) {
			Vector3f[] c = ShapeQuads.faceCorners(horiz, 0, 16, 0, 16,
				top ? 16f : 0f);
			for (ShapeQuads.SrcQuad src : srcFor.apply(horiz))
				out.add(ShapeQuads.emitFace(horiz, c, src));
		}
		// full wall: BOTTOM on the high (facing) side; TOP on the low side
		Direction wall = top ? f.getOpposite() : f;
		if (side == wall) {
			float plane = wall.getAxisDirection() == Direction.AxisDirection.POSITIVE
				? 16f : 0f;
			Vector3f[] c = ShapeQuads.faceCorners(wall, 0, 16, yLo, yHi, plane);
			for (ShapeQuads.SrcQuad src : srcFor.apply(wall))
				out.add(ShapeQuads.emitFace(wall, c, src));
		}
		// lip band on the other side: `step` of solid adjacent to the
		// horizontal boundary (the stepped collision keeps a lip)
		float step = height / 4f;
		Direction lip = wall.getOpposite();
		if (side == lip) {
			float plane = lip.getAxisDirection() == Direction.AxisDirection.POSITIVE
				? 16f : 0f;
			float v0 = top ? yHi - step : yLo;
			float v1 = top ? yHi : yLo + step;
			Vector3f[] c = ShapeQuads.faceCorners(lip, 0, 16, v0, v1, plane);
			for (ShapeQuads.SrcQuad src : srcFor.apply(lip))
				out.add(ShapeQuads.emitFace(lip, c, src));
		}
		// triangle caps on the two span faces
		for (int cap = 0; cap < 2; cap++) {
			float s0 = cap == 0 ? 0f : 16f;
			Direction capDir = Direction.fromAxisAndDirection(axisOf(a3),
				cap == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side != capDir)
				continue;
			Vector3f p1, p2, p3;
			if (top) { // triangle: top edge + low-side drop
				p1 = v(a1, a3, ch, yHi, s0);
				p2 = v(a1, a3, cl, yHi, s0);
				p3 = v(a1, a3, cl, yLo, s0);
			} else { // triangle: bottom edge + high-side rise
				p1 = v(a1, a3, cl, yLo, s0);
				p2 = v(a1, a3, ch, yLo, s0);
				p3 = v(a1, a3, ch, yHi, s0);
			}
			Vector3f[] tri = ShapeQuads.tri(p1, p2, p3);
			for (ShapeQuads.SrcQuad src : srcFor.apply(capDir))
				out.add(ShapeQuads.emitFace(capDir, tri, src));
		}
		// diagonal ramp surface: interior, double-sided (same line both halves)
		if (side == null) {
			Vector3f[] ramp = {v(a1, a3, ch, yHi, 0), v(a1, a3, ch, yHi, 16),
				v(a1, a3, cl, yLo, 16), v(a1, a3, cl, yLo, 0)};
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				out.add(ShapeQuads.emitFace(Direction.UP, ramp, src));
				out.add(ShapeQuads.emitFace(Direction.DOWN,
					new Vector3f[]{ramp[3], ramp[2], ramp[1], ramp[0]}, src));
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_SLOPE.get().defaultBlockState();
	}
}
