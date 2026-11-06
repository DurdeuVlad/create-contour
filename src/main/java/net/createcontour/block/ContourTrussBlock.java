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
 * Roof truss: an open triangular frame in a wall-parallel 2px
 * panel - bottom chord, king post, two web posts, and two sloped
 * top chords meeting at the apex. FACING is the panel normal.
 * Span roofs, brace gables, and dress industrial sheds.
 */
public class ContourTrussBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourTrussBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Chord slope: sheared 2px prism rising 10px over a half-span,
	 * bottom edge y = 3 + 1.25*u (left half), mirrored right.
	 */
	public static float chordLo(float u) {
		return 3f + 1.25f * Math.min(u, 16f - u);
	}

	/**
	 * u = wall-run axis, n = facing normal; panel sits at n 7..9.
	 * Sloped chords are covered by two AABB steps each so collision
	 * never under-fills the sheared render geometry.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(FACING).getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(8);
		float[] b = {0, 0, 0, 16, 16, 16};
		// bottom chord
		b[1] = 1f; b[4] = 3f; b[n] = 7f; b[n + 3] = 9f;
		out.add(b);
		// king post
		b = box(u, n, 7f, 9f, 3f, 13f);
		out.add(b);
		// web posts
		out.add(box(u, n, 3.5f, 5.5f, 3f, 9f));
		out.add(box(u, n, 10.5f, 12.5f, 3f, 9f));
		// sloped chord steps: u 0..4 y 3..10, u 4..8 y 8..15 + mirror
		out.add(box(u, n, 0f, 4f, 3f, 10f));
		out.add(box(u, n, 4f, 8f, 8f, 15f));
		out.add(box(u, n, 8f, 12f, 8f, 15f));
		out.add(box(u, n, 12f, 16f, 3f, 10f));
		return out;
	}

	private static float[] box(int u, int n, float u0, float u1,
		float y0, float y1) {
		float[] b = {0, 0, 0, 16, 16, 16};
		b[u] = u0; b[u + 3] = u1;
		b[1] = y0; b[4] = y1;
		b[n] = 7f; b[n + 3] = 9f;
		return b;
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
