package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourRosetteBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Rosette: the r7 base disc on the mount-face cull list, the
 * lathe surface (plate rim, shoulder ring, domed boss, apex tri)
 * protruding inward, all double-sided.
 */
public class ContourRosetteModel extends ContourModel {

	public ContourRosetteModel(BakedModel original,
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
		Direction f = state.getValue(ContourRosetteBlock.FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int au = (n + 1) % 3, av = (n + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();

		// base disc on the mount-face cull list
		if (side == f) {
			float[] c = {8f, 8f, 8f};
			c[n] = pos ? 16f : 0f;
			ShapeQuads.emitDiskFan(out, srcFor, f, c, 7f, 0,
				Math.PI * 2, 14);
		}
		if (side == null) {
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[au] = 1f;
			eV[av] = 1f;
			eD[n] = pos ? -1f : 1f;   // lathe runs inward from the face
			float[] c = {8f, 8f, 8f};
			c[n] = pos ? 16f : 0f;
			ShapeQuads.emitLathe(out, srcFor, c, eU, eV, eD,
				ContourRosetteBlock.RY, ContourRosetteBlock.RR, 14);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_ROSETTE.get().defaultBlockState();
	}
}
