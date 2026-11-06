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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Spandrel: the right-triangle panel filling the corner between a
 * stair or arch and its rectangular frame - the asymmetric
 * counterpart of the pediment. A centered 3px panel in a stepped
 * right triangle: four rows shrinking toward the low corner.
 * FACING is the wall normal; HIGH_LEFT picks which in-plane
 * corner the tall side leans on.
 */
public class ContourSpandrelBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty HIGH_LEFT =
		BooleanProperty.create("high_left");

	public ContourSpandrelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH)
			.setValue(HIGH_LEFT, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(HIGH_LEFT));
	}

	/** Four stepped rows; tall side sits on the high corner. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		int u = n == 0 ? 2 : 0;
		boolean left = state.getValue(HIGH_LEFT);
		List<float[]> out = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			b[n] = 6.5f;
			b[n + 3] = 9.5f;
			b[1] = i * 4f;
			b[1 + 3] = i * 4f + 4f;
			// rows shrink 4px per step from the low corner
			b[u] = left ? 0f : i * 4f;
			b[u + 3] = left ? 16f - i * 4f : 16f;
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
		// High corner = the side of the clicked face the player hit.
		Direction f = ctx.getHorizontalDirection();
		Direction.Axis u = f.getAxis() == Direction.Axis.X
			? Direction.Axis.Z : Direction.Axis.X;
		double hit = u == Direction.Axis.X
			? ctx.getClickLocation().x - ctx.getClickedPos().getX()
			: ctx.getClickLocation().z - ctx.getClickedPos().getZ();
		boolean positiveSide = hit > 0.5;
		// u+ is the viewer's left for SOUTH and WEST facings only
		boolean uPosLeft = f == Direction.SOUTH || f == Direction.WEST;
		return withWater(defaultBlockState()
			.setValue(FACING, f)
			.setValue(HIGH_LEFT, positiveSide == uPosLeft), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
			.setValue(HIGH_LEFT, !state.getValue(HIGH_LEFT));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// 8-state cycle: facings, then same facings mirrored
		BlockState s = state.cycle(FACING);
		return s.getValue(FACING) == Direction.NORTH
			? s.cycle(HIGH_LEFT) : s;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
