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
 * Create-flavored cog wheel: a flat hub plate plus 8 teeth (4 edge
 * stubs reaching the block boundary so adjacent cogs mesh visually,
 * 4 diagonal corner stubs). AXIS is the gear's rotation axis. Pure
 * AABB - render and collision share boxesFor via ContourBoxModel.
 */
public class ContourCogBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public ContourCogBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Hub plate [3,13]^2 at run 7..9, four edge teeth reaching the
	 * boundary, four 2px diagonal stubs at the in-plane corners.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int run = state.getValue(AXIS).ordinal();
		int u = (run + 1) % 3, v = (run + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(9);
		float[] plate = {0, 0, 0, 16, 16, 16};
		plate[u] = 3f;
		plate[u + 3] = 13f;
		plate[v] = 3f;
		plate[v + 3] = 13f;
		plate[run] = 7f;
		plate[run + 3] = 9f;
		out.add(plate);
		// edge teeth: reach the face so cog runs mesh
		for (int t = 0; t < 4; t++) {
			int ax = t < 2 ? v : u;
			float[] b = {0, 0, 0, 16, 16, 16};
			b[t < 2 ? u : v] = 6f;
			b[(t < 2 ? u : v) + 3] = 10f;
			b[ax] = (t & 1) == 0 ? 0f : 13f;
			b[ax + 3] = (t & 1) == 0 ? 3f : 16f;
			b[run] = 7f;
			b[run + 3] = 9f;
			out.add(b);
		}
		// diagonal teeth at the four in-plane corners; they overlap the
		// plate rim by 1px so the silhouette reads connected
		for (int du = 0; du < 2; du++)
			for (int dv = 0; dv < 2; dv++) {
				float[] b = {0, 0, 0, 16, 16, 16};
				b[u] = du == 0 ? 1f : 12f;
				b[u + 3] = du == 0 ? 4f : 15f;
				b[v] = dv == 0 ? 1f : 12f;
				b[v + 3] = dv == 0 ? 4f : 15f;
				b[run] = 7f;
				b[run + 3] = 9f;
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
