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
 * Beam: the centered step - an 8px-deep tread bar centred in the cell
 * rather than hugging a face. AXIS selects the run direction; on X/Z
 * the bar sits in the bottom half like a step tread, on Y it stands
 * upright as a half-height post. Pure box geometry.
 */
public class ContourBeamBlock extends ContourBlock {

	public static final EnumProperty<Direction.Axis> AXIS =
		BlockStateProperties.AXIS;

	public ContourBeamBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(AXIS, Direction.Axis.Z));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS));
	}

	/** One box: run axis full length, centred 4..12 across, y 0..8. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction.Axis axis = state.getValue(AXIS);
		float[] b = {0, 0, 0, 16, 16, 16};
		if (axis == Direction.Axis.Y) {
			b[0] = 4f;
			b[3] = 12f;
			b[2] = 4f;
			b[5] = 12f;
		} else {
			int n = axis.ordinal();
			int u = n == 0 ? 2 : 0;
			b[u] = 4f;
			b[u + 3] = 12f;
			b[4] = 8f;
		}
		return List.of(b);
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
		return withWater(defaultBlockState().setValue(AXIS,
			ctx.getClickedFace().getAxis()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction.Axis a = state.getValue(AXIS);
		if (a == Direction.Axis.Y || rotation == Rotation.CLOCKWISE_180)
			return state;
		return state.setValue(AXIS,
			a == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
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
