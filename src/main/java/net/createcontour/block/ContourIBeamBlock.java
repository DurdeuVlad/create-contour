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
 * Rolled I-beam: a central web plate with flanges on both sides,
 * running along AXIS. The girder primitive for steel frames,
 * crane rails, and industrial supports. The web is 2px thin but
 * the flanges give the section a full 16px depth, so beams read
 * correctly from every angle and stack into columns or trusses.
 */
public class ContourIBeamBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.AXIS;

	public ContourIBeamBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Three plates: two flanges (full section depth, 2px thick) and
	 * the web between them. u = run axis, v = flange normal (Y when
	 * the run is horizontal, Z when the beam is vertical).
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int u = state.getValue(AXIS).ordinal();
		int v = u == 1 ? 2 : 1; // flange normal: Y for X/Z runs, Z for Y runs
		int w = 3 - u - v;
		List<float[]> out = new java.util.ArrayList<>(3);
		for (int f = 0; f < 2; f++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[v] = f == 0 ? 0f : 14f;
			b[v + 3] = f == 0 ? 2f : 16f;
			b[w] = 3f;
			b[w + 3] = 13f;
			out.add(b);
		}
		float[] web = {7f, 7f, 7f, 9f, 9f, 9f};
		web[u] = 0f;
		web[u + 3] = 16f;
		web[v] = 2f;
		web[v + 3] = 14f;
		out.add(web);
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
			.setValue(AXIS, ctx.getClickedFace().getAxis()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state;
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(AXIS);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
