package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Catwalk: an open-grate walkway - a 1px sheet on the cell floor with
 * 8px rail lips running along the two edges perpendicular to AXIS.
 * AXIS X lays rails on the north and south edges; AXIS Z lays them on
 * east and west so adjacent catwalks chain along the walking
 * direction. The wrench flips the rail axis.
 */
public class ContourCatwalkBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.HORIZONTAL_AXIS;

	public ContourCatwalkBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		boolean x = state.getValue(AXIS) == Direction.Axis.X;
		return List.of(
			new float[]{0, 0, 0, 16, 1, 16},
			x ? new float[]{0, 1, 0, 1, 8, 16} : new float[]{0, 1, 0, 16, 8, 1},
			x ? new float[]{15, 1, 0, 16, 8, 16} : new float[]{0, 1, 15, 16, 8, 16});
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
		if (rotation == Rotation.CLOCKWISE_90
			|| rotation == Rotation.COUNTERCLOCKWISE_90)
			return state.cycle(AXIS);
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
