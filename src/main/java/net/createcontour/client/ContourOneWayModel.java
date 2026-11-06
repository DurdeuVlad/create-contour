package net.createcontour.client;

import java.util.List;
import net.createcontour.block.ContourOneWayBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * One-way glass model: the FACING side emits no quads at all, so the
 * block is invisible from that direction while the five remaining
 * sides keep their material-textured opaque faces (their quads are
 * backface-culled when seen through the open side).
 */
public class ContourOneWayModel extends ContourBoxModel {

	public ContourOneWayModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial,
			ContourOneWayBlock::boxesFor, ContourRegistries.CONTOUR_ONE_WAY);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
		RandomSource rand, ModelData data, @Nullable RenderType renderType) {
		if (state != null && side != null
			&& side == state.getValue(ContourOneWayBlock.FACING))
			return List.of();
		return super.getQuads(state, side, rand, data, renderType);
	}
}
