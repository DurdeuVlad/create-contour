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
 * Valley: the V-flashing where two roof slopes meet - two sheared
 * planes dropping 8px from the outer edges to the center line.
 * AXIS is the valley's run direction; the counterpart of the
 * ridge cap and the cousin of the gutter.
 */
public class ContourValleyBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.HORIZONTAL_AXIS;

	public ContourValleyBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * u = cross axis, w = run axis. Four stepped bands covering
	 * the 2px-thick planes without under-fill: u 0..4 y 10..16,
	 * u 4..8 y 6..12, mirrored right.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int w = state.getValue(AXIS).ordinal();
		int u = w == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(4);
		float[][] steps = {{0f, 4f, 10f, 16f}, {4f, 8f, 6f, 12f},
			{8f, 12f, 6f, 12f}, {12f, 16f, 10f, 16f}};
		for (float[] s : steps) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = s[0]; b[u + 3] = s[1];
			b[1] = s[2]; b[4] = s[3];
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
		// Run along the player's horizontal look direction.
		return withWater(defaultBlockState().setValue(AXIS,
			ctx.getHorizontalDirection().getAxis()), ctx);
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
