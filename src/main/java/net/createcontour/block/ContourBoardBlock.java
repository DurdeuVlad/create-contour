package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Board (issue #24): a glow-lichen-style 1px face-sheet system. Six
 * independent face booleans (down/up/north/south/west/east) each emit a
 * 1px-thick sheet on that face's interior shell; one cell can carry all
 * six sheets. Clicking a free face with a board item merges that face
 * onto the cell; the wrench removes the sheet under the click (or the
 * whole block when it was the last one).
 *
 * Geometry is ORTHOGONAL to the interior channels: board sheets hug the
 * 0..1px boundary shell while panel/pane geometry lives on the 7..9px
 * interior, so a painted board never collides with a panel in the same
 * volume. Under the issue #22 universal host this orthogonality is what
 * lets board shells co-reside with byte/panel/pane occupants on ANY
 * multipart host cell; the blockstate faces remain the board channel's
 * own claim surface (one BOARD-kind occupant record per present face),
 * and records on other-kind hosts carry face geometry directly.
 */
public class ContourBoardBlock extends ContourBlock {

	// Vanilla face booleans carry the exact names the blockstate contract
	// promises (down/up/north/south/west/east).
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;

	public ContourBoardBlock(Properties pProperties) {
		super(pProperties);
		// Contract default: all six faces absent. A zero-face board
		// never exists in-world (placement mounts exactly one sheet and
		// the wrench removes the block when the last face clears), but
		// the registry default itself must start empty so
		// getStateForPlacement builds strictly one-sheet states.
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(DOWN, false)
			.setValue(UP, false).setValue(NORTH, false)
			.setValue(SOUTH, false).setValue(WEST, false)
			.setValue(EAST, false));
	}

	@Override
	protected void createBlockStateDefinition(
		StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(
			DOWN, UP, NORTH, SOUTH, WEST, EAST));
	}

	public static BooleanProperty faceProperty(Direction d) {
		return switch (d) {
			case DOWN -> DOWN;
			case UP -> UP;
			case NORTH -> NORTH;
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
		};
	}

	public static boolean facePresent(BlockState state, Direction d) {
		return state.getValue(faceProperty(d));
	}

	public static int faceCount(BlockState state) {
		int n = 0;
		for (Direction d : Direction.values())
			if (facePresent(state, d))
				n++;
		return n;
	}

	/** The 1px interior shell on one face (pixel space). */
	public static List<float[]> boxesForFace(Direction d) {
		return List.of(switch (d) {
			case DOWN -> new float[]{0f, 0f, 0f, 16f, 1f, 16f};
			case UP -> new float[]{0f, 15f, 0f, 16f, 16f, 16f};
			case NORTH -> new float[]{0f, 0f, 0f, 16f, 16f, 1f};
			case SOUTH -> new float[]{0f, 0f, 15f, 16f, 16f, 16f};
			case WEST -> new float[]{0f, 0f, 0f, 1f, 16f, 16f};
			case EAST -> new float[]{15f, 0f, 0f, 16f, 16f, 16f};
		});
	}

	/** All present face shells (the full geometry of the cell). */
	public static List<float[]> boxesFor(BlockState state) {
		java.util.ArrayList<float[]> boxes = new java.util.ArrayList<>();
		for (Direction d : Direction.values())
			if (facePresent(state, d))
				boxes.addAll(boxesForFace(d));
		return boxes;
	}

	/**
	 * Which present face's shell the local hit point lies in (0..1 cell
	 * space). The clicked-face-OPPOSITE is preferred: an interior hit on
	 * a thin sheet sees the sheet behind the free face the ray entered,
	 * while an occupied clicked face means the ray hit that sheet's own
	 * outer surface. Corner hits inside several shells resolve in
	 * Direction order (down/up first).
	 */
	@Nullable
	public static Direction faceAtLocal(BlockState state, Vec3 local,
		Direction clickedFace) {
		Direction pref = clickedFace.getOpposite();
		if (facePresent(state, pref)
			&& PanelSlots.inBoxes(boxesForFace(pref), local))
			return pref;
		for (Direction d : Direction.values())
			if (d != pref && facePresent(state, d)
				&& PanelSlots.inBoxes(boxesForFace(d), local))
				return d;
		return facePresent(state, pref) ? pref : null;
	}

	@Override
	public int hostUnits(BlockState state) {
		return faceCount(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level,
		BlockPos pos, CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		// issue #22: union EVERY extra occupant's boxes (any kind).
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			for (float[] b : be.extraFootprintBoxes())
				s = Shapes.or(s,
					Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/**
	 * Merge eligibility (issue #22): universal geometric gate. A board
	 * item claims the CLICKED face's shell - free while the face is
	 * unclaimed on a board host and the shell is geometrically
	 * disjoint from every different-kind occupant.
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return ContourBlockEntity.canMergeInto(ctx);
	}

	/**
	 * Fresh placement mounts the sheet on the face OPPOSITE the clicked
	 * surface (it hugs the neighbor face the ray entered through); a
	 * merge click claims the clicked face itself. getClickedPos() is the
	 * resolved placement cell here (BlockPlaceContext already resolved
	 * replaceable cells), so a same-block cell means a merge.
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState existing = ctx.getLevel()
			.getBlockState(ctx.getClickedPos());
		if (existing.getBlock() == this)
			return existing.setValue(
				faceProperty(ctx.getClickedFace()), true);
		return withWater(defaultBlockState().setValue(
			faceProperty(ctx.getClickedFace().getOpposite()), true), ctx);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack,
		BlockState state, Level level, BlockPos pos, Player player,
		net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		return useBoardOccupantItemOn(stack, state, level, pos, player,
			hand, hit);
	}

	/**
	 * Wrench removes the sheet under the click: the face's blank plus
	 * its paid material refund are handed out, its flag clears, and its
	 * occupant entry drops. Removing the last sheet removes the whole
	 * block (full ledger, same as sneak-wrench - a zero-face board has
	 * no geometry left to carry).
	 */
	@Override
	public InteractionResult onWrenched(BlockState state,
		UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (level.isClientSide)
			return InteractionResult.sidedSuccess(true);
		if (!(level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			|| be.isFrozen())
			return InteractionResult.PASS;
		Vec3 local = context.getClickLocation()
			.subtract(Vec3.atLowerCornerOf(pos));
		Direction f = faceAtLocal(state, local, context.getClickedFace());
		if (f == null)
			return InteractionResult.PASS;
		boolean otherOccupants = be.getOccupants().stream()
			.anyMatch(o -> o.kind()
				!= ContourBlockEntity.OccupantKind.BOARD);
		if (faceCount(state) == 1 && !otherOccupants) {
			// Last sheet and nothing else carries the cell: identical
			// economics to the sneak-wrench pickup. (issue #22: when
			// other-kind occupants remain the zero-face state stays as
			// their carrier - the board flag simply clears.)
			List<ItemStack> drops = ledgerDrops(state, be);
			if (!level.removeBlock(pos, false))
				return InteractionResult.FAIL;
			for (ItemStack stack : drops)
				if (player == null || !player.getInventory().add(stack))
					popResource(level, pos, stack);
			return InteractionResult.SUCCESS;
		}
		int idx = be.boardOccupantIndex(f.ordinal());
		ContourBlockEntity.BoardOccupant occ = idx < 0 ? null
			: be.getBoardOccupants().get(idx);
		if (!level.setBlock(pos,
			state.setValue(faceProperty(f), false), Block.UPDATE_ALL))
			return InteractionResult.FAIL;
		// Same-block setBlock retains the BE - drop the face's record.
		be.removeBoardOccupant(f.ordinal());
		ItemStack blank = new ItemStack(asItem());
		if (player == null || !player.getInventory().add(blank))
			popResource(level, pos, blank);
		if (occ != null && occ.paid() > 0 && occ.material() != null
			&& !MaterialSafety.isNeutral(occ.material())) {
			ItemStack refund = new ItemStack(
				occ.material().getBlock().asItem(), occ.paid());
			if (player == null || !player.getInventory().add(refund))
				popResource(level, pos, refund);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		BlockState out = state;
		for (Direction d : Direction.values())
			out = out.setValue(faceProperty(rotation.rotate(d)),
				state.getValue(faceProperty(d)));
		return out;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		BlockState out = state;
		for (Direction d : Direction.values())
			out = out.setValue(faceProperty(mirror.mirror(d)),
				state.getValue(faceProperty(d)));
		return out;
	}

	@Override
	protected BlockState wrenchTarget(BlockState state,
		ContourBlockEntity be) {
		return state;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
