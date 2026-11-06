package net.createcontour.block;

import java.util.ArrayList;
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
 * Face-mounted grate (trellis/lattice): a 2px plate of 1px bars with
 * 4px openings, hugging any of the six faces. FACING points into the
 * supporting face, like slice/panel; the wrench cycles faces.
 */
public class ContourLatticeBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public ContourLatticeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		// The plate is 2px thick on the mounted face; 1px bars run in the
		// two in-plane axes at offsets 0, 5, 10, 15 leaving 4px openings.
		Direction f = state.getValue(FACING);
		int thick = f.getAxis().ordinal();
		float tLo = f.getAxisDirection() == Direction.AxisDirection.POSITIVE
			? 14 : 0;
		List<float[]> out = new ArrayList<>();
		for (float p : new float[]{0, 5, 10, 15})
			for (int barAxis = 0; barAxis < 3; barAxis++) {
				if (barAxis == thick)
					continue;
				float[] b = new float[6];
				b[thick] = tLo;
				b[thick + 3] = tLo + 2;
				b[barAxis] = p;
				b[barAxis + 3] = p + 1;
				for (int a = 0; a < 3; a++)
					if (a != thick && a != barAxis) {
						b[a] = 0;
						b[a + 3] = 16;
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
			.setValue(FACING, ctx.getClickedFace().getOpposite()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
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
