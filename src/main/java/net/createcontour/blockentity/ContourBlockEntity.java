package net.createcontour.blockentity;

import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.content.decoration.copycat.CopycatModel;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.createcontour.block.ContourBlock;
import net.createcontour.config.ContourConfig;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Contour copycat block entity.
 *
 * Create's base keeps `material`/`consumedItem` PRIVATE, so we cannot
 * attach a paid-unit ledger, a schema version, or canonical orientation
 * to those fields. Instead this class owns its own material, paidUnits,
 * orientation and schema fields and overrides the whole material /
 * accounting surface so the inherited private fields are never used.
 * super.read/write calls still run to preserve SmartBlockEntity
 * infrastructure; they only handle base keys we overwrite with ours.
 */
public class ContourBlockEntity extends CopycatBlockEntity {

	public static final int SCHEMA_VERSION = 1;

	private BlockState material = MaterialSafety.neutral();
	private int paidUnits;
	private Direction facing = Direction.NORTH;
	private boolean top;
	private CompoundTag unknownTag; // future-version payload, preserved raw
	private boolean frozen;         // unknown future version: no mutations

	public ContourBlockEntity(BlockPos pos, BlockState state) {
		// Type resolves lazily at BE creation, after registration binds.
		this(net.createcontour.registry.ContourRegistries.CONTOUR_BE.get(), pos, state);
	}

