package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourPilasterBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Vertical half-round column bulging 8px from a wall face: full
 * chord face on the wall (boundary), a 10-facet half-cylinder
 * surface, and half-disk caps on the floor/ceiling faces.
 */
public class ContourPilasterModel extends ContourModel {

	public ContourPilasterModel(BakedModel original, @Nullable BlockState boundState,
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
		Direction f = state.getValue(ContourPilasterBlock.FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = a == 0 ? 2 : 0; // in-plane horizontal axis
		List<BakedQuad> out = new ArrayList<>();

		// eU = in-plane horizontal, eV = bulge axis (toward facing),
		// eD = run axis +Y; arc center sits ON the wall plane
		float[] eU = new float[3], eV = new float[3], eD = {0, 1, 0};
		eU[u] = 1f;
		eV[a] = pos ? 1f : -1f;
		float[] c = {8f, 8f, 8f};
		c[a] = pos ? 0f : 16f;

		// flat chord face on the wall plane (full rectangle)
		Direction back = f.getOpposite();
		if (side == back) {
			float w = pos ? 0f : 16f;
			Vector3f[] q = ShapeQuads.faceCorners(back, 0, 16, 0, 16, w);
			for (ShapeQuads.SrcQuad src : srcFor.apply(back))
				out.add(ShapeQuads.emitFace(back, q, src));
		}
		// top/bottom half-disk caps: the half in the bulge direction.
		// On a Y face the fan's au=z (cos) and av=x (sin).
		for (int end = 0; end < 2; end++) {
			Direction cap = end == 0 ? Direction.DOWN : Direction.UP;
			if (side != cap)
				continue;
			float[] cc = {8f, 8f, 8f};
			cc[a] = pos ? 0f : 16f;
			cc[1] = end == 0 ? 0f : 16f;
			double a0 = a == 2
				? (pos ? -Math.PI / 2 : Math.PI / 2)
				: (pos ? 0 : Math.PI);
			ShapeQuads.emitDiskFan(out, srcFor, cap, cc, 8f,
				a0, a0 + Math.PI, 10);
		}
		// curved half-cylinder surface (interior, double-sided):
		// t 0..pi swings +eU -> +eV -> -eU around the wall center
		if (side == null)
			ShapeQuads.emitArc(out, srcFor, c, eU, eV, eD, 8f,
				0, Math.PI, 10, -8f, 8f);
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_PILASTER.get().defaultBlockState();
	}
}
