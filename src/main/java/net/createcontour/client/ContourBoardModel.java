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
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Board model (issue #24): emits one 1px face-shell box per present
 * face flag. Unlike the host-material shapes, EVERY face samples its
 * own material from the BOARD_EXTRAS model-data list (one record per
 * face); records missing or neutral emit the neutral mesh. Without
 * extras data - the bound item render or an uninstrumented call - the
 * uniform-material path renders the whole state like any other shape.
 */
public class ContourBoardModel extends ContourModel {

	/** Per-face paint: face ordinal + that sheet's material. */
	public record BoardExtra(int face, BlockState material) {
	}

	public static final ModelProperty<List<BoardExtra>> BOARD_EXTRAS =
		new ModelProperty<>();

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
		List<BoardExtra> faces = data == null ? null : data.get(BOARD_EXTRAS);
		if (faces == null || faces.isEmpty() || state == null)
			return super.getQuads(state, side, rand, data, renderType);
		try {
			BlockState[] mats = new BlockState[6];
			for (BoardExtra e : faces)
				if (e.face() >= 0 && e.face() < 6)
					mats[e.face()] = validMaterial(e.material());
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
			return out;
		} catch (RuntimeException e) {
			return super.getQuads(state, side, rand, data, renderType);
		}
	}

	/**
	 * Chunk render types are the UNION of the host type set and every
	 * painted face's material set - otherwise a translucent-painted
	 * face (all faces live in this channel) would never get a pass.
	 */
	@Override
	public ChunkRenderTypeSet getRenderTypes(BlockState state,
		RandomSource rand, ModelData data) {
		ChunkRenderTypeSet set = super.getRenderTypes(state, rand, data);
		List<BoardExtra> faces = data == null ? null : data.get(BOARD_EXTRAS);
		if (faces == null || faces.isEmpty())
			return set;
		List<ChunkRenderTypeSet> sets = new ArrayList<>();
		sets.add(set);
		for (BoardExtra e : faces) {
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
