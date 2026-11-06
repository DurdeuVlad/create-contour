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
 * Spoked wheel: a square rim at the plane boundary plus four spokes
 * and a center hub, on all three axes. Wagon wheels, valve handles,
 * mill gearing - the rim reaches the block edge so adjacent wheels
 * (or a wall face) read as one silhouette. AXIS is the wheel's axle.
 * Pure AABB - render and collision share boxesFor via ContourBoxModel.
 */
public class ContourWheelBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public ContourWheelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Nine boxes: four rim bars on the plane boundary (tire 4px wide on
	 * the axle), four spoke segments running rim-inner to hub, and the
	 * hub cube [6,10]^2 on a 6px axle. Spokes stop at the hub faces so
	 * nothing overlaps.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		int u = (run + 1) % 3, v = (run + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(9);
		float[][] rim = {
			{0, 2, 2, 14},   // u 0..2, v 2..14
			{14, 2, 16, 14}, // u 14..16, v 2..14
			{2, 0, 14, 2},   // u 2..14, v 0..2
			{2, 14, 14, 16}, // u 2..14, v 14..16
		};
		for (float[] r : rim) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = r[0];
			b[v] = r[1];
			b[u + 3] = r[2];
			b[v + 3] = r[3];
			b[run] = 6f;
			b[run + 3] = 10f;
			out.add(b);
		}
		float[][] spokes = {
			{7, 2, 9, 6}, {7, 10, 9, 14}, // u 7..9, v halves
			{2, 7, 6, 9}, {10, 7, 14, 9}, // v 7..9, u halves
		};
		for (float[] s : spokes) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[u] = s[0];
			b[v] = s[1];
			b[u + 3] = s[2];
			b[v + 3] = s[3];
			b[run] = 7f;
			b[run + 3] = 9f;
			out.add(b);
		}
		float[] hub = {0, 0, 0, 16, 16, 16};
		hub[u] = 6f;
		hub[u + 3] = 10f;
		hub[v] = 6f;
		hub[v + 3] = 10f;
		hub[run] = 5f;
		hub[run + 3] = 11f;
		out.add(hub);
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
