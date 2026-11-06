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
 * Pillar / baluster: an 8px shaft with 10px capital and base flares.
 * AXIS covers columns (Y), horizontal rails (X/Z). Distinct from the
 * plain post by the end caps. The wrench cycles axis.
 */
public class ContourPillarBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.AXIS;

	public ContourPillarBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	private static float[] alongAxis(Direction.Axis axis,
		float lo, float hi, float w0, float w1) {
		return switch (axis) {
			case Y -> new float[]{w0, lo, w0, w1, hi, w1};
			case X -> new float[]{lo, w0, w0, hi, w1, w1};
			default -> new float[]{w0, w0, lo, w1, w1, hi};
		};
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		Direction.Axis a = state.getValue(AXIS);
		return List.of(
			alongAxis(a, 2, 14, 4, 12),   // shaft
			alongAxis(a, 0, 2, 3, 13),    // base cap
			alongAxis(a, 14, 16, 3, 13)); // capital
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
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(AXIS, rotatedAxis(state.getValue(AXIS), rotation));
	}

	private static Direction.Axis rotatedAxis(Direction.Axis a, Rotation r) {
		boolean quarter = r == Rotation.CLOCKWISE_90
			|| r == Rotation.COUNTERCLOCKWISE_90;
		return switch (a) {
			case Y -> Direction.Axis.Y;
			case X -> quarter ? Direction.Axis.Z : Direction.Axis.X;
			default -> quarter ? Direction.Axis.X : Direction.Axis.Z;
		};
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
