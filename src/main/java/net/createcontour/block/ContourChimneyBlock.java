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
 * Chimney: the roofline stack - a wide plinth, a tapered shaft,
 * and a lip band, on all three axes (upright stacks plus
 * lying-down variants for angled flues). Three band boxes, the
 * square counterpart of the lathe bollard/newel posts.
 */
public class ContourChimneyBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.AXIS;

	public ContourChimneyBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/** Plinth [1,15], shaft [3,13], lip [2,14] bands along the axis. */
	public static List<float[]> boxesFor(BlockState state) {
		int a = state.getValue(AXIS).ordinal();
		int u = (a + 1) % 3, v = (a + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(3);
		float[] plinth = {0, 0, 0, 16, 16, 16};
		plinth[a] = 0f;
		plinth[a + 3] = 2f;
		plinth[u] = 1f;
		plinth[u + 3] = 15f;
		plinth[v] = 1f;
		plinth[v + 3] = 15f;
		out.add(plinth);
		float[] shaft = {0, 0, 0, 16, 16, 16};
		shaft[a] = 2f;
		shaft[a + 3] = 14f;
		shaft[u] = 3f;
		shaft[u + 3] = 13f;
		shaft[v] = 3f;
		shaft[v + 3] = 13f;
		out.add(shaft);
		float[] lip = {0, 0, 0, 16, 16, 16};
		lip[a] = 14f;
		lip[a + 3] = 16f;
		lip[u] = 2f;
		lip[u + 3] = 14f;
		lip[v] = 2f;
		lip[v + 3] = 14f;
		out.add(lip);
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
			.setValue(AXIS, ctx.getNearestLookingVerticalDirection()
				.getAxis() == Direction.Axis.Y
					? Direction.Axis.Y
					: ctx.getHorizontalDirection().getAxis()), ctx);
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
