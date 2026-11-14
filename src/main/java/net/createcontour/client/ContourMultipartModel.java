package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * Box-model host for the multipart occupancy channels (issue #12/#22):
 * emits the bound state's boxes via ContourBoxModel, then one box set
 * per extra occupant from the unified OCCUPANT_EXTRAS model data -
 * each Occupant record decodes its own channel geometry via
 * footprint(). Used for byte / byte_panel / half_panel / flat_pane;
 * the positional panel model emits the same list through
 * {@link #emitExtras}.
 */
public class ContourMultipartModel extends ContourBoxModel {

	public ContourMultipartModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial,
		Function<BlockState, List<float[]>> boxes,
		Supplier<? extends Block> block) {
		super(original, boundState, boundMaterial, boxes, block);
	}

	/**
	 * Occupant quads for one host model (issue #22 unified list). UV
	 * anchoring follows the same cell-grid rule as the base occupant
	 * (absolute pixel coordinates); each occupant samples its own
	 * material and emits its own channel's footprint boxes.
	 */
	static List<BakedQuad> emitExtras(ContourModel model,
		@Nullable ModelData data, @Nullable Direction side, RandomSource rand,
		@Nullable RenderType renderType) {
		return emitExtras(model, data, side, rand, renderType, null);
	}

	/**
	 * Same emission, skipping occupants of `skipKind` - the board model
	 * uses this because its own per-face loop already renders BOARD
	 * records with the neutral-pass gate.
	 */
	static List<BakedQuad> emitExtras(ContourModel model,
		@Nullable ModelData data, @Nullable Direction side, RandomSource rand,
		@Nullable RenderType renderType,
		@Nullable net.createcontour.blockentity.ContourBlockEntity
			.OccupantKind skipKind) {
		List<BakedQuad> out = new ArrayList<>();
		List<net.createcontour.blockentity.ContourBlockEntity.Occupant> extras =
			data == null ? null : data.get(ContourModel.OCCUPANT_EXTRAS);
		if (extras == null)
			return out;
		// Issue #19: each occupant's boundary faces blend toward
		// same-material Contour neighbors on the occupant's own paint.
		for (net.createcontour.blockentity.ContourBlockEntity.Occupant e
			: extras) {
			if (e.kind() == skipKind)
				continue;
			out.addAll(model.blendBoundaryQuads(ShapeQuads.emitBoxes(
				e.footprint(), side,
				d -> model.sourcesFor(e.material(), d, rand,
					ModelData.EMPTY, renderType)),
				side, e.material(),
				d -> model.sourcesFor(e.material(), d, rand,
					ModelData.EMPTY, renderType),
				rand, data, renderType));
		}
		return out;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state,
		@Nullable Direction side, RandomSource rand, ModelData data,
		@Nullable RenderType renderType) {
		List<BakedQuad> quads = new ArrayList<>(
			super.getQuads(state, side, rand, data, renderType));
		quads.addAll(emitExtras(this, data, side, rand, renderType));
		return quads;
	}
}
