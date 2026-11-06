package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourArcWallBlock;
import net.createcontour.block.ContourCornerSlopeBlock.Corner;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Arc wall: the quarter-annulus band emitted with emitArcBand
 * (outer + inner cylinder walls and both radial end caps,
 * double-sided), plus the top and bottom annular-sector faces on
 * the UP/DOWN cull lists.
 */
public class ContourArcWallModel extends ContourModel {

	private static final int SEG = 8;

	public ContourArcWallModel(BakedModel original,
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
		Corner c = state.getValue(ContourArcWallBlock.CORNER);
		float[] d = ContourArcWallBlock.dirs(c);
		float[] eU = {d[0], 0f, 0f};
		float[] eV = {0f, 0f, d[1]};
		List<BakedQuad> out = new ArrayList<>();

		if (side == null) {
			ShapeQuads.emitArcBand(out, srcFor,
				new float[]{c.ax, 0f, c.az}, eU, eV, new float[]{0f, 1f, 0f},
				16f, 12f, 0, Math.PI / 2, SEG, 0f, 16f);
		}
		if (side == Direction.UP)
			ShapeQuads.emitAnnularSector(out, srcFor, Direction.UP,
				new float[]{c.ax, 16f, c.az}, eU, eV,
				12f, 16f, 0, Math.PI / 2, SEG);
		if (side == Direction.DOWN)
			ShapeQuads.emitAnnularSector(out, srcFor, Direction.DOWN,
				new float[]{c.ax, 0f, c.az}, eU, eV,
				12f, 16f, 0, Math.PI / 2, SEG);
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_ARC_WALL.get().defaultBlockState();
	}
}
