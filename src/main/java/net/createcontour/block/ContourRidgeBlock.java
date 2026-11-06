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
 * Steep gable: two ramp faces meet at a center ridge line 16px up,
 * running along a horizontal axis (x or z). Roof caps, tent tops.
 * Collision is a 4-step staircase that never under-fills the ramps.
 */
public class ContourRidgeBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.HORIZONTAL_AXIS;

	public ContourRidgeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Pixel-space stepped boxes (collision only - render is the true
	 * ramp). Slab i along the span axis gets the max height of the
	 * profile h(u) = 2*min(u, 16-u): 8, 16, 16, 8.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		boolean x = state.getValue(AXIS) == Direction.Axis.X;
		float[] h = {8f, 16f, 16f, 8f};
		List<float[]> out = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			float s0 = i * 4f, s1 = s0 + 4f;
			// ridge runs along AXIS, so the profile varies on the
			// perpendicular in-plane axis
			out.add(x
				? new float[]{0, 0, s0, 16, h[i], s1}
				: new float[]{s0, 0, 0, s1, h[i], 16});
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
		// ridge runs along the horizontal axis nearest the click normal
		Direction.Axis a = ctx.getClickedFace().getAxis() == Direction.Axis.X
			? Direction.Axis.Z : Direction.Axis.X;
		if (ctx.getClickedFace().getAxis() == Direction.Axis.Y)
			a = ctx.getHorizontalDirection().getAxis();
		return withWater(defaultBlockState().setValue(AXIS, a), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return rotation == Rotation.CLOCKWISE_90
			|| rotation == Rotation.COUNTERCLOCKWISE_90
			? state.setValue(AXIS,
				state.getValue(AXIS) == Direction.Axis.X
					? Direction.Axis.Z : Direction.Axis.X)
			: state;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state; // a gable is symmetric under both flips
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.setValue(AXIS,
			state.getValue(AXIS) == Direction.Axis.X
				? Direction.Axis.Z : Direction.Axis.X);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
