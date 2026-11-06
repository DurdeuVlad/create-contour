package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Small redstone button on a floor/wall/ceiling face: a 6x2x4 press
 * plate. A non-material click powers it for 20 ticks (stone timing);
 * redstone semantics match the lever (15-strength weak signal all
 * sides, direct signal only into the mount block). Projectile
 * activation is not modeled (documented v1 scope). The wrench cycles
 * the mount face.
 */
public class ContourButtonBlock extends ContourBlock {

	public static final EnumProperty<AttachFace> FACE =
		BlockStateProperties.ATTACH_FACE;
	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final int PRESS_TICKS = 20;

	public ContourButtonBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACE, AttachFace.FLOOR)
			.setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACE).add(FACING).add(POWERED));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		boolean horiz = state.getValue(FACING).getAxis() == Direction.Axis.X;
		float[] b = horiz ? new float[]{5, 0, 6, 11, 2, 10}
			: new float[]{6, 0, 5, 10, 2, 11};
		return switch (state.getValue(FACE)) {
			case FLOOR -> List.of(b);
			case CEILING -> List.of(new float[]{b[0], 16 - b[4], b[2],
				b[3], 16 - b[1], b[5]});
			default -> List.of(toWall(b, state.getValue(FACING)));
		};
	}

	private static float[] toWall(float[] b, Direction f) {
		float depth0 = b[1], depth1 = b[4];
		float y0 = (16 - depth0 - depth1) / 2f, y1 = y0 + (depth1 - depth0);
		return switch (f) {
			case NORTH -> new float[]{b[0], y0, depth0, b[3], y1, depth1};
			case SOUTH -> new float[]{b[0], y0, 16 - depth1, b[3], y1, 16 - depth0};
			case EAST -> new float[]{16 - depth1, y0, b[2], 16 - depth0, y1, b[5]};
			default -> new float[]{depth0, y0, b[2], depth1, y1, b[5]};
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
		return switch (state.getValue(FACE)) {
			case FLOOR -> Direction.DOWN;
			case CEILING -> Direction.UP;
			default -> state.getValue(FACING);
		};
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
		AttachFace face = f == Direction.UP ? AttachFace.FLOOR
			: f == Direction.DOWN ? AttachFace.CEILING : AttachFace.WALL;
		Direction facing = face == AttachFace.WALL ? f.getOpposite()
			: ctx.getHorizontalDirection().getOpposite();
		BlockState state = withWater(defaultBlockState().setValue(FACE, face)
			.setValue(FACING, facing), ctx);
		return canSurvive(state, ctx.getLevel(), ctx.getClickedPos()) ? state : null;
	}

	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack,
		BlockState state, Level level, BlockPos pos, Player player,
		net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		var r = super.useItemOn(stack, state, level, pos, player, hand, hit);
		if (r != net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION)
			return r;
		if (level.isClientSide)
			return net.minecraft.world.ItemInteractionResult.SUCCESS;
		if (state.getValue(POWERED))
			return net.minecraft.world.ItemInteractionResult.CONSUME;
		level.setBlock(pos, state.setValue(POWERED, true),
			Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_NEIGHBORS);
		level.scheduleTick(pos, this, PRESS_TICKS);
		level.playSound(null, pos, net.minecraft.sounds.SoundEvents.STONE_BUTTON_CLICK_ON,
			net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 0.6f);
		return net.minecraft.world.ItemInteractionResult.CONSUME;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos,
		RandomSource random) {
		if (state.getValue(POWERED)) {
			level.setBlock(pos, state.setValue(POWERED, false),
				Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_NEIGHBORS);
			level.playSound(null, pos,
				net.minecraft.sounds.SoundEvents.STONE_BUTTON_CLICK_OFF,
				net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 0.5f);
		}
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos,
		Direction dir) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos,
		Direction dir) {
		return state.getValue(POWERED) && dir == mountFace(state) ? 15 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos,
		BlockState newState, boolean isMoving) {
		if (!isMoving && state.getValue(POWERED) && !state.is(newState.getBlock()))
			level.updateNeighborsAt(pos.relative(mountFace(state)), this);
		super.onRemove(state, level, pos, newState, isMoving);
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
