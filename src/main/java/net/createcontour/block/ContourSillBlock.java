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
 * Window sill: a 14px ledge protruding 8px from the wall face with
 * a weather-shedding sloped top (4px at the wall down to 2.5px at
 * the drip edge) and a flat bottom. FACING points at the wall, the
 * same convention as the buttress. Completes the window kit.
 */
public class ContourSillBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	/** Sloped top edge: 4 at the wall (n=0) to 2.5 at the tip (n=8). */
	public static float topAt(float n) {
		return 4f - 0.1875f * n;
	}

	public ContourSillBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Two collision steps covering the sheared prism without
	 * under-filling: n 0..4 up to y=4, n 4..8 up to y=3.5.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(2);
		float[][] steps = {{0f, 4f, 4f}, {4f, 8f, 3.5f}};
		for (float[] s : steps) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = 1f;
			b[u + 3] = 15f;
			b[n] = pos ? 16f - s[1] : s[0];
			b[n + 3] = pos ? 16f - s[0] : s[1];
			b[1] = 0f;
			b[4] = s[2];
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
