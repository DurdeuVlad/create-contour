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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Quarter-turn stair: a half slab plus octants in the raised half.
 * OUTER keeps only the named corner octant (the vanilla outer
 * corner); INNER keeps the other three (the L of the vanilla inner
 * corner). CORNER names the unique octant in both cases, matching
 * the notch's convention. Render and collision share the box list.
 */
public class ContourStairCornerBlock extends ContourBlock {

	public static final EnumProperty<Corner> CORNER =
		ContourCornerSlopeBlock.CORNER;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final BooleanProperty INNER = BooleanProperty.create("inner");

	public ContourStairCornerBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(CORNER, Corner.NE).setValue(HALF, Half.BOTTOM)
			.setValue(INNER, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CORNER).add(HALF).add(INNER));
	}

	/** Octant bit for the named corner, matching notch's x|y<<1|z<<2 order. */
	private static int cornerBit(BlockState s) {
		Corner c = s.getValue(CORNER);
		int x = c == Corner.NE || c == Corner.SE ? 1 : 0;
		// raised octants live in the half opposite the slab
		int y = s.getValue(HALF) == Half.BOTTOM ? 1 : 0;
		int z = c == Corner.SE || c == Corner.SW ? 1 : 0;
		return x | (y << 1) | (z << 2);
	}

	private static float[] oct(int i) {
		float x0 = (i & 1) * 8f, y0 = ((i >> 1) & 1) * 8f,
			z0 = ((i >> 2) & 1) * 8f;
		return new float[]{x0, y0, z0, x0 + 8, y0 + 8, z0 + 8};
	}

	public static List<float[]> boxesFor(BlockState s) {
		int named = cornerBit(s);
		boolean bottom = s.getValue(HALF) == Half.BOTTOM;
		List<float[]> out = new java.util.ArrayList<>(4);
		out.add(bottom ? new float[]{0, 0, 0, 16, 8, 16}
			: new float[]{0, 8, 0, 16, 16, 16});
		int yBit = bottom ? 2 : 0; // y-bit of the raised half's octants
		for (int x = 0; x < 2; x++)
			for (int z = 0; z < 8; z += 4) {
				int i = x | yBit | z;
				if (s.getValue(INNER) ? i != named : i == named)
					out.add(oct(i));
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
		// name the octant containing the click, like the byte's pick
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();
		double fx = hit.x - p.getX(), fy = hit.y - p.getY(),
			fz = hit.z - p.getZ();
		int ox = face.getAxis() == Direction.Axis.X
			? (face.getStepX() < 0 ? 1 : 0) : (fx > 0.5 ? 1 : 0);
		int oy = face.getAxis() == Direction.Axis.Y
			? (face.getStepY() < 0 ? 1 : 0) : (fy > 0.5 ? 1 : 0);
		int oz = face.getAxis() == Direction.Axis.Z
			? (face.getStepZ() < 0 ? 1 : 0) : (fz > 0.5 ? 1 : 0);
		Corner c = ox == 1
			? (oz == 1 ? Corner.SE : Corner.NE)
			: (oz == 1 ? Corner.SW : Corner.NW);
		return withWater(defaultBlockState().setValue(CORNER, c)
			.setValue(HALF, oy == 1 ? Half.TOP : Half.BOTTOM)
			.setValue(INNER, ctx.isSecondaryUseActive()), ctx);
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
		// all four corners on the bottom half, then the top half,
		// then flip inner/outer - 8 clicks per inner toggle
		Corner c = state.getValue(CORNER);
		if (c != Corner.NW)
			return state.setValue(CORNER, c.clockwise());
		if (state.getValue(HALF) == Half.BOTTOM)
			return state.setValue(CORNER, Corner.NE).setValue(HALF, Half.TOP);
		return state.setValue(CORNER, Corner.NE).setValue(HALF, Half.BOTTOM)
			.setValue(INNER, !state.getValue(INNER));
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
