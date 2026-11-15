package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import com.simibubi.create.content.decoration.copycat.CopycatModel;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.material.MaterialSafety;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * Shared painted-shape model. Extends Create's CopycatModel but overrides
 * every material-touching surface: quads are emitted from the shape's own
 * 2x2x2 occupancy grid (never a blind crop of the wrapped model), the
 * material state comes from ModelData's MATERIAL_PROPERTY or the bound
 * item material, and every material-model call is guarded so a throwing
 * or absent material model degrades to the neutral mesh, never crashes
 * chunk meshing.
 */
public abstract class ContourModel extends CopycatModel {

	protected final BakedModel original;
	@Nullable
	protected final BlockState boundState;
	@Nullable
	protected final BlockState boundMaterial;
	private ItemOverrides overrides = ItemOverrides.EMPTY;

	/**
	 * Bounded emission cache: chunk meshing calls getQuads many times with
	 * identical (state, side, material, renderType); without a cache each
	 * call re-queries the material model, scans its quads and rebuilds the
	 * whole shape mesh. Only used when no WRAPPED (CTM) data is present;
	 * wrapped-data paths stay uncached so CTM materials stay correct.
	 */
	private record QuadKey(BlockState state, @Nullable Direction side,
		@Nullable BlockState material, @Nullable RenderType renderType) {
	}

	private final com.google.common.cache.Cache<QuadKey, List<BakedQuad>>
		quadCache = com.google.common.cache.CacheBuilder.newBuilder()
			.maximumSize(64).build();

