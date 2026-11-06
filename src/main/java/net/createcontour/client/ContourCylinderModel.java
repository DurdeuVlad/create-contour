package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourCylinderBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Radius-6 round column: 12-facet curved surface plus full disk caps
 * on both run ends (boundary faces - they join the cull list).
 */
public class ContourCylinderModel extends ContourModel {

	public ContourCylinderModel(BakedModel original, @Nullable BlockState boundState,
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
		int run = state.getValue(ContourCylinderBlock.AXIS).ordinal();
		int au = (run + 1) % 3, av = (run + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();

		// end caps: full disks on both run faces
		for (int end = 0; end < 2; end++) {
			Direction cap = Direction.fromAxisAndDirection(
				Direction.Axis.values()[run],
				end == 0 ? Direction.AxisDirection.NEGATIVE
					: Direction.AxisDirection.POSITIVE);
			if (side == cap) {
				float[] c = {8f, 8f, 8f};
				c[run] = end == 0 ? 0f : 16f;
				ShapeQuads.emitDiskFan(out, srcFor, cap, c, 6f,
					0, Math.PI * 2, 12);
			}
		}
		// curved surface (interior, double-sided), d measured from center
		if (side == null) {
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[au] = 1f;
			eV[av] = 1f;
			eD[run] = 1f;
			ShapeQuads.emitArc(out, srcFor, new float[]{8f, 8f, 8f},
				eU, eV, eD, 6f, 0, Math.PI * 2, 12, -8f, 8f);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_CYLINDER.get().defaultBlockState();
	}
}
