package net.createcontour.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.createcontour.block.ContourPanelBlock.PanelPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Face-quadrant occupancy channel (issue #12, ADR-016/017): 6 faces x
 * 4 quadrants = 24 slots. One slot id encodes a panel-kind anchor:
 * {@code slot = face.ordinal() * 4 + sub} where {@code sub} is the
 * byte-panel quadrant for BYTE_PANEL, a half-id for HALF_PANEL
 * (0 = U-low pair {0,1}, 1 = U-high {2,3}, 2 = V-low {0,2},
 * 3 = V-high {1,3}) and ignored (0) for PANEL (all four quadrants of
 * the face). Quadrant numbering matches ContourBytePanelBlock: per-face
 * U/V in-face axes, q = (hiU ? 2 : 0) | (hiV ? 1 : 0).
 *
 * A contour_panel in a non-FACE position (mid/edge/diagonal) is not
 * representable on this grid (ADR-016a names them unsupported): its
 * host footprint conservatively claims all 24 slots so no merge can
 * collide with its geometry.
 */
public final class PanelSlots {

	private PanelSlots() {
	}

	public static final int KIND_BYTE_PANEL = 0;
	public static final int KIND_HALF_PANEL = 1;
	public static final int KIND_PANEL = 2;
	private static final int SLOT_COUNT = 24;

	/** slot id for a face + kind-specific sub index. */
	public static int id(Direction face, int sub) {
		return face.ordinal() * 4 + (sub & 3);
	}

	public static Direction faceOf(int slot) {
		return Direction.values()[(slot / 4) & 7];
	}

	public static int subOf(int slot) {
		return slot & 3;
	}

	/** Panel-family membership (the three channel-2 block types). */
	public static boolean isPanelFamily(Block b) {
		return b instanceof ContourBytePanelBlock
			|| b instanceof ContourHalfPanelBlock
			|| b instanceof ContourPanelBlock;
	}

	/** Occupant kind for a family block, -1 outside the family. */
	public static int kindOf(Block b) {
		if (b instanceof ContourBytePanelBlock)
			return KIND_BYTE_PANEL;
		if (b instanceof ContourHalfPanelBlock)
			return KIND_HALF_PANEL;
		if (b instanceof ContourPanelBlock)
			return KIND_PANEL;
		return -1;
	}

	/** Blank item refund unit for an occupant kind. */
	public static Item itemForKind(int kind) {
		return switch (kind) {
			case KIND_BYTE_PANEL -> net.createcontour.registry.ContourRegistries
				.CONTOUR_BYTE_PANEL_ITEM.get();
			case KIND_HALF_PANEL -> net.createcontour.registry.ContourRegistries
				.CONTOUR_HALF_PANEL_ITEM.get();
			default -> net.createcontour.registry.ContourRegistries
				.CONTOUR_PANEL_ITEM.get();
		};
	}

	// --- per-face U/V axes (identical mapping to ContourBytePanelBlock) ---

	public static Direction uDir(Direction face) {
		return switch (face) {
			case DOWN, UP -> Direction.EAST;    // +x
			case NORTH -> Direction.EAST;        // +x
			case SOUTH -> Direction.WEST;        // -x
			default -> Direction.NORTH;          // -z (WEST/EAST faces)
		};
	}

	public static Direction vDir(Direction face) {
		return switch (face) {
			case DOWN, UP -> Direction.SOUTH;    // +z
			default -> Direction.UP;             // +y
		};
	}

	/**
	 * Byte-panel quadrant (0..3) of an in-cell hit on mount face f,
	 * using the same per-face U/V mapping as placement.
	 */
	public static int quadrantAt(Direction f, Vec3 hit, BlockPos cell) {
		double hx = hit.x - cell.getX(), hy = hit.y - cell.getY(),
			hz = hit.z - cell.getZ();
		boolean hiU, hiV;
		switch (f) {
			case DOWN, UP -> { hiU = hx >= 0.5; hiV = hz >= 0.5; }
			case NORTH -> { hiU = hx >= 0.5; hiV = hy >= 0.5; }
			case SOUTH -> { hiU = hx < 0.5; hiV = hy >= 0.5; }
			case WEST -> { hiU = hz < 0.5; hiV = hy >= 0.5; }
			default -> { hiU = hz < 0.5; hiV = hy >= 0.5; }
		}
		return (hiU ? 2 : 0) | (hiV ? 1 : 0);
	}

	/**
	 * Half-id (0..3) for a perpendicular half direction on a face:
	 * 0 = U-low {0,1}, 1 = U-high {2,3}, 2 = V-low {0,2},
	 * 3 = V-high {1,3}. -1 when half is parallel to the face axis.
	 */
	public static int halfIdFor(Direction face, Direction half) {
		if (half == uDir(face).getOpposite())
			return 0;
		if (half == uDir(face))
			return 1;
		if (half == vDir(face).getOpposite())
			return 2;
		if (half == vDir(face))
			return 3;
		return -1;
	}

	/** Inverse of halfIdFor; null for out-of-range ids. */
	public static Direction halfDirFor(Direction face, int halfId) {
		return switch (halfId) {
			case 0 -> uDir(face).getOpposite();
			case 1 -> uDir(face);
			case 2 -> vDir(face).getOpposite();
			case 3 -> vDir(face);
			default -> null;
		};
	}

	// --- footprints ---

	/**
	 * Claimed face-quadrant slot ids for an occupant anchor. PANEL
	 * ignores sub (full face); HALF expands the encoded pair.
	 */
	public static Set<Integer> slots(int slot, int kind) {
		Set<Integer> s = new HashSet<>();
		int face = (slot / 4) * 4;
		switch (kind) {
			case KIND_HALF_PANEL -> {
				// 0 = U-low {0,1}, 1 = U-high {2,3},
				// 2 = V-low {0,2}, 3 = V-high {1,3}
				int half = slot & 3;
				int base = half <= 1 ? half * 2 : half - 2;
				s.add(face + base);
				s.add(face + base + (half <= 1 ? 1 : 2));
			}
			case KIND_PANEL -> {
				for (int q = 0; q < 4; q++)
					s.add(face + q);
			}
			default -> s.add(slot);
		}
		return s;
	}

	/**
	 * Host blockstate footprint (occupant #0). Non-FACE panel positions
	 * claim all 24 slots (unsupported on the grid - never mergeable).
	 */
	public static Set<Integer> stateSlots(BlockState state) {
		Set<Integer> s = new HashSet<>();
		Block b = state.getBlock();
		if (b instanceof ContourBytePanelBlock) {
			s.add(id(state.getValue(ContourBytePanelBlock.FACING),
				state.getValue(ContourBytePanelBlock.QUADRANT)));
		} else if (b instanceof ContourHalfPanelBlock) {
			int hid = halfIdFor(state.getValue(ContourHalfPanelBlock.FACE),
				state.getValue(ContourHalfPanelBlock.HALF));
			if (hid >= 0)
				s.addAll(slots(id(state.getValue(ContourHalfPanelBlock.FACE),
					hid), KIND_HALF_PANEL));
			else
				for (int i = 0; i < SLOT_COUNT; i++)
					s.add(i); // degenerate state: conservatively full
		} else if (b instanceof ContourPanelBlock) {
			PanelPos p = state.getValue(ContourPanelBlock.POSITION);
			Direction f = faceDir(p);
			if (f != null)
				for (int q = 0; q < 4; q++)
					s.add(id(f, q));
			else
				for (int i = 0; i < SLOT_COUNT; i++)
					s.add(i);
		}
		return s;
	}

	/** Direction of a FACE_* panel position, null for mid/edge/diag. */
	public static Direction faceDir(PanelPos p) {
		return switch (p) {
			case FACE_UP -> Direction.UP;
			case FACE_DOWN -> Direction.DOWN;
			case FACE_NORTH -> Direction.NORTH;
			case FACE_SOUTH -> Direction.SOUTH;
			case FACE_EAST -> Direction.EAST;
			case FACE_WEST -> Direction.WEST;
			default -> null;
		};
	}

	/** FACE_* position for a direction (inverse of faceDir). */
	public static PanelPos facePos(Direction d) {
		return switch (d) {
			case UP -> PanelPos.FACE_UP;
			case DOWN -> PanelPos.FACE_DOWN;
			case NORTH -> PanelPos.FACE_NORTH;
			case SOUTH -> PanelPos.FACE_SOUTH;
			case EAST -> PanelPos.FACE_EAST;
			default -> PanelPos.FACE_WEST;
		};
	}

	/**
	 * Shared merge gate for the panel family (canBeReplaced): the held
	 * item must be a family block and its slot claim on the clicked face
	 * must be disjoint from the cell's occupants. Clicked-cell
	 * reconstruction mirrors ContourByteBlock - getClickedPos() is
	 * unreliable inside canBeReplaced, so the hit point is nudged
	 * opposite the clicked face to land inside the hit cell.
	 */
	public static boolean canMergeReplace(BlockState state,
		net.minecraft.world.item.context.BlockPlaceContext ctx) {
		if (!(ctx.getItemInHand().getItem()
			instanceof net.createcontour.item.ContourBlockItem cbi)
			|| !isPanelFamily(cbi.getBlock()))
			return false;
		Vec3 click = ctx.getClickLocation();
		BlockPos clicked = BlockPos.containing(click.subtract(
			Vec3.atLowerCornerOf(ctx.getClickedFace().getNormal())
				.scale(1.0E-4)));
		if (!(ctx.getLevel().getBlockEntity(clicked)
			instanceof net.createcontour.blockentity.ContourBlockEntity be))
			return false;
		int kind = kindOf(cbi.getBlock());
		int slot = incomingSlot(cbi.getBlock(), ctx.getClickedFace(), click,
			clicked);
		return slot >= 0 && be.panelSlotsFree(slots(slot, kind));
	}

	/**
	 * Incoming merge anchor for a family item: the face slot claim the
	 * held block would make on the hit mount face. The mount face is the
	 * hit surface normal (ctx.getClickedFace): the player merges onto
	 * the face they see/whose edge they clicked.
	 */
	public static int incomingSlot(Block itemBlock, Direction face, Vec3 hit,
		BlockPos cell) {
		if (itemBlock instanceof ContourBytePanelBlock)
			return id(face, quadrantAt(face, hit, cell));
		if (itemBlock instanceof ContourHalfPanelBlock) {
			int hid = halfIdFor(face,
				ContourHalfPanelBlock.halfDirAt(face, hit, cell));
			return id(face, Math.max(0, hid));
		}
		if (itemBlock instanceof ContourPanelBlock)
			return id(face, 0);
		return -1;
	}

	// --- geometry (mirrors the three block classes' box math) ---

	/** Host-state boxes (same geometry the block's own shape emits). */
	public static List<float[]> hostBoxes(BlockState state) {
		Block b = state.getBlock();
		if (b instanceof ContourBytePanelBlock)
			return ContourBytePanelBlock.boxesFor(state);
		if (b instanceof ContourHalfPanelBlock)
			return ContourHalfPanelBlock.boxesFor(state);
		if (b instanceof ContourPanelBlock)
			return ContourPanelBlock.boxesFor(
				state.getValue(ContourPanelBlock.POSITION));
		return List.of();
	}

	/** Pixel-space boxes for an extra panel occupant (anchor + kind). */
	public static List<float[]> occupantBoxes(int slot, int kind) {
		Direction face = faceOf(slot);
		return switch (kind) {
			case KIND_HALF_PANEL -> ContourHalfPanelBlock.boxesFor(face,
				halfDirFor(face, subOf(slot)));
			case KIND_PANEL -> ContourPanelBlock.boxesFor(facePos(face));
			default -> ContourBytePanelBlock.boxesFor(face, subOf(slot));
		};
	}

	/** Local-cell point-in-boxes test (generous epsilon for surfaces). */
	public static boolean inBoxes(List<float[]> boxes, Vec3 local) {
		double x = local.x * 16.0, y = local.y * 16.0, z = local.z * 16.0;
		for (float[] b : boxes)
			if (x >= b[0] - 1e-4 && x <= b[3] + 1e-4
				&& y >= b[1] - 1e-4 && y <= b[4] + 1e-4
				&& z >= b[2] - 1e-4 && z <= b[5] + 1e-4)
				return true;
		return false;
	}
}
