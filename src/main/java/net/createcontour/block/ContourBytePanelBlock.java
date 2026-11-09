package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Byte panel: the quarter-size sibling of the face panel - an 8x8x1px
 * plate occupying one of the four quadrants of a face. FACING selects
 * the face, QUADRANT (0-3) picks which corner of that face the plate
 * hugs, read clockwise from the face's lower-left as seen by the
 * placer. Placement derives both from the hit face and hit point. The
 * wrench cycles the quadrant around the face.
 */
public class ContourBytePanelBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;
	public static final IntegerProperty QUADRANT =
		IntegerProperty.create("quadrant", 0, 3);

	public ContourBytePanelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.UP).setValue(QUADRANT, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(QUADRANT));
	}

	/** Quadrant order: 0 lower-left, 1 lower-right, 2 upper-left, 3 upper-right (viewed on the face). */
	public static List<float[]> boxesFor(BlockState state) {
		return boxesFor(state.getValue(FACING), state.getValue(QUADRANT));
	}

	/** Same geometry by (mount face, quadrant) - used for BE extras too. */
	public static List<float[]> boxesFor(Direction f, int q) {
		boolean hiU = (q & 2) != 0, hiV = (q & 1) != 0;
		return List.of(switch (f) {
			case DOWN -> new float[]{hiU ? 8 : 0, 0, hiV ? 8 : 0,
				hiU ? 16 : 8, 1, hiV ? 16 : 8};
			case UP -> new float[]{hiU ? 8 : 0, 15, hiV ? 8 : 0,
				hiU ? 16 : 8, 16, hiV ? 16 : 8};
			case NORTH -> new float[]{hiU ? 8 : 0, hiV ? 8 : 0, 0,
				hiU ? 16 : 8, hiV ? 16 : 8, 1};
			case SOUTH -> new float[]{hiU ? 0 : 8, hiV ? 8 : 0, 15,
				hiU ? 8 : 16, hiV ? 16 : 8, 16};
			case WEST -> new float[]{0, hiV ? 8 : 0, hiU ? 0 : 8,
				1, hiV ? 16 : 8, hiU ? 8 : 16};
			default -> new float[]{15, hiV ? 8 : 0, hiU ? 0 : 8,
				16, hiV ? 16 : 8, hiU ? 8 : 16};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			for (ContourBlockEntity.PanelOccupant occ : be.getPanelOccupants())
				for (float[] b : PanelSlots.occupantBoxes(occ.slot(), occ.kind()))
					s = Shapes.or(s,
						Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/**
	 * Merge eligibility: a panel-family item clicked onto this cell may
	 * replace it when the incoming slot claim is disjoint. Clicked-cell
	 * reconstruction mirrors ContourByteBlock (the getClickedPos trap).
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return PanelSlots.canMergeReplace(state, ctx);
	}

	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		net.minecraft.world.item.ItemStack stack, BlockState state,
		net.minecraft.world.level.Level level, BlockPos pos,
		net.minecraft.world.entity.player.Player player,
		net.minecraft.world.InteractionHand hand,
		net.minecraft.world.phys.BlockHitResult hit) {
		return usePanelOccupantItemOn(stack, state, level, pos, player, hand,
			hit);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction f = ctx.getClickedFace().getOpposite();
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		return withWater(defaultBlockState().setValue(FACING, f)
			.setValue(QUADRANT, PanelSlots.quadrantAt(f, hit, p)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction f = state.getValue(FACING);
		return state.setValue(FACING, rotation.rotate(f));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// Occupant #0 cycles to the next FREE quadrant on its face -
		// landing on an extra occupant's slot would collapse two units.
		if (be == null)
			return state.cycle(QUADRANT);
		java.util.Set<Integer> occupied = be.occupiedPanelSlots();
		int face = state.getValue(FACING).ordinal() * 4;
		int q = state.getValue(QUADRANT);
		for (int i = 0; i < 3; i++) {
			q = (q + 1) & 3;
			if (!occupied.contains(face + q))
				return state.setValue(QUADRANT, q);
		}
		return state; // all other quadrants occupied
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
