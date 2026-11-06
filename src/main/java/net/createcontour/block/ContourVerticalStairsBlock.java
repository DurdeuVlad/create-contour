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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wall corner step: an L-profile prism - the vanilla stair cross
 * section turned upright. Mass = the half-slab on the FACING-opposite
 * side plus an arm in the HALF half of the FACING side. The open
 * corner quadrant is on the facing side, opposite HALF. Used for
 * roof-eave corners, wall ledges and arch fill.
 */
public class ContourVerticalStairsBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	public ContourVerticalStairsBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH)
			.setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, HALF));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		boolean top = state.getValue(HALF) == Half.TOP;
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<float[]> out = new java.util.ArrayList<>(2);
		// back half slab, full height, on the side opposite facing
		{
			float[] b = {0, 0, 0, 16, 16, 16};
			if (pos) b[a + 3] = 8; else b[a] = 8;
			out.add(b);
		}
		// arm: front half, in the HALF band
		{
			float[] b = {0, 0, 0, 16, 16, 16};
			if (pos) b[a] = 8; else b[a + 3] = 8;
			b[1] = top ? 8 : 0;
			b[4] = top ? 16 : 8;
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
		Direction f = ctx.getHorizontalDirection();
		// click above mid-face puts the arm at the top (eave corner),
		// below mid-face at the bottom (wall ledge)
		double y = ctx.getClickLocation().y - ctx.getClickedPos().getY();
		Half half = y > 0.5 ? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState()
			.setValue(FACING, f).setValue(HALF, half), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(HALF);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
