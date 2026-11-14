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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
public class ContourBlockEntity extends CopycatBlockEntity
	implements ContourPaintable {

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
		if (!(getBlockState().getBlock() instanceof ContourBlock cb))
			return 1;
		// Occupants add one host unit each - except BOARD records on a
		// board host: their faces are already counted inside the
		// blockstate face count.
		boolean boardHost = getBlockState().getBlock()
			instanceof net.createcontour.block.ContourBoardBlock;
		int extra = 0;
		for (Occupant o : occupants)
			if (o.kind() != OccupantKind.BOARD || !boardHost)
				extra++;
		return cb.hostUnits(getBlockState()) + extra;
	}

	/**
	 * Whole material items the HOST state's paint is billed at: the
	 * block's materialUnits hook (contour_layers bills one item per
	 * eight layers - issue #20 - so painted stacks under size 8 carry
	 * zero billed credit). Extra occupants bill their own per-occupant
	 * paid fields separately, one item each.
	 */
	public int materialUnits() {
		return getBlockState().getBlock() instanceof ContourBlock cb
			? cb.materialUnits(getBlockState()) : 1;
	}

	// --- issue #22 / ADR-018: universal host - ONE occupant model ---
	//
	// Occupant #0 stays in the blockstate (legacy saves byte-for-byte);
	// every EXTRA occupant is one record in `occupants`:
	// (kind, slot, material, paid). The slot is channel-specific:
	//   BYTE  -> octant 0-7
	//   PANEL -> subKind*24 + faceSlot (subKind = PanelSlots.KIND_*,
	//            faceSlot = face*4 + sub, 0-23)
	//   PANE  -> axis 0/1/2 (X/Y/Z mid-plane)
	//   BOARD -> face ordinal 0-5
	//
	// Legality has two layers:
	//  1. INTRA-KIND lattice rules (unchanged semantics): octants are a
	//     set, panel slot claims must be disjoint, pane axes are
	//     exclusive, board faces are unique per face. Pane sheets on
	//     different axes therefore still cross by design, and face
	//     plates on adjacent faces still share edge corners.
	//  2. CROSS-KIND geometric disjointness: the candidate's occupied
	//     pixel-space boxes must not intersect any different-kind
	//     occupant's boxes, nor a different-kind host's footprint.
	//
	// Only the four multipart host families (byte, panel family, flat
	// pane, board) can hold extras - their models emit occupant quads
	// and their shapes union occupant boxes.

	public enum OccupantKind { BYTE, PANEL, PANE, BOARD }

	/** One extra occupant: channel kind, channel slot, paint, paid. */
	public record Occupant(OccupantKind kind, int slot, BlockState material,
		int paid) {
		/** Occupied pixel-space boxes (0-16 cell coords). */
		public java.util.List<float[]> footprint() {
			return occupantBoxes(kind, slot);
		}

		/** Blank shape item this occupant refunds on break. */
		public net.minecraft.world.item.Item blankItem() {
			return switch (kind) {
				case BYTE -> net.createcontour.registry.ContourRegistries
					.CONTOUR_BYTE_ITEM.get();
				case PANEL -> net.createcontour.block.PanelSlots
					.itemForKind(slot / 24);
				case PANE -> net.createcontour.registry.ContourRegistries
					.CONTOUR_FLAT_PANE_ITEM.get();
				case BOARD -> net.createcontour.registry.ContourRegistries
					.CONTOUR_BOARD_ITEM.get();
			};
		}
	}

	/** Channel kind + slot a held block claims on a merge click. */
	public record OccupantSpec(OccupantKind kind, int slot) {
	}

	private final java.util.List<Occupant> occupants =
		new java.util.ArrayList<>();

	public java.util.List<Occupant> getOccupants() {
		return java.util.Collections.unmodifiableList(occupants);
	}

	// --- per-kind compatibility views (issue #4/#12/#24 API surface) ---

	public record ByteOccupant(int octant, BlockState material, int paid) {
	}

	public record PanelOccupant(int slot, int kind, BlockState material,
		int paid) {
	}

	public record PaneOccupant(int axis, BlockState material, int paid) {
	}

	public record BoardOccupant(int face, BlockState material, int paid) {
	}

	public java.util.List<ByteOccupant> getByteOccupants() {
		java.util.List<ByteOccupant> out = new java.util.ArrayList<>();
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.BYTE)
				out.add(new ByteOccupant(o.slot(), o.material(), o.paid()));
		return java.util.Collections.unmodifiableList(out);
	}

	public java.util.List<PanelOccupant> getPanelOccupants() {
		java.util.List<PanelOccupant> out = new java.util.ArrayList<>();
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.PANEL)
				out.add(new PanelOccupant(
					net.createcontour.block.PanelSlots
						.slotFace(o.slot()),
					net.createcontour.block.PanelSlots
						.slotKind(o.slot()),
					o.material(), o.paid()));
		return java.util.Collections.unmodifiableList(out);
	}

	public java.util.List<PaneOccupant> getPaneOccupants() {
		java.util.List<PaneOccupant> out = new java.util.ArrayList<>();
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.PANE)
				out.add(new PaneOccupant(o.slot(), o.material(), o.paid()));
		return java.util.Collections.unmodifiableList(out);
	}

	public java.util.List<BoardOccupant> getBoardOccupants() {
		java.util.List<BoardOccupant> out = new java.util.ArrayList<>();
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.BOARD)
				out.add(new BoardOccupant(o.slot(), o.material(),
					o.paid()));
		return java.util.Collections.unmodifiableList(out);
	}

	// --- legality gates ---

	/** This cell's host channel kind, null on non-multipart hosts. */
	@org.jetbrains.annotations.Nullable
	public OccupantKind hostKind() {
		return hostKind(getBlockState());
	}

	@org.jetbrains.annotations.Nullable
	public static OccupantKind hostKind(BlockState state) {
		Block b = state.getBlock();
		if (b instanceof net.createcontour.block.ContourByteBlock)
			return OccupantKind.BYTE;
		if (net.createcontour.block.PanelSlots.isPanelFamily(b))
			return OccupantKind.PANEL;
		if (b instanceof net.createcontour.block.ContourFlatPaneBlock)
			return OccupantKind.PANE;
		if (b instanceof net.createcontour.block.ContourBoardBlock)
			return OccupantKind.BOARD;
		return null;
	}

	private static boolean slotValid(OccupantKind kind, int slot) {
		return switch (kind) {
			case BYTE -> slot >= 0 && slot < 8;
			case PANEL -> slot >= 0 && slot < 72;
			case PANE -> slot >= 0 && slot < 3;
			case BOARD -> slot >= 0 && slot < 6;
		};
	}

	/**
	 * Occupied pixel-space boxes for one channel slot - the same boxes
	 * the block geometry and the model emit.
	 */
	public static java.util.List<float[]> occupantBoxes(OccupantKind kind,
		int slot) {
		switch (kind) {
			case BYTE -> {
				net.createcontour.block.ContourByteBlock.Octant o =
					net.createcontour.block.ContourByteBlock.Octant
						.values()[slot];
				return java.util.List.of(new float[]{o.x * 8f, o.y * 8f,
					o.z * 8f, o.x * 8f + 8f, o.y * 8f + 8f, o.z * 8f + 8f});
			}
			case PANEL -> {
				return net.createcontour.block.PanelSlots.occupantBoxes(
					net.createcontour.block.PanelSlots.slotFace(slot),
					net.createcontour.block.PanelSlots.slotKind(slot));
			}
			case PANE -> {
				return net.createcontour.block.ContourFlatPaneBlock
					.boxesForAxis(slot);
			}
			default -> {
				return net.createcontour.block.ContourBoardBlock
					.boxesForFace(Direction.values()[slot]);
			}
		}
	}

	/**
	 * The host state's own occupied boxes (occupant #0). Diagonal
	 * panel positions claim their stepped collision slices PLUS the two
	 * rendered corner columns - both are real geometry a merge must
	 * avoid.
	 */
	public static java.util.List<float[]> hostFootprint(BlockState state) {
		Block b = state.getBlock();
		if (b instanceof net.createcontour.block.ContourByteBlock) {
			net.createcontour.block.ContourByteBlock.Octant o =
				state.getValue(
					net.createcontour.block.ContourByteBlock.OCTANT);
			return java.util.List.of(new float[]{o.x * 8f, o.y * 8f,
				o.z * 8f, o.x * 8f + 8f, o.y * 8f + 8f, o.z * 8f + 8f});
		}
		if (b instanceof net.createcontour.block.ContourPanelBlock) {
			net.createcontour.block.ContourPanelBlock.PanelPos p =
				state.getValue(
					net.createcontour.block.ContourPanelBlock.POSITION);
			if (p != null && p.isDiagonal()) {
				java.util.List<float[]> boxes =
					new java.util.ArrayList<>(
						net.createcontour.block.ContourPanelBlock
							.diagCollisionBoxes(p));
				boxes.addAll(net.createcontour.block.ContourPanelBlock
					.boxesFor(p));
				return boxes;
			}
			return net.createcontour.block.PanelSlots.hostBoxes(state);
		}
		if (net.createcontour.block.PanelSlots.isPanelFamily(b))
			return net.createcontour.block.PanelSlots.hostBoxes(state);
		if (b instanceof net.createcontour.block.ContourFlatPaneBlock)
			return net.createcontour.block.ContourFlatPaneBlock
				.boxesForAxis(Direction.Axis.Y.ordinal());
		if (b instanceof net.createcontour.block.ContourBoardBlock)
			return net.createcontour.block.ContourBoardBlock
				.boxesFor(state);
		return java.util.List.of();
	}

	private static boolean boxOverlap(float[] a, float[] b) {
		final float E = 1e-3f;
		return a[0] < b[3] - E && b[0] < a[3] - E
			&& a[1] < b[4] - E && b[1] < a[4] - E
			&& a[2] < b[5] - E && b[2] < a[5] - E;
	}

	/** Positive-volume AABB intersection (edge/corner touching is free). */
	public static boolean boxesOverlap(java.util.List<float[]> a,
		java.util.List<float[]> b) {
		for (float[] x : a)
			for (float[] y : b)
				if (boxOverlap(x, y))
					return true;
		return false;
	}

	/**
	 * Cross-kind geometric gate: candidate boxes must be disjoint from
	 * every DIFFERENT-kind occupant's footprint and from a
	 * different-kind host's own footprint. Same-kind legality is the
	 * channel's lattice rule, checked separately.
	 */
	public boolean crossKindClear(OccupantKind kind,
		java.util.List<float[]> cand) {
		for (Occupant o : occupants)
			if (o.kind() != kind && boxesOverlap(cand, o.footprint()))
				return false;
		OccupantKind hk = hostKind();
		return hk == kind || hk == null
			|| !boxesOverlap(cand, hostFootprint(getBlockState()));
	}

	/** Intra-kind claim legality shared by merge and insert paths. */
	private boolean channelClaimFree(OccupantKind kind, int slot) {
		return switch (kind) {
			case BYTE -> !occupiedOctants().contains(slot);
			case PANEL -> panelSlotsFree(
				net.createcontour.block.PanelSlots.slots(
					net.createcontour.block.PanelSlots.slotFace(slot),
					net.createcontour.block.PanelSlots.slotKind(slot)));
			case PANE -> !occupiedPaneAxes().contains(slot);
			case BOARD -> boardOccupantIndex(slot) < 0;
		};
	}

	/**
	 * Placement legality for the item/canBeReplaced route: intra-kind
	 * claim free AND cross-kind geometric disjoint. BOARD candidates on
	 * a board host additionally need the face flag FREE (the flag is
	 * the host's face claim and gets set by the merge itself).
	 */
	public boolean canMergeOccupant(OccupantKind kind, int slot) {
		if (frozen || kind == null || hostKind() == null
			|| !slotValid(kind, slot) || !channelClaimFree(kind, slot))
			return false;
		if (kind == OccupantKind.BOARD
			&& getBlockState().getBlock()
				instanceof net.createcontour.block.ContourBoardBlock
			&& net.createcontour.block.ContourBoardBlock.facePresent(
				getBlockState(), Direction.values()[slot]))
			return false;
		return crossKindClear(kind, occupantBoxes(kind, slot));
	}

	/**
	 * Insertion legality: same as merge EXCEPT a BOARD record on a
	 * board host requires its face flag PRESENT (the record is the
	 * ledger of an existing sheet - set by the merge before insert, or
	 * healed on first paint). NBT load runs this gate so malformed or
	 * overlapping records drop rather than being trusted.
	 */
	private boolean canAddOccupant(OccupantKind kind, int slot) {
		if (frozen || kind == null || hostKind() == null
			|| !slotValid(kind, slot) || !channelClaimFree(kind, slot))
			return false;
		if (kind == OccupantKind.BOARD
			&& getBlockState().getBlock()
				instanceof net.createcontour.block.ContourBoardBlock
			&& !net.createcontour.block.ContourBoardBlock.facePresent(
				getBlockState(), Direction.values()[slot]))
			return false;
		return crossKindClear(kind, occupantBoxes(kind, slot));
	}

	/** Gated insert without sync (NBT load adds through this). */
	private boolean loadOccupant(OccupantKind kind, int slot,
		BlockState mat, int paid) {
		if (!canAddOccupant(kind, slot))
			return false;
		occupants.add(new Occupant(kind, slot,
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		return true;
	}

	/** Add an extra occupant; the gate is canAddOccupant. */
	public boolean addOccupant(OccupantKind kind, int slot, BlockState mat,
		int paid) {
		if (!canAddOccupant(kind, slot))
			return false;
		occupants.add(new Occupant(kind, slot,
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	/** Unified occupant paint (unified-list index). */
	public boolean paintOccupant(int idx, BlockState mat, int paid) {
		if (frozen || idx < 0 || idx >= occupants.size())
			return false;
		Occupant old = occupants.get(idx);
		occupants.set(idx, new Occupant(old.kind(), old.slot(),
			MaterialSafety.sanitize(mat), Math.max(0, paid)));
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return true;
	}

	/** Unified-list index of the filteredIdx-th occupant of `kind`. */
	private int unifiedIndex(OccupantKind kind, int filteredIdx) {
		if (filteredIdx < 0)
			return -1;
		int seen = 0;
		for (int i = 0; i < occupants.size(); i++)
			if (occupants.get(i).kind() == kind && seen++ == filteredIdx)
				return i;
		return -1;
	}

	// --- merge resolution (item + canBeReplaced paths) ---

	/**
	 * The occupant claim a held Contour item makes on the hit cell:
	 * byte -> octant under the hit, panel family -> face slot per
	 * sub-kind, flat pane -> clicked face's axis, board -> clicked
	 * face. Non-occupant Contour items (slabs, stairs...) return null.
	 */
	@org.jetbrains.annotations.Nullable
	public static OccupantSpec specFor(Block heldBlock, Direction face,
		Vec3 hit, BlockPos cell) {
		if (heldBlock instanceof net.createcontour.block.ContourByteBlock)
			return new OccupantSpec(OccupantKind.BYTE,
				net.createcontour.block.ContourByteBlock.octantAt(hit,
					cell));
		int pk = net.createcontour.block.PanelSlots.kindOf(heldBlock);
		if (pk >= 0) {
			int s = net.createcontour.block.PanelSlots.incomingSlot(
				heldBlock, face, hit, cell);
			return s >= 0
				? new OccupantSpec(OccupantKind.PANEL,
					net.createcontour.block.PanelSlots.encodeSlot(s, pk))
				: null;
		}
		if (heldBlock instanceof net.createcontour.block.ContourFlatPaneBlock)
			return new OccupantSpec(OccupantKind.PANE,
				face.getAxis().ordinal());
		if (heldBlock instanceof net.createcontour.block.ContourBoardBlock)
			return new OccupantSpec(OccupantKind.BOARD, face.ordinal());
		return null;
	}

	/**
	 * Shared canBeReplaced gate for every multipart host block: the
	 * held item must map to an occupant spec whose claim is legal on
	 * the reconstructed clicked cell. getClickedPos() is unreliable
	 * inside canBeReplaced, so the hit point is nudged opposite the
	 * clicked face to land inside the hit cell.
	 */
	public static boolean canMergeInto(BlockPlaceContext ctx) {
		if (!(ctx.getItemInHand().getItem()
			instanceof net.createcontour.item.ContourBlockItem cbi))
			return false;
		Vec3 click = ctx.getClickLocation();
		BlockPos clicked = BlockPos.containing(click.subtract(
			Vec3.atLowerCornerOf(ctx.getClickedFace().getNormal())
				.scale(1.0E-4)));
		if (!(ctx.getLevel().getBlockEntity(clicked)
			instanceof ContourBlockEntity be))
			return false;
		OccupantSpec spec = specFor(cbi.getBlock(), ctx.getClickedFace(),
			click, clicked);
		return spec != null
			&& be.canMergeOccupant(spec.kind(), spec.slot());
	}

	// --- intra-kind claim sets (host + same-kind extras) ---

	public java.util.Set<Integer> occupiedOctants() {
		java.util.Set<Integer> s = new java.util.HashSet<>();
		s.add(getBlockState().hasProperty(
			net.createcontour.block.ContourByteBlock.OCTANT)
				? getBlockState().getValue(
					net.createcontour.block.ContourByteBlock.OCTANT)
						.ordinal()
				: -1);
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.BYTE)
				s.add(o.slot());
		return s;
	}

	/** Union of every claimed face-quadrant slot (host + extras). */
	public java.util.Set<Integer> occupiedPanelSlots() {
		java.util.Set<Integer> s =
			net.createcontour.block.PanelSlots.stateSlots(getBlockState());
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.PANEL)
				s.addAll(net.createcontour.block.PanelSlots.slots(
					net.createcontour.block.PanelSlots.slotFace(o.slot()),
					net.createcontour.block.PanelSlots.slotKind(o.slot())));
		return s;
	}

	public boolean panelSlotsFree(java.util.Set<Integer> claim) {
		return java.util.Collections.disjoint(occupiedPanelSlots(), claim);
	}

	/** Occupied interior-plane axes (host blockstate + extras). */
	public java.util.Set<Integer> occupiedPaneAxes() {
		java.util.Set<Integer> s = new java.util.HashSet<>();
		if (getBlockState().getBlock()
			instanceof net.createcontour.block.ContourFlatPaneBlock)
			s.add(Direction.Axis.Y.ordinal());
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.PANE)
				s.add(o.slot());
		return s;
	}

	/** Flattened pixel-space boxes of every extra (shape unions). */
	public java.util.List<float[]> extraFootprintBoxes() {
		java.util.List<float[]> out = new java.util.ArrayList<>();
		for (Occupant o : occupants)
			out.addAll(o.footprint());
		return out;
	}

	// --- per-kind wrappers over the unified list ---

	public boolean addByteOccupant(int octant, BlockState mat, int paid) {
		return addOccupant(OccupantKind.BYTE, octant, mat, paid);
	}

	public boolean addPanelOccupant(int slot, int kind, BlockState mat,
		int paid) {
		if (kind < 0 || kind > 2 || slot < 0 || slot >= 24)
			return false;
		return addOccupant(OccupantKind.PANEL,
			net.createcontour.block.PanelSlots.encodeSlot(slot, kind),
			mat, paid);
	}

	public boolean addPaneOccupant(int axis, BlockState mat, int paid) {
		return addOccupant(OccupantKind.PANE, axis, mat, paid);
	}

	/**
	 * Registers the material/paid record for one face. On a board host
	 * the face flag must be set in blockstate (presence is the caller's
	 * contract); on a non-board host the record itself is the presence.
	 */
	public boolean addBoardOccupant(int face, BlockState mat, int paid) {
		return addOccupant(OccupantKind.BOARD, face, mat, paid);
	}

	public int byteOccupantIndex(int octant) {
		int vi = 0;
		for (Occupant o : occupants) {
			if (o.kind() != OccupantKind.BYTE)
				continue;
			if (o.slot() == octant)
				return vi;
			vi++;
		}
		return -1;
	}

	/** Panel-filtered index whose slot claim contains faceSlot, or -1. */
	public int panelOccupantIndex(int faceSlot) {
		int vi = 0;
		for (Occupant o : occupants) {
			if (o.kind() != OccupantKind.PANEL)
				continue;
			if (net.createcontour.block.PanelSlots.slots(
				net.createcontour.block.PanelSlots.slotFace(o.slot()),
				net.createcontour.block.PanelSlots.slotKind(o.slot()))
				.contains(faceSlot))
				return vi;
			vi++;
		}
		return -1;
	}

	public int paneOccupantIndex(int axis) {
		int vi = 0;
		for (Occupant o : occupants) {
			if (o.kind() != OccupantKind.PANE)
				continue;
			if (o.slot() == axis)
				return vi;
			vi++;
		}
		return -1;
	}

	public int boardOccupantIndex(int face) {
		int vi = 0;
		for (Occupant o : occupants) {
			if (o.kind() != OccupantKind.BOARD)
				continue;
			if (o.slot() == face)
				return vi;
			vi++;
		}
		return -1;
	}

	public boolean paintByteOccupant(int idx, BlockState mat, int paid) {
		return paintOccupant(unifiedIndex(OccupantKind.BYTE, idx), mat,
			paid);
	}

	public boolean paintPanelOccupant(int idx, BlockState mat, int paid) {
		return paintOccupant(unifiedIndex(OccupantKind.PANEL, idx), mat,
			paid);
	}

	public boolean paintPaneOccupant(int idx, BlockState mat, int paid) {
		return paintOccupant(unifiedIndex(OccupantKind.PANE, idx), mat,
			paid);
	}

	public boolean paintBoardOccupant(int idx, BlockState mat, int paid) {
		return paintOccupant(unifiedIndex(OccupantKind.BOARD, idx), mat,
			paid);
	}

	@org.jetbrains.annotations.Nullable
	public BoardOccupant removeBoardOccupant(int face) {
		int idx = -1;
		for (int i = 0; i < occupants.size(); i++) {
			Occupant o = occupants.get(i);
			if (o.kind() == OccupantKind.BOARD && o.slot() == face) {
				idx = i;
				break;
			}
		}
		if (frozen || idx < 0)
			return null;
		Occupant o = occupants.remove(idx);
		notifyUpdate();
		if (level != null)
			refreshMaterialAppearance();
		return new BoardOccupant(face, o.material(), o.paid());
	}

	/**
	 * Clicked-block look for paint-at-placement on boards: the sheet
	 * under the click - the clicked face's own record when that face is
	 * occupied (its outer surface was hit), else the opposite sheet
	 * whose interior surface the ray met. Falls back to any painted
	 * sheet, then null (unpainted board).
	 */
	@org.jetbrains.annotations.Nullable
	public BlockState boardLook(Direction clickedFace) {
		if (!(getBlockState().getBlock()
			instanceof net.createcontour.block.ContourBoardBlock))
			return null;
		Direction f = net.createcontour.block.ContourBoardBlock
			.facePresent(getBlockState(), clickedFace)
				? clickedFace : clickedFace.getOpposite();
		int idx = boardOccupantIndex(f.ordinal());
		if (idx >= 0) {
			BlockState m = getBoardOccupants().get(idx).material();
			if (m != null && !MaterialSafety.isNeutral(m))
				return m;
		}
		for (Occupant o : occupants)
			if (o.kind() == OccupantKind.BOARD && o.material() != null
				&& !MaterialSafety.isNeutral(o.material()))
				return o.material();
		return null;
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
	 * the copied material at one item per material unit (consume-on only;
	 * contour_layers bills one item per eight layers - issue #20), plus
	 * any extra host units Create's default does not cover (multi-unit
	 * hosts like LAYERS/SLICE counts beyond the slab-double special case).
	 */
	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
		// Material is billed per MATERIAL unit (one item per host unit on
		// most shapes; contour_layers bills one item per eight layers, so
		// painted stacks under size 8 contribute no material entry).
		int blockUnits = state.getBlock()
			instanceof net.createcontour.block.ContourBlock cb2
				? cb2.materialUnits(state) : 1;
		if (hasCustomMaterial() && ContourConfig.consumeMaterial()
			&& blockUnits > 0
			&& !material.getBlock().asItem().getDefaultInstance().isEmpty()) {
			stacks.add(new ItemStack(material.getBlock().asItem(), blockUnits));
		}
		if (ContourConfig.consumeMaterial()) {
			for (Occupant o : occupants)
				if (o.material() != null && !MaterialSafety.isNeutral(o.material())
					&& !o.material().getBlock().asItem().getDefaultInstance()
						.isEmpty())
					stacks.add(new ItemStack(o.material().getBlock().asItem(), 1));
		}
		int defaultUnits =
			state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
				&& state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE)
					== net.minecraft.world.level.block.state.properties.SlabType.DOUBLE ? 2 : 1;
		// Host-item extras cover occupants whose blank item IS the host
		// item (byte on byte host, board face on board host, full panel
		// on panel host...); every other occupant needs its own kind's
		// blank item (a full panel on a byte-panel host costs a
		// contour_panel, not a contour_byte_panel).
		net.minecraft.world.item.Item hostItem = state.getBlock().asItem();
		int baseUnits = state.getBlock()
			instanceof net.createcontour.block.ContourBlock cb3
				? cb3.hostUnits(state) : 1;
		boolean boardHost = state.getBlock()
			instanceof net.createcontour.block.ContourBoardBlock;
		int extraUnits = baseUnits - defaultUnits;
		for (Occupant o : occupants) {
			// A BOARD record on a board host is the ledger of an
			// existing face - the face itself is already billed inside
			// baseUnits, so it adds no blank of its own.
			if (boardHost && o.kind() == OccupantKind.BOARD)
				continue;
			if (o.blankItem() == hostItem)
				extraUnits++;
			else
				stacks.add(new ItemStack(o.blankItem()));
		}
		if (extraUnits > 0 && !hostItem.getDefaultInstance().isEmpty())
			stacks.add(new ItemStack(hostItem, extraUnits));
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
		// Board face anchors are directions: they must track the
		// structure rotation/mirror alongside the blockstate face flags
		// (rotate()/mirror() on the block remap the flags themselves).
		// Other occupant slots (octants, face-quadrant sub-slots, axes)
		// historically do NOT remap and their materials do NOT rotate -
		// preserved semantics, documented as a limitation in
		// docs/Continuation.md.
		for (int i = 0; i < occupants.size(); i++) {
			Occupant o = occupants.get(i);
			if (o.kind() != OccupantKind.BOARD)
				continue;
			Direction f = transform.mirrorFacing(transform.rotation
				.rotate(Direction.values()[o.slot()]));
			BlockState m = o.material() != null
				&& !MaterialSafety.isNeutral(o.material())
				&& ContourConfig.rotateTextures()
					? transform.apply(o.material()) : o.material();
			occupants.set(i, new Occupant(o.kind(), f.ordinal(), m,
				o.paid()));
		}
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
		occupants.clear();
		// issue #22 unified format (forward-only): a single "Occupants"
		// list of {Kind, Slot, Material, Paid}. Every record passes the
		// same insertion gate as runtime adds, so malformed, overlapping
		// or unsupported entries drop rather than being trusted.
		if (tag.contains("Occupants")) {
			for (net.minecraft.nbt.Tag t : tag.getList("Occupants",
				net.minecraft.nbt.Tag.TAG_COMPOUND)) {
				CompoundTag e = (CompoundTag) t;
				OccupantKind kind = occupantKindFor(e.getString("Kind"));
				if (kind == null || !e.contains("Slot"))
					continue;
				try {
					loadOccupant(kind, e.getInt("Slot"),
						MaterialSafety.sanitize(NbtUtils.readBlockState(
							provider.lookupOrThrow(Registries.BLOCK),
							e.getCompound("Material"))),
						Math.max(0, e.getInt("Paid")));
				} catch (RuntimeException ignored) {
				}
			}
		}
		// Legacy per-channel keys (pre-issue-#22 saves): read once,
		// migrated into the unified list, never written again.
		loadLegacyExtras(tag, provider);
		if (level != null)
			refreshMaterialAppearance();
	}

	@org.jetbrains.annotations.Nullable
	private static OccupantKind occupantKindFor(String name) {
		return switch (name) {
			case "byte" -> OccupantKind.BYTE;
			case "panel" -> OccupantKind.PANEL;
			case "pane" -> OccupantKind.PANE;
			case "board" -> OccupantKind.BOARD;
			default -> null;
		};
	}

	@org.jetbrains.annotations.Nullable
	private static BlockState readMaterial(CompoundTag e,
		HolderLookup.Provider provider) {
		try {
			return MaterialSafety.sanitize(NbtUtils.readBlockState(
				provider.lookupOrThrow(Registries.BLOCK),
				e.getCompound("Material")));
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	/**
	 * Legacy per-channel extras keys, migrated once into `occupants`
	 * and never re-emitted: ByteExtras {Octant}, PanelExtras
	 * {Slot, Kind}, PaneExtras {Axis}, BoardExtras {Face}. Each entry
	 * goes through the normal insertion gate, so the same malformed /
	 * conflicting / drifted-record drops the old per-channel loaders
	 * enforced still apply (plus the new cross-kind geometry gate).
	 */
	private void loadLegacyExtras(CompoundTag tag,
		HolderLookup.Provider provider) {
		for (net.minecraft.nbt.Tag t : tag.getList("ByteExtras",
			net.minecraft.nbt.Tag.TAG_COMPOUND)) {
			CompoundTag e = (CompoundTag) t;
			if (!e.contains("Octant"))
				continue;
			BlockState m = readMaterial(e, provider);
			if (m != null)
				loadOccupant(OccupantKind.BYTE, e.getInt("Octant"), m,
					Math.max(0, e.getInt("Paid")));
		}
		for (net.minecraft.nbt.Tag t : tag.getList("PanelExtras",
			net.minecraft.nbt.Tag.TAG_COMPOUND)) {
			CompoundTag e = (CompoundTag) t;
			if (!e.contains("Slot") || !e.contains("Kind"))
				continue;
			int slot = e.getInt("Slot"), kind = e.getInt("Kind");
			if (kind < 0 || kind > 2)
				continue;
			BlockState m = readMaterial(e, provider);
			if (m != null)
				loadOccupant(OccupantKind.PANEL,
					net.createcontour.block.PanelSlots.encodeSlot(slot,
						kind),
					m, Math.max(0, e.getInt("Paid")));
		}
		for (net.minecraft.nbt.Tag t : tag.getList("PaneExtras",
			net.minecraft.nbt.Tag.TAG_COMPOUND)) {
			CompoundTag e = (CompoundTag) t;
			if (!e.contains("Axis"))
				continue;
			BlockState m = readMaterial(e, provider);
			if (m != null)
				loadOccupant(OccupantKind.PANE, e.getInt("Axis"), m,
					Math.max(0, e.getInt("Paid")));
		}
		for (net.minecraft.nbt.Tag t : tag.getList("BoardExtras",
			net.minecraft.nbt.Tag.TAG_COMPOUND)) {
			CompoundTag e = (CompoundTag) t;
			if (!e.contains("Face"))
				continue;
			BlockState m = readMaterial(e, provider);
			if (m != null)
				loadOccupant(OccupantKind.BOARD, e.getInt("Face"), m,
					Math.max(0, e.getInt("Paid")));
		}
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
		writeOccupants(tag, false);
	}

	/**
	 * Unified occupant serialization (issue #22): one "Occupants" list
	 * of {Kind, Slot, Material, Paid} compounds. safePaid projects the
	 * ledger for schematic export (see writeSafe); legacy per-channel
	 * keys are never emitted again.
	 */
	private void writeOccupants(CompoundTag tag, boolean safePaid) {
		if (occupants.isEmpty())
			return;
		net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
		for (Occupant o : occupants) {
			CompoundTag e = new CompoundTag();
			e.putString("Kind", switch (o.kind()) {
				case BYTE -> "byte";
				case PANEL -> "panel";
				case PANE -> "pane";
				case BOARD -> "board";
			});
			e.putInt("Slot", o.slot());
			e.put("Material", NbtUtils.writeBlockState(o.material()));
			boolean custom = o.material() != null
				&& !MaterialSafety.isNeutral(o.material());
			e.putInt("Paid", safePaid
				? (custom && ContourConfig.consumeMaterial() ? 1 : 0)
				: o.paid());
			list.add(e);
		}
		tag.put("Occupants", list);
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
						? cb3.materialUnits(getBlockState()) : 1)
				: 0);
		// Extra occupants export with paid only where the requirement
		// above will charge one material unit at print; consume-off
		// exports none so free paint can never become a refund source.
		writeOccupants(tag, true);
		tag.remove("Item");
	}

	// --- model data (guarded behind Create's final getModelData seam) ---

	@Override
	public ModelData getModelData() {
		ModelData.Builder b = ModelData.builder()
			.with(CopycatModel.MATERIAL_PROPERTY, material);
		// issue #22: one unified extras property every host-family model
		// consumes; per-kind decode lives in the Occupant record itself.
		if (!occupants.isEmpty())
			b.with(net.createcontour.client.ContourModel.OCCUPANT_EXTRAS,
				java.util.List.copyOf(occupants));
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
