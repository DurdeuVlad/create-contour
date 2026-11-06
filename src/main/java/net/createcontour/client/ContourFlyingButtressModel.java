package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourFlyingButtressBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Flying buttress: pier and wall abutment from the shared solid
 * box list, plus the quarter-arc strut emitted with emitArcBand
 * in the vertical inward plane (band r5.5-8 about n=4, y=8,
 * extruded across the 6px strut width). The stepped arc-cover
 * collision boxes are collision only and never rendered.
 */
public class ContourFlyingButtressModel extends ContourModel {

	private static final int SEG = 8;

	public ContourFlyingButtressModel(BakedModel original,
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
		Direction f = state.getValue(ContourFlyingButtressBlock.FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = n == 0 ? 2 : 0;
		List<BakedQuad> out = new ArrayList<>();

		out.addAll(ShapeQuads.emitBoxes(
			ContourFlyingButtressBlock.solidBoxes(state), side, srcFor));

		if (side == null) {
			// arc center (u=0, n_in=4, y=8): eU inward on n, eV up, eD along u
			float[] c = {0f, 0f, 0f};
			c[n] = pos ? 12f : 4f;
			c[1] = 8f;
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[n] = pos ? -1f : 1f;
			eV[1] = 1f;
			eD[u] = 1f;
			ShapeQuads.emitArcBand(out, srcFor, c, eU, eV, eD,
				8f, 5.5f, 0, Math.PI / 2, SEG, 5f, 11f);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_FLYING_BUTTRESS.get().defaultBlockState();
	}
}
