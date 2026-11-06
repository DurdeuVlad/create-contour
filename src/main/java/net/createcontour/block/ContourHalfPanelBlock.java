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
 * Half panel: a 2px plate covering one half of a face. FACE picks the
 * mount face (6), HALF picks which perpendicular half the plate covers
 * (4 per face). Pure box geometry.
 */
public class ContourHalfPanelBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACE =
		BlockStateProperties.FACING;
	public static final EnumProperty<Direction> HALF =
		EnumProperty.create("half", Direction.class);

	public ContourHalfPanelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACE, Direction.UP)
			.setValue(HALF, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACE, HALF));
	}

	/**
	 * One box: 2px plate on FACE, clipped to the HALF side - the half
	 * direction's axis carries 0..8 or 8..16, the third axis runs full.
	 */
	public static List<float[]> boxesFor(BlockState state) {
		Direction face = state.getValue(FACE);
		Direction half = state.getValue(HALF);
		int n = face.getAxis().ordinal();
		boolean pos = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		int h = half.getAxis().ordinal();
		boolean hpos = half.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float[] b = {0, 0, 0, 16, 16, 16};
		b[n] = pos ? 14f : 0f;
		b[n + 3] = pos ? 16f : 2f;
		b[h] = hpos ? 8f : 0f;
		b[h + 3] = hpos ? 16f : 8f;
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

	/**
	 * Placement maps the clicked face; the covered half follows the
	 * click's off-centre dominant axis. A face-parallel half falls back
	 * to the player's look direction.
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction face = ctx.getClickedFace();
		double dx = ctx.getClickLocation().x - ctx.getClickedPos().getX() - 0.5;
		double dy = ctx.getClickLocation().y - ctx.getClickedPos().getY() - 0.5;
		double dz = ctx.getClickLocation().z - ctx.getClickedPos().getZ() - 0.5;
		double u, v;
		switch (face.getAxis()) {
			case Z -> { u = dy; v = dx; }
			case X -> { u = dy; v = dz; }
			default -> { u = dx; v = dz; }
		}
		Direction half;
		if (Math.abs(u) >= Math.abs(v))
			half = u >= 0 ? perpPlus(face, 0) : perpMinus(face, 0);
		else
			half = v >= 0 ? perpPlus(face, 1) : perpMinus(face, 1);
		return withWater(defaultBlockState().setValue(FACE, face)
			.setValue(HALF, half), ctx);
	}

	private static Direction perpPlus(Direction face, int which) {
		return perp(face, which, true);
	}

	private static Direction perpMinus(Direction face, int which) {
		return perp(face, which, false);
	}

	private static Direction perp(Direction face, int which, boolean pos) {
		Direction.Axis a = switch (face.getAxis()) {
			case Z -> which == 0 ? Direction.Axis.Y : Direction.Axis.X;
			case X -> which == 0 ? Direction.Axis.Y : Direction.Axis.Z;
			default -> which == 0 ? Direction.Axis.X : Direction.Axis.Z;
		};
		return Direction.fromAxisAndDirection(a,
			pos ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACE, rotation.rotate(state.getValue(FACE)))
			.setValue(HALF, rotation.rotate(state.getValue(HALF)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACE, mirror.mirror(state.getValue(FACE)))
			.setValue(HALF, mirror.mirror(state.getValue(HALF)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(HALF);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
