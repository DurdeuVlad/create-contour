package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourRidgeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Steep gable: two planar ramps rising 2:1 to a center ridge line.
 * Bottom face and the two run-axis triangle caps join their cull
 * lists; both ramps are interior and double-sided.
 */
public class ContourRidgeModel extends ContourModel {

	public ContourRidgeModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of(); // every face is emitted by extraQuads
	}

	private static Vector3f v(int run, int span, float cRun, float y, float cSpan) {
		float[] f = new float[3];
		f[run] = cRun;
		f[1] = y;
		f[span] = cSpan;
		return new Vector3f(f[0], f[1], f[2]);
	}

	private static Direction dirOf(int axis, boolean pos) {
		return Direction.fromAxisAndDirection(
			axis == 0 ? Direction.Axis.X : Direction.Axis.Z,
			pos ? Direction.AxisDirection.POSITIVE
				: Direction.AxisDirection.NEGATIVE);
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		int run = state.getValue(ContourRidgeBlock.AXIS) == Direction.Axis.X ? 0 : 2;
		int span = run == 0 ? 2 : 0;
		List<BakedQuad> out = new ArrayList<>();

		// full bottom face - the gable sits on its whole footprint
		if (side == Direction.DOWN) {
			Vector3f[] c = ShapeQuads.faceCorners(Direction.DOWN,
				0, 16, 0, 16, 0f);
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.DOWN))
				out.add(ShapeQuads.emitFace(Direction.DOWN, c, src));
		}
		// triangle caps on the two run-axis end faces
		for (int cap = 0; cap < 2; cap++) {
			float s0 = cap == 0 ? 0f : 16f;
			Direction capDir = dirOf(run, cap == 1);
			if (side != capDir)
				continue;
			Vector3f[] tri = ShapeQuads.tri(
				v(run, span, s0, 0, 0),
				v(run, span, s0, 0, 16),
				v(run, span, s0, 16, 8));
			for (ShapeQuads.SrcQuad src : srcFor.apply(capDir))
				out.add(ShapeQuads.emitFace(capDir, tri, src));
		}
		// the two ramps: bottom edge at span 0/16 up to the ridge at
		// span 8, y 16 - interior, double-sided
		if (side == null) {
			for (int half = 0; half < 2; half++) {
				float sLo = half == 0 ? 0f : 16f;
				Vector3f[] ramp = {
					v(run, span, 0, 0, sLo), v(run, span, 16, 0, sLo),
					v(run, span, 16, 16, 8), v(run, span, 0, 16, 8)};
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP))
					ShapeQuads.emitDouble(out, src, ramp);
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_RIDGE.get().defaultBlockState();
	}
}
