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
 * Radius-8 pointed cone - spires, turret roofs, icicles, spikes.
 * FACING is the apex direction; the base disk sits on the attach
 * face. Collision is four stepped slabs at the widest cross-section
 * of each band, so it never under-fills the visible cone.
 */
public class ContourConeBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;

	public ContourConeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/** Four bands along the facing axis, half-widths 8/6/4/2. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float[] w = {8f, 6f, 4f, 2f};
		List<float[]> out = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			float[] b = {0, 0, 0, 16, 16, 16};
			float lo = i * 4f, hi = lo + 4f;
			if (pos) {
				b[a] = lo;
				b[a + 3] = hi;
			} else {
				b[a] = 16 - hi;
				b[a + 3] = 16 - lo;
			}
			for (int c = 0; c < 3; c++) {
				if (c == a)
					continue;
				b[c] = 8 - w[i];
				b[c + 3] = 8 + w[i];
			}
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
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
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
