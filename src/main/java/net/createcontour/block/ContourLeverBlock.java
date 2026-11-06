package net.createcontour.block;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Small redstone lever on a floor/wall/ceiling face: a 6x4x6 base plus
 * a tilted handle approximated by two thin boxes. Click (non-paint)
 * toggles POWERED and emits a 15-strength signal toward the face it's
 * mounted on (vanilla lever rule). The wrench cycles mounting faces.
 */
public class ContourLeverBlock extends ContourBlock {

	public static final EnumProperty<AttachFace> FACE =
		BlockStateProperties.ATTACH_FACE;
	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ContourLeverBlock(Properties pProperties) {
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
		AttachFace face = state.getValue(FACE);
		List<float[]> out = new ArrayList<>();
		// base plate on the attach face
		float[] base = horiz ? new float[]{5, 0, 4, 11, 3, 12}
			: new float[]{4, 0, 5, 12, 3, 11};
		// handle: thin tilted box toward facing (approximation)
		float[] handle = horiz ? new float[]{6, 3, 7, 10, 9, 9}
			: new float[]{7, 3, 6, 9, 9, 10};
		switch (face) {
			case FLOOR -> {
			}
			case CEILING -> { // flip y
				base = flipY(base);
				handle = flipY(handle);
			}
			default -> { // WALL: rotate onto the facing wall
				base = toWall(base, state.getValue(FACING));
				handle = toWall(handle, state.getValue(FACING));
			}
		}
		out.add(base);
		out.add(handle);
		return out;
	}

	private static float[] flipY(float[] b) {
		return new float[]{b[0], 16 - b[4], b[2], b[3], 16 - b[1], b[5]};
	}

	/** Fold the floor footprint onto the wall named by facing. */
	private static float[] toWall(float[] b, Direction f) {
		// base/handle were defined as floor boxes; map y-depth onto the wall
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
		level.setBlock(pos, state.cycle(POWERED),
			Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_NEIGHBORS);
		level.playSound(null, pos, net.minecraft.sounds.SoundEvents.LEVER_CLICK,
			net.minecraft.sounds.SoundSource.BLOCKS, 0.3f,
			state.getValue(POWERED) ? 0.5f : 0.6f);
		return net.minecraft.world.ItemInteractionResult.CONSUME;
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
