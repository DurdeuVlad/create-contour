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
 * Coffer: a recessed panel for ceilings, walls, and floors - a
 * 3px-deep border frame on the facing plane with the center field
 * set 1px back, the classic sunken coffer. FACING is the visible
 * face (all six directions): down for ceilings, sideways for
 * wainscot, up for floor medallions.
 */
public class ContourCofferBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;

	public ContourCofferBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/**
	 * n = facing axis (frame front on the facing boundary, 3 deep),
	 * u/v = the two in-plane axes. Four border bars plus a recessed
	 * center field.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int n = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int u = (n + 1) % 3, v = (n + 2) % 3;
		List<float[]> out = new java.util.ArrayList<>(5);
		out.add(frame(u, n, v, pos, 0f, 3f, 0f, 16f));
		out.add(frame(u, n, v, pos, 13f, 16f, 0f, 16f));
		out.add(frame(v, n, u, pos, 0f, 3f, 3f, 13f));
		out.add(frame(v, n, u, pos, 13f, 16f, 3f, 13f));
		// recessed field: u,v 3..13, one plane deeper than the frame
		float[] p = {0, 0, 0, 16, 16, 16};
		p[u] = 3f; p[u + 3] = 13f;
		p[v] = 3f; p[v + 3] = 13f;
		p[n] = pos ? 12f : 3f;
		p[n + 3] = pos ? 13f : 4f;
		out.add(p);
		return out;
	}

	/** Border bar on the n-normal frame plane (3px deep). */
	private static float[] frame(int along, int n, int other, boolean pos,
		float a0, float a1, float o0, float o1) {
		float[] b = {0, 0, 0, 16, 16, 16};
		b[along] = a0; b[along + 3] = a1;
		b[other] = o0; b[other + 3] = o1;
		b[n] = pos ? 13f : 0f;
		b[n + 3] = pos ? 16f : 3f;
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
		// Visible face = the face you clicked on.
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getClickedFace()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
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
