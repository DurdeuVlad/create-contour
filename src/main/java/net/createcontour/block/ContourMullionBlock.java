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
 * Mullion: a casement window grille - 2px perimeter frame plus a
 * center cross of 1px bars in a centered wall-parallel panel,
 * leaving four 6px glazing panes. FACING is the panel normal.
 * Pairs with the sill for the complete window kit; the open panes
 * take the material's texture (glass, leaves, or solid infill).
 */
public class ContourMullionBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourMullionBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * u = wall-run axis, n = facing normal; panel sits at n 7..9.
	 * Seven boxes: four frame bars + horizontal cross + the
	 * vertical cross split into two segments so no boxes overlap
	 * (coplanar faces would z-fight).
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(FACING).getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(7);
		// perimeter frame: u edges then y edges
		out.add(bar(u, n, 0f, 2f, 0f, 16f));
		out.add(bar(u, n, 14f, 16f, 0f, 16f));
		out.add(bar(u, n, 2f, 14f, 0f, 2f));
		out.add(bar(u, n, 2f, 14f, 14f, 16f));
		// center cross: full horizontal + split vertical
		out.add(bar(u, n, 2f, 14f, 7.5f, 8.5f));
		out.add(bar(u, n, 7.5f, 8.5f, 2f, 7.5f));
		out.add(bar(u, n, 7.5f, 8.5f, 8.5f, 14f));
		return out;
	}

	private static float[] bar(int u, int n, float u0, float u1,
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
