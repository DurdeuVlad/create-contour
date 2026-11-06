package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourConeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Radius-8 cone: base disk on the attach face (boundary, joins the
 * cull list) plus a 4x16 side surface to the apex point on the far
 * face (interior, double-sided).
 */
public class ContourConeModel extends ContourModel {

	public ContourConeModel(BakedModel original, @Nullable BlockState boundState,
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
		Direction f = state.getValue(ContourConeBlock.FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<BakedQuad> out = new ArrayList<>();

		// base disk on the attach face (opposite the apex)
		Direction back = f.getOpposite();
		if (side == back) {
			float[] c = {8f, 8f, 8f};
			c[a] = pos ? 0f : 16f;
			ShapeQuads.emitDiskFan(out, srcFor, back, c, 8f,
				0, Math.PI * 2, 12);
		}
		// side surface from the base ring to the apex (double-sided)
		if (side == null) {
			int au = (a + 1) % 3, av = (a + 2) % 3;
			float[] c = {8f, 8f, 8f};
			c[a] = pos ? 0f : 16f;
			float[] eU = new float[3], eV = new float[3],
				eD = new float[3];
			eU[au] = 1f;
			eV[av] = 1f;
			eD[a] = pos ? 1f : -1f;
			ShapeQuads.emitCone(out, srcFor, c, 8f, 16f, eU, eV, eD,
				4, 16);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_CONE.get().defaultBlockState();
	}
}
