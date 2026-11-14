package net.createcontour.gametest;

import net.createcontour.block.ContourByteBlock;
import net.createcontour.block.ContourBytePanelBlock;
import net.createcontour.block.ContourBoardBlock;
import net.createcontour.block.PanelSlots;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Issue #22 / ADR-018 universal host tests: one unified Occupant list,
 * cross-kind co-residence gated purely by geometric disjointness, the
 * legacy per-channel NBT keys migrating into the unified list on load,
 * and the unified drop/requirement ledger. Octants occupy an 8px
 * corner cube, byte_panel plates a quadrant face slice (2px), board
 * shells a full-face 1px sheet - co-residence is arranged on faces
 * the host octant does not touch.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourUniversalHostGameTests {

	// Anchor at (2,1,2) so neighbours stay inside the 5x5x5 area.
	private static final BlockPos P = new BlockPos(2, 1, 2);

	private ContourBlockEntity be(GameTestHelper h, BlockPos pos, BlockState s) {
		h.setBlock(pos, s);
		return (ContourBlockEntity) h.getLevel()
			.getBlockEntity(h.absolutePos(pos));
	}

	private Player player(GameTestHelper h) {
		return h.makeMockPlayer(GameType.SURVIVAL);
	}

	private UseOnContext ctx(GameTestHelper h, Player p, ItemStack held,
		BlockPos pos, Direction face, Vec3 hitLocal) {
		var abs = h.absolutePos(pos);
		return new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND,
			held, new BlockHitResult(abs.getCenter().add(hitLocal), face,
				abs, false));
	}

	private static BlockState byteHost(ContourByteBlock.Octant o) {
		return ContourRegistries.CONTOUR_BYTE.get().defaultBlockState()
			.setValue(ContourByteBlock.OCTANT, o);
	}

	// --- byte + panel co-residence in one cell ---

	@GameTest(template = "test_empty")
	public void bytePlusPanelCoResidence(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			ContourBlockEntity be = be(h, P,
				byteHost(ContourByteBlock.Octant.X0Y0Z0));
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);
			// UP face plate y15..16 is disjoint from the y0..8 octant.
			h.assertTrue(be.addPanelOccupant(
				PanelSlots.id(Direction.UP, 0), PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 1),
				"byte+panel co-residence refused");
			h.assertTrue(be.getOccupants().size() == 1
				&& be.getPanelOccupants().size() == 1
				&& be.getOccupants().get(0).kind()
					== ContourBlockEntity.OccupantKind.PANEL,
				"unified list did not record the panel extra");
			// A second byte in a far octant still fits (intra-kind).
			h.assertTrue(be.addByteOccupant(
				ContourByteBlock.Octant.X1Y0Z0.ordinal(),
				Blocks.OAK_PLANKS.defaultBlockState(), 0),
				"second byte refused beside panel");
			// A plate slicing THROUGH the host octant must reject:
			// DOWN q0 occupies y0..1 x0..8 z0..8 - inside the octant.
			h.assertTrue(!be.addPanelOccupant(
				PanelSlots.id(Direction.DOWN, 0), PanelSlots.KIND_BYTE_PANEL,
				Blocks.GLASS.defaultBlockState(), 0),
				"overlapping plate accepted on byte host");
			// NORTH q0 plate (z0..1 x0..8 y0..8) also hits the octant.
			h.assertTrue(!be.addPanelOccupant(
				PanelSlots.id(Direction.NORTH, 0), PanelSlots.KIND_BYTE_PANEL,
				Blocks.GLASS.defaultBlockState(), 0),
				"octant-crossing plate accepted on byte host");
			h.assertTrue(be.getOccupants().size() == 2,
				"rejected merge mutated the occupant list");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- byte + board co-residence via the item-route merge ---

	@GameTest(template = "test_empty")
	public void bytePlusBoardCoResidence(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			ContourBlockEntity be = be(h, P,
				byteHost(ContourByteBlock.Octant.X0Y0Z0));
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);
			// Board item clicked on the cell's UP face: the y15..16 shell
			// is disjoint from the y0..8 octant -> BOARD occupant merge.
			Player p = player(h);
			ItemStack frame = new ItemStack(
				ContourRegistries.CONTOUR_BOARD_ITEM.get(), 1);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			// the cell is dirt-painted: look-fallback charges one dirt
			// for the new shell under consume-on.
			p.getInventory().add(new ItemStack(Items.DIRT, 1));
			var res = frame.getItem().useOn(ctx(h, p, frame, P,
				Direction.UP, new Vec3(0, 0.49, 0)));
			h.assertTrue(res.consumesAction(),
				"board-onto-byte merge rejected: " + res);
			h.assertTrue(be.getBoardOccupants().size() == 1
				&& be.getBoardOccupants().get(0).face()
					== Direction.UP.ordinal(),
				"board occupant not added to byte cell");
			h.assertTrue(be.getOccupants().get(0).kind()
				== ContourBlockEntity.OccupantKind.BOARD,
				"unified list kind wrong");
			// The cell's blockstate stayed a byte: the shell lives in the
			// occupant list, not in a second blockstate.
			h.assertTrue(h.getBlockState(P).getBlock()
				== ContourRegistries.CONTOUR_BYTE.get(),
				"merge rewrote the host blockstate");
			// A second board face shell disjoint from the octant (SOUTH)
			// merges too; the merged record is geometric, not channel.
			h.assertTrue(be.addBoardOccupant(Direction.SOUTH.ordinal(),
				Blocks.STONE.defaultBlockState(), 1),
				"second disjoint board shell refused");
			h.assertTrue(be.getOccupants().size() == 2,
				"board shells not counted as occupants");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- panel + board co-residence in one cell ---

	@GameTest(template = "test_empty")
	public void panelPlusBoardCoResidence(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// host: byte_panel plate on UP quadrant 0 (y15..16 x0..8 z0..8)
			BlockState host = ContourRegistries.CONTOUR_BYTE_PANEL.get()
				.defaultBlockState()
				.setValue(ContourBytePanelBlock.FACING, Direction.UP)
				.setValue(ContourBytePanelBlock.QUADRANT, 0);
			ContourBlockEntity be = be(h, P, host);
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);
			// board DOWN shell (y0..1) is disjoint from the UP plate.
			h.assertTrue(be.addBoardOccupant(Direction.DOWN.ordinal(),
				Blocks.STONE.defaultBlockState(), 1),
				"board+panel co-residence refused");
			// board UP shell overlaps the host plate - reject.
			h.assertTrue(!be.addBoardOccupant(Direction.UP.ordinal(),
				Blocks.GLASS.defaultBlockState(), 0),
				"same-face board shell accepted over plate");
			// a byte_panel extra in the free UP quadrant merges
			// (same-kind slot rule), and stays disjoint from DOWN.
			h.assertTrue(be.addPanelOccupant(
				PanelSlots.id(Direction.UP, 3), PanelSlots.KIND_BYTE_PANEL,
				Blocks.OAK_PLANKS.defaultBlockState(), 0),
				"same-kind panel extra refused beside board");
			h.assertTrue(be.getOccupants().size() == 2
				&& be.getBoardOccupants().size() == 1
				&& be.getPanelOccupants().size() == 1,
				"co-resident occupants miscounted");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- geometric-overlap rejection: board shell vs occupied octant ---

	@GameTest(template = "test_empty")
	public void geometricOverlapRejected(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			ContourBlockEntity be = be(h, P,
				byteHost(ContourByteBlock.Octant.X0Y0Z0));
			// Board shells are full-face 1px sheets; octant X0Y0Z0
			// (x0..8,y0..8,z0..8) punches through DOWN/NORTH/WEST.
			for (Direction d : new Direction[]{Direction.DOWN,
				Direction.NORTH, Direction.WEST})
				h.assertTrue(!be.addBoardOccupant(d.ordinal(),
					Blocks.GLASS.defaultBlockState(), 0),
					"overlapping " + d + " shell accepted");
			// The three faces the octant does not touch stay free.
			for (Direction d : new Direction[]{Direction.UP,
				Direction.SOUTH, Direction.EAST})
				h.assertTrue(be.addBoardOccupant(d.ordinal(),
					Blocks.GLASS.defaultBlockState(), 0),
					"disjoint " + d + " shell refused");
			// A byte extra in the opposite corner overlaps the merged
			// UP/SOUTH/EAST shells on the inside corner - geometrically
			// it touches each shell's far edge region, so it rejects.
			h.assertTrue(!be.addByteOccupant(
				ContourByteBlock.Octant.X1Y1Z1.ordinal(),
				Blocks.STONE.defaultBlockState(), 0),
				"octant overlapping board shells accepted");
			// A mid-plane pane always crosses an octant cube (it spans
			// the cell centre 7..9) - never co-resident on a byte host.
			h.assertTrue(!be.addPaneOccupant(Direction.Axis.X.ordinal(),
				Blocks.STONE.defaultBlockState(), 0),
				"pane plane accepted inside byte host");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- legacy NBT migration: ByteExtras + PanelExtras -> Occupants ---

	@GameTest(template = "test_empty")
	public void legacyNbtMigration(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			BlockState host = byteHost(ContourByteBlock.Octant.X0Y0Z0);
			ContourBlockEntity be = be(h, P, host);
			// A pre-issue-#22 save: per-channel keys, no "Occupants" list.
			CompoundTag legacy = be.saveWithFullMetadata(
				h.getLevel().registryAccess());
			legacy.remove("Occupants");
			var byteList = new net.minecraft.nbt.ListTag();
			CompoundTag b = new CompoundTag();
			b.putInt("Octant", ContourByteBlock.Octant.X1Y1Z1.ordinal());
			b.put("Material", net.minecraft.nbt.NbtUtils.writeBlockState(
				Blocks.STONE.defaultBlockState()));
			b.putInt("Paid", 1);
			byteList.add(b);
			legacy.put("ByteExtras", byteList);
			var panelList = new net.minecraft.nbt.ListTag();
			CompoundTag e = new CompoundTag();
			e.putInt("Slot", PanelSlots.id(Direction.NORTH, 1));
			e.putInt("Kind", PanelSlots.KIND_BYTE_PANEL);
			e.put("Material", net.minecraft.nbt.NbtUtils.writeBlockState(
				Blocks.OAK_PLANKS.defaultBlockState()));
			e.putInt("Paid", 0);
			panelList.add(e);
			legacy.put("PanelExtras", panelList);
			h.setBlock(P, host);
			ContourBlockEntity be2 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			be2.loadWithComponents(legacy, h.getLevel().registryAccess());
			h.assertTrue(be2.getOccupants().size() == 2
				&& be2.getByteOccupants().size() == 1
				&& be2.getByteOccupants().get(0).octant()
					== ContourByteBlock.Octant.X1Y1Z1.ordinal()
				&& be2.getByteOccupants().get(0).paid() == 1
				&& be2.getPanelOccupants().size() == 1
				&& be2.getPanelOccupants().get(0).slot()
					== PanelSlots.id(Direction.NORTH, 1)
				&& be2.getPanelOccupants().get(0).material().getBlock()
					== Blocks.OAK_PLANKS,
				"legacy extras did not migrate into the unified list");
			// Migration is forward-only: re-save writes only Occupants.
			CompoundTag resaved = be2.saveWithFullMetadata(
				h.getLevel().registryAccess());
			h.assertTrue(resaved.contains("Occupants")
				&& !resaved.contains("ByteExtras")
				&& !resaved.contains("PanelExtras")
				&& !resaved.contains("PaneExtras")
				&& !resaved.contains("BoardExtras"),
				"resave re-emitted legacy keys");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- unified drops + requirements across multiple kinds ---

	@GameTest(template = "test_empty")
	public void unifiedDropsMultiKind(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			ContourBlockEntity be = be(h, P,
				byteHost(ContourByteBlock.Octant.X0Y0Z0));
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);
			// byte extra X1Y0Z0 (x8..16,y0..8,z0..8); a SOUTH q0 plate
			// (x8..16,y0..8,z15..16) clears both octants and the UP shell;
			// the UP shell clears both octants (y0..8) and the plate.
			h.assertTrue(be.addByteOccupant(
				ContourByteBlock.Octant.X1Y0Z0.ordinal(),
				Blocks.STONE.defaultBlockState(), 1), "byte extra refused");
			h.assertTrue(be.addPanelOccupant(
				PanelSlots.id(Direction.SOUTH, 0), PanelSlots.KIND_BYTE_PANEL,
				Blocks.OAK_PLANKS.defaultBlockState(), 1),
				"panel extra refused");
			h.assertTrue(be.addBoardOccupant(Direction.UP.ordinal(),
				Blocks.GLASS.defaultBlockState(), 1), "board extra refused");
			BlockState host = h.getBlockState(P);
			var drops = ((net.createcontour.block.ContourBlock) host.getBlock())
				.ledgerDrops(host, be);
			int bytes = 0, bytePanels = 0, boards = 0, dirt = 0, stone = 0,
				planks = 0, glass = 0;
			for (ItemStack s : drops) {
				if (s.getItem() == ContourRegistries.CONTOUR_BYTE_ITEM.get())
					bytes += s.getCount();
				else if (s.getItem() == ContourRegistries
					.CONTOUR_BYTE_PANEL_ITEM.get())
					bytePanels += s.getCount();
				else if (s.getItem() == ContourRegistries.CONTOUR_BOARD_ITEM
					.get())
					boards += s.getCount();
				else if (s.getItem() == Items.DIRT)
					dirt += s.getCount();
				else if (s.getItem() == Items.STONE)
					stone += s.getCount();
				else if (s.getItem() == Items.OAK_PLANKS)
					planks += s.getCount();
				else if (s.getItem() == Items.GLASS)
					glass += s.getCount();
			}
			// 2 byte blanks (host + byte extra), one blank per other
			// kind, and every paid unit refunded as material.
			h.assertTrue(bytes == 2 && bytePanels == 1 && boards == 1
				&& dirt == 1 && stone == 1 && planks == 1 && glass == 1,
				"unified drops wrong bytes=" + bytes + " bp=" + bytePanels
					+ " boards=" + boards + " dirt=" + dirt + " stone="
					+ stone + " planks=" + planks + " glass=" + glass);

			// schematic requirement mirrors the drops: material per paid
			// unit plus one blank per occupant (+ host unit).
			var req = be.getRequiredItems(host).getRequiredItems();
			int rByte = 0, rBp = 0, rBoard = 0, rMat = 0;
			for (var sr : req) {
				if (sr.stack.is(ContourRegistries.CONTOUR_BYTE_ITEM.get()))
					rByte += sr.stack.getCount();
				else if (sr.stack.is(
					ContourRegistries.CONTOUR_BYTE_PANEL_ITEM.get()))
					rBp += sr.stack.getCount();
				else if (sr.stack.is(
					ContourRegistries.CONTOUR_BOARD_ITEM.get()))
					rBoard += sr.stack.getCount();
				else
					rMat += sr.stack.getCount();
			}
			h.assertTrue(rByte == 1 && rBp == 1 && rBoard == 1 && rMat == 4,
				"unified requirement wrong byte=" + rByte + " bp=" + rBp
					+ " board=" + rBoard + " mat=" + rMat);
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}
}
