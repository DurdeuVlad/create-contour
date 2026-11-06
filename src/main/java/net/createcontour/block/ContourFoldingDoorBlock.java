package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Folding door: same pairing, ledger, redstone and hinge semantics as
 * the hinged door (ADR-015 state toggle - no sweep), but OPEN folds
 * the leaf in half against the jamb instead of pivoting the full
 * leaf perpendicular: the open silhouette is a half-span plate of
 * double thickness parked on the hinge-side edge.
 */
public class ContourFoldingDoorBlock extends ContourDoorBlock {

	public ContourFoldingDoorBlock(Properties pProperties) {
		super(pProperties);
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		if (!state.getValue(OPEN)) {
			return List.of(switch (f) {
				case NORTH -> new float[]{0, 0, 0, 16, 16, 3};
				case SOUTH -> new float[]{0, 0, 13, 16, 16, 16};
				case EAST -> new float[]{13, 0, 0, 16, 16, 16};
				default -> new float[]{0, 0, 0, 3, 16, 16};
			});
		}
		// OPEN: leaf folded in half within its own plane, parked at the
		// open-side jamb - half the span, doubled thickness (distinct
		// from the sliding leaf's half-span single thickness).
		Direction side = openSide(f, state.getValue(HINGE));
		boolean westOrNorth = side == Direction.WEST || side == Direction.NORTH;
		return List.of(switch (f.getAxis()) {
			case Direction.Axis.Z -> westOrNorth
				? new float[]{0, 0, f == Direction.NORTH ? 0 : 10, 8, 16,
					f == Direction.NORTH ? 6 : 16}
				: new float[]{8, 0, f == Direction.NORTH ? 0 : 10, 16, 16,
					f == Direction.NORTH ? 6 : 16};
			default -> westOrNorth
				? new float[]{f == Direction.EAST ? 10 : 0, 0, 0,
					f == Direction.EAST ? 16 : 6, 16, 8}
				: new float[]{f == Direction.EAST ? 10 : 0, 0, 8,
					f == Direction.EAST ? 16 : 6, 16, 16};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}
}
