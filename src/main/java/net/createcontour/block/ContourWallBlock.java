package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connective wall: 4px square post (always full height) plus 4px arms
 * toward connected sides. Connects to sturdy faces, other contour walls,
 * vanilla walls, and fence gates (golden-rule neighbor behavior).
 */
public class ContourWallBlock extends ContourConnectiveBlock {

	public ContourWallBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected List<float[]> postBoxes() {
		return List.of(new float[]{6, 0, 6, 10, 16, 10});
	}

	@Override
	protected List<float[]> armBoxes(Direction dir) {
		return switch (dir) {
			case NORTH -> List.of(new float[]{6, 0, 0, 10, 14, 6});
			case SOUTH -> List.of(new float[]{6, 0, 10, 10, 14, 16});
			case EAST -> List.of(new float[]{10, 0, 6, 16, 14, 10});
			default -> List.of(new float[]{0, 0, 6, 6, 14, 10});
		};
	}

	@Override
	protected boolean connectsTo(BlockState neighbor, Direction dir,
		BlockGetter level, BlockPos pos, BlockPos neighborPos) {
		return neighbor.getBlock() instanceof ContourWallBlock
			|| neighbor.getBlock() instanceof WallBlock
			|| neighbor.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock
			|| sturdy(neighbor, dir, level, pos, neighborPos);
	}
}
