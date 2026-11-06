package net.createcontour.block;

import java.util.ArrayList;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sphere octant of radius 16px hugging one cube corner (rounded corner
 * cap - the corner variant of the quarter round). CORNER + HALF name
 * the 8 positions; placement picks the clicked octant like the notch;
 * the wrench cycles all 8. Collision is a 4-step staircase that never
 * under-fills the sphere.
 */
public class ContourRoundCornerBlock extends ContourBlock {

	public static final EnumProperty<Corner> CORNER =
		ContourCornerSlopeBlock.CORNER;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	/** Square half-extent of each collision slab (sphere bound per step). */
	private static final float[] R = {15.5f, 13.9f, 12f, 10.6f};

	public ContourRoundCornerBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(CORNER, Corner.NE).setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CORNER).add(HALF));
	}

	private static int[] cornerBits(BlockState s) {
		Corner c = s.getValue(CORNER);
		return new int[]{
			c == Corner.NE || c == Corner.SE ? 1 : 0,
			s.getValue(HALF) == Half.TOP ? 1 : 0,
			c == Corner.SE || c == Corner.SW ? 1 : 0};
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState s) {
		int[] b = cornerBits(s);
		List<float[]> out = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			float[] box = new float[6];
			// slab i along the corner's y edge, inward 4px steps
			box[1] = b[1] == 1 ? 16 - (i + 1) * 4f : i * 4f;
			box[4] = box[1] + 4f;
			// x and z anchored at the corner side, half-extent R[i]
			box[0] = b[0] == 1 ? 16 - R[i] : 0;
			box[3] = box[0] + R[i];
			box[2] = b[2] == 1 ? 16 - R[i] : 0;
			box[5] = box[2] + R[i];
			out.add(box);
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
		// pick the octant containing the click, like the notch
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();
		double fx = hit.x - p.getX(), fy = hit.y - p.getY(), fz = hit.z - p.getZ();
		int ox = face.getAxis() == Direction.Axis.X
			? (face.getStepX() < 0 ? 1 : 0) : (fx > 0.5 ? 1 : 0);
		int oy = face.getAxis() == Direction.Axis.Y
			? (face.getStepY() < 0 ? 1 : 0) : (fy > 0.5 ? 1 : 0);
		int oz = face.getAxis() == Direction.Axis.Z
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
