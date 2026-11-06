package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 3px openable plate. Closed: horizontal plate in the TOP or BOTTOM
 * half. Open: vertical plate hinged against the FACING side. Clicking
 * toggles (no material credit change); redstone power holds it open.
 * The wrench cycles FACING so a closed plate can be re-hinged.
 */
public class ContourTrapdoorBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

	public ContourTrapdoorBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM)
			.setValue(OPEN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(HALF).add(OPEN));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		if (state.getValue(OPEN)) {
			return List.of(switch (state.getValue(FACING)) {
				case NORTH -> new float[]{0, 0, 0, 16, 16, 3};
				case SOUTH -> new float[]{0, 0, 13, 16, 16, 16};
				case EAST -> new float[]{13, 0, 0, 16, 16, 16};
				default -> new float[]{0, 0, 0, 3, 16, 16};
			});
		}
		return state.getValue(HALF) == Half.TOP
			? List.of(new float[]{0, 13, 0, 16, 16, 16})
			: List.of(new float[]{0, 0, 0, 16, 3, 16});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = net.minecraft.world.phys.shapes.Shapes.empty();
		for (float[] b : boxesFor(state))
			s = net.minecraft.world.phys.shapes.Shapes.or(s,
				Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getHorizontalDirection().getOpposite();
		Half half = ctx.getClickedFace() == Direction.DOWN
			|| (ctx.getClickedFace().getAxis().isHorizontal()
				&& ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5)
			? Half.TOP : Half.BOTTOM;
		return withWater(defaultBlockState().setValue(FACING, f)
			.setValue(HALF, half).setValue(OPEN, false), ctx);
	}

	/**
	 * Any click the shared material workflow does not consume toggles the
	 * plate: paintable stacks (materials, sneak+template) fall through to
	 * super first, so painting still works.
	 */
	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		net.minecraft.world.item.ItemStack stack, BlockState state, Level level,
		BlockPos pos, Player player, net.minecraft.world.InteractionHand hand,
		BlockHitResult hit) {
		var r = super.useItemOn(stack, state, level, pos, player, hand, hit);
		if (r != net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION)
			return r;
		if (level.isClientSide)
			return net.minecraft.world.ItemInteractionResult.SUCCESS;
		state = state.cycle(OPEN);
		level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
		level.levelEvent(player, state.getValue(OPEN) ? 1007 : 1013, pos, 0);
		return net.minecraft.world.ItemInteractionResult.CONSUME;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos,
		Block block, BlockPos fromPos, boolean isMoving) {
		boolean powered = level.hasNeighborSignal(pos);
		if (powered != state.getValue(OPEN))
			level.setBlock(pos, state.setValue(OPEN, powered),
				Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
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
