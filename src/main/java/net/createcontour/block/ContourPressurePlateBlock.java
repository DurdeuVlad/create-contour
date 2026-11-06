package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Material-bearing pressure plate: 14x14x1 plate, compressed to half
 * height while pressed. Detects living entities standing on it
 * (players and mobs; items do not count - documented v1 scope).
 * Redstone semantics match vanilla plates: 15-strength weak signal,
 * direct signal into the block below. Rechecks occupancy every tick
 * while powered; drops when the supporting block is removed.
 */
public class ContourPressurePlateBlock extends ContourBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final int CHECK_TICKS = 20;

	public ContourPressurePlateBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		float h = state.getValue(POWERED) ? 0.5f : 1f;
		return List.of(new float[]{1, 0, 1, 15, h, 15});
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
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below())
			.isFaceSturdy(level, pos.below(), Direction.UP);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = withWater(defaultBlockState(), ctx);
		return canSurvive(state, ctx.getLevel(), ctx.getClickedPos()) ? state : null;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir,
		BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
		if (dir == Direction.DOWN && !canSurvive(state, level, pos))
			return Blocks.AIR.defaultBlockState();
		return super.updateShape(state, dir, neighbor, level, pos, fromPos);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos,
		Entity entity) {
		if (!level.isClientSide && !state.getValue(POWERED))
			setPowered(level, pos, state, true);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos,
		RandomSource random) {
		if (state.getValue(POWERED)
			&& !level.getEntitiesOfClass(LivingEntity.class,
				new AABB(pos).inflate(0, 1, 0)).isEmpty()) {
			level.scheduleTick(pos, this, CHECK_TICKS);
			return;
		}
		if (state.getValue(POWERED))
			setPowered(level, pos, state, false);
	}

	private void setPowered(Level level, BlockPos pos, BlockState state,
		boolean powered) {
		level.setBlock(pos, state.setValue(POWERED, powered),
			Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_NEIGHBORS);
		level.playSound(null, pos,
			powered ? net.minecraft.sounds.SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON
				: net.minecraft.sounds.SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF,
			net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, powered ? 0.6f : 0.5f);
		if (powered)
			level.scheduleTick(pos, this, CHECK_TICKS);
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos,
		Direction dir) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos,
		Direction dir) {
		return state.getValue(POWERED) && dir == Direction.DOWN ? 15 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos,
		BlockState newState, boolean isMoving) {
		if (!isMoving && state.getValue(POWERED) && !state.is(newState.getBlock()))
			level.updateNeighborsAt(pos.below(), this);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
