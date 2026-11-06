package net.createcontour.gametest;

import net.createcontour.item.ContourPlacerItem;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Place-gun tests: selection read/write, placement consumes a
 * contour_blank in survival, no-ammo fails cleanly, swoop reclaims
 * a Contour shape as a blank, and non-shape targets are ignored.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourPlacerGameTests {

	private static final BlockPos P = new BlockPos(2, 1, 2);

	private static Player holder(GameTestHelper h, ItemStack gun) {
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		p.setPos(Vec3.atCenterOf(h.absolutePos(P)));
		p.getInventory().clearContent();
		p.setItemInHand(InteractionHand.MAIN_HAND, gun);
		return p;
	}

	private static ItemStack gun() {
		return new ItemStack(ContourRegistries.CONTOUR_PLACER.get());
	}

	private static ItemStack blank(int n) {
		return new ItemStack(ContourRegistries.CONTOUR_BLANK_ITEM.get(), n);
	}

	private static int blanks(Player p) {
		int n = 0;
		for (ItemStack s : p.getInventory().items)
			if (s.is(ContourRegistries.CONTOUR_BLANK_ITEM.get()))
				n += s.getCount();
		return n;
	}

	@GameTest(template = "test_empty")
	public void placerPlace(GameTestHelper h) {
		h.setBlock(P, Blocks.STONE.defaultBlockState());
		ItemStack g = gun();
		h.assertTrue(ContourPlacerItem.setShape(g,
			ResourceLocation.fromNamespaceAndPath("create_contour", "contour_post")),
			"setShape rejected post");
		Player p = holder(h, g);
		p.getInventory().add(blank(2));
		InteractionResult r = ContourPlacerItem.tryPlace(h.getLevel(), p,
			InteractionHand.MAIN_HAND, h.absolutePos(P), Direction.UP);
		h.assertTrue(r.consumesAction(), "place failed: " + r);
		h.assertTrue(h.getLevel().getBlockState(h.absolutePos(P.above()))
			.is(ContourRegistries.CONTOUR_POST.get()), "post not placed");
		h.assertTrue(h.getLevel().getBlockEntity(h.absolutePos(P.above()))
			instanceof net.createcontour.blockentity.ContourBlockEntity,
			"placed shape has no BE");
		h.assertTrue(blanks(p) == 1, "blank not consumed: " + blanks(p));
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void placerNoAmmo(GameTestHelper h) {
		h.setBlock(P, Blocks.STONE.defaultBlockState());
		Player p = holder(h, gun());
		InteractionResult r = ContourPlacerItem.tryPlace(h.getLevel(), p,
			InteractionHand.MAIN_HAND, h.absolutePos(P), Direction.UP);
		h.assertTrue(!r.consumesAction(), "placed with no ammo");
		h.assertTrue(h.getLevel().getBlockState(h.absolutePos(P.above())).isAir(),
			"placed with no ammo");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void placerSwoop(GameTestHelper h) {
		h.setBlock(P, ContourRegistries.CONTOUR_WALL.get().defaultBlockState());
		Player p = holder(h, gun());
		InteractionResult r = ContourPlacerItem.trySwoop(h.getLevel(), p,
			h.absolutePos(P));
		h.assertTrue(r.consumesAction(), "swoop failed: " + r);
		h.assertTrue(h.getLevel().getBlockState(h.absolutePos(P)).isAir(),
			"shape not reclaimed");
		h.assertTrue(blanks(p) == 1, "blank not returned: " + blanks(p));
		// painted shape: blank feedstock + paid material refund
		h.setBlock(P, ContourRegistries.CONTOUR_WALL.get().defaultBlockState());
		var be = (net.createcontour.blockentity.ContourBlockEntity) h.getLevel()
			.getBlockEntity(h.absolutePos(P));
		be.applyPayload(Blocks.STONE.defaultBlockState(), 1,
			net.minecraft.core.Direction.NORTH, false);
		r = ContourPlacerItem.trySwoop(h.getLevel(), p, h.absolutePos(P));
		h.assertTrue(r.consumesAction(), "painted swoop failed: " + r);
		h.assertTrue(blanks(p) == 2, "painted shape blank not returned");
		h.assertTrue(p.getInventory().countItem(Items.STONE) == 1,
			"paid material not refunded on swoop");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void placerSwoopNonShape(GameTestHelper h) {
		h.setBlock(P, Blocks.STONE.defaultBlockState());
		Player p = holder(h, gun());
		InteractionResult r = ContourPlacerItem.trySwoop(h.getLevel(), p,
			h.absolutePos(P));
		h.assertTrue(r == InteractionResult.PASS, "swooped a non-shape: " + r);
		h.assertTrue(h.getLevel().getBlockState(h.absolutePos(P))
			.is(Blocks.STONE), "non-shape removed");
		h.assertTrue(blanks(p) == 0, "blank minted from nothing");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void placerSelect(GameTestHelper h) {
		ItemStack g = gun();
		// default selection falls back to the first shape (slab)
		h.assertTrue(ContourPlacerItem.selectedBlock(g)
			== ContourRegistries.CONTOUR_SLAB.get(), "default not slab");
		// valid shape id round-trips
		h.assertTrue(ContourPlacerItem.setShape(g, ResourceLocation
			.fromNamespaceAndPath("create_contour", "contour_dome")),
			"setShape rejected dome");
		h.assertTrue(ContourPlacerItem.selectedBlock(g)
			== ContourRegistries.CONTOUR_DOME.get(), "dome not selected");
		// non-shape ids are rejected without mutating the selection
		h.assertTrue(!ContourPlacerItem.setShape(g, ResourceLocation
			.fromNamespaceAndPath("minecraft", "stone")), "accepted stone");
		h.assertTrue(ContourPlacerItem.selectedBlock(g)
			== ContourRegistries.CONTOUR_DOME.get(), "selection clobbered");
		h.succeed();
	}
}
