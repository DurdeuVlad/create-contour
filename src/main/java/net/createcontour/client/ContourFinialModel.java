package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourFinialBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Turned finial: lathe profile (plate, stem, knob, taper, point)
 * along the facing axis with a base disk cap on the attach face
 * (boundary face - it joins the cull list).
 */
public class ContourFinialModel extends ContourModel {

	private static final float[] YS = {0f, 1.5f, 1.5f, 6f, 9f, 11f, 11f, 14f, 16f};
	private static final float[] RS = {5f, 5f, 2f, 2f, 4f, 4f, 2.5f, 1f, 0f};

	public ContourFinialModel(BakedModel original, @Nullable BlockState boundState,
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
		Direction f = state.getValue(ContourFinialBlock.FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int au = (a + 1) % 3, av = (a + 2) % 3;
		List<BakedQuad> out = new ArrayList<>();

		// base cap on the attach face (r5 plate)
		if (side == f.getOpposite()) {
			float[] c = {8f, 8f, 8f};
			c[a] = pos ? 0f : 16f;
			ShapeQuads.emitDiskFan(out, srcFor, f.getOpposite(), c, 5f,
				0, Math.PI * 2, 12);
		}
		// lathe surface (interior, double-sided), measured from the
		// attach face toward the tip
		if (side == null) {
			float[] eU = new float[3], eV = new float[3], eD = new float[3];
			eU[au] = 1f;
			eV[av] = 1f;
			eD[a] = pos ? 1f : -1f;
			float[] c = {8f, 8f, 8f};
			c[a] = pos ? 0f : 16f;
			ShapeQuads.emitLathe(out, srcFor, c, eU, eV, eD, YS, RS, 12);
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_FINIAL.get().defaultBlockState();
	}
}
