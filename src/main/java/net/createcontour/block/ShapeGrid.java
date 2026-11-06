package net.createcontour.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;

/**
 * Occupancy-grid helper: each shape is described as a set of occupied
 * cells on a 2x2x2 grid (8 voxel octants, 8x8x8 each). Collision == render
 * silhouette for the first three shapes by construction.
 */
public final class ShapeGrid {

	private ShapeGrid() {
	}

	/** Build a VoxelShape from an 8-bit occupancy mask (index = x|y<<1|z<<2). */
	public static VoxelShape build(int mask) {
		VoxelShape shape = Shapes.empty();
		for (int i = 0; i < 8; i++) {
			if ((mask & (1 << i)) == 0)
				continue;
			int x = i & 1, y = (i >> 1) & 1, z = (i >> 2) & 1;
			shape = Shapes.or(shape, Block.box(x * 8, y * 8, z * 8,
				(x + 1) * 8, (y + 1) * 8, (z + 1) * 8));
		}
		return shape.optimize();
	}

	/** index helper: x 0/1, y 0/1, z 0/1 (each in half-block octants). */
	public static int idx(int x, int y, int z) {
		return x | (y << 1) | (z << 2);
	}

	// --- stair occupancy math (pure: no vanilla registry class init) ---

	/** cells where the coordinate along `axis` equals `bit` in layer `y`. */
	private static int axisHalf(Direction.Axis axis, int bit, int y) {
		int mask = 0;
		for (int a = 0; a < 2; a++)
			for (int b = 0; b < 2; b++) {
				int x = axis == Direction.Axis.X ? bit : a;
				int z = axis == Direction.Axis.Z ? bit : b;
				mask |= 1 << (x | (y << 1) | (z << 2));
			}
		return mask;
	}

	private static int layerMask(int y) {
		int mask = 0;
		for (int x = 0; x < 2; x++)
			for (int z = 0; z < 2; z++)
				mask |= 1 << (x | (y << 1) | (z << 2));
		return mask;
	}

	/**
	 * Stair occupancy mask for a (half, facing, shape) triple. Tall half
	 * occupies the FACING side; STRAIGHT = slab layer + facing half, INNER
	 * = + perpendicular leg, OUTER = slab + facing-side corner cell.
	 */
	public static int cellMask(Half half, Direction facing, StairsShape shape) {
		int baseLayer = half == Half.BOTTOM ? 0 : 1;
		int highLayer = 1 - baseLayer;
		int faceBit = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
		int faceCells = axisHalf(facing.getAxis(), faceBit, highLayer);

		boolean left = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
		Direction side = left ? facing.getCounterClockWise() : facing.getClockWise();
		int sideBit = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;

		int high;
		switch (shape) {
			case STRAIGHT -> high = faceCells;
			case INNER_LEFT, INNER_RIGHT -> high = faceCells | axisHalf(side.getAxis(), sideBit, highLayer);
			default -> {
				int corner = 0;
				for (int i = 0; i < 8; i++) {
					int cx = i & 1, cz = (i >> 2) & 1;
					int alongFace = facing.getAxis() == Direction.Axis.X ? cx : cz;
					int alongSide = side.getAxis() == Direction.Axis.X ? cx : cz;
					if (((i >> 1) & 1) == highLayer && alongFace == faceBit && alongSide == sideBit)
						corner |= 1 << i;
				}
				high = corner;
			}
		}
		return layerMask(baseLayer) | high;
	}
}
