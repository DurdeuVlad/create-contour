package net.createcontour.block;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Cube minus one 8px octant: recessed corner joinery, sculpted edges.
 * CORNER picks the horizontal corner, HALF the vertical one; together
 * they name the removed octant. Wrench cycles all 8 cut positions.
 * Render and collision share the same 7-octant box list.
 */
public class ContourNotchBlock extends ContourBlock {

	public static final EnumProperty<Corner> CORNER =
		ContourCornerSlopeBlock.CORNER;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	public ContourNotchBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(CORNER, Corner.NE).setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CORNER).add(HALF));
	}

	/** Octant bit for the removed corner, matching boxesForMask order. */
	private static int cornerBit(BlockState s) {
		Corner c = s.getValue(CORNER);
		int x = c == Corner.NE || c == Corner.SE ? 1 : 0;
		int y = s.getValue(HALF) == Half.TOP ? 1 : 0;
		int z = c == Corner.SE || c == Corner.SW ? 1 : 0;
		return x | (y << 1) | (z << 2);
	}

	public static List<float[]> boxesFor(BlockState s) {
		int cut = cornerBit(s);
		List<float[]> out = new java.util.ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (i == cut)
				continue;
			float x0 = (i & 1) * 8f, y0 = ((i >> 1) & 1) * 8f,
				z0 = ((i >> 2) & 1) * 8f;
			out.add(new float[]{x0, y0, z0, x0 + 8, y0 + 8, z0 + 8});
		}
		return out;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s.optimize();
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		// cut the octant containing the click, like the byte's pick
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();
		double fx = hit.x - p.getX(), fy = hit.y - p.getY(), fz = hit.z - p.getZ();
		int ox = face.getAxis() == net.minecraft.core.Direction.Axis.X
			? (face.getStepX() < 0 ? 1 : 0) : (fx > 0.5 ? 1 : 0);
		int oy = face.getAxis() == net.minecraft.core.Direction.Axis.Y
			? (face.getStepY() < 0 ? 1 : 0) : (fy > 0.5 ? 1 : 0);
		int oz = face.getAxis() == net.minecraft.core.Direction.Axis.Z
			? (face.getStepZ() < 0 ? 1 : 0) : (fz > 0.5 ? 1 : 0);
		Corner c = ox == 1
			? (oz == 1 ? Corner.SE : Corner.NE)
			: (oz == 1 ? Corner.SW : Corner.NW);
		Half h = oy == 1 ? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState().setValue(CORNER, c).setValue(HALF, h),
			ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Corner c = state.getValue(CORNER);
		int steps = switch (rotation) {
			case CLOCKWISE_90 -> 1;
			case CLOCKWISE_180 -> 2;
			case COUNTERCLOCKWISE_90 -> 3;
			default -> 0;
		};
		for (int i = 0; i < steps; i++)
			c = c.clockwise();
		return state.setValue(CORNER, c);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Corner c = state.getValue(CORNER);
		Corner m = switch (mirror) {
			case FRONT_BACK -> switch (c) {
				case NE -> Corner.SE;
				case SE -> Corner.NE;
				case SW -> Corner.NW;
				default -> Corner.SW;
			};
			case LEFT_RIGHT -> switch (c) {
				case NE -> Corner.NW;
				case NW -> Corner.NE;
				case SE -> Corner.SW;
				default -> Corner.SE;
			};
			default -> c;
		};
		return state.setValue(CORNER, m);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// cycle bottom ring then top ring: ne->se->sw->nw, flip half
		Corner c = state.getValue(CORNER);
		if (c != Corner.NW)
			return state.setValue(CORNER, c.clockwise());
		return state.setValue(CORNER, Corner.NE).setValue(HALF,
			state.getValue(HALF) == Half.BOTTOM ? Half.TOP : Half.BOTTOM);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
