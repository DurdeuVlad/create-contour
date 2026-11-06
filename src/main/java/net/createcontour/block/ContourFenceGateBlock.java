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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Openable gate matching the connective fence/wall family. FACING is
 * the direction the gate faces; its span runs perpendicular. IN_WALL
 * drops the bars 3px when both ends meet a connective neighbor
 * (vanilla in-wall rule). Click toggles; redstone holds open; the
 * wrench cycles facing. Same box list drives collision and render.
 */
public class ContourFenceGateBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty IN_WALL = BooleanProperty.create("in_wall");

	public ContourFenceGateBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH).setValue(OPEN, false)
			.setValue(IN_WALL, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(OPEN).add(IN_WALL));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		// gate planks span perpendicular to facing: N/S facing -> X span
		boolean span = state.getValue(FACING).getAxis() == Direction.Axis.Z;
		boolean open = state.getValue(OPEN);
		boolean inWall = state.getValue(IN_WALL);
		float drop = inWall ? -3f : 0f;
		List<float[]> out = new ArrayList<>();
		// two end posts along the span axis
		for (float[] post : new float[][]{{0, 0, 6, 2, 16, 10},
			{14, 0, 6, 16, 16, 10}}) {
			float[] p = post.clone();
			if (!span) { // transpose x<->z for Z-span
				float t = p[0]; p[0] = p[2]; p[2] = t;
				t = p[3]; p[3] = p[5]; p[5] = t;
			}
			out.add(p);
		}
		if (!open) { // two bars across the middle of the span
			for (float[] rail : new float[][]{{2, 6 + drop, 7, 14, 9 + drop, 9},
				{2, 12 + drop, 7, 14, 15 + drop, 9}}) {
				float[] r = rail.clone();
				if (!span) {
					float t = r[0]; r[0] = r[2]; r[2] = t;
					t = r[3]; r[3] = r[5]; r[5] = t;
				}
				out.add(r);
			}
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

	private static boolean isConnective(BlockState s) {
		return s.getBlock() instanceof ContourFenceBlock
			|| s.getBlock() instanceof ContourWallBlock
			|| s.getBlock() instanceof net.minecraft.world.level.block.FenceBlock
			|| s.getBlock() instanceof net.minecraft.world.level.block.WallBlock;
	}

	private static BlockState wallState(BlockState state, BlockGetter level,
		BlockPos pos) {
		boolean span = state.getValue(FACING).getAxis() == Direction.Axis.Z;
		Direction a = span ? Direction.EAST : Direction.NORTH;
		boolean w = isConnective(level.getBlockState(pos.relative(a)))
			|| isConnective(level.getBlockState(pos.relative(a.getOpposite())));
		return state.setValue(IN_WALL, w);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = withWater(defaultBlockState()
			.setValue(FACING, ctx.getHorizontalDirection())
			.setValue(OPEN, false), ctx);
		return wallState(state, ctx.getLevel(), ctx.getClickedPos());
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir,
		BlockState neighbor, net.minecraft.world.level.LevelAccessor level,
		BlockPos pos, BlockPos neighborPos) {
		Direction.Axis spanAxis = state.getValue(FACING).getAxis() == Direction.Axis.X
			? Direction.Axis.Z : Direction.Axis.X;
		if (dir.getAxis() == spanAxis)
			return wallState(state, level, pos);
		return state;
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
		state = state.cycle(OPEN);
		level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
		level.levelEvent(player, state.getValue(OPEN) ? 1008 : 1014, pos, 0);
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
