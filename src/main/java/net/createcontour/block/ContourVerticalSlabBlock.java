package net.createcontour.block;

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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Vertical half slab attached to a side of its cell. FACING names the
 * half the slab occupies: NORTH = z in [0,8), SOUTH = z in [8,16),
 * WEST = x in [0,8), EAST = x in [8,16). Placement attaches against the
 * clicked face (EAST face -> WEST half); on UP/DOWN hits the attached
 * side is the opposite of the player's horizontal look. No double state.
 */
public class ContourVerticalSlabBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	public ContourVerticalSlabBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> Block.box(0, 0, 8, 16, 16, 16);
			case WEST -> Block.box(0, 0, 0, 8, 16, 16);
			case EAST -> Block.box(8, 0, 0, 16, 16, 16);
			default -> Block.box(0, 0, 0, 16, 16, 8);
		};
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction clicked = ctx.getClickedFace();
		Direction facing = clicked.getAxis().isHorizontal()
			? clicked.getOpposite()
			: ctx.getHorizontalDirection().getOpposite();
		return withWater(defaultBlockState().setValue(FACING, facing), ctx);
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
	protected Block nextInFamily() {
		return net.createcontour.registry.ContourRegistries.CONTOUR_STAIRS.get();
	}

	@Override
	protected BlockState carryOrientation(BlockState state, Direction facing, boolean top) {
		return state.setValue(FACING, facing);
	}
}
