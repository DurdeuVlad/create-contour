package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourNewelBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Newel post: lathe profile revolved around the run axis - plinth
 * foot, stem, collar, cap shoulder, ball knob rising to a point.
 * Only the foot end gets a boundary disk cap; the tip is the apex
 * triangle emitted by the final lathe band.
 */
public class ContourNewelModel extends ContourModel {

	private static final float[] YS = {0f, 2.5f, 3f, 3.5f, 9.5f, 10f, 12f,
		12.5f, 13f, 14.5f, 15.5f, 16f};
	private static final float[] RS = {4.5f, 4.5f, 4f, 3.2f, 3.2f, 4f, 4f,
		3.4f, 3.8f, 3.2f, 1.8f, 0f};

	public ContourNewelModel(BakedModel original, @Nullable BlockState boundState,
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

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		int run = state.getValue(ContourNewelBlock.AXIS).ordinal();
		int au = (run + 1) % 3, av = (run + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();

		// foot disk cap on the run-negative boundary face
		Direction foot = Direction.fromAxisAndDirection(
			Direction.Axis.values()[run], Direction.AxisDirection.NEGATIVE);
		if (side == foot) {
			float[] c = {8f, 8f, 8f};
			c[run] = 0f;
			ShapeQuads.emitDiskFan(out, srcFor, foot, c, 4.5f, 0,
				Math.PI * 2, 12);
		}
		if (side == null) {
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[au] = 1f;
			eV[av] = 1f;
			eD[run] = 1f;
			float[] c = {8f, 8f, 8f};
			c[run] = 0f;
			ShapeQuads.emitLathe(out, srcFor, c, eU, eV, eD, YS, RS, 12);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_NEWEL.get().defaultBlockState();
	}
}
