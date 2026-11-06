package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ghost block: renders as a full material cube but has no entity
 * collision - a walk-through facade for hidden passages. The targeting
 * outline stays a full cube so the block can be aimed at, wrenched,
 * and painted like any other contour shape. Documented: the block
 * still occludes neighbor-face culling like a solid cube (it is meant
 * to look solid), but never blocks movement.
 */
public class ContourGhostBlock extends ContourBlock {

	public ContourGhostBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		return List.of(new float[]{0, 0, 0, 16, 16, 16});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return Shapes.block();
	}

	/** Entities pass through: the ghost is only a visual. */
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
		BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
