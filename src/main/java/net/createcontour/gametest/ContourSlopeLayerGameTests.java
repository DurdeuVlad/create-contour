package net.createcontour.gametest;

import net.createcontour.block.ContourBlock;
import net.createcontour.block.ContourSlopeLayerBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialPolicy;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Issue #21 - stackable slope slices (contour_slope_layer, FACING + SIZE
 * 1..8). Covers: registry/recipe/tag membership, stack-to-8 growth via the
 * real useOn merge path, wrong-order (wrong-facing) rejection, the 2px-pitch
 * staircase collision contract and the SIZE=8 contour_slope equivalence,
 * the per-slice (1/8) material ledger, and writeSafe schematic export.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourSlopeLayerGameTests {

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
		BlockPos pos, Direction face) {
		var abs = h.absolutePos(pos);
		return new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND,
			held, new BlockHitResult(abs.getCenter(), face, abs, false));
	}

	private BlockPlaceContext placeCtx(GameTestHelper h, Player p,
		ItemStack held, BlockPos pos, Direction face) {
		var abs = h.absolutePos(pos);
		return new BlockPlaceContext(h.getLevel(), p, InteractionHand.MAIN_HAND,
			held, new BlockHitResult(abs.getCenter(), face, abs, false));
	}

	private static BlockState slopeLayer(Direction f, int size) {
		return ContourRegistries.CONTOUR_SLOPE_LAYER.get().defaultBlockState()
			.setValue(ContourSlopeLayerBlock.FACING, f)
			.setValue(ContourSlopeLayerBlock.SIZE, size);
	}

	/** Highest filled px of the shape in the 2px run column [lo,hi) on `axis`. */
	private static int columnTop(VoxelShape shape, int axis, int lo) {
		int hi = lo + 2, top = 0;
		for (int y = 0; y < 16; y++) {
			VoxelShape probe = axis == 0
				? Block.box(lo, y, 0, hi, y + 1, 16)
				: Block.box(0, y, lo, 16, y + 1, hi);
			if (Shapes.joinIsNotEmpty(shape, probe, BooleanOp.AND))
				top = y + 1;
		}
		return top;
	}

	// --- registration / datapack surface ---

	@GameTest(template = "test_empty")
	public void slopeLayerRegistry(GameTestHelper h) {
		Block b = ContourRegistries.CONTOUR_SLOPE_LAYER.get();
		h.assertTrue(BuiltInRegistries.BLOCK.getKey(b).getNamespace()
			.equals("create_contour"), "slope layer unregistered");
		h.assertTrue(ContourRegistries.CONTOUR_BE.get()
			.isValid(b.defaultBlockState()), "BE misses slope layer");
		h.assertTrue(BuiltInRegistries.ITEM.get(ResourceLocation
			.fromNamespaceAndPath("create_contour", "contour_slope_layer"))
			instanceof ContourBlockItem, "slope layer item missing");
		var rm = h.getLevel().getServer().getRecipeManager();
		h.assertTrue(rm.byKey(ResourceLocation.fromNamespaceAndPath(
			"create_contour", "contour_slope_layer")).isPresent(),
			"manual recipe contour_slope_layer");
		h.assertTrue(rm.byKey(ResourceLocation.fromNamespaceAndPath(
			"create_contour", "mechanical_crafting/contour_slope_layer"))
			.isPresent(), "mechanical recipe contour_slope_layer");
		h.assertTrue(BuiltInRegistries.BLOCK
			.getTag(TagKey.create(Registries.BLOCK,
				ResourceLocation.fromNamespaceAndPath("create_contour",
					"contour_shapes")))
			.map(t -> t.contains(b.builtInRegistryHolder())).orElse(false),
			"contour_shapes tag missing slope layer");
		h.succeed();
	}

	// --- stack-to-8 via the real useOn merge path ---

	@GameTest(template = "test_empty")
	public void stackToEight(GameTestHelper h) {
		Block b = ContourRegistries.CONTOUR_SLOPE_LAYER.get();
		ItemStack slices = new ItemStack(
			ContourRegistries.CONTOUR_SLOPE_LAYER_ITEM.get(), 9);
		Player p = player(h);
		p.setYRot(180); // face NORTH - the stack's ascend direction
		p.setItemInHand(InteractionHand.MAIN_HAND, slices);

		h.setBlock(P, slopeLayer(Direction.NORTH, 1));
		for (int want = 2; want <= 8; want++) {
			slices.getItem().useOn(ctx(h, p, slices, P, Direction.UP));
			BlockState s = h.getBlockState(P);
			h.assertTrue(s.is(b)
					&& s.getValue(ContourSlopeLayerBlock.SIZE) == want
					&& s.getValue(ContourSlopeLayerBlock.FACING) == Direction.NORTH,
				"stack did not grow to " + want + ": " + s);
		}
		// cap: the 9th click cannot grow SIZE=8 - the merge is rejected and
		// the click retargets to the adjacent cell above.
		slices.getItem().useOn(ctx(h, p, slices, P, Direction.UP));
		h.assertTrue(h.getBlockState(P).getValue(ContourSlopeLayerBlock.SIZE) == 8,
			"stack grew past 8");
		h.assertTrue(h.getBlockState(P.above()).is(b)
				&& h.getBlockState(P.above()).getValue(ContourSlopeLayerBlock.SIZE) == 1,
			"overflow click did not start a fresh stack above");
		h.succeed();
	}

	// --- wrong-order rejection: only facing-consistent clicks may merge ---

	@GameTest(template = "test_empty")
	public void wrongOrderRejected(GameTestHelper h) {
		ItemStack held = new ItemStack(ContourRegistries.CONTOUR_SLOPE_LAYER_ITEM.get());
		h.setBlock(P, slopeLayer(Direction.NORTH, 3));
		BlockState stack = h.getBlockState(P);

		// A north-facing implied placement (null player -> NORTH) may grow
		// the north stack; any other facing is rejected so a mis-aimed
		// slice can never join the ramp out of order.
		h.assertTrue(stack.canBeReplaced(placeCtx(h, null, held, P, Direction.UP)),
			"facing-consistent merge rejected");
		Player south = player(h);
		south.setYRot(0); // faces SOUTH
		h.assertTrue(!stack.canBeReplaced(placeCtx(h, south, held, P, Direction.UP)),
			"wrong-facing merge accepted");
		Player east = player(h);
		east.setYRot(270); // faces EAST
		h.assertTrue(!stack.canBeReplaced(placeCtx(h, east, held, P, Direction.UP)),
			"perpendicular-facing merge accepted");

		// The merge itself always extends the ramp in the stack's facing.
		BlockState grown = stack.getBlock().getStateForPlacement(
			placeCtx(h, null, held, P, Direction.UP));
		h.assertTrue(grown.getValue(ContourSlopeLayerBlock.SIZE) == 4
				&& grown.getValue(ContourSlopeLayerBlock.FACING) == Direction.NORTH,
			"grown stack lost facing/size: " + grown);

		// Path-level: a wrong-facing useOn must not mutate the stack - the
		// slice falls through to the adjacent cell as a fresh stack.
		south.setItemInHand(InteractionHand.MAIN_HAND,
			new ItemStack(ContourRegistries.CONTOUR_SLOPE_LAYER_ITEM.get(), 1));
		south.getItemInHand(InteractionHand.MAIN_HAND).getItem()
			.useOn(ctx(h, south,
				south.getItemInHand(InteractionHand.MAIN_HAND), P, Direction.UP));
		h.assertTrue(h.getBlockState(P).getValue(ContourSlopeLayerBlock.SIZE) == 3
				&& h.getBlockState(P).getValue(ContourSlopeLayerBlock.FACING)
					== Direction.NORTH,
			"wrong-facing click mutated the stack");
		BlockState above = h.getBlockState(P.above());
		h.assertTrue(above.is(ContourRegistries.CONTOUR_SLOPE_LAYER.get())
				&& above.getValue(ContourSlopeLayerBlock.SIZE) == 1
				&& above.getValue(ContourSlopeLayerBlock.FACING) == Direction.SOUTH,
			"rejected slice did not start a fresh stack: " + above);
		h.succeed();
	}

	// --- collision staircase + SIZE=8 contour_slope equivalence ---

	@GameTest(template = "test_empty")
	public void slopeLayerCollision(GameTestHelper h) {
		// North stack: run axis Z, 2px columns from the north wall.
		for (int n : new int[]{1, 4, 8}) {
			h.setBlock(P, slopeLayer(Direction.NORTH, n));
			VoxelShape vs = h.getBlockState(P).getShape(h.getLevel(),
				h.absolutePos(P), CollisionContext.empty());
			for (int j = 0; j < 8; j++) {
				int expected = Math.min(2 * n, 16 - 2 * j);
				h.assertTrue(columnTop(vs, 2, j * 2) == expected,
					"SIZE=" + n + " column " + j + " top != " + expected
						+ " (got " + columnTop(vs, 2, j * 2) + ")");
			}
		}
		// East stack: run axis X, plateau at the east wall.
		h.setBlock(P, slopeLayer(Direction.EAST, 4));
		VoxelShape east = h.getBlockState(P).getShape(h.getLevel(),
			h.absolutePos(P), CollisionContext.empty());
		for (int j = 0; j < 8; j++) {
			int expected = Math.min(8, 16 - 2 * j);
			h.assertTrue(columnTop(east, 0, 16 - 2 * j - 2) == expected,
				"EAST SIZE=4 column " + j + " top != " + expected);
		}
		// SIZE=8 = the contour_slope silhouette: the same wedge, stepped at
		// 2px pitch (the slope's own collision steps it at 4px pitch - a
		// documented staircase family, both upper-bound on the 45-degree
		// wedge). Bounds and per-column reach are asserted, not block ids.
		h.setBlock(P, slopeLayer(Direction.NORTH, 8));
		VoxelShape full = h.getBlockState(P).getShape(h.getLevel(),
			h.absolutePos(P), CollisionContext.empty());
		h.assertTrue(full.bounds().getYsize() == 1.0
				&& full.bounds().getXsize() == 1.0
				&& full.bounds().getZsize() == 1.0,
			"SIZE=8 bounds not full cell");
		h.setBlock(P, ContourRegistries.CONTOUR_SLOPE.get().defaultBlockState()
			.setValue(net.createcontour.block.ContourSlopeBlock.FACING,
				Direction.NORTH));
		VoxelShape slope = h.getBlockState(P).getShape(h.getLevel(),
			h.absolutePos(P), CollisionContext.empty());
		// every 4px slope column is covered by our 2px columns' max reach
		for (int j = 0; j < 4; j++) {
			int slopeTop = columnTop(slope, 2, j * 4);
			int ourTop = Math.max(columnTop(full, 2, j * 4),
				columnTop(full, 2, j * 4 + 2));
			h.assertTrue(ourTop == slopeTop,
				"SIZE=8 staircase does not match slope column " + j
					+ " (" + ourTop + " vs " + slopeTop + ")");
		}
		h.succeed();
	}

	// --- per-slice (1/8) material ledger ---

	@GameTest(template = "test_empty")
	public void slopeLayerLedger(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Block b = ContourRegistries.CONTOUR_SLOPE_LAYER.get();
			h.assertTrue(((ContourBlock) b).hostUnits(slopeLayer(Direction.NORTH, 3)) == 3,
				"hostUnits != SIZE");
			h.assertTrue(((ContourBlock) b).hostUnits(slopeLayer(Direction.NORTH, 8)) == 8,
				"hostUnits != 8 at full stack");

			// Painting a SIZE=3 stack costs 3 units (whole-block paint,
			// per-slice price).
			ContourBlockEntity be = be(h, P, slopeLayer(Direction.NORTH, 3));
			Player p = player(h);
			p.getInventory().add(new ItemStack(Items.STONE, 3));
			h.assertTrue(MaterialPolicy.paint(h.getLevel(), be, p,
				Blocks.STONE.defaultBlockState()), "paint rejected");
			h.assertTrue(be.getPaidUnits() == 3, "paid != 3: " + be.getPaidUnits());
			h.assertTrue(p.getInventory().countItem(Items.STONE) == 0,
				"paint did not charge 3 units");

			// Break drops: SIZE blank slices + paid material refund.
			var drops = ((ContourBlock) b).ledgerDrops(h.getBlockState(P), be);
			int blanks = 0, stone = 0;
			for (ItemStack s : drops) {
				if (s.is(ContourRegistries.CONTOUR_SLOPE_LAYER_ITEM.get()))
					blanks += s.getCount();
				else if (s.is(Items.STONE))
					stone += s.getCount();
			}
			h.assertTrue(blanks == 3, "drops not 3 slices: " + blanks);
			h.assertTrue(stone == 3, "refund not 3 units: " + stone);

			// Merge accumulates per-slice paid: painted SIZE=2 stack +
			// blank slice (look-fallback material match) charges one unit.
			ContourBlockEntity be2 = be(h, P.east(2),
				slopeLayer(Direction.NORTH, 2));
			be2.applyPayload(Blocks.STONE.defaultBlockState(), 2,
				Direction.NORTH, false);
			Player p2 = player(h);
			p2.setYRot(180); // NORTH = stack facing
			p2.setItemInHand(InteractionHand.MAIN_HAND,
				new ItemStack(ContourRegistries.CONTOUR_SLOPE_LAYER_ITEM.get(), 1));
			p2.getInventory().add(new ItemStack(Items.STONE, 1));
			p2.getItemInHand(InteractionHand.MAIN_HAND).getItem().useOn(
				ctx(h, p2, p2.getItemInHand(InteractionHand.MAIN_HAND),
					P.east(2), Direction.UP));
			ContourBlockEntity mbe = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.east(2)));
			h.assertTrue(h.getBlockState(P.east(2))
					.getValue(ContourSlopeLayerBlock.SIZE) == 3
					&& mbe.getPaidUnits() == 3,
				"merge lost ledger: SIZE="
					+ h.getBlockState(P.east(2)).getValue(ContourSlopeLayerBlock.SIZE)
					+ " paid=" + mbe.getPaidUnits());
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	// --- writeSafe: schematic export carries the per-slice ledger ---

	@GameTest(template = "test_empty")
	public void slopeLayerWriteSafe(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			// consume-on: painted SIZE=5 exports PaidUnits=5 (the ledger the
			// schematic will charge at print), not the stored paid.
			ContourConfig.CONSUME_MATERIAL.set(true);
			ContourBlockEntity be = be(h, P, slopeLayer(Direction.NORTH, 5));
			be.applyPayload(Blocks.STONE.defaultBlockState(), 2,
				Direction.NORTH, false);
			CompoundTag tag = new CompoundTag();
			be.writeSafe(tag, h.getLevel().registryAccess());
			h.assertTrue(tag.getInt("PaidUnits") == 5,
				"safeNBT paid != hostUnits: " + tag.getInt("PaidUnits"));
			h.assertTrue(tag.contains("Material"), "safeNBT material missing");

			// consume-off: no credit is exported.
			ContourConfig.CONSUME_MATERIAL.set(false);
			ContourBlockEntity be2 = be(h, P.east(2),
				slopeLayer(Direction.NORTH, 5));
			be2.applyPayload(Blocks.STONE.defaultBlockState(), 2,
				Direction.NORTH, false);
			CompoundTag tag2 = new CompoundTag();
			be2.writeSafe(tag2, h.getLevel().registryAccess());
			h.assertTrue(tag2.getInt("PaidUnits") == 0,
				"safeNBT exported credit with consume off");
			h.assertTrue(!MaterialSafety.isNeutral(be2.getMaterial()),
				"material not stored for writeSafe");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}
}
