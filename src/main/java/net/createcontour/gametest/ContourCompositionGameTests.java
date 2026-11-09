package net.createcontour.gametest;

import net.createcontour.block.ContourHalfPanelBlock;
import net.createcontour.block.ContourBytePanelBlock;
import net.createcontour.block.ContourPanelBlock;
import net.createcontour.block.PanelSlots;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.item.ContourBlockItem;
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
 * Issue #12 composition tests: the panel face-quadrant channel
 * (byte_panel / half_panel / panel sharing one cell) and the interior
 * flat-pane axis channel, mirroring the byteComposition precedent.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourCompositionGameTests {

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

	// --- panel face-quadrant channel ---

	@GameTest(template = "test_empty")
	public void panelComposition(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// host: byte panel on the UP face, quadrant 0
			BlockState host = ContourRegistries.CONTOUR_BYTE_PANEL.get()
				.defaultBlockState()
				.setValue(ContourBytePanelBlock.FACING, Direction.UP)
				.setValue(ContourBytePanelBlock.QUADRANT, 0);
			ContourBlockEntity be = be(h, P, host);
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);

			// disjoint byte add ok; occupied + host slots refused
			int hostSlot = PanelSlots.id(Direction.UP, 0);
			h.assertTrue(be.addPanelOccupant(PanelSlots.id(Direction.UP, 2),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 1), "disjoint slot refused");
			h.assertTrue(!be.addPanelOccupant(PanelSlots.id(Direction.UP, 2),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.OAK_LOG.defaultBlockState(), 1),
				"occupied slot accepted");
			h.assertTrue(!be.addPanelOccupant(hostSlot,
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.OAK_LOG.defaultBlockState(), 1),
				"blockstate slot accepted");
			// cross-kind extras: a half on NORTH + a full face on EAST
			h.assertTrue(be.addPanelOccupant(PanelSlots.id(Direction.NORTH, 2),
				PanelSlots.KIND_HALF_PANEL,
				Blocks.OAK_PLANKS.defaultBlockState(), 1),
				"half merge refused");
			h.assertTrue(be.addPanelOccupant(PanelSlots.id(Direction.EAST, 0),
				PanelSlots.KIND_PANEL, Blocks.GLASS.defaultBlockState(), 0),
				"full-face merge refused");
			// overlapping a half pair must reject (N q0/q2 claimed)
			h.assertTrue(!be.addPanelOccupant(PanelSlots.id(Direction.NORTH, 0),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.OAK_LOG.defaultBlockState(), 1),
				"half-overlap accepted");
			h.assertTrue(be.hostUnits() == 4,
				"hostUnits ignores occupants: " + be.hostUnits());

			// per-occupant paint hits only that occupant
			int idx = be.panelOccupantIndex(PanelSlots.id(Direction.NORTH, 0));
			h.assertTrue(idx >= 0, "half occupant not resolved by member slot");
			h.assertTrue(be.paintPanelOccupant(idx,
				Blocks.DIORITE.defaultBlockState(), 1), "occupant paint failed");
			h.assertTrue(be.getMaterial().getBlock() == Blocks.DIRT,
				"occupant paint hit slot0");
			h.assertTrue(be.getPanelOccupants().get(1).material().getBlock()
				== Blocks.DIORITE && be.getPanelOccupants().get(1).paid() == 1,
				"occupant material/paid wrong");

			// union shape spans host plate + extras
			var vs = host.getShape(h.getLevel(), h.absolutePos(P),
				net.minecraft.world.phys.shapes.CollisionContext.empty());
			h.assertTrue(vs.bounds().getXsize() == 1.0
				&& vs.bounds().getYsize() == 1.0 && vs.bounds().getZsize() == 1.0,
				"union bounds not full cell");

			// drops: kind-aware blanks + per-occupant material refunds
			var drops = ((net.createcontour.block.ContourBlock) host.getBlock())
				.ledgerDrops(host, be);
			int bytes = 0, halves = 0, panels = 0, dirt = 0, stone = 0,
				diorite = 0;
			for (ItemStack s : drops) {
				if (s.getItem() == ContourRegistries.CONTOUR_BYTE_PANEL_ITEM.get())
					bytes += s.getCount();
				else if (s.getItem() == ContourRegistries.CONTOUR_HALF_PANEL_ITEM.get())
					halves += s.getCount();
				else if (s.getItem() == ContourRegistries.CONTOUR_PANEL_ITEM.get())
					panels += s.getCount();
				else if (s.getItem() == Items.DIRT)
					dirt += s.getCount();
				else if (s.getItem() == Items.STONE)
					stone += s.getCount();
				else if (s.getItem() == Items.DIORITE)
					diorite += s.getCount();
			}
			h.assertTrue(bytes == 2 && halves == 1 && panels == 1
				&& dirt == 1 && stone == 1 && diorite == 1,
				"drops wrong bytes=" + bytes + " halves=" + halves
					+ " panels=" + panels + " dirt=" + dirt + " stone=" + stone
					+ " diorite=" + diorite);

			// schematic requirement: per-occupant material + per-kind blank
			var reqList = be.getRequiredItems(host).getRequiredItems();
			int reqDirt = 0, reqStone = 0, reqDiorite = 0, reqByte = 0,
				reqHalf = 0, reqPanel = 0;
			for (var sr : reqList) {
				if (sr.stack.is(Items.DIRT))
					reqDirt += sr.stack.getCount();
				else if (sr.stack.is(Items.STONE))
					reqStone += sr.stack.getCount();
				else if (sr.stack.is(Items.DIORITE))
					reqDiorite += sr.stack.getCount();
				else if (sr.stack.is(ContourRegistries.CONTOUR_BYTE_PANEL_ITEM.get()))
					reqByte += sr.stack.getCount();
				else if (sr.stack.is(ContourRegistries.CONTOUR_HALF_PANEL_ITEM.get()))
					reqHalf += sr.stack.getCount();
				else if (sr.stack.is(ContourRegistries.CONTOUR_PANEL_ITEM.get()))
					reqPanel += sr.stack.getCount();
			}
			// the byte-kind extra's blank is a byte_panel item (same as
			// the host's own item, covered by Create's default entry)
			h.assertTrue(reqDirt == 1 && reqStone == 1 && reqDiorite == 1
				&& reqByte == 1 && reqHalf == 1 && reqPanel == 1,
				"requirement wrong dirt=" + reqDirt + " stone=" + reqStone
					+ " diorite=" + reqDiorite + " byte=" + reqByte
					+ " half=" + reqHalf + " panel=" + reqPanel);

			// save/load round-trip preserves occupants
			CompoundTag tag = be.saveWithFullMetadata(
				h.getLevel().registryAccess());
			h.setBlock(P, host);
			ContourBlockEntity be2 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			be2.loadWithComponents(tag, h.getLevel().registryAccess());
			h.assertTrue(be2.getPanelOccupants().size() == 3
				&& be2.getPanelOccupants().get(0).slot()
					== PanelSlots.id(Direction.UP, 2)
				&& be2.getPanelOccupants().get(1).kind()
					== PanelSlots.KIND_HALF_PANEL
				&& be2.getPanelOccupants().get(2).kind()
					== PanelSlots.KIND_PANEL
				&& be2.getPanelOccupants().get(1).material().getBlock()
					== Blocks.DIORITE,
				"occupants lost in save/load");

			// writeSafe exports the unit the schematic requirement will
			// charge at print (1 per custom-material occupant under
			// consume-on), not the stored paid - mirroring ByteExtras
			CompoundTag safe = new CompoundTag();
			be2.writeSafe(safe, h.getLevel().registryAccess());
			var extras = safe.getList("PanelExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND);
			h.assertTrue(extras.size() == 3
				&& extras.getCompound(1).getInt("Kind")
					== PanelSlots.KIND_HALF_PANEL
				&& extras.getCompound(1).getInt("Paid") == 1
				&& extras.getCompound(2).getInt("Paid") == 1,
				"writeSafe dropped occupants or paid");

			// item-route merge: byte_panel frame on a free UP quadrant.
			// The cell is dirt-painted, so look-fallback paints dirt and
			// charges one unit under consume-on.
			Player p2 = player(h);
			ItemStack frame = new ItemStack(
				ContourRegistries.CONTOUR_BYTE_PANEL_ITEM.get(), 1);
			p2.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p2.getInventory().add(new ItemStack(Items.DIRT, 1));
			var resMerge = frame.getItem().useOn(ctx(h, p2, frame, P,
				Direction.UP, new Vec3(0.25, 0.49, 0.25)));
			h.assertTrue(resMerge.consumesAction(),
				"quadrant merge rejected res=" + resMerge
					+ " occupied=" + be2.occupiedPanelSlots());
			h.assertTrue(be2.getPanelOccupants().size() == 4,
				"merge did not add occupant");
			h.assertTrue(be2.getPanelOccupants().get(3).material().getBlock()
				== Blocks.DIRT && be2.getPanelOccupants().get(3).paid() == 1,
				"merged occupant ledger wrong");
			h.assertTrue(frame.isEmpty(), "merged frame not consumed");
			h.assertTrue(p2.getInventory().countItem(Items.DIRT) == 0,
				"merge material not charged");

			// occupied quadrant merge falls through to a normal FAIL
			Player p3 = player(h);
			ItemStack frame2 = new ItemStack(
				ContourRegistries.CONTOUR_BYTE_PANEL_ITEM.get(), 1);
			p3.setItemInHand(InteractionHand.MAIN_HAND, frame2);
			var resBad = frame2.getItem().useOn(ctx(h, p3, frame2, P,
				Direction.UP, new Vec3(0.25, 0.49, 0.25)));
			// same quadrant as the just-merged occupant: merge refuses
			// (adjacent placement may still consume the frame, so only
			// the occupant count is asserted)
			h.assertTrue(be2.getPanelOccupants().size() == 4,
				"occupied quadrant accepted a merge");

			// wrench skips occupied quadrants instead of colliding
			var absP = h.absolutePos(P);
			((net.createcontour.block.ContourBlock) h.getBlockState(P)
				.getBlock()).onWrenched(h.getBlockState(P),
				new UseOnContext(h.getLevel(), p2, InteractionHand.MAIN_HAND,
					ItemStack.EMPTY, new BlockHitResult(
						Vec3.atCenterOf(absP), Direction.UP, absP, false)));
			BlockState afterWrench = h.getBlockState(P);
			h.assertTrue(afterWrench.hasProperty(ContourBytePanelBlock.QUADRANT),
				"wrench changed block");
			int newSlot = PanelSlots.id(afterWrench
				.getValue(ContourBytePanelBlock.FACING),
				afterWrench.getValue(ContourBytePanelBlock.QUADRANT));
			h.assertTrue(be2.getPanelOccupants().stream().noneMatch(o ->
				PanelSlots.slots(o.slot(), o.kind()).contains(newSlot)),
				"wrench landed slot0 on an occupied slot");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- half-panel host + panel host coverage ---

	@GameTest(template = "test_empty")
	public void panelFamilyHosts(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// half-panel host on UP face, NORTH half -> pair {q0,q2}
			BlockState halfHost = ContourRegistries.CONTOUR_HALF_PANEL.get()
				.defaultBlockState()
				.setValue(ContourHalfPanelBlock.FACE, Direction.UP)
				.setValue(ContourHalfPanelBlock.HALF, Direction.NORTH);
			ContourBlockEntity be = be(h, P, halfHost);
			h.assertTrue(be.occupiedPanelSlots().containsAll(
				java.util.Set.of(PanelSlots.id(Direction.UP, 0),
					PanelSlots.id(Direction.UP, 2))),
				"half host footprint wrong: " + be.occupiedPanelSlots());
			// byte extra on the free half (q3) ok; inside the claimed
			// pair (q0/q2) rejects
			h.assertTrue(be.addPanelOccupant(PanelSlots.id(Direction.UP, 3),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 0), "free half refused");
			h.assertTrue(!be.addPanelOccupant(PanelSlots.id(Direction.UP, 0),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 0),
				"claimed half accepted");
			// a full face elsewhere merges
			h.assertTrue(be.addPanelOccupant(PanelSlots.id(Direction.DOWN, 0),
				PanelSlots.KIND_PANEL,
				Blocks.GLASS.defaultBlockState(), 0), "down face refused");

			// positional panel host: FACE_NORTH claims all 4 north slots
			BlockState panelHost = ContourRegistries.CONTOUR_PANEL.get()
				.defaultBlockState().setValue(ContourPanelBlock.POSITION,
					ContourPanelBlock.PanelPos.FACE_NORTH);
			ContourBlockEntity be2 = be(h, P.east(2), panelHost);
			h.assertTrue(be2.occupiedPanelSlots().containsAll(
				java.util.Set.of(PanelSlots.id(Direction.NORTH, 0),
					PanelSlots.id(Direction.NORTH, 1),
					PanelSlots.id(Direction.NORTH, 2),
					PanelSlots.id(Direction.NORTH, 3))),
				"face panel footprint wrong");
			h.assertTrue(!be2.addPanelOccupant(PanelSlots.id(Direction.NORTH, 1),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 0),
				"face-panel overlap accepted");
			h.assertTrue(be2.addPanelOccupant(PanelSlots.id(Direction.SOUTH, 1),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 0), "south byte refused");

			// unsupported positions (mid/edge/diag) claim every slot:
			// conservative, never mergeable (ADR-016a named unsupported)
			ContourBlockEntity be3 = be(h, P.west(2),
				panelHost.setValue(ContourPanelBlock.POSITION,
					ContourPanelBlock.PanelPos.MID_X));
			h.assertTrue(be3.occupiedPanelSlots().size() == 24,
				"mid position did not claim all slots");
			h.assertTrue(!be3.addPanelOccupant(PanelSlots.id(Direction.UP, 0),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 0),
				"mid-position cell accepted an occupant");

			// half-panel item merging into the half-panel cell claims
			// the clicked half's pair via the item route
			Player p = player(h);
			ItemStack frame = new ItemStack(
				ContourRegistries.CONTOUR_HALF_PANEL_ITEM.get(), 1);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			// click south face low band -> SOUTH face V-low pair {12,14}
			var res = frame.getItem().useOn(ctx(h, p, frame, P,
				Direction.SOUTH, new Vec3(0, -0.25, 0.49)));
			h.assertTrue(res.consumesAction(), "half item merge rejected");
			h.assertTrue(be.getPanelOccupants().stream().anyMatch(o ->
				o.kind() == PanelSlots.KIND_HALF_PANEL
					&& o.slot() == PanelSlots.id(Direction.SOUTH, 2)),
				"half merge anchor wrong: " + be.getPanelOccupants());
			h.assertTrue(frame.isEmpty(), "half frame not consumed");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- per-occupant paint gesture via useItemOn ---

	@GameTest(template = "test_empty")
	public void panelOccupantPainting(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = player(h);
			BlockState host = ContourRegistries.CONTOUR_BYTE_PANEL.get()
				.defaultBlockState()
				.setValue(ContourBytePanelBlock.FACING, Direction.UP)
				.setValue(ContourBytePanelBlock.QUADRANT, 0);
			ContourBlockEntity be = be(h, P, host);
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);
			h.assertTrue(be.addPanelOccupant(PanelSlots.id(Direction.UP, 3),
				PanelSlots.KIND_BYTE_PANEL,
				Blocks.STONE.defaultBlockState(), 1), "extra refused");

			// hit inside the extra's box -> paints the extra, not slot0
			var absP = h.absolutePos(P);
			p.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			var r = h.getBlockState(P).useItemOn(
				new ItemStack(Items.OAK_PLANKS, 1), h.getLevel(), p,
				InteractionHand.MAIN_HAND, new BlockHitResult(
					new Vec3(absP.getX() + 0.75, absP.getY() + 0.99,
						absP.getZ() + 0.75),
					Direction.UP, absP, false));
			h.assertTrue(r.consumesAction(), "extra paint rejected: " + r);
			h.assertTrue(be.getPanelOccupants().get(0).material().getBlock()
				== Blocks.OAK_PLANKS
				&& be.getPanelOccupants().get(0).paid() == 1,
				"extra paint ledger wrong");
			h.assertTrue(be.getMaterial().getBlock() == Blocks.DIRT,
				"extra paint hit slot0");
			// replaced paid unit refunded as an item
			h.assertTrue(p.getInventory().countItem(Items.STONE) == 1,
				"old paid unit not refunded");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- interior flat-pane axis channel ---

	@GameTest(template = "test_empty")
	public void flatPaneComposition(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			BlockState host = ContourRegistries.CONTOUR_FLAT_PANE.get()
				.defaultBlockState();
			ContourBlockEntity be = be(h, P, host);
			be.applyPayload(Blocks.DIRT.defaultBlockState(), 1,
				Direction.NORTH, false);

			// same-axis exclusive; different axes cross freely
			h.assertTrue(!be.addPaneOccupant(Direction.Axis.Y.ordinal(),
				Blocks.STONE.defaultBlockState(), 1),
				"host axis accepted");
			h.assertTrue(be.addPaneOccupant(Direction.Axis.X.ordinal(),
				Blocks.STONE.defaultBlockState(), 1), "x pane refused");
			h.assertTrue(!be.addPaneOccupant(Direction.Axis.X.ordinal(),
				Blocks.OAK_LOG.defaultBlockState(), 1),
				"occupied axis accepted");
			h.assertTrue(be.addPaneOccupant(Direction.Axis.Z.ordinal(),
				Blocks.OAK_PLANKS.defaultBlockState(), 0), "z pane refused");
			h.assertTrue(be.hostUnits() == 3,
				"hostUnits ignores panes: " + be.hostUnits());

			// union shape spans the crossed sheets
			var vs = host.getShape(h.getLevel(), h.absolutePos(P),
				net.minecraft.world.phys.shapes.CollisionContext.empty());
			h.assertTrue(vs.bounds().getXsize() == 1.0
				&& vs.bounds().getYsize() == 1.0 && vs.bounds().getZsize() == 1.0,
				"pane union bounds not full cell");

			// drops: 3 flat-pane blanks + per-occupant refunds
			var drops = ((net.createcontour.block.ContourBlock) host.getBlock())
				.ledgerDrops(host, be);
			int panes = 0, dirt = 0, stone = 0;
			for (ItemStack s : drops) {
				if (s.getItem() == ContourRegistries.CONTOUR_FLAT_PANE_ITEM.get())
					panes += s.getCount();
				else if (s.getItem() == Items.DIRT)
					dirt += s.getCount();
				else if (s.getItem() == Items.STONE)
					stone += s.getCount();
			}
			h.assertTrue(panes == 3 && dirt == 1 && stone == 1,
				"pane drops wrong panes=" + panes + " dirt=" + dirt
					+ " stone=" + stone);

			// save/load + writeSafe
			CompoundTag tag = be.saveWithFullMetadata(
				h.getLevel().registryAccess());
			h.setBlock(P, host);
			ContourBlockEntity be2 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			be2.loadWithComponents(tag, h.getLevel().registryAccess());
			h.assertTrue(be2.getPaneOccupants().size() == 2
				&& be2.getPaneOccupants().get(0).axis()
					== Direction.Axis.X.ordinal()
				&& be2.getPaneOccupants().get(0).paid() == 1,
				"pane occupants lost in save/load");
			CompoundTag safe = new CompoundTag();
			be2.writeSafe(safe, h.getLevel().registryAccess());
			var extras = safe.getList("PaneExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND);
			h.assertTrue(extras.size() == 2
				&& extras.getCompound(0).getInt("Axis")
					== Direction.Axis.X.ordinal()
				&& extras.getCompound(0).getInt("Paid") == 1,
				"writeSafe dropped pane occupants");

			// item-route merge: flat-pane frame clicked on a side face
			// claims that axis - every axis is now occupied, so a fresh
			// cell hosts the merge instead
			BlockPos q = P.east(2);
			ContourBlockEntity fresh = be(h, q, host);
			Player p2 = player(h);
			ItemStack frame = new ItemStack(
				ContourRegistries.CONTOUR_FLAT_PANE_ITEM.get(), 1);
			p2.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p2.getInventory().add(new ItemStack(Items.DIRT, 1));
			var res = frame.getItem().useOn(ctx(h, p2, frame, q,
				Direction.NORTH, new Vec3(0, 0, -0.49)));
			h.assertTrue(res.consumesAction(), "z-axis merge rejected: " + res);
			h.assertTrue(fresh.getPaneOccupants().size() == 1
				&& fresh.getPaneOccupants().get(0).axis()
					== Direction.Axis.Z.ordinal(),
				"z merge axis wrong");
			h.assertTrue(frame.isEmpty(), "pane frame not consumed");
			// same-axis re-merge on the occupied Y channel falls through
			ItemStack frame3 = new ItemStack(
				ContourRegistries.CONTOUR_FLAT_PANE_ITEM.get(), 1);
			p2.setItemInHand(InteractionHand.MAIN_HAND, frame3);
			frame3.getItem().useOn(ctx(h, p2, frame3, q,
				Direction.UP, new Vec3(0, 0.49, 0)));
			h.assertTrue(fresh.getPaneOccupants().size() == 1,
				"occupied Y axis accepted a merge");

			// per-occupant paint via containment: a hit inside the X
			// sheet but outside the Y sheet paints the X occupant
			Player p3 = player(h);
			p3.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			var absP = h.absolutePos(P);
			var r = h.getBlockState(P).useItemOn(
				new ItemStack(Items.OAK_PLANKS, 1), h.getLevel(), p3,
				InteractionHand.MAIN_HAND, new BlockHitResult(
					new Vec3(absP.getX() + 0.5, absP.getY() + 0.9,
						absP.getZ() + 0.5),
					Direction.UP, absP, false));
			h.assertTrue(r.consumesAction(), "pane extra paint rejected: " + r);
			h.assertTrue(be2.getPaneOccupants().get(0).material().getBlock()
				== Blocks.OAK_PLANKS
				&& be2.getPaneOccupants().get(0).paid() == 1,
				"pane occupant paint wrong");
			h.assertTrue(be2.getMaterial().getBlock() == Blocks.DIRT,
				"pane paint hit slot0");

			// wrench is a no-op for panes (no positional state)
			BlockState before = h.getBlockState(P);
			var wr = ((net.createcontour.block.ContourBlock) before.getBlock())
				.onWrenched(before, new UseOnContext(h.getLevel(), p3,
					InteractionHand.MAIN_HAND, ItemStack.EMPTY,
					new BlockHitResult(Vec3.atCenterOf(absP), Direction.UP,
						absP, false)));
			h.assertTrue(h.getBlockState(P) == before,
				"pane wrench mutated state");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}
}
