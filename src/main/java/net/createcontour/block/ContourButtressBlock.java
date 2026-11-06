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
 * Stepped buttress: an 8px-wide rib that descends in four 4px steps
 * away from the wall face (16, 12, 8, 4 tall). FACING points at the
 * wall the buttress leans on. The gothic/fortress counterpart of
 * the bracket - pairs with gothic arches and portcullises.
 */
public class ContourButtressBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourButtressBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Four depth bands descending away from the facing face:
	 * 16, 12, 8, 4 tall, 8px wide centered.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int w = n == 0 ? 2 : 0;
		float[] heights = {16f, 12f, 8f, 4f};
		List<float[]> out = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			float d0 = i * 4f, d1 = d0 + 4f;
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = pos ? 16f - d1 : d0;
			b[n + 3] = pos ? 16f - d0 : d1;
			b[1] = 0f;
			b[4] = heights[i];
			b[w] = 4f;
			b[w + 3] = 12f;
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
		// Back against the wall: FACING is the clicked horizontal
		// face; floor clicks use the player's look direction.
		Direction f = ctx.getClickedFace().getAxis().isHorizontal()
			? ctx.getClickedFace() : ctx.getHorizontalDirection();
		return withWater(defaultBlockState().setValue(FACING, f), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(FACING);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
