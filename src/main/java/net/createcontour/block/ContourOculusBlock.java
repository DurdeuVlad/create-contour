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
 * Oculus: the flat ring window frame - an annulus (inner r4,
 * outer r8) standing in a plane, 3px thick, for round wall
 * openings and rose windows. AXIS is the ring normal, so the
 * three states stand it on each coordinate plane. The bore
 * stays open; collision is four stepped covers around the ring
 * that never reach inside r4.
 */
public class ContourOculusBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.AXIS;

	public ContourOculusBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/**
	 * Sixteen 22.5-degree covers of the annulus band. Monotonic
	 * extremes at segment endpoints give an exact ring cover; the
	 * bore stays open except a slight over-fill at the inner
	 * corners (min covered radius ~3.7 vs the r4 visual bore),
	 * which is invisible at this scale.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		int n = state.getValue(AXIS).ordinal();
		int au = (n + 1) % 3, av = (n + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(16);
		for (int q = 0; q < 16; q++) {
			double ta = Math.PI / 8 * q, tb = ta + Math.PI / 8;
			float u0 = 16f, u1 = 0f, v0 = 16f, v1 = 0f;
			for (double t : new double[]{ta, tb})
				for (float r : new float[]{8f, 4f}) {
					float pu = 8f + (float) (r * Math.cos(t));
					float pv = 8f + (float) (r * Math.sin(t));
					u0 = Math.min(u0, pu);
					u1 = Math.max(u1, pu);
					v0 = Math.min(v0, pv);
					v1 = Math.max(v1, pv);
				}
			float[] b = {8f, 8f, 8f, 8f, 8f, 8f};
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
			b[au] = u0;
			b[au + 3] = u1;
			b[av] = v0;
			b[av + 3] = v1;
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
			.setValue(AXIS, ctx.getNearestLookingDirection().getAxis()), ctx);
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
