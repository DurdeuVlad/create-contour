package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourCornerSlopeBlock.Corner;
import net.createcontour.block.ContourRoundCornerBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import org.jetbrains.annotations.Nullable;

/**
 * Sphere octant at a cube corner: three quarter-disk caps on the
 * corner's faces plus a 6x6 octant surface emitted double-sided.
 */
public class ContourRoundCornerModel extends ContourModel {

	public ContourRoundCornerModel(BakedModel original,
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
		Corner c = state.getValue(ContourRoundCornerBlock.CORNER);
		boolean top = state.getValue(ContourRoundCornerBlock.HALF) == Half.TOP;
		int bx = c == Corner.NE || c == Corner.SE ? 1 : 0;
		int by = top ? 1 : 0;
		int bz = c == Corner.SE || c == Corner.SW ? 1 : 0;
		float[] cc = {bx * 16f, by * 16f, bz * 16f};
		// outward face dirs containing the corner, and inward unit vecs
		Direction[] faces = {
			bx == 0 ? Direction.WEST : Direction.EAST,
			by == 0 ? Direction.DOWN : Direction.UP,
			bz == 0 ? Direction.NORTH : Direction.SOUTH};
		float[][] inward = new float[3][3];
		for (int a = 0; a < 3; a++)
			inward[a][a] = new int[]{bx, by, bz}[a] == 0 ? 1f : -1f;
		List<BakedQuad> out = new ArrayList<>();

		// quarter-disk caps on the three corner faces
		for (Direction face : faces) {
			if (side != face)
				continue;
			int au = (face.getAxis().ordinal() + 1) % 3;
			int av = (face.getAxis().ordinal() + 2) % 3;
			boolean su = cc[au] == 0, sv = cc[av] == 0;
			double a0 = su ? (sv ? 0 : -Math.PI / 2)
				: (sv ? Math.PI / 2 : Math.PI);
			ShapeQuads.emitDiskFan(out, srcFor, face, cc.clone(), 16f,
				a0, a0 + Math.PI / 2, 8);
		}

		// octant surface: pole on axis 0, theta/phi in [0, pi/2]
		if (side == null)
			ShapeQuads.emitSphere(out, srcFor, cc, 16f,
				inward[1], inward[0], inward[2], 6, 6,
				0, Math.PI / 2, 0, Math.PI / 2);
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_ROUND_CORNER.get().defaultBlockState();
	}
}
