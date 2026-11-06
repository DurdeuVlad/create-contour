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
 * Hollow wireframe cube: 3px bars along all 12 cube edges, open
 * 10x10 faces on every side. Window frames, machine cages, portal
 * edging - the see-through center takes the painted material on the
 * bars only.
 */
public class ContourFrameBlock extends ContourBlock {

	public ContourFrameBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		java.util.ArrayList<float[]> out = new java.util.ArrayList<>(12);
		for (int i = 0; i < 2; i++) {
			float lo = i == 0 ? 0 : 13;
			for (int j = 0; j < 2; j++) {
				float lo2 = j == 0 ? 0 : 13;
				out.add(new float[]{0, lo, lo2, 16, lo + 3, lo2 + 3}); // X-run
				out.add(new float[]{lo, 0, lo2, lo + 3, 16, lo2 + 3}); // Y-run
				out.add(new float[]{lo, lo2, 0, lo + 3, lo2 + 3, 16}); // Z-run
			}
		}
		return out;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
