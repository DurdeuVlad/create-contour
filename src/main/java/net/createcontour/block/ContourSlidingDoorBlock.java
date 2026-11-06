package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sliding door: same pairing, ledger, redstone and hinge semantics as
 * the hinged door (ADR-015 state toggle - no sweep), but OPEN slides
 * the leaf along its own plane toward the hinge side instead of
 * pivoting it perpendicular. The slid leaf parks in the pocket half
 * of the cell so the other half of the doorway is clear.
 */
public class ContourSlidingDoorBlock extends ContourDoorBlock {

	public ContourSlidingDoorBlock(Properties pProperties) {
		super(pProperties);
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		boolean hi = openSide(f, state.getValue(HINGE)) == f.getClockWise();
		if (!state.getValue(OPEN))
			return List.of(plateFor(f));
		// OPEN: leaf slid to the hinge-side half, still on the facing plane
		return List.of(switch (f) {
			case NORTH, SOUTH -> hi
				? new float[]{8, 0, f == Direction.NORTH ? 0 : 13, 16, 16, f == Direction.NORTH ? 3 : 16}
				: new float[]{0, 0, f == Direction.NORTH ? 0 : 13, 8, 16, f == Direction.NORTH ? 3 : 16};
			default -> hi
				? new float[]{f == Direction.EAST ? 13 : 0, 0, 8, f == Direction.EAST ? 16 : 3, 16, 16}
				: new float[]{f == Direction.EAST ? 13 : 0, 0, 0, f == Direction.EAST ? 16 : 3, 16, 8};
		});
	}

	private static float[] plateFor(Direction side) {
		return switch (side) {
			case NORTH -> new float[]{0, 0, 0, 16, 16, 3};
			case SOUTH -> new float[]{0, 0, 13, 16, 16, 16};
			case EAST -> new float[]{13, 0, 0, 16, 16, 16};
			default -> new float[]{0, 0, 0, 3, 16, 16};
		};
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