	/** Subtype ctor for shapes carrying extra BE fields (sign text). */
	protected ContourBlockEntity(
		net.minecraft.world.level.block.entity.BlockEntityType<?> type,
		BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	// --- material surface (owned) ---

	@Override
	public BlockState getMaterial() {
		return material;
	}

	@Override
	public boolean hasCustomMaterial() {
		return !MaterialSafety.isNeutral(material);
	}

	@Override
	public void setMaterial(BlockState blockState) {
		if (frozen)
			return;
		material = MaterialSafety.sanitize(blockState);
		afterOwnedMutation();
	}

	@Override
	public boolean cycleMaterial() {
		return false;
	}

	public int getPaidUnits() {
		return paidUnits;
	}

	/** Paid accounting is ours; the base consumedItem is never currency. */
	public void setPaidUnits(int units) {
		if (frozen)
			return;
		paidUnits = Math.max(0, Math.min(units, hostUnits()));
		if (MaterialSafety.isNeutral(material))
			paidUnits = 0;
		notifyUpdate();
	}

	@Override
	public void setConsumedItem(ItemStack stack) {
		if (frozen)
			return;
		if (!stack.isEmpty())
			setPaidUnits(paidUnits + 1);
	}

	/** Consumed-item hint is never economic currency: always empty. */
	@Override
	public ItemStack getConsumedItem() {
		return ItemStack.EMPTY;
	}

	public Direction getFacing() {
		return facing;
	}

	public boolean isTop() {
		return top;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setCanonical(Direction f, boolean t) {
		if (frozen)
			return;
		facing = (f != null && f.getAxis().isHorizontal()) ? f : Direction.NORTH;
		top = t;
	}

	public int hostUnits() {
		return getBlockState().getBlock() instanceof ContourBlock cb
			? cb.hostUnits(getBlockState()) : 1;
	}

	/**
	 * Owned appearance refresh: aux light + light engine +, on the client,
	 * model-data invalidation AND the levelRenderer remesh trigger. The
	 * last one is what our stripped super.read cannot reach (Create's
	 * private redraw), which is why painted material used to stay gray on
	 * the client until a manual reload.
	 */
	private void refreshMaterialAppearance() {
		if (level == null)
			return;
		try {
			level.getAuxLightManager(getBlockPos()).setLightAt(getBlockPos(),
				hasCustomMaterial()
					? Math.min(15, material.getLightEmission(level, getBlockPos())) : 0);
		} catch (RuntimeException ignored) {
		}
		level.getLightEngine().checkBlock(getBlockPos());
		if (level.isClientSide) {
			if (!isVirtual())
				requestModelDataUpdate();
			level.sendBlockUpdated(getBlockPos(), getBlockState(),
				getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	/** Sync + light + model-data refresh after owned fields settled. */
	private void afterOwnedMutation() {
		if (!MaterialSafety.isNeutral(material) || paidUnits != 0)
			paidUnits = MaterialSafety.isNeutral(material) ? 0
				: Math.min(paidUnits, hostUnits());
		notifyUpdate();
		if (level != null) {
			refreshMaterialAppearance();
			if (!level.isClientSide) {
				level.blockUpdated(getBlockPos(), getBlockState().getBlock());
				level.sendBlockUpdated(getBlockPos(), getBlockState(),
					getBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	public void applyPayload(BlockState payloadMaterial, int payloadPaid,
		Direction payloadFacing, boolean payloadTop) {
		if (frozen)
			return;
		material = MaterialSafety.sanitize(payloadMaterial);
		setCanonical(payloadFacing, payloadTop);
		setPaidUnits(payloadPaid);
		afterOwnedMutation();
	}

	// --- schematic/requirement surface ---

	/**
	 * Schematic checklist contribution, UNIONED by Create with its default
	 * host-item entry (1x block item, or 2x for SLAB_TYPE=DOUBLE). We add:
	 * the copied material at one unit per host unit (consume-on only), plus
	 * any extra host units Create's default does not cover (multi-unit
	 * hosts like LAYERS/SLICE counts beyond the slab-double special case).
	 */
	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
		if (hasCustomMaterial() && ContourConfig.consumeMaterial()
			&& !material.getBlock().asItem().getDefaultInstance().isEmpty()) {
			stacks.add(new ItemStack(material.getBlock().asItem(), hostUnits()));
		}
		int defaultUnits =
			state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
				&& state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
					== net.minecraft.world.level.block.state.properties.SlabType.DOUBLE ? 2 : 1;
		int extraUnits = hostUnits() - defaultUnits;
		if (extraUnits > 0
			&& !state.getBlock().asItem().getDefaultInstance().isEmpty()) {
			stacks.add(new ItemStack(state.getBlock().asItem(), extraUnits));
		}
		if (stacks.isEmpty())
			return ItemRequirement.NONE;
		return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, stacks);
	}

	@Override
	public void transform(BlockEntity be, StructureTransform transform) {
		if (frozen)
			return;
		if (hasCustomMaterial() && ContourConfig.rotateTextures())
			material = transform.apply(material);
		facing = transform.mirrorFacing(transform.rotation.rotate(facing));
		notifyUpdate();
	}

	// --- persistence ---

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
		// Extract OUR keys first from the RAW tag; the base then receives a
		// sanitized copy so untrusted owned payloads can never reach its
		// private decode path (and future versions freeze before it runs).
		int version = tag.getInt("ContourVersion");
		if (version > SCHEMA_VERSION) {
			// Preserve the RAW unknown payload untouched, but reset the
			// operational view: a frozen BE shows neutral/no-credit, never
			// a stale pre-load material.
			unknownTag = tag.copy();
			frozen = true;
			material = MaterialSafety.neutral();
			paidUnits = 0;
			facing = Direction.NORTH;
			top = false;
			CompoundTag base = tag.copy();
			base.remove("Material");
			base.remove("Item");
			super.read(base, provider, clientPacket);
			if (level != null)
				refreshMaterialAppearance();
			return;
		}
		frozen = false;
		unknownTag = null;
		BlockState parsedMaterial = parseMaterial(tag, provider);
		Direction parsedFacing = parseFacing(tag);
		boolean parsedTop = tag.getBoolean("Top");
		int parsedPaid = Math.max(0, tag.getInt("PaidUnits"));
		CompoundTag base = tag.copy();
		base.remove("Material");
		base.remove("Item");
		super.read(base, provider, clientPacket);
		material = parsedMaterial;
		facing = parsedFacing;
		top = parsedTop;
		paidUnits = Math.min(parsedPaid, hostUnits());
		if (MaterialSafety.isNeutral(material))
			paidUnits = 0;
		if (level != null)
			refreshMaterialAppearance();
	}

	/**
	 * Material decode uses the supplied HolderLookup (registry-driven),
	 * never the possibly-null level; malformed/air/unknown-registry input
	 * degrades to neutral without throwing.
	 */
	private BlockState parseMaterial(CompoundTag tag, HolderLookup.Provider provider) {
		if (!tag.contains("Material"))
			return MaterialSafety.neutral();
		try {
			HolderLookup<Block> blocks = provider.lookupOrThrow(Registries.BLOCK);
			BlockState parsed = NbtUtils.readBlockState(blocks, tag.getCompound("Material"));
			return MaterialSafety.sanitize(parsed);
		} catch (RuntimeException e) {
			return MaterialSafety.neutral();
		}
	}

	private static Direction parseFacing(CompoundTag tag) {
		if (tag.contains("Facing")) {
			try {
				Direction d = Direction.valueOf(tag.getString("Facing"));
				return d.getAxis().isHorizontal() ? d : Direction.NORTH;
			} catch (IllegalArgumentException ignored) {
			}
		}
		return Direction.NORTH;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
		if (frozen && unknownTag != null) {
			// Re-emit the unknown future payload untouched; no downwrite.
			tag.merge(unknownTag.copy());
			return;
		}
		super.write(tag, provider, clientPacket);
		tag.putInt("ContourVersion", SCHEMA_VERSION);
		tag.put("Material", NbtUtils.writeBlockState(material));
		tag.putInt("PaidUnits", paidUnits);
		tag.putString("Facing", facing.name());
		tag.putBoolean("Top", top);
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider provider) {
		if (frozen) {
			// Refuse schematic export of raw unknown data: the paid ledger
			// would otherwise leak as reusable currency. Emit a neutralized
			// safe projection only; the raw payload still survives save.
			tag.putInt("ContourVersion", SCHEMA_VERSION);
			tag.put("Material", NbtUtils.writeBlockState(MaterialSafety.neutral()));
			tag.putInt("PaidUnits", 0);
			return;
		}
		super.writeSafe(tag, provider);
		tag.putInt("ContourVersion", SCHEMA_VERSION);
		tag.put("Material", NbtUtils.writeBlockState(material));
		tag.putString("Facing", facing.name());
		tag.putBoolean("Top", top);
		// Paid is exported only as the ledger the schematic's own requirement
		// will consume at print (material x hostUnits under consume-on): the
		// printed block may then refund exactly what the cannon charged.
		// Consume-off exports no credit, so a free paint can never become a
		// refundable material source. Item hints are always stripped.
		tag.putInt("PaidUnits", hasCustomMaterial()
			&& ContourConfig.consumeMaterial() ? hostUnits() : 0);
		tag.remove("Item");
	}

	// --- model data (guarded behind Create's final getModelData seam) ---

	@Override
	public ModelData getModelData() {
		return ModelData.builder()
			.with(CopycatModel.MATERIAL_PROPERTY, material)
			.build();
	}

	// --- lifecycle ---

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null) {
			refreshMaterialAppearance();
			sendData();
		}
	}

	// Defined WITHOUT @Override: Create 6.0.8's CBE does not implement
	// Clearable; 6.0.10 does. super.clearContent() is intentionally not
	// called - base private fields are never used by our owned surface, so
	// leaving them is inert while staying compile-portable across profiles.
	public void clearContent() {
		// Never destroy preserved unknown-version data without user action.
		if (frozen)
			return;
		material = MaterialSafety.neutral();
		paidUnits = 0;
		facing = Direction.NORTH;
		top = false;
		afterOwnedMutation();
	}
}
