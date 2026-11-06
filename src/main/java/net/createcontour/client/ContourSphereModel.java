package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Radius-8 full sphere: a 12x16 surface grid, pole rows paired into
 * real quads. Every quad is interior (the ball only touches each
 * face at a point), so all emission is double-sided on side==null.
 */
public class ContourSphereModel extends ContourModel {

	public ContourSphereModel(BakedModel original, @Nullable BlockState boundState,
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
		if (side == null)
			ShapeQuads.emitSphere(out, srcFor, new float[]{8f, 8f, 8f}, 8f,
				new float[]{1f, 0, 0}, new float[]{0, 1f, 0},
				new float[]{0, 0, 1f}, 12, 16, 0, Math.PI, 0, Math.PI * 2);
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_SPHERE.get().defaultBlockState();
	}
}
