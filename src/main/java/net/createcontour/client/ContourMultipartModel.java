package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.createcontour.block.PanelSlots;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Box-model host for the multipart occupancy channels (issue #12):
 * emits the bound state's boxes via ContourBoxModel, then one box set
 * per extra occupant from model data - PanelExtra (slot anchor + kind,
 * geometry via PanelSlots) and PaneExtra (axis mid-plane). Used for
 * byte_panel / half_panel / flat_pane; the positional panel model
 * emits the same lists through {@link #emitExtras}.
 */
public class ContourMultipartModel extends ContourBoxModel {

	public record PanelExtra(int slot, int kind, BlockState material) {
	}

	public record PaneExtra(int axis, BlockState material) {
	}

	public static final ModelProperty<List<PanelExtra>> PANEL_EXTRAS =
		new ModelProperty<>();

	public static final ModelProperty<List<PaneExtra>> PANE_EXTRAS =
		new ModelProperty<>();

	public ContourMultipartModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial,
		Function<BlockState, List<float[]>> boxes,
		Supplier<? extends Block> block) {
		super(original, boundState, boundMaterial, boxes, block);
	}

	/**
	 * Occupant quads for one host model. UV anchoring follows the same
	 * cell-grid rule as the base occupant (absolute pixel coordinates);
	 * each occupant samples its own material.
	 */
	static List<BakedQuad> emitExtras(ContourModel model,
		@Nullable ModelData data, @Nullable Direction side, RandomSource rand,
		@Nullable RenderType renderType) {
		List<BakedQuad> out = new ArrayList<>();
		List<PanelExtra> panels = data == null ? null : data.get(PANEL_EXTRAS);
		if (panels != null)
			for (PanelExtra e : panels)
				out.addAll(ShapeQuads.emitBoxes(
					PanelSlots.occupantBoxes(e.slot(), e.kind()), side,
					d -> model.sourcesFor(e.material(), d, rand,
						ModelData.EMPTY, renderType)));
		List<PaneExtra> panes = data == null ? null : data.get(PANE_EXTRAS);
		if (panes != null)
			for (PaneExtra e : panes)
				out.addAll(ShapeQuads.emitBoxes(
					net.createcontour.block.ContourFlatPaneBlock
						.boxesForAxis(e.axis()),
					side,
					d -> model.sourcesFor(e.material(), d, rand,
						ModelData.EMPTY, renderType)));
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
