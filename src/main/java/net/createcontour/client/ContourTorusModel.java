package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourTorusBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Ring/donut: the lathe profile is a closed circle - minor radius 3
 * centered at (8, major radius 5) - revolved around the run axis.
 * 16 profile stations x 16 revolution segments, all emitted as
 * interior double-sided quads so the ring keeps showing on tangent
 * block boundaries. A torus is closed: no end caps exist.
 */
public class ContourTorusModel extends ContourModel {

	private static final float[] YS = new float[17];
	private static final float[] RS = new float[17];
	static {
		for (int k = 0; k < 17; k++) {
			double a = Math.PI * 2 * k / 16;
			YS[k] = (float) (8 + 3 * Math.sin(a));
			RS[k] = (float) (5 + 3 * Math.cos(a));
		}
	}

	public ContourTorusModel(BakedModel original, @Nullable BlockState boundState,
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
		List<BakedQuad> out = new ArrayList<>();
		if (side != null)
			return out; // closed interior surface only
		int run = state.getValue(ContourTorusBlock.AXIS).ordinal();
		int au = (run + 1) % 3, av = (run + 2) % 3;
		float[] eU = new float[3], eV = new float[3], eD = new float[3];
		eU[au] = 1f;
		eV[av] = 1f;
		eD[run] = 1f;
		float[] c = {8f, 8f, 8f};
		c[run] = 0f;
		ShapeQuads.emitLathe(out, srcFor, c, eU, eV, eD, YS, RS, 16);
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_TORUS.get().defaultBlockState();
	}
}
