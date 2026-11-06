package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourDomeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Radius-8 hemisphere on the mounted face: a full disk cap on the
 * face plane plus a 4x16 hemisphere surface emitted double-sided.
 */
public class ContourDomeModel extends ContourModel {

	public ContourDomeModel(BakedModel original, @Nullable BlockState boundState,
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
		Direction f = state.getValue(ContourDomeBlock.FACING);
		int thick = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float[] c = {8f, 8f, 8f};
		c[thick] = pos ? 16f : 0f;
		List<BakedQuad> out = new ArrayList<>();

		// flat disk cap on the mounted face
		if (side == f)
			ShapeQuads.emitDiskFan(out, srcFor, f, c.clone(), 8f,
				0, Math.PI * 2, 16);

		// hemisphere surface, pole pointing inward from the face
		if (side == null) {
			float[] eP = new float[3];
			eP[thick] = pos ? -1f : 1f;
			float[] eU = new float[3], eV = new float[3];
			int au = (thick + 1) % 3, av = (thick + 2) % 3;
			eU[au] = 1f;
			eV[av] = 1f;
			ShapeQuads.emitSphere(out, srcFor, c, 8f, eU, eP, eV,
				4, 16, 0, Math.PI / 2, 0, Math.PI * 2);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_DOME.get().defaultBlockState();
	}
}
