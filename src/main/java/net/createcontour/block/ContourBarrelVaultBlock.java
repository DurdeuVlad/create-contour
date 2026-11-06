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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Semicircular barrel vault: an r8 half-cylinder shell spanning the
 * full block width, extruded along the run axis (HORIZONTAL_AXIS).
 * HALF says which half of the block the solid occupies: BOTTOM is the
 * classic floor-standing vault, TOP hangs it as an inverted cove
 * ceiling. Collision is a 6-step cover of the half-disk that never
 * under-fills the arc.
 */
public class ContourBarrelVaultBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.HORIZONTAL_AXIS;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	public ContourBarrelVaultBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(AXIS, Direction.Axis.X).setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS).add(HALF));
	}

	/**
	 * Stepped half-disk cover in the arc plane. For BOTTOM the solid
	 * is y 0..sqrt(64-(u-8)^2); TOP mirrors to 16-arc..16. Six bands
	 * top out at the max outer-arc height inside each u band.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		int u = run == 2 ? 0 : 2;
		boolean bottom = state.getValue(HALF) == Half.BOTTOM;
		List<float[]> out = new java.util.ArrayList<>(6);
		float[][] bands = {{0f, 2f, 5.3f}, {2f, 4f, 7f}, {4f, 6f, 7.8f},
			{6f, 10f, 8f}, {10f, 12f, 7.8f}, {12f, 14f, 7f}, {14f, 16f, 5.3f}};
		for (float[] bd : bands) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = bd[0];
			b[u + 3] = bd[1];
			b[1] = bottom ? 0f : 16f - bd[2];
			b[4] = bottom ? bd[2] : 16f;
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
		Direction face = ctx.getClickedFace();
		Direction.Axis a = face.getAxis() == Direction.Axis.Y
			? ctx.getHorizontalDirection().getAxis()
			: face.getAxis() == Direction.Axis.X ? Direction.Axis.Z
				: Direction.Axis.X;
		double fy = ctx.getClickLocation().y - ctx.getClickedPos().getY();
		Half h = face == Direction.DOWN
			|| (face.getAxis() != Direction.Axis.Y && fy > 0.5)
			? Half.BOTTOM : Half.TOP;
		return withWater(defaultBlockState().setValue(AXIS, a)
			.setValue(HALF, h), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
		return state.cycle(AXIS);
	}

	@Override
	public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
		return state;
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		if (state.getValue(AXIS) == Direction.Axis.Z)
			return state.setValue(AXIS, Direction.Axis.X).setValue(HALF,
				state.getValue(HALF) == Half.BOTTOM ? Half.TOP : Half.BOTTOM);
		return state.cycle(AXIS);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
