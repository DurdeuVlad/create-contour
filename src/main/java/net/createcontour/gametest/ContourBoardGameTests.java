package net.createcontour.gametest;

import net.createcontour.block.ContourBoardBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.material.MaterialSafety;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Issue #24 board tests: the six-face 1px-sheet channel - fresh + merge
 * placement through the item route, per-face paint/ledger isolation,
 * union collision, schematic-safe persistence and the wrench's
 * face-targeted removal.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourBoardGameTests {

	// Anchor at (2,1,2) so neighbours stay inside the 5x5x5 area.
	private static final BlockPos P = new BlockPos(2, 1, 2);

	private ContourBlockEntity be(GameTestHelper h, BlockPos pos,
		BlockState s) {
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

	private static BlockState faces(Direction... ds) {
		BlockState s = ContourRegistries.CONTOUR_BOARD.get()
			.defaultBlockState()
			.setValue(ContourBoardBlock.DOWN, false);
		for (Direction d : ds)
			s = s.setValue(ContourBoardBlock.faceProperty(d), true);
		return s;
	}

	// --- multi-face placement (fresh + per-face merges) ---

	@GameTest(template = "test_empty")
	public void boardMultiFacePlacement(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// registry + datapack surface
			h.assertTrue(ContourRegistries.CONTOUR_BOARD.get() != null
				&& ContourRegistries.CONTOUR_BOARD_ITEM.get() != null,
				"board not registered");
			var rm = h.getLevel().getServer().getRecipeManager();
			h.assertTrue(rm.byKey(net.minecraft.resources.ResourceLocation
				.fromNamespaceAndPath("create_contour", "contour_board"))
				.isPresent(), "board recipe missing");

			Player p = player(h);
			h.setBlock(P, Blocks.STONE.defaultBlockState());

			// fresh placement on the stone top face: mounts the DOWN
			// sheet on the adjacent cell and paints it like the clicked
			// block (look QoL), charging one stone under consume-on.
			// Equip BEFORE filling the inventory: setItemInHand writes
			// the selected slot, which add() would otherwise reuse.
			ItemStack frame = new ItemStack(
				ContourRegistries.CONTOUR_BOARD_ITEM.get(), 1);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p.getInventory().add(new ItemStack(Items.STONE, 10));
			var r0 = frame.getItem().useOn(ctx(h, p, frame, P,
				Direction.UP, new Vec3(0, 0.49, 0)));
			h.assertTrue(r0.consumesAction(),
				"fresh placement rejected: " + r0);
			BlockPos cell = P.above();
			BlockState placed = h.getBlockState(cell);
			h.assertTrue(placed.getBlock() instanceof ContourBoardBlock,
				"placed block is not a board: " + placed);
			h.assertTrue(ContourBoardBlock.facePresent(placed, Direction.DOWN)
				&& ContourBoardBlock.faceCount(placed) == 1,
				"first sheet is not the clicked-down face");
			ContourBlockEntity be = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(cell));
			h.assertTrue(be.getBoardOccupants().size() == 1
				&& be.getBoardOccupants().get(0).face()
					== Direction.DOWN.ordinal()
				&& be.getBoardOccupants().get(0).material().getBlock()
					== Blocks.STONE
				&& be.getBoardOccupants().get(0).paid() == 1,
				"seeded face ledger wrong: " + be.getBoardOccupants());
			h.assertTrue(MaterialSafety.isNeutral(be.getMaterial()),
				"board host material must stay neutral");
			h.assertTrue(p.getInventory().countItem(Items.STONE) == 9,
				"fresh placement did not charge the material");

			// merge the remaining five faces: each click on a free face
			// of the cell claims that face's sheet.
			Direction[] fs = {Direction.SOUTH, Direction.NORTH,
				Direction.EAST, Direction.WEST, Direction.UP};
			Vec3[] hs = {new Vec3(0, 0, 0.49), new Vec3(0, 0, -0.49),
				new Vec3(0.49, 0, 0), new Vec3(-0.49, 0, 0),
				new Vec3(0, 0.49, 0)};
			for (int i = 0; i < fs.length; i++) {
				ItemStack f = new ItemStack(
					ContourRegistries.CONTOUR_BOARD_ITEM.get(), 1);
				p.setItemInHand(InteractionHand.MAIN_HAND, f);
				var r = f.getItem().useOn(
					ctx(h, p, f, cell, fs[i], hs[i]));
				h.assertTrue(r.consumesAction(),
					"merge on " + fs[i] + " rejected: " + r);
				h.assertTrue(f.isEmpty(),
					"merged frame not consumed on " + fs[i]);
			}
			BlockState full = h.getBlockState(cell);
			h.assertTrue(ContourBoardBlock.faceCount(full) == 6,
				"cell did not accumulate all six faces: " + full);
			h.assertTrue(be.getBoardOccupants().size() == 6
				&& be.hostUnits() == 6,
				"occupants/host units wrong after merges: "
					+ be.getBoardOccupants().size() + "/"
					+ be.hostUnits());
			for (Direction d : Direction.values()) {
				int idx = be.boardOccupantIndex(d.ordinal());
				h.assertTrue(idx >= 0
					&& be.getBoardOccupants().get(idx).material()
						.getBlock() == Blocks.STONE
					&& be.getBoardOccupants().get(idx).paid() == 1,
					"face " + d + " did not inherit the cell paint");
			}
			// 1 fresh + 5 merges = 6 stone charged
			h.assertTrue(p.getInventory().countItem(Items.STONE) == 4,
				"merge material charge wrong");

			// a click on an OCCUPIED face no longer merges into the
			// cell - the free-face gate falls through to a normal
			// adjacent placement hugging the clicked surface.
			ItemStack extra = new ItemStack(
				ContourRegistries.CONTOUR_BOARD_ITEM.get(), 1);
			p.setItemInHand(InteractionHand.MAIN_HAND, extra);
			var rFull = extra.getItem().useOn(ctx(h, p, extra, cell,
				Direction.SOUTH, new Vec3(0, 0, 0.49)));
			h.assertTrue(rFull.consumesAction(),
				"occupied-face click consumed nothing: " + rFull);
			h.assertTrue(ContourBoardBlock.faceCount(h.getBlockState(cell)) == 6
				&& be.getBoardOccupants().size() == 6,
				"occupied face merged into the full cell");
			BlockState adj = h.getBlockState(cell.south());
			h.assertTrue(adj.getBlock() instanceof ContourBoardBlock
				&& ContourBoardBlock.facePresent(adj, Direction.NORTH)
				&& ContourBoardBlock.faceCount(adj) == 1,
				"adjacent fallback did not hug the clicked surface: " + adj);
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- per-face paint + drop isolation ---

	@GameTest(template = "test_empty")
	public void boardPaintAndDropsIndependent(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			BlockState host = faces(Direction.NORTH, Direction.SOUTH);
			ContourBlockEntity be = be(h, P, host);
			h.assertTrue(be.addBoardOccupant(Direction.NORTH.ordinal(),
				Blocks.STONE.defaultBlockState(), 1)
				&& be.addBoardOccupant(Direction.SOUTH.ordinal(),
					Blocks.DIRT.defaultBlockState(), 1),
				"occupant seeding refused");

			// paint the NORTH sheet through the useItemOn gesture - hit
			// inside the north shell with the south-facing surface.
			var absP = h.absolutePos(P);
			Player p = player(h);
			p.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			var r = h.getBlockState(P).useItemOn(
				new ItemStack(Items.OAK_PLANKS, 1), h.getLevel(), p,
				InteractionHand.MAIN_HAND, new BlockHitResult(
					new Vec3(absP.getX() + 0.5, absP.getY() + 0.5,
						absP.getZ() + 0.03),
					Direction.SOUTH, absP, false));
			h.assertTrue(r.consumesAction(),
				"north-face paint rejected: " + r);
			int ni = be.boardOccupantIndex(Direction.NORTH.ordinal());
			int si = be.boardOccupantIndex(Direction.SOUTH.ordinal());
			h.assertTrue(ni >= 0 && si >= 0, "face records lost");
			h.assertTrue(be.getBoardOccupants().get(ni).material()
				.getBlock() == Blocks.OAK_PLANKS
				&& be.getBoardOccupants().get(ni).paid() == 1,
				"north sheet did not take the paint");
			h.assertTrue(be.getBoardOccupants().get(si).material()
				.getBlock() == Blocks.DIRT
				&& be.getBoardOccupants().get(si).paid() == 1,
				"north paint leaked onto the south sheet");
			h.assertTrue(MaterialSafety.isNeutral(be.getMaterial()),
				"face paint touched the host material");
			h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == 0
				&& p.getInventory().countItem(Items.STONE) == 1,
				"paint charge/refund wrong planks="
					+ p.getInventory().countItem(Items.OAK_PLANKS)
					+ " stone=" + p.getInventory().countItem(Items.STONE));

			// drift heal: a face flag without a record (placer-set or
			// malformed NBT) becomes paintable again on first use.
			ContourBlockEntity drift = be(h, P.east(2),
				faces(Direction.WEST));
			var absQ = h.absolutePos(P.east(2));
			p.getInventory().add(new ItemStack(Items.GLASS, 1));
			var r2 = h.getBlockState(P.east(2)).useItemOn(
				new ItemStack(Items.GLASS, 1), h.getLevel(), p,
				InteractionHand.MAIN_HAND, new BlockHitResult(
					new Vec3(absQ.getX() + 0.03, absQ.getY() + 0.5,
						absQ.getZ() + 0.5),
					Direction.EAST, absQ, false));
			h.assertTrue(r2.consumesAction(),
				"drift-face paint rejected: " + r2);
			int wi = drift.boardOccupantIndex(Direction.WEST.ordinal());
			h.assertTrue(wi >= 0
				&& drift.getBoardOccupants().get(wi).material().getBlock()
					== Blocks.GLASS
				&& drift.getBoardOccupants().get(wi).paid() == 1,
				"drift face was not healed + painted");

			// drops: one board blank per face + each face's own paid
			// material, nothing shared.
			var drops = ((net.createcontour.block.ContourBlock) host
				.getBlock()).ledgerDrops(host, be);
			int blanks = 0, planks = 0, dirt = 0;
			for (ItemStack s : drops) {
				if (s.getItem() == ContourRegistries.CONTOUR_BOARD_ITEM.get())
					blanks += s.getCount();
				else if (s.getItem() == Items.OAK_PLANKS)
					planks += s.getCount();
				else if (s.getItem() == Items.DIRT)
					dirt += s.getCount();
			}
			h.assertTrue(blanks == 2 && planks == 1 && dirt == 1,
				"drops wrong blanks=" + blanks + " planks=" + planks
					+ " dirt=" + dirt);
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- collision union across present faces ---

	@GameTest(template = "test_empty")
	public void boardCollisionUnion(GameTestHelper h) {
		// exact voxel parity: getShape IS the union of the present
		// 1px face shells, for every accumulation stage.
		BlockState north = faces(Direction.NORTH);
		VoxelShape expected = Shapes.empty();
		for (float[] b : ContourBoardBlock.boxesFor(north))
			expected = Shapes.or(expected,
				net.minecraft.world.level.block.Block.box(
					b[0], b[1], b[2], b[3], b[4], b[5]));
		VoxelShape got = north.getShape(h.getLevel(), h.absolutePos(P),
			CollisionContext.empty());
		h.assertTrue(!Shapes.joinIsNotEmpty(got, expected,
			BooleanOp.NOT_SAME), "single-face shape mismatch");

		BlockState all = faces(Direction.values());
		VoxelShape full = all.getShape(h.getLevel(), h.absolutePos(P),
			CollisionContext.empty());
		h.assertTrue(full.bounds().getXsize() == 1.0
			&& full.bounds().getYsize() == 1.0
			&& full.bounds().getZsize() == 1.0,
			"six-face union does not span the cell");
		expected = Shapes.empty();
		for (float[] b : ContourBoardBlock.boxesFor(all))
			expected = Shapes.or(expected,
				net.minecraft.world.level.block.Block.box(
					b[0], b[1], b[2], b[3], b[4], b[5]));
		h.assertTrue(!Shapes.joinIsNotEmpty(full, expected,
			BooleanOp.NOT_SAME), "six-face union mismatch");

		// absent faces contribute nothing: a north-only sheet must not
		// occupy the up shell interior or the cell centre. Probe the
		// interior of the up shell: face shells legitimately share edge
		// corners (the north sheet reaches z<=1 at every y).
		VoxelShape n = north.getShape(h.getLevel(), h.absolutePos(P),
			CollisionContext.empty());
		h.assertTrue(!Shapes.joinIsNotEmpty(n,
			net.minecraft.world.level.block.Block.box(2, 15, 2, 14, 16, 14),
			BooleanOp.AND), "absent up shell interior occupied");
		h.assertTrue(!Shapes.joinIsNotEmpty(n,
			net.minecraft.world.level.block.Block.box(6, 6, 6, 10, 10, 10),
			BooleanOp.AND), "board sheet occupies the cell centre");
		h.assertTrue(Shapes.joinIsNotEmpty(n,
			net.minecraft.world.level.block.Block.box(0, 0, 0, 16, 16, 1),
			BooleanOp.AND), "present north shell not covered");
		h.succeed();
	}

	// --- writeSafe / save-load round-trip ---

	@GameTest(template = "test_empty")
	public void boardWriteSafeRoundTrip(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			BlockState host = faces(Direction.DOWN, Direction.NORTH,
				Direction.UP);
			ContourBlockEntity be = be(h, P, host);
			be.addBoardOccupant(Direction.DOWN.ordinal(),
				Blocks.STONE.defaultBlockState(), 1);
			be.addBoardOccupant(Direction.NORTH.ordinal(),
				Blocks.DIRT.defaultBlockState(), 1);
			be.addBoardOccupant(Direction.UP.ordinal(),
				MaterialSafety.neutral(), 0);

			// save/load preserves faces, materials and paid records.
			CompoundTag tag = be.saveWithFullMetadata(
				h.getLevel().registryAccess());
			h.setBlock(P, host);
			ContourBlockEntity be2 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			be2.loadWithComponents(tag, h.getLevel().registryAccess());
			h.assertTrue(be2.getBoardOccupants().size() == 3
				&& be2.boardOccupantIndex(Direction.DOWN.ordinal()) >= 0
				&& be2.boardOccupantIndex(Direction.UP.ordinal()) >= 0,
				"board occupants lost in save/load: "
					+ be2.getBoardOccupants().size());
			int di = be2.boardOccupantIndex(Direction.DOWN.ordinal());
			h.assertTrue(be2.getBoardOccupants().get(di).material()
				.getBlock() == Blocks.STONE
				&& be2.getBoardOccupants().get(di).paid() == 1,
				"loaded face ledger wrong");

			// writeSafe exports paid only where the schematic
			// requirement charges at print (custom materials under
			// consume-on); neutral sheets export no credit.
			CompoundTag safe = new CompoundTag();
			be2.writeSafe(safe, h.getLevel().registryAccess());
			var extras = safe.getList("BoardExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND);
			h.assertTrue(extras.size() == 3, "writeSafe dropped faces: "
				+ extras.size());
			int paidDown = -1, paidUp = -1;
			for (int i = 0; i < extras.size(); i++) {
				int f = extras.getCompound(i).getInt("Face");
				if (f == Direction.DOWN.ordinal())
					paidDown = extras.getCompound(i).getInt("Paid");
				if (f == Direction.UP.ordinal())
					paidUp = extras.getCompound(i).getInt("Paid");
			}
			h.assertTrue(paidDown == 1 && paidUp == 0,
				"writeSafe paid projection wrong down=" + paidDown
					+ " up=" + paidUp);
			h.assertTrue(safe.getInt("PaidUnits") == 0,
				"board exported host credit it does not own");
			h.assertTrue(!safe.contains("Item"),
				"writeSafe leaked the item hint");

			// records for faces not present in the blockstate - or
			// duplicate faces - are dropped on load, not trusted.
			CompoundTag dirty = tag.copy();
			var dirtyList = dirty.getList("BoardExtras",
				net.minecraft.nbt.Tag.TAG_COMPOUND);
			CompoundTag orphan = new CompoundTag();
			orphan.putInt("Face", Direction.EAST.ordinal());
			orphan.put("Material", net.minecraft.nbt.NbtUtils
				.writeBlockState(Blocks.GLASS.defaultBlockState()));
			orphan.putInt("Paid", 1);
			dirtyList.add(orphan);
			CompoundTag dup = new CompoundTag();
			dup.putInt("Face", Direction.DOWN.ordinal());
			dup.put("Material", net.minecraft.nbt.NbtUtils
				.writeBlockState(Blocks.GLASS.defaultBlockState()));
			dup.putInt("Paid", 1);
			dirtyList.add(dup);
			h.setBlock(P, host);
			ContourBlockEntity be3 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			be3.loadWithComponents(dirty, h.getLevel().registryAccess());
			h.assertTrue(be3.getBoardOccupants().size() == 3
				&& be3.boardOccupantIndex(Direction.EAST.ordinal()) < 0
				&& be3.getBoardOccupants().get(be3.boardOccupantIndex(
					Direction.DOWN.ordinal())).material().getBlock()
						== Blocks.STONE,
				"malformed board records were trusted");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}

	// --- wrench removes the clicked face's sheet ---

	@GameTest(template = "test_empty")
	public void boardWrenchRemovesFaces(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			BlockState host = faces(Direction.NORTH, Direction.SOUTH,
				Direction.UP);
			ContourBlockEntity be = be(h, P, host);
			be.addBoardOccupant(Direction.NORTH.ordinal(),
				Blocks.STONE.defaultBlockState(), 1);
			be.addBoardOccupant(Direction.SOUTH.ordinal(),
				Blocks.DIRT.defaultBlockState(), 1);
			be.addBoardOccupant(Direction.UP.ordinal(),
				MaterialSafety.neutral(), 0);
			Player p = player(h);
			var block = (net.createcontour.block.ContourBlock) h
				.getBlockState(P).getBlock();

			// wrench the north sheet off: 1 blank + its stone refund.
			block.onWrenched(h.getBlockState(P), ctx(h, p,
				ItemStack.EMPTY, P, Direction.SOUTH,
				new Vec3(0, 0, -0.47)));
			h.assertTrue(h.getBlockState(P).getBlock()
				instanceof ContourBoardBlock, "wrench removed the block");
			h.assertTrue(!ContourBoardBlock.facePresent(h.getBlockState(P),
				Direction.NORTH)
				&& ContourBoardBlock.faceCount(h.getBlockState(P)) == 2,
				"wrench did not clear exactly the north face");
			h.assertTrue(be.boardOccupantIndex(Direction.NORTH.ordinal()) < 0
				&& be.getBoardOccupants().size() == 2,
				"north record survived the wrench");
			h.assertTrue(p.getInventory().countItem(
				ContourRegistries.CONTOUR_BOARD_ITEM.get()) == 1
				&& p.getInventory().countItem(Items.STONE) == 1,
				"wrench refunds wrong");

			// south sheet next: blank + dirt, up face untouched.
			block.onWrenched(h.getBlockState(P), ctx(h, p,
				ItemStack.EMPTY, P, Direction.NORTH,
				new Vec3(0, 0, 0.47)));
			h.assertTrue(ContourBoardBlock.faceCount(h.getBlockState(P)) == 1
				&& ContourBoardBlock.facePresent(h.getBlockState(P),
					Direction.UP),
				"second wrench removed the wrong face");
			h.assertTrue(p.getInventory().countItem(
				ContourRegistries.CONTOUR_BOARD_ITEM.get()) == 2
				&& p.getInventory().countItem(Items.DIRT) == 1,
				"second wrench refunds wrong");

			// last sheet: the whole block goes (same economics as the
			// sneak-wrench pickup), never a zero-face board.
			block.onWrenched(h.getBlockState(P), ctx(h, p,
				ItemStack.EMPTY, P, Direction.DOWN,
				new Vec3(0, 0.47, 0)));
			h.assertTrue(h.getBlockState(P).isAir(),
				"last-face wrench left a zero-face board");
			h.assertTrue(p.getInventory().countItem(
				ContourRegistries.CONTOUR_BOARD_ITEM.get()) == 3,
				"last-face pickup refunds wrong");
			h.succeed();
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
	}
}
