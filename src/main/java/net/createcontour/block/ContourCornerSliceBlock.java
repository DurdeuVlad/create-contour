package net.createcontour.block;

import java.util.ArrayList;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Corner slice: a stepped wedge filling the 45-degree corner of a block
 * cell. FACING is the horizontal direction the corner opens toward; the
 * wedge hugs the corner edge opposite to it. LAYERS 1..4 at 2px steps
 * control how far the cut reaches along both adjacent edges (max 8px),
 * so a 4-layer slice is the full quarter-cell corner ramp. The wrench
 * cycles thickness; one paid unit per placed block (ADR-013).
 */
public class ContourCornerSliceBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty LAYERS =
		IntegerProperty.create("layers", 1, 4);

	public ContourCornerSliceBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH).setValue(LAYERS, 2));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(LAYERS));
	}

	/**
	 * Stepped wedge: row i (2px deep) reaches reach-2i pixels along the
	 * corner's long axis, giving a 45-degree staircase profile. Anchor
	 * corner is derived from FACING's counter-clockwise edge so rotations
	 * map cleanly: NORTH hugs the NW corner, EAST the NE, SOUTH the SE,
	 * WEST the SW.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int reach = state.getValue(LAYERS) * 2;
		List<float[]> out = new ArrayList<>();
		for (int i = 0; i < reach; i += 2) {
			int span = reach - i;
			out.add(switch (state.getValue(FACING)) {
				case EAST -> new float[]{16 - i - 2, 0, 0, 16, 16, span};
				case SOUTH -> new float[]{16 - span, 0, 16 - i - 2, 16, 16, 16};
				case WEST -> new float[]{i, 0, 16 - span, i + 2, 16, 16};
				default -> new float[]{0, 0, i, span, 16, i + 2};
			});
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
		// The corner is the horizontal direction bisecting the clicked
		// edge pair: use the player's look axis so wall-mounted corner
		// cuts land where the player aims.
		Direction face = ctx.getHorizontalDirection();
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.isSecondaryUseActive()
				? face.getCounterClockWise() : face)
			.setValue(LAYERS, 2), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(LAYERS);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
