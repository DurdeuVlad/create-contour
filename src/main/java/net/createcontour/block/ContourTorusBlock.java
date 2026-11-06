package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ring / donut: major radius 5, minor radius 3, revolved around the
 * run axis (horizontal donut on Y, vertical rings on X/Z). Pipe
 * flanges, wheel rims, decorative circles. Collision is four bars
 * covering the r2..r8 annulus; they reach far enough into the
 * diagonal quadrants that no annulus point is left uncovered while
 * the center hole stays open. Placement axis = clicked face; the
 * wrench cycles axes.
 */
public class ContourTorusBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public ContourTorusBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Four bars covering the annulus (center 8,8, radii 2..8) in the
	 * ring plane. Each bar spans its edge band (0..7 or 9..16) and
	 * runs u 2..14 so diagonal annulus corners are inside one of the
	 * two adjacent bars. The (7,9)x(7,9) center square stays open -
	 * it lies wholly inside the r<=2 hole.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		int u = (run + 1) % 3, v = (run + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(4);
		// {band axis lo/hi on band edge, span axis lo/hi}
		float[][] bars = {
			{v, 0f, 7f, u, 2f, 14f},
			{v, 9f, 16f, u, 2f, 14f},
			{u, 0f, 7f, v, 2f, 14f},
			{u, 9f, 16f, v, 2f, 14f}};
		for (float[] bar : bars) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[(int) bar[0]] = bar[1];
			b[(int) bar[0] + 3] = bar[2];
			b[(int) bar[3]] = bar[4];
			b[(int) bar[3] + 3] = bar[5];
			b[run] = 5f;
			b[run + 3] = 11f;
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
			.setValue(AXIS, ctx.getClickedFace().getAxis()), ctx);
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
