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
 * Portcullis: a castle drop-gate - four vertical bars with pointed
 * tips plus two horizontal straps in a 2px wall-parallel panel.
 * FACING is the panel's outward normal; the gate reads as a spiked
 * grille in a doorway or under a gatehouse arch. Pairs with the
 * crenellation and gothic arch for fortification builds.
 */
public class ContourPortcullisBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourPortcullisBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * u = the wall-run axis (X for north/south panels, Z for
	 * east/west), n = the facing normal; the panel sits centered.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(FACING).getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		List<float[]> out = new java.util.ArrayList<>(10);
		// four vertical bars
		float[][] bars = {{1, 3}, {5, 7}, {9, 11}, {13, 15}};
		for (float[] ba : bars) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = ba[0];
			b[u + 3] = ba[1];
			b[1] = 2f;
			b[4] = 16f;
			b[n] = 7f;
			b[n + 3] = 9f;
			out.add(b);
			// pointed tip
			float[] t = {0, 0, 0, 16, 16, 16};
			t[u] = ba[0] + 0.5f;
			t[u + 3] = ba[1] - 0.5f;
			t[1] = 0f;
			t[4] = 2f;
			t[n] = 7.5f;
			t[n + 3] = 8.5f;
			out.add(t);
		}
		// two horizontal straps
		for (float y = 4f; y <= 10f; y += 6f) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = 0f;
			b[u + 3] = 16f;
			b[1] = y;
			b[4] = y + 2f;
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
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
		// Panel sits in the wall plane: normal = clicked face when
		// horizontal, else the player's look direction.
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
