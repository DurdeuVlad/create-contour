package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Turned vase-profile baluster on an axis - railings, stair
 * balustrades, fence posts. Profile (along the run axis):
 * foot r4.5 [0,1.5], stem r2.5 [1.5,4], belly out to r4.2 at 9
 * and back to r2.2 at 13, collar/cap r4 [13,16]. Collision is a
 * 4-band square cover (band half-width = band max radius) so it
 * never under-fills the turned surface.
 * Placement axis = clicked face; the wrench cycles axes.
 */
public class ContourBalusterBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public ContourBalusterBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/** {lo, hi, halfWidth} bands along the run axis. */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		int u = (run + 1) % 3, v = (run + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(4);
		float[][] bands = {{0f, 1.5f, 4.5f}, {1.5f, 4f, 2.5f},
			{4f, 13f, 4.2f}, {13f, 16f, 4f}};
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[run] = bd[0];
			b[run + 3] = bd[1];
			b[u] = 8 - bd[2];
			b[u + 3] = 8 + bd[2];
			b[v] = 8 - bd[2];
			b[v + 3] = 8 + bd[2];
			out.add(b);
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
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return withWater(defaultBlockState()
			.setValue(AXIS, ctx.getClickedFace().getAxis()), ctx);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(AXIS);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
