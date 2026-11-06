package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Climbable scaffold: four corner posts, a walkable top platform, and
 * four mid rails. Unlike the ladder it needs no backing wall -
 * NeoForge isLadder returns true for every face so an entity inside
 * the open lattice climbs from any direction, and the top plate is a
 * standing surface. Stateless (waterlogged only); the wrench is a
 * no-op via nextInFamily()==null.
 */
public class ContourScaffoldBlock extends ContourBlock {

	public ContourScaffoldBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
	}

	/**
	 * Nine boxes: four 1.5px corner posts 0..14.5 meeting the top
	 * plate's underside, the plate 14.5..16 across the whole face,
	 * and a 7..8.5 rail band on each side. Posts stop under the plate
	 * so nothing overlaps.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		List<float[]> out = new java.util.ArrayList<>(9);
		out.add(new float[]{0f, 0f, 0f, 1.5f, 14.5f, 1.5f});
		out.add(new float[]{14.5f, 0f, 0f, 16f, 14.5f, 1.5f});
		out.add(new float[]{0f, 0f, 14.5f, 1.5f, 14.5f, 16f});
		out.add(new float[]{14.5f, 0f, 14.5f, 16f, 14.5f, 16f});
		out.add(new float[]{0f, 14.5f, 0f, 16f, 16f, 16f});
		out.add(new float[]{1.5f, 7f, 0f, 14.5f, 8.5f, 1.5f});
		out.add(new float[]{1.5f, 7f, 14.5f, 14.5f, 8.5f, 16f});
		out.add(new float[]{0f, 7f, 1.5f, 1.5f, 8.5f, 14.5f});
		out.add(new float[]{14.5f, 7f, 1.5f, 16f, 8.5f, 14.5f});
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
	public boolean isLadder(BlockState state, LevelReader level, BlockPos pos,
		LivingEntity entity) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return withWater(defaultBlockState(), ctx);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
