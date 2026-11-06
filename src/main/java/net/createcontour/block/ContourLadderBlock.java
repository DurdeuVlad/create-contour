package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 2px climbable plate attached to a wall face. FACING names the wall
 * side the ladder is mounted on; it can only survive with a sturdy
 * block behind that face (vanilla ladder rule). NeoForge isLadder makes
 * it climbable for entities standing inside the cell.
 */
public class ContourLadderBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;

	public ContourLadderBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	public static List<float[]> boxesFor(BlockState state) {
		return List.of(switch (state.getValue(FACING)) {
			case NORTH -> new float[]{0, 0, 0, 16, 16, 2};
			case SOUTH -> new float[]{0, 0, 14, 16, 16, 16};
			case EAST -> new float[]{14, 0, 0, 16, 16, 16};
			default -> new float[]{0, 0, 0, 2, 16, 16};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		float[] b = boxesFor(state).get(0);
		return Block.box(b[0], b[1], b[2], b[3], b[4], b[5]);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction f = state.getValue(FACING);
		return level.getBlockState(pos.relative(f))
			.isFaceSturdy(level, pos.relative(f), f.getOpposite());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getClickedFace();
		if (f.getAxis().isVertical())
			f = ctx.getHorizontalDirection().getOpposite();
		BlockState state = withWater(defaultBlockState().setValue(FACING, f), ctx);
		return canSurvive(state, ctx.getLevel(), ctx.getClickedPos()) ? state : null;
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader level, BlockPos pos,
		LivingEntity entity) {
		return true;
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
