package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourBoardBlock;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * Board model (issue #24/#22): emits one 1px face-shell box per
 * present face flag, each sampling its own material from the unified
 * OCCUPANT_EXTRAS list's BOARD-kind records (one per face); records
 * missing or neutral emit the neutral mesh. Non-BOARD occupants on
 * the cell emit through the shared extras path. Without extras data -
 * the bound item render or an uninstrumented call - the
 * uniform-material path renders the whole state like any other shape.
 */
public class ContourBoardModel extends ContourModel {

	public ContourBoardModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		// Display fallback: the all-false default is the blockstate
		// contract but never exists in-world (placement mounts one
		// sheet, the wrench removes the block on the last face). The
		// unbound/item render binds that default and would emit an
		// invisible mesh, so a faceless state shows the fully populated
		// shell - the same "every face" icon glow-lichen reads as.
		if (ContourBoardBlock.faceCount(state) > 0)
			return ContourBoardBlock.boxesFor(state);
		List<float[]> all = new ArrayList<>();
		for (Direction d : Direction.values())
			all.addAll(ContourBoardBlock.boxesForFace(d));
		return all;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_BOARD.get().defaultBlockState();
	}

	/** Accepted non-neutral material, else null (neutral emission). */
	@Nullable
	private static BlockState validMaterial(@Nullable BlockState m) {
		if (m == null)
			return null;
		BlockState n = MaterialSafety.normalize(m);
		return MaterialSafety.isAccepted(n) && !MaterialSafety.isNeutral(n)
			? MaterialSafety.sanitize(n) : null;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state,
		@Nullable Direction side, RandomSource rand, ModelData data,
		@Nullable RenderType renderType) {
		List<net.createcontour.blockentity.ContourBlockEntity.Occupant> extras =
			data == null ? null : data.get(ContourModel.OCCUPANT_EXTRAS);
		if (extras == null || extras.isEmpty() || state == null)
			return super.getQuads(state, side, rand, data, renderType);
		try {
			BlockState[] mats = new BlockState[6];
			for (net.createcontour.blockentity.ContourBlockEntity.Occupant e
				: extras)
				if (e.kind() == net.createcontour.blockentity
					.ContourBlockEntity.OccupantKind.BOARD
					&& e.slot() >= 0 && e.slot() < 6)
					mats[e.slot()] = validMaterial(e.material());
			// Neutral faces emit only in the passes the unbound model
			// would use; material faces are renderType-filtered by
			// sourcesFor itself. Without this gate a neutral face would
			// double-emit in any extra translucent pass another face
			// brought into the set.
			ChunkRenderTypeSet neutralTypes;
			try {
				neutralTypes = original.getRenderTypes(state, rand, data);
			} catch (RuntimeException e) {
				neutralTypes = ChunkRenderTypeSet.of(RenderType.solid());
			}
			List<BakedQuad> out = new ArrayList<>();
			for (Direction d : Direction.values()) {
				if (!ContourBoardBlock.facePresent(state, d))
					continue;
				BlockState m = mats[d.ordinal()];
				if (m == null && renderType != null
					&& !neutralTypes.contains(renderType))
					continue;
				out.addAll(ShapeQuads.emitBoxes(
					ContourBoardBlock.boxesForFace(d), side,
					dir -> sourcesFor(m, dir, rand, ModelData.EMPTY,
						renderType)));
			}
			// issue #22: non-BOARD occupants on this cell emit through
			// the shared extras path (BOARD records were rendered by the
			// per-face loop above with its neutral-pass gate).
			out.addAll(ContourMultipartModel.emitExtras(this, data, side,
				rand, renderType,
				net.createcontour.blockentity.ContourBlockEntity
					.OccupantKind.BOARD));
			return out;
		} catch (RuntimeException e) {
			return super.getQuads(state, side, rand, data, renderType);
		}
	}

	/**
	 * Chunk render types are the UNION of the host type set and every
	 * painted occupant's material set - otherwise a translucent-painted
	 * sheet or interior occupant would never get a pass.
	 */
	@Override
	public ChunkRenderTypeSet getRenderTypes(BlockState state,
		RandomSource rand, ModelData data) {
		ChunkRenderTypeSet set = super.getRenderTypes(state, rand, data);
		List<net.createcontour.blockentity.ContourBlockEntity.Occupant> extras =
			data == null ? null : data.get(ContourModel.OCCUPANT_EXTRAS);
		if (extras == null || extras.isEmpty())
			return set;
		List<ChunkRenderTypeSet> sets = new ArrayList<>();
		sets.add(set);
		for (net.createcontour.blockentity.ContourBlockEntity.Occupant e
			: extras) {
			BlockState m = validMaterial(e.material());
			if (m == null)
				continue;
			try {
				sets.add(Minecraft.getInstance().getBlockRenderer()
					.getBlockModel(m)
					.getRenderTypes(m, rand, ModelData.EMPTY));
			} catch (RuntimeException ignored) {
			}
		}
		return sets.size() == 1 ? set : ChunkRenderTypeSet.union(sets);
	}
}
