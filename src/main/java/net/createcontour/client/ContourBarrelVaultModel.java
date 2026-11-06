package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourBarrelVaultBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import org.jetbrains.annotations.Nullable;

/**
 * Barrel vault: r8 semicircular shell extruded along the run axis
 * (interior double-sided) plus half-disk gable caps on both run ends
 * (boundary faces, they join the cull list). The fan's au/av frame is
 * axis-derived, so the cap's angle window differs per run axis.
 */
public class ContourBarrelVaultModel extends ContourModel {

	public ContourBarrelVaultModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial) {
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
		List<BakedQuad> out = new ArrayList<>();
		int run = state.getValue(ContourBarrelVaultBlock.AXIS).ordinal();
		int u = run == 2 ? 0 : 2;
		boolean bottom = state.getValue(ContourBarrelVaultBlock.HALF)
			== Half.BOTTOM;
		double t0 = bottom ? 0 : Math.PI, t1 = bottom ? Math.PI : Math.PI * 2;

		if (side == null) {
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[u] = 1f;
			eV[1] = 1f;
			eD[run] = 1f;
			float[] c = {0f, 0f, 0f};
			c[u] = 8f;
			c[1] = bottom ? 0f : 16f;
			ShapeQuads.emitArc(out, srcFor, c, eU, eV, eD, 8f, t0, t1, 12,
				0f, 16f);
		}
		// gable half-disk caps on both run ends
		for (int end = 0; end < 2; end++) {
			Direction cap = Direction.fromAxisAndDirection(
				Direction.Axis.values()[run],
				end == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side != cap)
				continue;
			float[] c = {0f, 0f, 0f};
			c[u] = 8f;
			c[1] = bottom ? 0f : 16f;
			c[run] = end == 0 ? 0f : 16f;
			// fan au/av is axis-derived: run=Z puts the span on X so the
			// half-disk is a0..pi; run=X puts it on Z at +/-pi/2
			boolean runZ = run == 2;
			double a0 = runZ ? t0 : (bottom ? -Math.PI / 2 : Math.PI / 2);
			double a1 = runZ ? t1 : (bottom ? Math.PI / 2 : Math.PI * 1.5);
			ShapeQuads.emitDiskFan(out, srcFor, cap, c, 8f, a0, a1, 10);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_BARREL_VAULT.get().defaultBlockState();
	}
}
