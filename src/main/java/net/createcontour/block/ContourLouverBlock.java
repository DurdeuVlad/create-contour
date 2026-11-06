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
 * Louvered vent grille: a 1.5px mount plate on the clicked face plus
 * four slats that tilt down-and-out toward the FACING direction (the
 * classic rain-shedding vent profile - from outside you see the
 * slats' top surfaces). On wall facings the slats pitch in Y; on
 * floor/ceiling facings they pitch in Z so the grille still reads.
 * Collision boxes cover each slat's tilted extent, never under it.
 */
public class ContourLouverBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	/** slat pitch window: bottom edge at SLAT_LO+i*3.5, top +2.5 */
	public static final float SLAT_LO = 1.5f, SLAT_STEP = 3.5f,
		SLAT_TALL = 2.5f, SLAT_DEPTH = 3f;
	public static final int SLATS = 4;

	public ContourLouverBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/** Mount plate (opposite the facing) + four slat cover boxes. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int fi = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int p = fi == 1 ? 2 : 1; // pitch axis: Y on walls, Z on floor/ceiling
		int w = 3 - fi - p;
		List<float[]> out = new java.util.ArrayList<>(SLATS + 1);
		float[] plate = {0, 0, 0, 16, 16, 16};
		plate[fi] = pos ? 0f : 14.5f;
		plate[fi + 3] = pos ? 1.5f : 16f;
		out.add(plate);
		for (int i = 0; i < SLATS; i++) {
			float lo = SLAT_LO + i * SLAT_STEP;
			float[] b = {0, 0, 0, 16, 16, 16};
			b[w] = 0f;
			b[w + 3] = 16f;
			b[p] = lo;
			b[p + 3] = lo + SLAT_TALL;
			b[fi] = pos ? 16f - SLAT_DEPTH : 0f;
			b[fi + 3] = pos ? 16f : SLAT_DEPTH;
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
			.setValue(FACING, ctx.getClickedFace()), ctx);
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
