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
 * Flying buttress: the gothic flyer - a 4px pier at 9-13px from
 * the wall face topped by a quarter-arc strut (band r5.5-8 swept
 * 90 deg about center n=4, y=8) that springs from the pier top
 * into a wall abutment near the block top. FACING points at the
 * wall face, like the stepped buttress. The strut is 6px wide
 * centered on the run.
 */
public class ContourFlyingButtressBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourFlyingButtressBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/** Inward-distance coordinate: 0 at the facing face, 16 opposite. */
	private static void span(float[] b, int n, boolean pos, float d0, float d1) {
		b[n] = pos ? 16f - d1 : d0;
		b[n + 3] = pos ? 16f - d0 : d1;
	}

	/** Rendered solid: pier + wall abutment (arc covers are collision-only). */
	public static List<float[]> solidBoxes(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(2);
		// pier: u 4..12, inward 9..13, y 0..11
		float[] pier = {0, 0, 0, 16, 16, 16};
		pier[u] = 4f;
		pier[u + 3] = 12f;
		pier[1] = 0f;
		pier[4] = 11f;
		span(pier, n, pos, 9f, 13f);
		out.add(pier);
		// wall abutment: u 5..11, inward 0..4, y 13..16
		float[] abut = {0, 0, 0, 16, 16, 16};
		abut[u] = 5f;
		abut[u + 3] = 11f;
		abut[1] = 13f;
		abut[4] = 16f;
		span(abut, n, pos, 0f, 4f);
		out.add(abut);
		return out;
	}

	/** Pier + abutment + three stepped arc covers. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(solidBoxes(state));
		// arc covers: band r5.5..8 about (n=4, y=8), swept 0..90 deg
		for (int i = 0; i < 3; i++) {
			double ta = Math.PI / 2 * i / 3,
				tb = Math.PI / 2 * (i + 1) / 3;
			float d0 = 16f, d1 = 0f, y0 = 16f, y1 = 0f;
			for (double t : new double[]{ta, tb})
				for (float r : new float[]{8f, 5.5f}) {
					float pd = 4f + (float) (r * Math.cos(t));
					float py = 8f + (float) (r * Math.sin(t));
					d0 = Math.min(d0, pd);
					d1 = Math.max(d1, pd);
					y0 = Math.min(y0, py);
					y1 = Math.max(y1, py);
				}
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = 5f;
			b[u + 3] = 11f;
			b[1] = y0;
			b[4] = y1;
			span(b, n, pos, d0, d1);
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
