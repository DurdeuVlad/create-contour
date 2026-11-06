package net.createcontour.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shared base for NSEW-connective contour shapes (wall/pane/fence).
 * Geometry is a pixel-space box list: the SAME list builds the collision
 * VoxelShape and drives the render emitter, so render == collision by
 * construction (the golden rule for arbitrary neighbors).
 */
public abstract class ContourConnectiveBlock extends ContourBlock {

	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;

	private static final BooleanProperty[] SIDES = {NORTH, EAST, SOUTH, WEST};
	private static final Direction[] DIRS = {Direction.NORTH, Direction.EAST,
		Direction.SOUTH, Direction.WEST};

	private final VoxelShape[] shapeCache = new VoxelShape[16];

	protected ContourConnectiveBlock(Properties pProperties) {
		super(pProperties);
		BlockState def = defaultBlockState().setValue(WATERLOGGED, false);
		for (BooleanProperty p : SIDES)
			def = def.setValue(p, false);
		registerDefaultState(def);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(NORTH).add(EAST).add(SOUTH).add(WEST));
	}

	/** Center column boxes (always present). */
	protected abstract List<float[]> postBoxes();

	/** Arm/rail boxes reaching from the center toward `dir`. */
	protected abstract List<float[]> armBoxes(Direction dir);

	/** Connection rule against a horizontal neighbor. */
	protected abstract boolean connectsTo(BlockState neighbor, Direction dir,
		BlockGetter level, BlockPos pos, BlockPos neighborPos);

	/** Vanilla-compatible shortcut: the neighbor's facing side is solid. */
	protected boolean sturdy(BlockState neighbor, Direction dir,
		BlockGetter level, BlockPos pos, BlockPos neighborPos) {
		return neighbor.isFaceSturdy(level, neighborPos, dir.getOpposite());
	}

	private int indexOf(BlockState state) {
		int i = 0;
		for (int s = 0; s < 4; s++)
			if (state.getValue(SIDES[s]))
				i |= 1 << s;
		return i;
	}

	/** Full box list: post + an arm per connected horizontal side. */
	public List<float[]> boxesFor(BlockState state) {
		List<float[]> out = new ArrayList<>(postBoxes());
		for (int s = 0; s < 4; s++)
			if (state.getValue(SIDES[s]))
				out.addAll(armBoxes(DIRS[s]));
		return out;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		int i = indexOf(state);
		if (shapeCache[i] == null) {
			VoxelShape s = Shapes.empty();
			for (float[] b : boxesFor(state))
				s = Shapes.joinUnoptimized(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]),
					BooleanOp.OR);
			shapeCache[i] = s;
		}
		return shapeCache[i];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	protected boolean anyConnected(BlockState state) {
		for (BooleanProperty p : SIDES)
			if (state.getValue(p))
				return true;
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = withWater(defaultBlockState(), ctx);
		for (int s = 0; s < 4; s++) {
			Direction d = DIRS[s];
			BlockPos np = ctx.getClickedPos().relative(d);
			state = state.setValue(SIDES[s], connectsTo(
				ctx.getLevel().getBlockState(np), d, ctx.getLevel(), ctx.getClickedPos(), np));
		}
		return state;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir,
		BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		for (int s = 0; s < 4; s++)
			if (DIRS[s] == dir)
				return state.setValue(SIDES[s],
					connectsTo(neighbor, dir, level, pos, neighborPos));
		return state;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
