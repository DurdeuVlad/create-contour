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
 * Open U-channel: three 2px plates leaving a 12px open trough.
 * Runs along AXIS, open on FACING (must be perpendicular). A
 * horizontal channel is a gutter; a vertical one is a chute.
 * Clicked face sets the open side; click a wall to mount a channel
 * running horizontally along it, sneak for a vertical chute.
 */
public class ContourGutterBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public ContourGutterBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(AXIS, Direction.Axis.X)
			.setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS, FACING));
	}

	/** FACING must never lie along the run axis. */
	public static BlockState fix(BlockState s) {
		if (s.getValue(FACING).getAxis() == s.getValue(AXIS))
			return s.setValue(FACING, s.getValue(AXIS) == Direction.Axis.Y
				? Direction.NORTH : Direction.UP);
		return s;
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		state = fix(state); // degenerate axis=facing states can't exist
		Direction.Axis run = state.getValue(AXIS);
		Direction f = state.getValue(FACING);
		int back = f.getAxis().ordinal();
		int wall = 3 - run.ordinal() - back; // third in-plane axis
		boolean backPos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<float[]> out = new java.util.ArrayList<>(3);
		// back plate, opposite the open face: open toward +axis puts
		// the plate at the low end
		{
			float[] b = {0, 0, 0, 16, 16, 16};
			if (backPos) b[back + 3] = 2; else b[back] = 14;
			out.add(b);
		}
		// two full-height side walls on the third axis
		for (int side = 0; side < 2; side++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[wall] = side == 0 ? 0 : 14;
			b[wall + 3] = side == 0 ? 2 : 16;
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
		Direction open = ctx.getClickedFace();
		Direction.Axis axis;
		if (open.getAxis() == Direction.Axis.Y)
			// floor/ceiling channel runs along the player's look axis
			axis = ctx.getHorizontalDirection().getAxis();
		else
			// wall channel runs horizontally along the wall; sneak = chute
			axis = ctx.isSecondaryUseActive()
				? Direction.Axis.Y
				: (open.getAxis() == Direction.Axis.X
					? Direction.Axis.Z : Direction.Axis.X);
		return withWater(fix(defaultBlockState()
			.setValue(AXIS, axis).setValue(FACING, open)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction f = rotation.rotate(state.getValue(FACING));
		Direction.Axis a = state.getValue(AXIS);
		if (a != Direction.Axis.Y && rotation != Rotation.NONE)
			a = a == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		return fix(state.setValue(FACING, f).setValue(AXIS, a));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return fix(state.setValue(FACING,
			mirror.mirror(state.getValue(FACING))));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// cycle the run axis, keeping the open face valid
		return fix(state.cycle(AXIS));
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
