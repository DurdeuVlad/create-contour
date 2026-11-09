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
			? cb.hostUnits(getBlockState()) + byteOccupants.size()
				+ panelOccupants.size() + paneOccupants.size() : 1;
	}

	// --- issue #4: extra Byte occupants (occupant #0 stays in
	// blockstate OCTANT + material + paidUnits, so old saves are
	// byte-for-byte compatible) ---

	public record ByteOccupant(int octant, BlockState material, int paid) {
	}

	private final java.util.List<ByteOccupant> byteOccupants =
		new java.util.ArrayList<>();

	public java.util.List<ByteOccupant> getByteOccupants() {
		return java.util.Collections.unmodifiableList(byteOccupants);
	}

	public boolean addByteOccupant(int octant, BlockState mat, int paid) {
		if (frozen || occupiedOctants().contains(octant))
			return false;
		byteOccupants.add(new ByteOccupant(octant,
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	public int byteOccupantIndex(int octant) {
		for (int i = 0; i < byteOccupants.size(); i++)
			if (byteOccupants.get(i).octant() == octant)
				return i;
		return -1;
	}

	public boolean paintByteOccupant(int idx, BlockState mat, int paid) {
		if (frozen || idx < 0 || idx >= byteOccupants.size())
			return false;
		ByteOccupant old = byteOccupants.get(idx);
		byteOccupants.set(idx, new ByteOccupant(old.octant(),
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	public java.util.Set<Integer> occupiedOctants() {
		java.util.Set<Integer> s = new java.util.HashSet<>();
		s.add(getBlockState().hasProperty(
			net.createcontour.block.ContourByteBlock.OCTANT)
				? getBlockState().getValue(
					net.createcontour.block.ContourByteBlock.OCTANT)
						.ordinal()
				: -1);
		for (ByteOccupant o : byteOccupants)
			s.add(o.octant());
		return s;
	}

	// --- issue #12: panel face-quadrant channel (occupant #0 stays in
	// the host blockstate; extras carry a slot anchor + kind) ---

	/**
	 * Extra panel-family occupant: slot = face*4 + kind-specific sub
	 * (byte-panel quadrant / half-id / 0 for full-face); kind is one of
	 * PanelSlots.KIND_*. The slot footprint expands per kind.
	 */
	public record PanelOccupant(int slot, int kind, BlockState material,
		int paid) {
	}

	private final java.util.List<PanelOccupant> panelOccupants =
		new java.util.ArrayList<>();

	public java.util.List<PanelOccupant> getPanelOccupants() {
		return java.util.Collections.unmodifiableList(panelOccupants);
	}

	/** Union of every claimed face-quadrant slot (host + extras). */
	public java.util.Set<Integer> occupiedPanelSlots() {
		java.util.Set<Integer> s =
			net.createcontour.block.PanelSlots.stateSlots(getBlockState());
		for (PanelOccupant o : panelOccupants)
			s.addAll(net.createcontour.block.PanelSlots.slots(o.slot(),
				o.kind()));
		return s;
	}

	public boolean panelSlotsFree(java.util.Set<Integer> claim) {
		return java.util.Collections.disjoint(occupiedPanelSlots(), claim);
	}

	public boolean addPanelOccupant(int slot, int kind, BlockState mat,
		int paid) {
		if (frozen || kind < 0 || slot < 0 || slot >= 24)
			return false;
		if (!panelSlotsFree(
			net.createcontour.block.PanelSlots.slots(slot, kind)))
			return false;
		panelOccupants.add(new PanelOccupant(slot, kind,
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	/** Extra-occupant index whose footprint contains faceSlot, or -1. */
	public int panelOccupantIndex(int faceSlot) {
		for (int i = 0; i < panelOccupants.size(); i++) {
			PanelOccupant o = panelOccupants.get(i);
			if (net.createcontour.block.PanelSlots
				.slots(o.slot(), o.kind()).contains(faceSlot))
				return i;
		}
		return -1;
	}

	public boolean paintPanelOccupant(int idx, BlockState mat, int paid) {
		if (frozen || idx < 0 || idx >= panelOccupants.size())
			return false;
		PanelOccupant old = panelOccupants.get(idx);
		panelOccupants.set(idx, new PanelOccupant(old.slot(), old.kind(),
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	// --- issue #12: interior flat-pane channel (axis mid-planes) ---

	/**
	 * Extra flat-pane occupant: axis 0/1/2 = X/Y/Z mid-plane sheet.
	 * Occupant #0 is always the blockstate Y sheet; same-axis is
	 * exclusive (coincident planes), different axes cross freely.
	 */
	public record PaneOccupant(int axis, BlockState material, int paid) {
	}

	private final java.util.List<PaneOccupant> paneOccupants =
		new java.util.ArrayList<>();

	public java.util.List<PaneOccupant> getPaneOccupants() {
		return java.util.Collections.unmodifiableList(paneOccupants);
	}

	/** Occupied interior-plane axes (host blockstate + extras). */
	public java.util.Set<Integer> occupiedPaneAxes() {
		java.util.Set<Integer> s = new java.util.HashSet<>();
		if (getBlockState().getBlock()
			instanceof net.createcontour.block.ContourFlatPaneBlock)
			s.add(Direction.Axis.Y.ordinal());
		for (PaneOccupant o : paneOccupants)
			s.add(o.axis());
		return s;
	}

	public boolean addPaneOccupant(int axis, BlockState mat, int paid) {
		if (frozen || axis < 0 || axis > 2
			|| occupiedPaneAxes().contains(axis))
			return false;
		paneOccupants.add(new PaneOccupant(axis,
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	public int paneOccupantIndex(int axis) {
		for (int i = 0; i < paneOccupants.size(); i++)
			if (paneOccupants.get(i).axis() == axis)
				return i;
		return -1;
	}

	public boolean paintPaneOccupant(int idx, BlockState mat, int paid) {
		if (frozen || idx < 0 || idx >= paneOccupants.size())
			return false;
		PaneOccupant old = paneOccupants.get(idx);
		paneOccupants.set(idx, new PaneOccupant(old.axis(),
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
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
		int blockUnits = state.getBlock()
			instanceof net.createcontour.block.ContourBlock cb2
				? cb2.hostUnits(state) : 1;
		if (hasCustomMaterial() && ContourConfig.consumeMaterial()
			&& !material.getBlock().asItem().getDefaultInstance().isEmpty()) {
			stacks.add(new ItemStack(material.getBlock().asItem(), blockUnits));
		}
		if (ContourConfig.consumeMaterial()) {
			for (ByteOccupant o : byteOccupants)
				if (o.material() != null && !MaterialSafety.isNeutral(o.material())
					&& !o.material().getBlock().asItem().getDefaultInstance()
						.isEmpty())
					stacks.add(new ItemStack(o.material().getBlock().asItem(), 1));
			for (PanelOccupant o : panelOccupants)
				if (o.material() != null && !MaterialSafety.isNeutral(o.material())
					&& !o.material().getBlock().asItem().getDefaultInstance()
						.isEmpty())
					stacks.add(new ItemStack(o.material().getBlock().asItem(), 1));
			for (PaneOccupant o : paneOccupants)
				if (o.material() != null && !MaterialSafety.isNeutral(o.material())
					&& !o.material().getBlock().asItem().getDefaultInstance()
						.isEmpty())
					stacks.add(new ItemStack(o.material().getBlock().asItem(), 1));
		}
		int defaultUnits =
			state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
				&& state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
					== net.minecraft.world.level.block.state.properties.SlabType.DOUBLE ? 2 : 1;
		// Host-item extras cover own-kind occupants only (bytes); panel
		// and pane occupants each require their own kind's blank item.
		int extraUnits = hostUnits() - panelOccupants.size()
			- paneOccupants.size() - defaultUnits;
		if (extraUnits > 0
			&& !state.getBlock().asItem().getDefaultInstance().isEmpty()) {
			stacks.add(new ItemStack(state.getBlock().asItem(), extraUnits));
		}
		for (PanelOccupant o : panelOccupants)
			stacks.add(new ItemStack(
				net.createcontour.block.PanelSlots.itemForKind(o.kind())));
		for (PaneOccupant o : paneOccupants)
			stacks.add(new ItemStack(net.createcontour.registry
				.ContourRegistries.CONTOUR_FLAT_PANE_ITEM.get()));
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
		byteOccupants.clear();
		if (tag.contains("ByteExtras")) {
			for (net.minecraft.nbt.Tag t : tag.getList("ByteExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND)) {
				CompoundTag e = (CompoundTag) t;
				int o = e.getInt("Octant");
				if (o >= 0 && o < 8) {
					try {
						byteOccupants.add(new ByteOccupant(o,
							MaterialSafety.sanitize(NbtUtils.readBlockState(
								provider.lookupOrThrow(Registries.BLOCK),
								e.getCompound("Material"))),
							Math.max(0, e.getInt("Paid"))));
					} catch (RuntimeException ignored) {
					}
				}
			}
		}
		panelOccupants.clear();
		if (tag.contains("PanelExtras")) {
			java.util.Set<Integer> claimed =
				new java.util.HashSet<>(occupiedPanelSlots());
			for (net.minecraft.nbt.Tag t : tag.getList("PanelExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND)) {
				CompoundTag e = (CompoundTag) t;
				int slot = e.getInt("Slot"), kind = e.getInt("Kind");
				java.util.Set<Integer> fp = e.contains("Slot")
					&& e.contains("Kind") && kind >= 0 && kind <= 2
					&& slot >= 0 && slot < 24
						? net.createcontour.block.PanelSlots.slots(slot, kind)
						: java.util.Set.of();
				// malformed or slot-conflicting entries are dropped, not
				// trusted (footprints are disjointness-checked on load).
				if (fp.isEmpty() || !java.util.Collections.disjoint(claimed, fp))
					continue;
				try {
					panelOccupants.add(new PanelOccupant(slot, kind,
						MaterialSafety.sanitize(NbtUtils.readBlockState(
							provider.lookupOrThrow(Registries.BLOCK),
							e.getCompound("Material"))),
						Math.max(0, e.getInt("Paid"))));
					claimed.addAll(fp);
				} catch (RuntimeException ignored) {
				}
			}
		}
		paneOccupants.clear();
		if (tag.contains("PaneExtras")) {
			java.util.Set<Integer> claimed =
				new java.util.HashSet<>(occupiedPaneAxes());
			for (net.minecraft.nbt.Tag t : tag.getList("PaneExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND)) {
				CompoundTag e = (CompoundTag) t;
				int axis = e.getInt("Axis");
				if (axis < 0 || axis > 2 || claimed.contains(axis))
					continue;
				try {
					paneOccupants.add(new PaneOccupant(axis,
						MaterialSafety.sanitize(NbtUtils.readBlockState(
							provider.lookupOrThrow(Registries.BLOCK),
							e.getCompound("Material"))),
						Math.max(0, e.getInt("Paid"))));
					claimed.add(axis);
				} catch (RuntimeException ignored) {
				}
			}
		}
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
		if (!byteOccupants.isEmpty()) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (ByteOccupant o : byteOccupants) {
				CompoundTag e = new CompoundTag();
				e.putInt("Octant", o.octant());
				e.put("Material", NbtUtils.writeBlockState(o.material()));
				e.putInt("Paid", o.paid());
				list.add(e);
			}
			tag.put("ByteExtras", list);
		}
		if (!panelOccupants.isEmpty()) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (PanelOccupant o : panelOccupants) {
				CompoundTag e = new CompoundTag();
				e.putInt("Slot", o.slot());
				e.putInt("Kind", o.kind());
				e.put("Material", NbtUtils.writeBlockState(o.material()));
				e.putInt("Paid", o.paid());
				list.add(e);
			}
			tag.put("PanelExtras", list);
		}
		if (!paneOccupants.isEmpty()) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (PaneOccupant o : paneOccupants) {
				CompoundTag e = new CompoundTag();
				e.putInt("Axis", o.axis());
				e.put("Material", NbtUtils.writeBlockState(o.material()));
				e.putInt("Paid", o.paid());
				list.add(e);
			}
			tag.put("PaneExtras", list);
		}
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
			&& ContourConfig.consumeMaterial()
				? (getBlockState().getBlock()
					instanceof net.createcontour.block.ContourBlock cb3
						? cb3.hostUnits(getBlockState()) : 1)
				: 0);
		// Extra occupants export with paid only where the requirement
		// above will charge one material unit at print; consume-off
		// exports none so free paint can never become a refund source.
		if (!byteOccupants.isEmpty()) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (ByteOccupant o : byteOccupants) {
				CompoundTag e = new CompoundTag();
				e.putInt("Octant", o.octant());
				e.put("Material", NbtUtils.writeBlockState(o.material()));
				boolean custom = o.material() != null
					&& !MaterialSafety.isNeutral(o.material());
				e.putInt("Paid", custom && ContourConfig.consumeMaterial() ? 1 : 0);
				list.add(e);
			}
			tag.put("ByteExtras", list);
		}
		if (!panelOccupants.isEmpty()) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (PanelOccupant o : panelOccupants) {
				CompoundTag e = new CompoundTag();
				e.putInt("Slot", o.slot());
				e.putInt("Kind", o.kind());
				e.put("Material", NbtUtils.writeBlockState(o.material()));
				boolean custom = o.material() != null
					&& !MaterialSafety.isNeutral(o.material());
				e.putInt("Paid", custom && ContourConfig.consumeMaterial() ? 1 : 0);
				list.add(e);
			}
			tag.put("PanelExtras", list);
		}
		if (!paneOccupants.isEmpty()) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (PaneOccupant o : paneOccupants) {
				CompoundTag e = new CompoundTag();
				e.putInt("Axis", o.axis());
				e.put("Material", NbtUtils.writeBlockState(o.material()));
				boolean custom = o.material() != null
					&& !MaterialSafety.isNeutral(o.material());
				e.putInt("Paid", custom && ContourConfig.consumeMaterial() ? 1 : 0);
				list.add(e);
			}
			tag.put("PaneExtras", list);
		}
		tag.remove("Item");
	}

	// --- model data (guarded behind Create's final getModelData seam) ---

	@Override
	public ModelData getModelData() {
		ModelData.Builder b = ModelData.builder()
			.with(CopycatModel.MATERIAL_PROPERTY, material);
		if (!byteOccupants.isEmpty()) {
			java.util.List<net.createcontour.client.ContourByteModel.ByteExtra> ex =
				new java.util.ArrayList<>();
			for (ByteOccupant o : byteOccupants)
				ex.add(new net.createcontour.client.ContourByteModel.ByteExtra(
					o.octant(), o.material()));
			b.with(net.createcontour.client.ContourByteModel.BYTE_EXTRAS, ex);
		}
		if (!panelOccupants.isEmpty()) {
			java.util.List<net.createcontour.client.ContourMultipartModel.PanelExtra> ex =
				new java.util.ArrayList<>();
			for (PanelOccupant o : panelOccupants)
				ex.add(new net.createcontour.client.ContourMultipartModel.PanelExtra(
					o.slot(), o.kind(), o.material()));
			b.with(net.createcontour.client.ContourMultipartModel.PANEL_EXTRAS,
				ex);
		}
		if (!paneOccupants.isEmpty()) {
			java.util.List<net.createcontour.client.ContourMultipartModel.PaneExtra> ex =
				new java.util.ArrayList<>();
			for (PaneOccupant o : paneOccupants)
				ex.add(new net.createcontour.client.ContourMultipartModel.PaneExtra(
					o.axis(), o.material()));
			b.with(net.createcontour.client.ContourMultipartModel.PANE_EXTRAS,
				ex);
		}
		return b.build();
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
