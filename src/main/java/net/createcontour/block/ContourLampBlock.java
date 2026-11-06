package net.createcontour.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Material-bearing redstone lamp: light level 15 while LIT (powered),
 * dark otherwise. Follows the vanilla lamp rule: a state change on
 * power loss is deferred one tick (brief flicker suppression).
 */
public class ContourLampBlock extends ContourBlock {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public ContourLampBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(LIT));
	}

	/** Pixel-space boxes for collision AND render (single source). */
	public static List<float[]> boxesFor(BlockState state) {
		return List.of(new float[]{0, 0, 0, 16, 16, 16});
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
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return withWater(defaultBlockState()
			.setValue(LIT, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos())), ctx);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos,
		Block block, BlockPos fromPos, boolean isMoving) {
		if (!level.isClientSide) {
			boolean lit = state.getValue(LIT);
			boolean powered = level.hasNeighborSignal(pos);
			if (lit != powered) {
				if (lit) // lit -> unpowered: defer one tick like vanilla
					level.scheduleTick(pos, this, 4);
				else
					level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
			}
		}
		super.neighborChanged(state, level, pos, block, fromPos, isMoving);
	}

	@Override
	public void tick(BlockState state, net.minecraft.server.level.ServerLevel level,
		BlockPos pos, net.minecraft.util.RandomSource random) {
		if (state.getValue(LIT) && !level.hasNeighborSignal(pos))
			level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
