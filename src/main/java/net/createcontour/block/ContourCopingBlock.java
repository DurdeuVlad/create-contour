package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Coping: the wall-top cap - a narrow seat that drops over the
 * wall and a wider 3px cap plate overhanging both faces. Runs
 * along the wall on HORIZONTAL_AXIS, completing the flat-wall
 * top the way the ridge caps a gable.
 */
public class ContourCopingBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.HORIZONTAL_AXIS;

	public ContourCopingBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/** Seat u 5..11 y 0..2 + cap plate u 2..14 y 2..5. */
	public static List<float[]> boxesFor(BlockState state) {
		int a = state.getValue(AXIS).ordinal();
		int u = a == 0 ? 2 : 0;
		float[] seat = {0, 0, 0, 16, 16, 16};
		seat[a] = 0f;
		seat[a + 3] = 16f;
		seat[1] = 0f;
		seat[4] = 2f;
		seat[u] = 5f;
		seat[u + 3] = 11f;
		float[] cap = {0, 0, 0, 16, 16, 16};
		cap[a] = 0f;
		cap[a + 3] = 16f;
		cap[1] = 2f;
		cap[4] = 5f;
		cap[u] = 2f;
		cap[u + 3] = 14f;
		return List.of(seat, cap);
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
			.setValue(AXIS, ctx.getHorizontalDirection().getAxis()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(AXIS, rotation.rotate(Direction
			.fromAxisAndDirection(state.getValue(AXIS),
				Direction.AxisDirection.POSITIVE)).getAxis());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state;
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
