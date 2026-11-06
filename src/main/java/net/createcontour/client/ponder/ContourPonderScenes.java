package net.createcontour.client.ponder;

import net.createcontour.block.ContourSlabBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.registry.ContourRegistries;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Contour tutorial scenes. World content lives in
 * assets/create_contour/ponder/*.nbt; dynamic demonstrations place and
 * mutate blocks through the scene instructions.
 */
public final class ContourPonderScenes {

	private ContourPonderScenes() {
	}

	/** "Using the Contour Placer": configure, place, and swoop. */
	public static void placer(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("placer", "Using the Contour Placer");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		scene.world().showSection(util.select().everywhere(), Direction.DOWN);
		scene.idle(20);

		ItemStack placer = new ItemStack(ContourRegistries.CONTOUR_PLACER.get());
		ItemStack blank = new ItemStack(ContourRegistries.CONTOUR_BLANK_ITEM.get());
		ItemStack oak = new ItemStack(Items.OAK_PLANKS);
		BlockPos spot = util.grid().at(2, 1, 2);
		Vec3 spotTop = util.vector().topOf(spot);
		Vec3 above = util.vector().of(2.5, 3.4, 2.5);

		scene.overlay().showText(60)
			.text("The Contour Placer places any Contour shape using Contour Blanks as feedstock")
			.attachKeyFrame().placeNearTarget().pointAt(above);
		scene.world().createItemEntity(above, Vec3.ZERO, placer);
		scene.idle(70);

		scene.overlay().showText(50)
			.text("Hold it, sneak, and right-click to choose a shape on the rear screen")
			.attachKeyFrame().placeNearTarget().pointAt(util.vector().of(2.5, 2.6, 2.5));
		scene.overlay().showControls(util.vector().of(2.5, 2.7, 2.5), Pointing.LEFT, 50)
			.whileSneaking().rightClick().withItem(placer);
		scene.idle(60);

		scene.overlay().showText(50)
			.text("Each placement consumes one Contour Blank in survival")
			.attachKeyFrame().placeNearTarget().pointAt(spotTop);
		scene.overlay().showControls(util.vector().of(1.5, 2.6, 1.5), Pointing.RIGHT, 50)
			.withItem(blank);
		scene.idle(55);

		scene.overlay().showControls(spotTop, Pointing.DOWN, 40)
			.leftClick().withItem(placer);
		scene.idle(15);
		scene.world().setBlock(spot,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState(), true);
		scene.world().modifyBlockEntity(spot, ContourBlockEntity.class, be -> {
			be.setMaterial(Blocks.OAK_PLANKS.defaultBlockState());
			be.setPaidUnits(1);
		});
		scene.effects().indicateSuccess(spot);
		scene.overlay().showText(60)
			.text("Left-click places the configured shape; a copied material is charged like normal")
			.attachKeyFrame().placeNearTarget().pointAt(spotTop);
		scene.idle(75);

		scene.overlay().showControls(util.vector().blockSurface(spot, Direction.UP),
			Pointing.UP, 40).rightClick().withItem(placer);
		scene.idle(15);
		scene.world().destroyBlock(spot);
		scene.world().createItemEntity(spotTop, new Vec3(-0.1, 0.2, -0.05), blank);
		scene.world().createItemEntity(spotTop, new Vec3(0.1, 0.2, 0.05), oak);
		scene.overlay().showText(65)
			.text("Right-click a Contour shape to swoop it back into feedstock plus its material")
			.attachKeyFrame().placeNearTarget().pointAt(spotTop);
		scene.idle(80);
		scene.markAsFinished();
	}
}
