package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connective fence: 4px post plus two 2px rails per connected side.
 * Connects to sturdy faces, other contour fences, vanilla fences, and
 * fence gates.
 */
public class ContourFenceBlock extends ContourConnectiveBlock {

	public ContourFenceBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected List<float[]> postBoxes() {
		return List.of(new float[]{6, 0, 6, 10, 16, 10});
	}

	@Override
	protected List<float[]> armBoxes(Direction dir) {
		return switch (dir) {
			case NORTH -> List.of(new float[]{6, 6, 0, 10, 9, 6},
				new float[]{6, 12, 0, 10, 15, 6});
			case SOUTH -> List.of(new float[]{6, 6, 10, 10, 9, 16},
				new float[]{6, 12, 10, 10, 15, 16});
			case EAST -> List.of(new float[]{10, 6, 6, 16, 9, 10},
				new float[]{10, 12, 6, 16, 15, 10});
			default -> List.of(new float[]{0, 6, 6, 6, 9, 10},
				new float[]{0, 12, 6, 6, 15, 10});
		};
	}

	@Override
	protected boolean connectsTo(BlockState neighbor, Direction dir,
		BlockGetter level, BlockPos pos, BlockPos neighborPos) {
		return neighbor.getBlock() instanceof ContourFenceBlock
			|| neighbor.getBlock() instanceof FenceBlock
			|| neighbor.getBlock() instanceof FenceGateBlock
			|| sturdy(neighbor, dir, level, pos, neighborPos);
	}
}
