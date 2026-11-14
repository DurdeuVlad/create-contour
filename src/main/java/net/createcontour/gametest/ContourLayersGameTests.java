package net.createcontour.gametest;

import net.createcontour.block.ContourBlock;
import net.createcontour.block.ContourLayersBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialPolicy;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Issue #20: resizable contour_layers SIZE 1..8 (the `layers` property,
 * 2px per layer). Covers the snow-style growth path to a full block,
 * per-size collision + drops, the per-eighth material ledger (8 layers
 * = 1 material item; sub-full stacks paint free), wrench top-layer
 * removal with ledger reconcile, and schematic requirement/writeSafe
 * proportionality.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourLayersGameTests {

	private static final BlockPos P = new BlockPos(2, 1, 2);

	private static BlockState layers(int size) {
		return ContourRegistries.CONTOUR_LAYERS.get().defaultBlockState()
			.setValue(ContourLayersBlock.LAYERS, size);
	}

	private static ContourBlockEntity be(GameTestHelper h, BlockState state) {
		return be(h, P, state);
	}

	private static ContourBlockEntity be(GameTestHelper h, BlockPos pos,
		BlockState state) {
		h.setBlock(pos, state);
		return (ContourBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
	}

	private static UseOnContext ctx(GameTestHelper h, Player p, ItemStack held,
		BlockPos pos, Direction face) {
		return new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND, held,
			new BlockHitResult(Vec3.atCenterOf(h.absolutePos(pos)), face,
				h.absolutePos(pos), false));
	}

	private static UseOnContext wrenchCtx(GameTestHelper h, Player p, BlockPos pos,
		Direction face) {
		return new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND,
			ItemStack.EMPTY, new BlockHitResult(
				Vec3.atCenterOf(h.absolutePos(pos)),
				face, h.absolutePos(pos), false));
	}

	private static int count(Player p, net.minecraft.world.item.Item item) {
		int n = 0;
		for (ItemStack s : p.getInventory().items)
			if (s.is(item))
				n += s.getCount();
		return n;
	}

	// --- growth path: merge +1 per click up to 8 = full block ---

	@GameTest(template = "test_empty", batch = "contour_config")
	public void growthToFull(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = h.makeMockPlayer(GameType.SURVIVAL);
			// A painted size-1 plate: placing seven more painted frames
			// grows it snow-style to a full block. Only the 7->8 merge
			// completes a whole-eighth and is charged (1 material item);
			// paint persists on every step.
			be(h, layers(1)).applyPayload(Blocks.STONE.defaultBlockState(), 0,
				Direction.NORTH, false);
			ItemStack frames = new ItemStack(
				ContourRegistries.CONTOUR_LAYERS_ITEM.get(), 8);
			ContourBlockItem.writePayload(frames,
				Blocks.STONE.defaultBlockState(), 0, Direction.NORTH, false);
			p.setItemInHand(InteractionHand.MAIN_HAND, frames);
			p.getInventory().add(new ItemStack(Items.STONE, 1));
			for (int want = 2; want <= 8; want++) {
				h.assertTrue(frames.getItem()
						.useOn(ctx(h, p, frames, P, Direction.UP))
						.consumesAction(), "merge " + want + " rejected");
				BlockState s = h.getBlockState(P);
				h.assertTrue(s.getValue(ContourLayersBlock.LAYERS) == want,
					"did not grow to " + want + ": " + s);
				ContourBlockEntity be = (ContourBlockEntity) h.getLevel()
					.getBlockEntity(h.absolutePos(P));
				h.assertTrue(be.getMaterial().getBlock() == Blocks.STONE,
					"paint lost at size " + want);
				h.assertTrue(be.getPaidUnits() == (want == 8 ? 1 : 0),
					"ledger wrong at size " + want + ": " + be.getPaidUnits());
			}
			h.assertTrue(count(p, Items.STONE) == 0,
				"growth charged more than one whole-eighth");
			h.assertTrue(frames.getCount() == 1,
				"frames not consumed per layer: " + frames.getCount());
			// Cap: a full plate rejects the merge - the click resolves to
			// the adjacent cell and builds a NEW painted full block (one
			// more whole-eighth charge).
			p.getInventory().add(new ItemStack(Items.STONE, 1));
			h.assertTrue(frames.getItem()
					.useOn(ctx(h, p, frames, P, Direction.UP))
					.consumesAction(), "adjacent place after cap rejected");
			h.assertTrue(h.getBlockState(P).getValue(ContourLayersBlock.LAYERS) == 8,
				"cap grew past 8");
			ContourBlockEntity topBe = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.above()));
			h.assertTrue(topBe != null
					&& topBe.getMaterial().getBlock() == Blocks.STONE
					&& topBe.getPaidUnits() == 1,
				"new full block at cap lost paint/credit");
			h.assertTrue(count(p, Items.STONE) == 0,
				"full-block paint under-charged");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	// --- per-size collision shape + size-scaled drops ---

	@GameTest(template = "test_empty")
	public void perSizeCollisionAndDrops(GameTestHelper h) {
		ContourLayersBlock block = ContourRegistries.CONTOUR_LAYERS.get();
		for (int i = 1; i <= 8; i++) {
			BlockState s = layers(i);
			ContourBlockEntity be = be(h, s);
			be.applyPayload(MaterialSafety.neutral(), 0, Direction.NORTH, false);
			double yMax = h.getBlockState(P)
				.getShape(h.getLevel(), P, CollisionContext.empty())
				.max(Direction.Axis.Y);
			h.assertTrue(Math.abs(yMax - i * 2.0 / 16.0) < 1e-6,
				"size " + i + " height != " + (i * 2) + "px: " + yMax);
			// ledger drops: i blank frames, no material (unpainted)
			java.util.List<ItemStack> drops = block.ledgerDrops(
				h.getBlockState(P), be);
			int blanks = 0;
			for (ItemStack d : drops)
				if (d.is(ContourRegistries.CONTOUR_LAYERS_ITEM.get()))
					blanks += d.getCount();
			h.assertTrue(blanks == i && drops.size() == i,
				"size " + i + " drops wrong: " + drops.size() + "/" + blanks);
		}
		// painted size-8 (paid 1): 8 blanks + 1 material item
		ContourBlockEntity be8 = be(h, layers(8));
		be8.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
		var params = new LootParams.Builder(h.getLevel()).withParameter(
			LootContextParams.BLOCK_ENTITY, be8);
		java.util.List<ItemStack> drops8 = block.getDrops(h.getBlockState(P), params);
		int blanks8 = 0, mats8 = 0;
		for (ItemStack d : drops8) {
			if (d.is(ContourRegistries.CONTOUR_LAYERS_ITEM.get()))
				blanks8 += d.getCount();
			if (d.is(Items.STONE))
				mats8 += d.getCount();
		}
		h.assertTrue(blanks8 == 8 && mats8 == 1,
			"full painted drops wrong: " + blanks8 + "+" + mats8);
		// painted size-5 (paid 0 - free fractional paint): 5 blanks, 0 material
		ContourBlockEntity be5 = be(h, layers(5));
		be5.applyPayload(Blocks.STONE.defaultBlockState(), 0, Direction.NORTH, false);
		java.util.List<ItemStack> drops5 = block.ledgerDrops(h.getBlockState(P), be5);
		int blanks5 = 0, mats5 = 0;
		for (ItemStack d : drops5) {
			if (d.is(ContourRegistries.CONTOUR_LAYERS_ITEM.get()))
				blanks5 += d.getCount();
			if (d.is(Items.STONE))
				mats5 += d.getCount();
		}
		h.assertTrue(blanks5 == 5 && mats5 == 0,
			"free-painted drops wrong: " + blanks5 + "+" + mats5);
		h.succeed();
	}

	// --- ledger fractions: paint + merge + wrench billing ---

	@GameTest(template = "test_empty", batch = "contour_config")
	public void layerLedgerFractions(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// (a) paint gesture on size-5: floor(5/8)=0 -> free, paid 0,
			// material item NOT consumed
			ContourBlockEntity be5 = be(h, layers(5));
			Player p1 = h.makeMockPlayer(GameType.SURVIVAL);
			p1.getInventory().add(new ItemStack(Items.STONE, 1));
			h.assertTrue(MaterialPolicy.paint(h.getLevel(), be5, p1,
					Blocks.STONE.defaultBlockState()), "size-5 paint rejected");
			h.assertTrue(be5.getMaterial().getBlock() == Blocks.STONE
					&& be5.getPaidUnits() == 0, "size-5 paint ledger wrong");
			h.assertTrue(count(p1, Items.STONE) == 1,
				"free sub-full paint consumed an item");

			// (b) paint gesture on size-8: one whole-eighth -> 1 item, paid 1
			// (fresh position: P's BE is already same-base painted from (a))
			ContourBlockEntity be8 = be(h, P.above(), layers(8));
			Player p2 = h.makeMockPlayer(GameType.SURVIVAL);
			p2.getInventory().add(new ItemStack(Items.STONE, 1));
			h.assertTrue(MaterialPolicy.paint(h.getLevel(), be8, p2,
					Blocks.STONE.defaultBlockState()), "size-8 paint rejected");
			h.assertTrue(be8.getMaterial().getBlock() == Blocks.STONE
					&& be8.getPaidUnits() == 1, "size-8 paint ledger wrong");
			h.assertTrue(count(p2, Items.STONE) == 0,
				"size-8 paint did not charge");

			// (c) repaint size-8 with a different material: charge 1 new,
			// refund the old paid item
			Player p3 = h.makeMockPlayer(GameType.SURVIVAL);
			p3.getInventory().add(new ItemStack(Items.DIRT, 1));
			h.assertTrue(MaterialPolicy.paint(h.getLevel(), be8, p3,
					Blocks.DIRT.defaultBlockState()), "size-8 repaint rejected");
			h.assertTrue(be8.getMaterial().getBlock() == Blocks.DIRT
					&& be8.getPaidUnits() == 1, "size-8 repaint ledger wrong");
			h.assertTrue(count(p3, Items.DIRT) == 0
					&& count(p3, Items.STONE) == 1,
				"repaint did not swap material items");

			// (d) wrench removes the top layer: 8->7 keeps the paid ledger
			// (real charge, stays refundable on break) and drops one blank
			// frame; paint persists across the shrink
			Player p4 = h.makeMockPlayer(GameType.SURVIVAL);
			((ContourBlock) h.getBlockState(P.above()).getBlock())
				.onWrenched(h.getBlockState(P.above()),
					wrenchCtx(h, p4, P.above(), Direction.UP));
			h.assertTrue(h.getBlockState(P.above())
					.getValue(ContourLayersBlock.LAYERS) == 7,
				"wrench did not remove top layer");
			ContourBlockEntity be7 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.above()));
			h.assertTrue(be7.getPaidUnits() == 1
					&& be7.getMaterial().getBlock() == Blocks.DIRT,
				"wrench lost paint/ledger");
			h.assertTrue(count(p4, Items.DIRT) == 0
					&& count(p4, ContourRegistries.CONTOUR_LAYERS_ITEM.get()) == 1,
				"wrench dropped wrong: " + count(p4, Items.DIRT) + "+"
					+ count(p4, ContourRegistries.CONTOUR_LAYERS_ITEM.get()));
			// 7->6: nothing changes on the ledger, only the frame returns
			((ContourBlock) h.getBlockState(P.above()).getBlock())
				.onWrenched(h.getBlockState(P.above()),
					wrenchCtx(h, p4, P.above(), Direction.UP));
			ContourBlockEntity be6 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.above()));
			h.assertTrue(h.getBlockState(P.above())
					.getValue(ContourLayersBlock.LAYERS) == 6
					&& be6.getPaidUnits() == 1,
				"wrench mutated ledger below billed boundary");
			h.assertTrue(count(p4, Items.DIRT) == 0
					&& count(p4, ContourRegistries.CONTOUR_LAYERS_ITEM.get()) == 2,
				"second wrench dropped wrong");

			// (e) merge billing at the boundary only: painted 3->4 costs 0,
			// painted 7->8 costs 1, and regrowing an already-billed cell
			// (paid survived the shrink) is free
			Player p5 = h.makeMockPlayer(GameType.SURVIVAL);
			be(h, layers(3)).applyPayload(Blocks.STONE.defaultBlockState(), 0,
				Direction.NORTH, false);
			ItemStack t = new ItemStack(ContourRegistries.CONTOUR_LAYERS_ITEM.get());
			ContourBlockItem.writePayload(t, Blocks.STONE.defaultBlockState(),
				0, Direction.NORTH, false);
			p5.setItemInHand(InteractionHand.MAIN_HAND, t);
			p5.getInventory().add(new ItemStack(Items.STONE, 1));
			h.assertTrue(t.getItem().useOn(ctx(h, p5, t, P, Direction.UP))
				.consumesAction(), "3->4 merge rejected");
			ContourBlockEntity me = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			h.assertTrue(h.getBlockState(P).getValue(ContourLayersBlock.LAYERS) == 4
					&& me.getPaidUnits() == 0 && count(p5, Items.STONE) == 1,
				"sub-full merge was charged");

			be(h, P.south(2), layers(7))
				.applyPayload(Blocks.STONE.defaultBlockState(), 0,
					Direction.NORTH, false);
			ItemStack t2 = new ItemStack(
				ContourRegistries.CONTOUR_LAYERS_ITEM.get(), 2);
			ContourBlockItem.writePayload(t2, Blocks.STONE.defaultBlockState(),
				0, Direction.NORTH, false);
			p5.setItemInHand(InteractionHand.MAIN_HAND, t2);
			h.assertTrue(t2.getItem()
					.useOn(ctx(h, p5, t2, P.south(2), Direction.UP))
					.consumesAction(), "7->8 merge rejected");
			ContourBlockEntity me8 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.south(2)));
			h.assertTrue(h.getBlockState(P.south(2))
					.getValue(ContourLayersBlock.LAYERS) == 8
					&& me8.getPaidUnits() == 1
					&& count(p5, Items.STONE) == 0,
				"boundary merge did not bill the whole-eighth");
			// shrink keeps the charge; regrow must not re-bill it
			((ContourBlock) h.getBlockState(P.south(2)).getBlock())
				.onWrenched(h.getBlockState(P.south(2)),
					wrenchCtx(h, p5, P.south(2), Direction.UP));
			h.assertTrue(t2.getItem()
					.useOn(ctx(h, p5, t2, P.south(2), Direction.UP))
					.consumesAction(), "regrow merge rejected");
			ContourBlockEntity reg = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.south(2)));
			h.assertTrue(h.getBlockState(P.south(2))
					.getValue(ContourLayersBlock.LAYERS) == 8
					&& reg.getPaidUnits() == 1
					&& count(p5, Items.STONE) == 0,
				"regrow re-billed an already-paid whole-eighth");

			// (f) config OFF: paint on size-8 is free and mints no credit
			ContourConfig.CONSUME_MATERIAL.set(false);
			ContourBlockEntity beOff = be(h, layers(8));
			Player p6 = h.makeMockPlayer(GameType.SURVIVAL);
			p6.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			h.assertTrue(MaterialPolicy.paint(h.getLevel(), beOff, p6,
					Blocks.OAK_PLANKS.defaultBlockState()), "config-off paint failed");
			h.assertTrue(beOff.getPaidUnits() == 0,
				"config-off minted credit");
			h.assertTrue(count(p6, Items.OAK_PLANKS) == 1,
				"config-off consumed material");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	// --- schematic requirement + writeSafe proportional to size ---

	@GameTest(template = "test_empty")
	public void schematicLayers(GameTestHelper h) {
		// painted size-8: requirement = 1x material + 7 extra host units
		// (Create unions the default 1x host item itself)
		ContourBlockEntity be8 = be(h, layers(8));
		be8.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
		var list8 = be8.getRequiredItems(h.getBlockState(P)).getRequiredItems();
		int mat8 = 0, host8 = 0;
		for (var sr : list8) {
			if (sr.stack.is(Items.STONE))
				mat8 += sr.stack.getCount();
			if (sr.stack.is(ContourRegistries.CONTOUR_LAYERS_ITEM.get()))
				host8 += sr.stack.getCount();
		}
		h.assertTrue(mat8 == 1, "size-8 requirement material != 1: " + mat8);
		h.assertTrue(host8 == 7, "size-8 requirement extras != 7: " + host8);
		// writeSafe: painted full block exports its one billed item
		CompoundTag tag8 = new CompoundTag();
		be8.writeSafe(tag8, h.getLevel().registryAccess());
		h.assertTrue(tag8.getInt("PaidUnits") == 1,
			"size-8 writeSafe paid wrong: " + tag8.getInt("PaidUnits"));
		h.assertTrue(tag8.contains("Material"), "writeSafe material missing");

		// painted size-5: no material entry (floor(5/8)=0), 4 host extras,
		// writeSafe exports no credit
		ContourBlockEntity be5 = be(h, layers(5));
		be5.applyPayload(Blocks.STONE.defaultBlockState(), 0, Direction.NORTH, false);
		var list5 = be5.getRequiredItems(h.getBlockState(P)).getRequiredItems();
		int mat5 = 0, host5 = 0;
		for (var sr : list5) {
			if (sr.stack.is(Items.STONE))
				mat5 += sr.stack.getCount();
			if (sr.stack.is(ContourRegistries.CONTOUR_LAYERS_ITEM.get()))
				host5 += sr.stack.getCount();
		}
		h.assertTrue(mat5 == 0, "size-5 requirement charged material: " + mat5);
		h.assertTrue(host5 == 4, "size-5 requirement extras != 4: " + host5);
		CompoundTag tag5 = new CompoundTag();
		be5.writeSafe(tag5, h.getLevel().registryAccess());
		h.assertTrue(tag5.getInt("PaidUnits") == 0,
			"size-5 writeSafe exported credit");

		// writeSafe -> read round-trip (print path): the saved projection
		// restores material + billed ledger on the fresh BE
		ContourBlockEntity fresh = new ContourBlockEntity(P, layers(8));
		fresh.loadWithComponents(tag8, h.getLevel().registryAccess());
		h.assertTrue(fresh.getMaterial().getBlock() == Blocks.STONE
				&& fresh.getPaidUnits() == 1,
			"writeSafe round-trip lost material/ledger");

		// full save/load round-trip at a sub-full size
		ContourBlockEntity be3 = be(h, layers(3));
		be3.applyPayload(Blocks.DIRT.defaultBlockState(), 0, Direction.EAST, false);
		CompoundTag saved = be3.saveWithFullMetadata(h.getLevel().registryAccess());
		ContourBlockEntity back = new ContourBlockEntity(P, layers(3));
		back.loadWithComponents(saved, h.getLevel().registryAccess());
		h.assertTrue(back.getMaterial().getBlock() == Blocks.DIRT
				&& back.getPaidUnits() == 0 && back.getFacing() == Direction.EAST,
			"save/load round-trip lost fields");
		h.succeed();
	}
}
