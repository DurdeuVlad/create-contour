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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Half timber: the Tudor framing panel - a 2px perimeter frame in
 * a centered wall panel with one diagonal brace running
 * corner-to-corner inside it. FACING is the wall normal; DIAG
 * flips the brace between rising (low-left to high-right) and
 * falling. Render is a custom model (sheared brace); collision is
 * the frame plus three stepped cover boxes for the brace.
 */
public class ContourHalfTimberBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty DIAG =
		BooleanProperty.create("diag_rising");

	public ContourHalfTimberBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH)
			.setValue(DIAG, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(DIAG));
	}

	/** Perimeter frame only (rails + stiles) - the render boxes. */
	public static List<float[]> frameBoxes(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(4);
		// top and bottom rails full width
		for (int i = 0; i < 2; i++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
			b[1] = i == 0 ? 0f : 14f;
			b[1 + 3] = i == 0 ? 2f : 16f;
			out.add(b);
		}
		// stiles between the rails only (no coplanar overlap)
		for (int i = 0; i < 2; i++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
			b[1] = 2f;
			b[1 + 3] = 14f;
			b[u] = i == 0 ? 0f : 14f;
			b[u + 3] = i == 0 ? 2f : 16f;
			out.add(b);
		}
		return out;
	}

	/** Frame rails/stiles plus stepped boxes covering the brace. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		boolean rise = state.getValue(DIAG);
		List<float[]> out = new java.util.ArrayList<>(frameBoxes(state));
		// 8px-tall band tracking the diagonal rise
		for (int i = 0; i < 3; i++) {
			float uA = 2f + i * 4f, uB = uA + 4f;
			float yA = rise ? i * 4f : 8f - i * 4f;
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
			b[u] = uA;
			b[u + 3] = uB;
			b[1] = yA;
			b[1 + 3] = yA + 8f;
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
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
			.setValue(DIAG, !state.getValue(DIAG));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		BlockState s = state.cycle(FACING);
		return s.getValue(FACING) == Direction.NORTH
			? s.cycle(DIAG) : s;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
