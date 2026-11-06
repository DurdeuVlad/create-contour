package net.createcontour.block;

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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Inner (concave) corner wedge: the valley counterpart of the corner
 * slope. Height field is max(dist-to-far-X, dist-to-far-Z), so both
 * apex-adjacent faces are full height and the sloped surface dips to a
 * concave crease running from the low opposite corner up to the apex.
 * Render emits true triangles; collision is a documented
 * quarter-stepped approximation. The wrench cycles corner then half.
 */
public class ContourInnerSlopeBlock extends ContourBlock {

	public static final EnumProperty<ContourCornerSlopeBlock.Corner> CORNER =
		ContourCornerSlopeBlock.CORNER;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

	private static final VoxelShape[] SHAPES = new VoxelShape[8];

	static {
		for (ContourCornerSlopeBlock.Corner c : ContourCornerSlopeBlock.Corner.values())
			for (Half h : Half.values())
				SHAPES[index(c, h)] = build(c, h);
	}

	private static int index(ContourCornerSlopeBlock.Corner c, Half h) {
		return c.ordinal() * 2 + (h == Half.TOP ? 1 : 0);
	}

	/** Height field at (x,z): full at the apex faces, dips to the far corner. */
	private static float heightAt(ContourCornerSlopeBlock.Corner c, float x, float z) {
		return Math.max(c.ax == 16 ? x : 16 - x, c.az == 16 ? z : 16 - z);
	}

	/**
	 * Stepped approximation: 4 quadrants, each sampled at its apex-
	 * nearest corner (quarter-max, so collision never under-fills the
	 * visible wedge).
	 */
	private static VoxelShape build(ContourCornerSlopeBlock.Corner c, Half h) {
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

	public ContourInnerSlopeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(CORNER, ContourCornerSlopeBlock.Corner.NE).setValue(HALF, Half.BOTTOM));
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
		double fx = ctx.getClickLocation().x - ctx.getClickedPos().getX();
		double fz = ctx.getClickLocation().z - ctx.getClickedPos().getZ();
		ContourCornerSlopeBlock.Corner c = fx > 0.5
			? (fz > 0.5 ? ContourCornerSlopeBlock.Corner.SE : ContourCornerSlopeBlock.Corner.NE)
			: (fz > 0.5 ? ContourCornerSlopeBlock.Corner.SW : ContourCornerSlopeBlock.Corner.NW);
		Half half = ctx.getClickedFace() == Direction.DOWN
			|| (ctx.getClickedFace().getAxis().isHorizontal()
				&& ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5)
			? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState().setValue(CORNER, c)
			.setValue(HALF, half), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		ContourCornerSlopeBlock.Corner c = state.getValue(CORNER);
		ContourCornerSlopeBlock.Corner r = switch (rotation) {
			case CLOCKWISE_90 -> c.clockwise();
			case CLOCKWISE_180 -> c.clockwise().clockwise();
			case COUNTERCLOCKWISE_90 -> c.counterClockwise();
			default -> c;
		};
		return state.setValue(CORNER, r);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		ContourCornerSlopeBlock.Corner c = state.getValue(CORNER);
		ContourCornerSlopeBlock.Corner r = switch (mirror) {
			case LEFT_RIGHT -> switch (c) {
				case NE -> ContourCornerSlopeBlock.Corner.NW;
				case NW -> ContourCornerSlopeBlock.Corner.NE;
				case SE -> ContourCornerSlopeBlock.Corner.SW;
				default -> ContourCornerSlopeBlock.Corner.SE;
			};
			case FRONT_BACK -> switch (c) {
				case NE -> ContourCornerSlopeBlock.Corner.SE;
				case SE -> ContourCornerSlopeBlock.Corner.NE;
				case NW -> ContourCornerSlopeBlock.Corner.SW;
				default -> ContourCornerSlopeBlock.Corner.NW;
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
