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
 * Lancet: a tall narrow window with a pointed head - two jambs,
 * stepped shoulders closing to an apex, and a sill bar. The
 * opening stays open for glazing or air. FACING is the panel
 * normal; pairs with the gothic arch for church builds.
 */
public class ContourLancetBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourLancetBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * u = wall-run axis, n = panel normal (4px centered). Six
	 * boxes: full-height jambs, stepped shoulder + apex fills
	 * forming the pointed head, and a sill bar. The lancet
	 * opening u 6..10 y 2..12 narrowing to u 7..9 y 12..14
	 * stays open.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(FACING).getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(6);
		out.add(bar(u, n, 4f, 6f, 0f, 16f));   // left jamb
		out.add(bar(u, n, 10f, 12f, 0f, 16f)); // right jamb
		out.add(bar(u, n, 6f, 7f, 12f, 16f));  // left shoulder
		out.add(bar(u, n, 9f, 10f, 12f, 16f)); // right shoulder
		out.add(bar(u, n, 7f, 9f, 14f, 16f));  // apex
		out.add(bar(u, n, 4f, 12f, 0f, 2f));   // sill
		return out;
	}

	private static float[] bar(int u, int n, float u0, float u1,
		float y0, float y1) {
		float[] b = {0, 0, 0, 16, 16, 16};
		b[u] = u0; b[u + 3] = u1;
		b[1] = y0; b[4] = y1;
		b[n] = 6f; b[n + 3] = 10f;
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
