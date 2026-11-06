package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
 * Material-bearing torch: light level 14, standing (FACING=UP) or
 * wall-mounted on the named side. The flame quad is a neutral-emissive
 * part of the model (not material-tinted); the stick takes the bound
 * material. Wall geometry is an axis-aligned lean approximation.
 * Documented: no particles in v1.
 */
public class ContourTorchBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;

	public ContourTorchBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		return switch (state.getValue(FACING)) {
			case UP -> List.of(new float[]{7, 0, 7, 9, 10, 9});
			case NORTH -> List.of(new float[]{7, 3, 0, 9, 13, 5});
			case SOUTH -> List.of(new float[]{7, 3, 11, 9, 13, 16});
			case EAST -> List.of(new float[]{11, 3, 7, 16, 13, 9});
			case WEST -> List.of(new float[]{0, 3, 7, 5, 13, 9});
			default -> List.of(new float[]{7, 0, 7, 9, 10, 9});
		};
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

	private Direction mountFace(BlockState state) {
		Direction f = state.getValue(FACING);
		return f == Direction.UP ? Direction.DOWN : f;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction mount = mountFace(state);
		return level.getBlockState(pos.relative(mount))
			.isFaceSturdy(level, pos.relative(mount), mount.getOpposite());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getClickedFace();
		if (f == Direction.DOWN)
			return null; // torches cannot hang from ceilings
		BlockState state = withWater(defaultBlockState()
			.setValue(FACING, f), ctx);
		return canSurvive(state, ctx.getLevel(), ctx.getClickedPos()) ? state : null;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir,
		BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
		if (dir == mountFace(state) && !canSurvive(state, level, pos))
			return Blocks.AIR.defaultBlockState();
		return super.updateShape(state, dir, neighbor, level, pos, fromPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction f = state.getValue(FACING);
		return state.setValue(FACING,
			f.getAxis().isHorizontal() ? rotation.rotate(f) : f);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		Direction f = state.getValue(FACING);
		Direction[] order = {Direction.UP, Direction.NORTH, Direction.EAST,
			Direction.SOUTH, Direction.WEST};
		return state.setValue(FACING,
			order[(java.util.Arrays.asList(order).indexOf(f) + 1) % order.length]);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
