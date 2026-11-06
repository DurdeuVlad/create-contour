package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Outer corner wedge (hip-roof piece): the apex rises to the named
 * CORNER while the two far edges sit at zero height. Height field is
 * min(dist-to-far-X-edge, dist-to-far-Z-edge) so the ridge runs the
 * diagonal from the low opposite corner to the apex. Render emits true
 * triangles; collision is a documented quarter-stepped approximation.
 * The wrench cycles corner then half, like the slope.
 */
public class ContourCornerSlopeBlock extends ContourBlock {

	public enum Corner implements StringRepresentable {
		NE("ne", 16, 0), SE("se", 16, 16), SW("sw", 0, 16), NW("nw", 0, 0);
		private final String name;
		public final float ax, az;
		Corner(String name, float ax, float az) {
			this.name = name;
			this.ax = ax;
			this.az = az;
		}
		@Override
		public String getSerializedName() {
			return name;
		}
		public Corner clockwise() {
			return switch (this) {
				case NE -> SE; case SE -> SW; case SW -> NW; default -> NE;
			};
		}
		public Corner counterClockwise() {
			return switch (this) {
				case NE -> NW; case NW -> SW; case SW -> SE; default -> NE;
			};
		}
	}

	public static final EnumProperty<Corner> CORNER =
		EnumProperty.create("corner", Corner.class);
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	private static final VoxelShape[] SHAPES = new VoxelShape[8];

	static {
		for (Corner c : Corner.values())
			for (Half h : Half.values())
				SHAPES[index(c, h)] = build(c, h);
	}

	private static int index(Corner c, Half h) {
		return c.ordinal() * 2 + (h == Half.TOP ? 1 : 0);
	}

	/** Height field at (x,z): rises toward the apex corner. */
	private static float heightAt(Corner c, float x, float z) {
		return Math.min(c.ax == 16 ? x : 16 - x, c.az == 16 ? z : 16 - z);
	}

	/**
	 * Stepped approximation: 4 quadrants, each sampled at its apex-
	 * nearest corner (quarter-max, so collision never under-fills the
	 * visible wedge).
	 */
	private static VoxelShape build(Corner c, Half h) {
		VoxelShape s = Shapes.empty();
		for (int qx = 0; qx < 2; qx++)
			for (int qz = 0; qz < 2; qz++) {
				float x0 = qx * 8f, z0 = qz * 8f;
				float sx = c.ax == 16 ? x0 + 8f : x0;
				float sz = c.az == 16 ? z0 + 8f : z0;
				float y = heightAt(c, sx, sz);
				if (y <= 0)
					continue;
				s = Shapes.or(s, h == Half.BOTTOM
					? Block.box(x0, 0, z0, x0 + 8, y, z0 + 8)
					: Block.box(x0, 16 - y, z0, x0 + 8, 16, z0 + 8));
			}
		return s.optimize();
	}

	public ContourCornerSlopeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(CORNER, Corner.NE).setValue(HALF, Half.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(CORNER).add(HALF));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return SHAPES[index(state.getValue(CORNER), state.getValue(HALF))];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		// Apex = the horizontal quadrant clicked; half from face/height.
		double fx = ctx.getClickLocation().x - ctx.getClickedPos().getX();
		double fz = ctx.getClickLocation().z - ctx.getClickedPos().getZ();
		Corner c = fx > 0.5 ? (fz > 0.5 ? Corner.SE : Corner.NE)
			: (fz > 0.5 ? Corner.SW : Corner.NW);
		Half half = ctx.getClickedFace() == Direction.DOWN
			|| (ctx.getClickedFace().getAxis().isHorizontal()
				&& ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5)
			? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState().setValue(CORNER, c)
			.setValue(HALF, half), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Corner c = state.getValue(CORNER);
		Corner r = switch (rotation) {
			case CLOCKWISE_90 -> c.clockwise();
			case CLOCKWISE_180 -> c.clockwise().clockwise();
			case COUNTERCLOCKWISE_90 -> c.counterClockwise();
			default -> c;
		};
		return state.setValue(CORNER, r);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		// Mirror on Z keeps north/south, flips east/west; on X vice versa.
		Corner c = state.getValue(CORNER);
		Corner r = switch (mirror) {
			case LEFT_RIGHT -> switch (c) { // x mirror
				case NE -> Corner.NW; case NW -> Corner.NE;
				case SE -> Corner.SW; default -> Corner.SE;
			};
			case FRONT_BACK -> switch (c) { // z mirror
				case NE -> Corner.SE; case SE -> Corner.NE;
				case NW -> Corner.SW; default -> Corner.NW;
			};
			default -> c;
		};
		return state.setValue(CORNER, r);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		if (state.getValue(HALF) == Half.BOTTOM)
			return state.setValue(HALF, Half.TOP);
		return state.setValue(CORNER, state.getValue(CORNER).clockwise())
			.setValue(HALF, Half.BOTTOM);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
