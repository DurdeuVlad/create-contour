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
 * Pipe flange: an r7 ring plate (r3 bore matching contour_tube)
 * 2px thick against a face, with four bolt studs. FACING points at
 * the supporting face, like slice/lattice. Completes the pipe-run
 * family: tube, elbow, flange.
 */
public class ContourFlangeBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;

	public ContourFlangeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * Four strips covering the ring square [1,15]^2 minus the 6px
	 * bore, plus the four bolt studs that poke proud of the plate
	 * face. Plate depth is 2px against the facing face.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float lo = pos ? 14f : 0f, hi = pos ? 16f : 2f;
		int au = (n + 1) % 3, av = (n + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(8);
		float[][] strips = {{1, 1, 15, 5}, {1, 11, 15, 15},
			{1, 5, 5, 11}, {11, 5, 15, 11}};
		for (float[] st : strips) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = lo;
			b[n + 3] = hi;
			b[au] = st[0];
			b[au + 3] = st[2];
			b[av] = st[1];
			b[av + 3] = st[3];
			out.add(b);
		}
		out.addAll(studBoxes(state));
		return out;
	}

	/** Bolt studs at 45/135/225/315 degrees, radius 5, 1.5px cubes. */
	public static List<float[]> studBoxes(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int au = (n + 1) % 3, av = (n + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			double a = Math.PI / 4 + Math.PI / 2 * i;
			double pu = 8 + 5 * Math.cos(a), pv = 8 + 5 * Math.sin(a);
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = pos ? 12.5f : 2f;
			b[n + 3] = pos ? 14f : 3.5f;
			b[au] = (float) pu - 0.75f;
			b[au + 3] = (float) pu + 0.75f;
			b[av] = (float) pv - 0.75f;
			b[av + 3] = (float) pv + 0.75f;
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
