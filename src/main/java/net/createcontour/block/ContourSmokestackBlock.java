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
 * Smokestack: the flared locomotive funnel - skirted base, slim
 * barrel, flared crown and rim, lathed in the model. AXIS is the
 * stack's run direction. The curved counterpart of the
 * orthogonal chimney, for locomotives and boilers.
 */
public class ContourSmokestackBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.AXIS;

	public ContourSmokestackBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Four collision bands on the run axis: skirt (r5.5), barrel
	 * (r4), flare (r6.5), rim (r7).
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		List<float[]> out = new java.util.ArrayList<>(4);
		out.add(band(run, 0f, 2f, 2.5f, 13.5f));
		out.add(band(run, 2f, 9f, 4f, 12f));
		out.add(band(run, 9f, 13f, 1.5f, 14.5f));
		out.add(band(run, 13f, 15.5f, 1f, 15f));
		return out;
	}

	private static float[] band(int run, float d0, float d1,
		float w0, float w1) {
		float[] b = {w0, w0, w0, w1, w1, w1};
		b[run] = d0;
		b[run + 3] = d1;
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
		return withWater(defaultBlockState()
			.setValue(AXIS, ctx.getClickedFace().getAxis()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(AXIS, rotation.rotate(Direction
			.fromAxisAndDirection(state.getValue(AXIS),
				Direction.AxisDirection.POSITIVE)).getAxis());
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
