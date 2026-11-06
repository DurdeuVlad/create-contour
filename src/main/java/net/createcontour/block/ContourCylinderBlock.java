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
 * Radius-6 round column on an axis - the classic column, pipe and
 * axle. Collision is a 3-step octagon that never under-fills the
 * circle. Placement axis = clicked face; the wrench cycles axes.
 */
public class ContourCylinderBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public ContourCylinderBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Octagonal collision for the r6 circle: corner chamfers at +-4.24.
	 * Three bands: a wide center band reaching the rim, plus two
	 * chamfered edge bands - the union covers the circle and never
	 * under-fills it.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		int u = (run + 1) % 3, v = (run + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(3);
		// {vLo, vHi, uLo, uHi} - center band covers |dv|<=3.76 fully
		float[][] bands = {{4.24f, 11.76f, 2f, 14f},
			{2f, 4.24f, 3.32f, 12.68f},
			{11.76f, 14f, 3.32f, 12.68f}};
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[v] = bd[0];
			b[v + 3] = bd[1];
			b[u] = bd[2];
			b[u + 3] = bd[3];
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
