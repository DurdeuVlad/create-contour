package net.createcontour.block;

import java.util.List;
import net.createcontour.block.ContourCornerSlopeBlock.Corner;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Arc wall: the curved corner segment of a round tower or bay -
 * a quarter-annulus band (inner r12, outer r16) swept 90 degrees
 * about the block corner, full height. Both radial end faces sit
 * on the block faces so straight walls join flush. CORNER picks
 * which of the four bottom corners is the arc center.
 */
public class ContourArcWallBlock extends ContourBlock {

	public static final EnumProperty<Corner> CORNER =
		ContourCornerSlopeBlock.CORNER;

	public ContourArcWallBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(CORNER, Corner.NE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CORNER));
	}

	/** Inward unit directions from the corner anchor on x and z. */
	public static float[] dirs(Corner c) {
		return new float[]{c.ax == 16f ? -1f : 1f, c.az == 16f ? -1f : 1f};
	}

	/** Four stepped boxes covering the annular quadrant. */
	public static List<float[]> boxesFor(BlockState state) {
		Corner c = state.getValue(CORNER);
		float[] d = dirs(c);
		List<float[]> out = new java.util.ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			double ta = Math.PI / 2 * i / 4,
				tb = Math.PI / 2 * (i + 1) / 4;
			float x0 = 16f, x1 = 0f, z0 = 16f, z1 = 0f;
			for (double t : new double[]{ta, tb})
				for (float r : new float[]{16f, 12f}) {
					float px = c.ax + (float) (r * Math.cos(t)) * d[0];
					float pz = c.az + (float) (r * Math.sin(t)) * d[1];
					x0 = Math.min(x0, px);
					x1 = Math.max(x1, px);
					z0 = Math.min(z0, pz);
					z1 = Math.max(z1, pz);
				}
			out.add(new float[]{x0, 0f, z0, x1, 16f, z1});
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
		// Corner = the quadrant of the face the player hit
		double dx = ctx.getClickLocation().x - ctx.getClickedPos().getX();
		double dz = ctx.getClickLocation().z - ctx.getClickedPos().getZ();
		Corner c = dx > 0.5
			? (dz > 0.5 ? Corner.SE : Corner.NE)
			: (dz > 0.5 ? Corner.SW : Corner.NW);
		return withWater(defaultBlockState().setValue(CORNER, c), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Corner c = state.getValue(CORNER);
		return state.setValue(CORNER, switch (rotation) {
			case CLOCKWISE_90 -> c.clockwise();
			case CLOCKWISE_180 -> c.clockwise().clockwise();
			case COUNTERCLOCKWISE_90 -> c.counterClockwise();
			default -> c;
		});
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE)
			return state;
		Corner c = state.getValue(CORNER);
		return state.setValue(CORNER, switch (c) {
			case NE -> mirror == Mirror.LEFT_RIGHT ? Corner.NW : Corner.SE;
			case SE -> mirror == Mirror.LEFT_RIGHT ? Corner.SW : Corner.NE;
			case SW -> mirror == Mirror.LEFT_RIGHT ? Corner.SE : Corner.NW;
			default -> mirror == Mirror.LEFT_RIGHT ? Corner.NE : Corner.SW;
		});
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.setValue(CORNER, state.getValue(CORNER).clockwise());
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
