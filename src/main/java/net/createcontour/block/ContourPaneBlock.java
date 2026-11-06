package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connective glazing pane: 2px center post plus 2px arms toward
 * connected sides. Connects to sturdy faces, other contour panes, and
 * vanilla panes/bars.
 */
public class ContourPaneBlock extends ContourConnectiveBlock {

	public ContourPaneBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected List<float[]> postBoxes() {
		return List.of(new float[]{7, 0, 7, 9, 16, 9});
	}

	@Override
	protected List<float[]> armBoxes(Direction dir) {
		return switch (dir) {
			case NORTH -> List.of(new float[]{7, 0, 0, 9, 16, 7});
			case SOUTH -> List.of(new float[]{7, 0, 9, 9, 16, 16});
			case EAST -> List.of(new float[]{9, 0, 7, 16, 16, 9});
			default -> List.of(new float[]{0, 0, 7, 7, 16, 9});
		};
	}

	@Override
	protected boolean connectsTo(BlockState neighbor, Direction dir,
		BlockGetter level, BlockPos pos, BlockPos neighborPos) {
		return neighbor.getBlock() instanceof ContourPaneBlock
			|| neighbor.getBlock() instanceof IronBarsBlock
			|| sturdy(neighbor, dir, level, pos, neighborPos);
	}
}
