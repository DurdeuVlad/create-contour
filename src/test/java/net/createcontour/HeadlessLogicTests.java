package net.createcontour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.createcontour.block.ShapeGrid;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

/**
 * Headless logic tests: pure static math (no MC bootstrap - NeoForge's
 * FeatureFlagLoader requires LoadingModList, so Blocks/registries are
 * unavailable here). World-dependent cases (BE NBT, economics, gestures,
 * vanilla-oracle parity, recipes) are covered by the registered
 * create_contour GameTests executed inside a dev server.
 */
public class HeadlessLogicTests {

	private static int cell(Direction facing, StairsShape shape) {
		return ShapeGrid.cellMask(Half.BOTTOM, facing, shape);
	}

	private static int count(int mask) {
		return Integer.bitCount(mask);
	}

	@Test
	void cellMaskCardinality() {
		for (Half half : Half.values())
			for (Direction f : Direction.Plane.HORIZONTAL)
				for (StairsShape s : StairsShape.values()) {
					int m = ShapeGrid.cellMask(half, f, s);
					int expected = switch (s) {
						case STRAIGHT -> 6;   // slab layer + facing half
						case INNER_LEFT, INNER_RIGHT -> 7;   // + perpendicular leg
						default -> 5;   // OUTER: slab + corner cell
					};
					assertEquals(expected, count(m), half + "/" + f + "/" + s);
				}
	}

	@Test
	void tallHalfFacesFacing() {
		// NORTH-facing straight bottom stair: full y=0 layer (4 cells,
		// indices 0,1,4,5) + z=0 cells of y=1 layer (indices 2,3).
		assertEquals(0b00111111, cell(Direction.NORTH, StairsShape.STRAIGHT));
		// TOP half mirrors the layers.
		assertEquals(0b11001111,
			ShapeGrid.cellMask(Half.TOP, Direction.NORTH, StairsShape.STRAIGHT));
	}

	@Test
	void innerOuterMirrorSymmetry() {
		for (Direction f : Direction.Plane.HORIZONTAL) {
			int il = cell(f, StairsShape.INNER_LEFT);
			int ir = cell(f, StairsShape.INNER_RIGHT);
			int ol = cell(f, StairsShape.OUTER_LEFT);
			int or = cell(f, StairsShape.OUTER_RIGHT);
			// left/right variants must differ only by reflection along the
			// perpendicular axis: same cardinality, different masks.
			assertEquals(count(il), count(ir));
			assertEquals(count(ol), count(or));
			assertTrue(il != ir || f == null, "left/right identical for " + f);
			assertTrue(ol != or, "outer left/right identical for " + f);
			// OUTER corner cell is contained in the INNER mask.
			assertEquals(ol, ol & il, "outer not subset of inner " + f);
		}
	}

	@Test
	void shapeGridBuildsExpectedMask() {
		var shape = ShapeGrid.build(0b00110011); // all 4 cells of y=0 layer
		assertTrue(Shapes.joinIsNotEmpty(shape, Block.box(0, 0, 0, 16, 8, 16), BooleanOp.AND));
		assertFalse(Shapes.joinIsNotEmpty(shape, Block.box(0, 8, 0, 16, 16, 16), BooleanOp.AND));
	}
}
