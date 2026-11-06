package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourBollardBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Bollard render: lathe profile revolved around the run axis -
 * plinth foot, shaft, collar bulge, dome shrinking to a point.
 * Foot disk cap on the run-negative boundary; the tip is the apex
 * triangle from the final lathe band.
 */
public class ContourBollardModel extends ContourModel {

	private static final float[] YS = {0f, 1.5f, 2f, 3f, 10f, 11f, 12f,
		13f, 14f, 15f, 15.8f, 16f};
	private static final float[] RS = {6f, 6f, 5f, 5f, 5f, 5.5f, 5.5f,
		4.5f, 3.5f, 2.5f, 1.2f, 0f};

	public ContourBollardModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
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
		int run = state.getValue(ContourBollardBlock.AXIS).ordinal();
		int au = (run + 1) % 3, av = (run + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();

		// plinth foot disk on the run-negative boundary face
		Direction foot = Direction.fromAxisAndDirection(
			Direction.Axis.values()[run], Direction.AxisDirection.NEGATIVE);
		if (side == foot) {
			float[] c = {8f, 8f, 8f};
			c[run] = 0f;
			ShapeQuads.emitDiskFan(out, srcFor, foot, c, 6f, 0,
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
		return ContourRegistries.CONTOUR_BOLLARD.get().defaultBlockState();
	}
}
