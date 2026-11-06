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
 * Semicircular arch spanning the block: 3px jambs up to y=8, then a
 * 3px arc band (outer r8, inner r5, centered at y=8) across the top.
 * AXIS is the span direction - the passage runs perpendicular to it.
 * Collision under-fills the arc so the opening stays walkable.
 */
public class ContourArchBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.HORIZONTAL_AXIS;

	public ContourArchBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/** Pixel-space collision boxes: jambs + arc approximation columns. */
	public static List<float[]> boxesFor(BlockState state) {
		boolean x = state.getValue(AXIS) == Direction.Axis.X;
		List<float[]> out = new java.util.ArrayList<>(6);
		// jambs: 3px wide, up to y=8 where the arc springs
		out.add(span(x, 0, 0, 3, 8));
		out.add(span(x, 13, 0, 16, 8));
		// arc columns, tops under the outer arc (never over the opening)
		out.add(span(x, 3, 8, 6, 14));
		out.add(span(x, 6, 8, 8, 15));
		out.add(span(x, 8, 8, 10, 15));
		out.add(span(x, 10, 8, 13, 14));
		return out;
	}

	private static float[] span(boolean x, float s0, float y0, float s1, float y1) {
		return x
			? new float[]{s0, y0, 0, s1, y1, 16}
			: new float[]{0, y0, s0, 16, y1, s1};
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
		// span perpendicular to the player's look direction, or to the
		// clicked face when placing on a wall
		Direction.Axis a = ctx.getClickedFace().getAxis() == Direction.Axis.X
			|| ctx.getClickedFace().getAxis() == Direction.Axis.Z
			? (ctx.getClickedFace().getAxis() == Direction.Axis.X
				? Direction.Axis.Z : Direction.Axis.X)
			: ctx.getHorizontalDirection().getAxis();
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
		return state; // symmetric under both flips
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