	protected ContourModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original);
		this.original = original;
		this.boundState = boundState;
		this.boundMaterial = boundMaterial;
	}

	/** Occupancy mask (bit per 2x2x2 cell) for the given block state. */
	protected abstract int maskFor(BlockState state);

	/**
	 * Pixel-space AABB list for the given block state; the default derives
	 * one 8px box per occupied 2x2x2 cell (identical output to the mask
	 * path). Shapes with sub-octant geometry override this.
	 */
	protected java.util.List<float[]> boxesFor(BlockState state) {
		return ShapeQuads.boxesForMask(maskFor(state));
	}

	/**
	 * Emit the shape's quads for `side`. UV anchoring is supplied by
	 * ShapeQuads.emitFace: `boxesFor` boxes and `extraQuads` corners are
	 * absolute pixel coordinates on the block's 16x16 face plane, so each
	 * vertex samples the material sprite at its cell-grid position
	 * (ADR-017 texture continuation) - never face-local 0..1.
	 */
	protected List<BakedQuad> emitShape(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		List<BakedQuad> out = new ArrayList<>(
			ShapeQuads.emitBoxes(boxesFor(state), side, srcFor));
		out.addAll(extraQuads(state, side, srcFor));
		return out;
	}

	/**
	 * Non-AABB extra quads (diagonal plates etc.) emitted alongside the
	 * box geometry. Default: none.
	 */
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		return List.of();
	}

	/** Fallback state when neither the render state nor a bound state exists. */
	protected abstract BlockState neutralState();

	void setOverrides(ItemOverrides overrides) {
		this.overrides = overrides;
	}

	@Override
	public ItemOverrides getOverrides() {
		return overrides;
	}

	@Nullable
	private BlockState materialFromData(@Nullable ModelData data) {
		try {
			if (data != null) {
				Object m = data.get(CopycatModel.MATERIAL_PROPERTY);
				if (m instanceof BlockState bs
					&& MaterialSafety.isAccepted(MaterialSafety.normalize(bs))
					&& !MaterialSafety.isNeutral(bs))
					return MaterialSafety.sanitize(bs);
			}
		} catch (RuntimeException e) {
			// bad model data -> bound/neutral
		}
		// Canonical bound-material seam: same validation as the data branch -
		// normalize + isAccepted + never the neutral sentinel (Create's
		// copycat_base must render OUR mesh, never its art).
		if (boundMaterial != null) {
			BlockState n = MaterialSafety.normalize(boundMaterial);
			if (MaterialSafety.isAccepted(n) && !MaterialSafety.isNeutral(n))
				return MaterialSafety.sanitize(n);
		}
		return null;
	}

	/**
	 * Item render passes: return the BOUND model itself (with its resolved
	 * material) - the NF wrapper would delegate to the unbound original.
	 */
	@Override
	public List<BakedModel> getRenderPasses(
		net.minecraft.world.item.ItemStack stack, boolean fabulous) {
		return List.of(this);
	}

	/**
	 * ItemRenderer calls ClientHooks.handleCameraTransforms BEFORE render
	 * passes and uses the RETURNED model. NF's wrapper delegates to the
	 * unbound original, which loses our resolved material; apply the
	 * original's pose transform but keep `this` bound model.
	 */
	@Override
	public BakedModel applyTransform(
		net.minecraft.world.item.ItemDisplayContext context,
		com.mojang.blaze3d.vertex.PoseStack poseStack, boolean leftHand) {
		original.applyTransform(context, poseStack, leftHand);
		return this;
	}

	/** Item render types honor the material item's own passes (e.g. glass). */
	@Override
	public List<RenderType> getRenderTypes(
		net.minecraft.world.item.ItemStack stack, boolean fabulous) {
		BlockState material = materialFromData(ModelData.EMPTY);
		if (material != null) {
			try {
				List<RenderType> types = Minecraft.getInstance().getBlockRenderer()
					.getBlockModel(material)
					.getRenderTypes(new net.minecraft.world.item.ItemStack(
						material.getBlock().asItem()), fabulous);
				if (!types.isEmpty())
					return types;
			} catch (RuntimeException e) {
				// fall through
			}
		}
		try {
			return original.getRenderTypes(stack, fabulous);
		} catch (RuntimeException e) {
			return List.of(net.minecraft.client.renderer.Sheets.cutoutBlockSheet());
		}
	}

	/** Texture sources for one face dir: material quads, or neutral synth. */
	protected List<ShapeQuads.SrcQuad> sourcesFor(@Nullable BlockState material,
		Direction dir, RandomSource rand, ModelData wrappedData,
		@Nullable RenderType renderType) {
		if (material != null) {
			try {
				BakedModel mm = Minecraft.getInstance().getBlockRenderer()
					.getBlockModel(material);
				ModelData md = wrappedData == null ? ModelData.EMPTY : wrappedData;
				if (renderType != null
					&& !mm.getRenderTypes(material, rand, md).contains(renderType))
					return List.of();
				List<BakedQuad> raw = new ArrayList<>(
					mm.getQuads(material, dir, rand, md, renderType));
				for (BakedQuad q : mm.getQuads(material, null, rand, md, renderType))
					if (q.getDirection() == dir)
						raw.add(q);
				if (!raw.isEmpty()) {
					List<ShapeQuads.SrcQuad> out = new ArrayList<>(raw.size());
					for (BakedQuad q : raw)
						out.add(ShapeQuads.extract(q));
					return out;
				}
			} catch (RuntimeException e) {
				// fall through to neutral
			}
		}
		return List.of(ShapeQuads.neutral(dir, original.getParticleIcon()));
	}

	/** Neutral-mesh emission: our shape grid + our own sprite, never throws. */
	private List<BakedQuad> neutralQuads(@Nullable Direction side) {
		try {
			BlockState st = boundState != null ? boundState : neutralState();
			if (st == null)
				return List.of();
			return emitShape(st, side,
				d -> List.of(ShapeQuads.neutral(d, original.getParticleIcon())));
		} catch (RuntimeException e) {
			return List.of();
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
		RandomSource rand, ModelData data, @Nullable RenderType renderType) {
		try {
			if (state == null)
				state = boundState != null ? boundState : neutralState();
			if (state == null)
				return neutralQuads(side);
			ModelData safe = data == null ? ModelData.EMPTY : data;
			ModelData wrapped = safe.get(WRAPPED_PROPERTY) instanceof ModelData w
				? w : ModelData.EMPTY;
			if (wrapped.getProperties().isEmpty()) {
				BlockState material = materialFromData(safe);
				// Issue #19: blended faces depend on the neighbor cells,
				// which the position-less QuadKey cannot key on - skip
				// the cache entirely while a painted Contour neighbor is
				// present (the same cells edge blending targets).
				boolean blend = blendingNeighbor(safe);
				List<BakedQuad> cached = blend ? null : quadCache.getIfPresent(
					new QuadKey(state, side, material, renderType));
				if (cached != null)
					return cached;
				List<BakedQuad> quads = emitShape(state, side,
					d -> sourcesFor(material, d, rand, wrapped, renderType));
				quads = blendBoundaryQuads(quads, side, material,
					d -> sourcesFor(material, d, rand, wrapped, renderType),
					rand, safe, renderType);
				if (!blend)
					quadCache.put(
						new QuadKey(state, side, material, renderType), quads);
				return quads;
			}
			BlockState material = materialFromData(safe);
			return blendBoundaryQuads(
				emitShape(state, side,
					d -> sourcesFor(material, d, rand, wrapped, renderType)),
				side, material,
				d -> sourcesFor(material, d, rand, wrapped, renderType),
				rand, safe, renderType);
		} catch (RuntimeException e) {
			return neutralQuads(side); // never invisible on a failure
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
		RandomSource rand) {
		return getQuads(state, side, rand, ModelData.EMPTY, null);
	}

	public static final net.neoforged.neoforge.client.model.data.ModelProperty<ModelData>
		WRAPPED_PROPERTY = new net.neoforged.neoforge.client.model.data.ModelProperty<>();

	/**
	 * Issue #19 edge blending: per-direction (Direction.get3DDataValue)
	 * painted material of each axial Contour-BE neighbor that presents a
	 * face on the shared boundary plane, or null where the neighbor is
	 * non-Contour, unpainted, or has no coplanar face. Occupant extras
	 * compare their own material against these entries.
	 */
	public static final net.neoforged.neoforge.client.model.data.ModelProperty<BlockState[]>
		NEIGHBOR_MATERIALS = new net.neoforged.neoforge.client.model.data.ModelProperty<>();

	/**
	 * Issue #19: per-direction material model data gathered AT the
	 * same-material Contour neighbor's cell - its own CTM context, so a
	 * blended quad samples the tile region the neighbor's inward face
	 * actually shows ("the neighbor's region").
	 */
	public static final net.neoforged.neoforge.client.model.data.ModelProperty<ModelData[]>
		NEIGHBOR_WRAPPED = new net.neoforged.neoforge.client.model.data.ModelProperty<>();

	/**
	 * Issue #22 universal host: ONE extras list every host-family model
	 * consumes. Each {@link ContourBlockEntity.Occupant} decodes its own
	 * channel geometry via footprint(); the board model additionally
	 * partitions BOARD-kind records into its per-face material map.
	 */
	public static final net.neoforged.neoforge.client.model.data.ModelProperty<
		java.util.List<ContourBlockEntity.Occupant>> OCCUPANT_EXTRAS =
			new net.neoforged.neoforge.client.model.data.ModelProperty<>();

	/**
	 * Issue #19: remap the emitted boundary list for `side` to the
	 * material's opposite-direction face region when the axial Contour
	 * neighbor presents a coplanar face painted the SAME material.
	 * Non-Contour or different-material neighbors fall through with the
	 * issue-#9 anchoring untouched; interior/inset quads are excluded
	 * because callers only pass the boundary `side` list. `srcFor` is
	 * the direction->sources function the quads were emitted with (used
	 * only for the per-face source count).
	 */
	protected final List<BakedQuad> blendBoundaryQuads(List<BakedQuad> quads,
		@Nullable Direction side, @Nullable BlockState quadMaterial,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor,
		RandomSource rand, ModelData data, @Nullable RenderType renderType) {
		try {
			// QA harness knob (test-only): -Dcreate_contour.noEdgeBlend=true
			// disables the issue-#19 seam remap entirely so a fixed-camera
			// before/after capture isolates exactly the blending pixels.
			if (Boolean.getBoolean("create_contour.noEdgeBlend"))
				return quads;
			if (side == null || quadMaterial == null || quads.isEmpty()
				|| MaterialSafety.isNeutral(quadMaterial))
				return quads;
			BlockState[] nm = data == null ? null : data.get(NEIGHBOR_MATERIALS);
			if (nm == null || side.get3DDataValue() >= nm.length)
				return quads;
			BlockState nmat = nm[side.get3DDataValue()];
			if (nmat == null || !MaterialSafety.normalize(nmat)
				.equals(MaterialSafety.normalize(quadMaterial)))
				return quads;
			ModelData[] nd = data.get(NEIGHBOR_WRAPPED);
			ModelData ndata = nd != null && side.get3DDataValue() < nd.length
				? nd[side.get3DDataValue()] : null;
			List<ShapeQuads.SrcQuad> blendSrcs = sourcesFor(quadMaterial,
				side.getOpposite(), rand,
				ndata != null ? ndata : ModelData.EMPTY, renderType);
			if (blendSrcs.isEmpty())
				return quads;
			return ShapeQuads.remapSources(quads,
				srcFor.apply(side).size(), blendSrcs);
		} catch (RuntimeException e) {
			return quads; // blending is cosmetic; never break the mesh
		}
	}

	/** Any painted Contour neighbor at all (cache bypass signal). */
	private static boolean blendingNeighbor(@Nullable ModelData data) {
		BlockState[] nm = data == null ? null : data.get(NEIGHBOR_MATERIALS);
		if (nm == null)
			return false;
		for (BlockState s : nm)
			if (s != null)
				return true;
		return false;
	}

	/**
	 * Coplanarity gate (ADR-017 shared lattice plane): does the Contour
	 * cell at `np` emit any face on `face`'s boundary plane? Probed via
	 * the neighbor model's own emission with a neutral source so the
	 * check follows render geometry, not collision; extra occupants
	 * (issue #12 channels) count through the BE's model-data lists.
	 */
	private static boolean neighborPresentsFace(BlockAndTintGetter level,
		BlockPos np, Direction face) {
		try {
			BlockState ns = level.getBlockState(np);
			if (!(Minecraft.getInstance().getBlockRenderer().getBlockModel(ns)
				instanceof ContourModel cm))
				return false;
			if (!cm.emitShape(ns, face,
				dd -> List.of(ShapeQuads.neutral(dd,
					cm.original.getParticleIcon()))).isEmpty())
				return true;
			// Occupant extras reach the shared plane outside the host
			// shape's footprint (byte octants, panel slots, pane sheets,
			// board shells - issue #22's unified list decodes each kind's
			// own boxes).
			ModelData nd = level.getModelData(np);
			if (nd == null)
				return false;
			List<ContourBlockEntity.Occupant> extras =
				nd.get(OCCUPANT_EXTRAS);
			if (extras != null)
				for (ContourBlockEntity.Occupant e : extras)
					for (float[] b : e.footprint())
						if (boxReachesPlane(b, face))
							return true;
			return false;
		} catch (RuntimeException e) {
			return false;
		}
	}

	/** Does an 0..16 px AABB touch the boundary plane `face` exits on? */
	private static boolean boxReachesPlane(float[] b, Direction face) {
		return switch (face.getAxis()) {
			case X -> face.getStepX() > 0 ? b[3] >= 16f - 1e-4
				: b[0] <= 1e-4;
			case Y -> face.getStepY() > 0 ? b[4] >= 16f - 1e-4
				: b[1] <= 1e-4;
			case Z -> face.getStepZ() > 0 ? b[5] >= 16f - 1e-4
				: b[2] <= 1e-4;
		};
	}

	@Override
	public net.neoforged.neoforge.client.ChunkRenderTypeSet getRenderTypes(
		BlockState state, RandomSource rand, ModelData data) {
		BlockState material = materialFromData(data);
		ModelData wrapped = data != null
			&& data.get(WRAPPED_PROPERTY) instanceof ModelData w ? w : ModelData.EMPTY;
		if (material != null) {
			try {
				return Minecraft.getInstance().getBlockRenderer()
					.getBlockModel(material).getRenderTypes(material, rand, wrapped);
			} catch (RuntimeException e) {
				// neutral path below
			}
		}
		try {
			return original.getRenderTypes(state, rand, data);
		} catch (RuntimeException e) {
			return net.neoforged.neoforge.client.ChunkRenderTypeSet
				.of(RenderType.solid());
		}
	}

	/** Per-shape hook required by CopycatModel: grid emission for a material. */
	@Override
	protected List<BakedQuad> getCroppedQuads(BlockState state, Direction side,
		RandomSource rand, BlockState material, ModelData wrappedData,
		RenderType renderType) {
		try {
			if (state == null)
				state = boundState != null ? boundState : neutralState();
			if (state == null)
				return neutralQuads(side);
			return emitShape(state, side,
				d -> sourcesFor(
					material != null
						&& MaterialSafety.isAccepted(MaterialSafety.normalize(material))
							? MaterialSafety.sanitize(material) : null,
					d, rand, wrappedData, renderType));
		} catch (RuntimeException e) {
			return neutralQuads(side);
		}
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		BlockState material = materialFromData(data);
		if (material != null) {
			try {
				return Minecraft.getInstance().getBlockRenderer()
					.getBlockModel(material).getParticleIcon(data);
			} catch (RuntimeException e) {
				// neutral fallback
			}
		}
		return original.getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return boundMaterial != null
			? getParticleIcon(ModelData.EMPTY) : original.getParticleIcon();
	}

	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder,
		BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
		try {
			ModelData.Builder b = super.gatherModelData(builder, level, pos, state, data);
			// Attach the material model's own wrapped data for CTM-aware
			// materials (guarded; absent on failure).
			Object m = b.build().get(CopycatModel.MATERIAL_PROPERTY);
			if (m instanceof BlockState bs
				&& MaterialSafety.isAccepted(MaterialSafety.normalize(bs))) {
				try {
					ModelData md = Minecraft.getInstance().getBlockRenderer()
						.getBlockModel(bs).getModelData(level, pos, bs, data);
					if (md != null)
						b = b.with(WRAPPED_PROPERTY, md);
				} catch (RuntimeException ignored) {
				}
			}
			// Issue #19 edge blending: probe the six axial neighbors for
			// painted ContourBE cells that present a coplanar face on the
			// shared lattice plane. Runs regardless of host paint so
			// painted occupant extras on an unpainted host still blend;
			// host quads compare their own material and no-match entries
			// simply never remap. NEIGHBOR_MATERIALS records each painted
			// Contour neighbor's material; NEIGHBOR_WRAPPED carries the
			// NEIGHBOR's own material-model data gathered at its cell
			// (its CTM context -> our quads sample the actual tile the
			// neighbor's inward face shows). The same-material gate in
			// blendBoundaryQuads consumes nd[i] only when it applies.
			try {
				BlockState[] nm = null;
				ModelData[] nd = null;
				for (Direction dir : Direction.values()) {
					int i = dir.get3DDataValue();
					try {
						BlockPos np = pos.relative(dir);
						if (!(level.getBlockEntity(np)
							instanceof ContourBlockEntity cbe))
							continue;
						BlockState nmat = MaterialSafety.sanitize(
							cbe.getMaterial());
						if (MaterialSafety.isNeutral(nmat)
							|| !MaterialSafety.isAccepted(nmat)
							|| !neighborPresentsFace(level, np,
								dir.getOpposite()))
							continue;
						if (nm == null)
							nm = new BlockState[6];
						nm[i] = nmat;
						ModelData w = Minecraft.getInstance()
							.getBlockRenderer().getBlockModel(nmat)
							.getModelData(level, np, nmat, ModelData.EMPTY);
						if (w != null) {
							if (nd == null)
								nd = new ModelData[6];
							nd[i] = w;
						}
					} catch (RuntimeException ignored) {
					}
				}
				if (nm != null)
					b = b.with(NEIGHBOR_MATERIALS, nm);
				if (nd != null)
					b = b.with(NEIGHBOR_WRAPPED, nd);
			} catch (RuntimeException ignored) {
			}
			return b;
		} catch (RuntimeException e) {
			return builder.with(CopycatModel.MATERIAL_PROPERTY,
				MaterialSafety.neutral());
		}
	}
}
