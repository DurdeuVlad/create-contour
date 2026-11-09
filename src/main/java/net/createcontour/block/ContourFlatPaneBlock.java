package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Flat pane: the pane turned horizontal - a 2px sheet on the cell's
 * mid-height plane (y 7..9), spanning the full footprint. Neighbouring
 * flat panes abut edge-to-edge into a continuous deck. Pure box
 * geometry, no facing state.
 *
 * Issue #12: interior mid-plane occupancy channel - the blockstate
 * sheet is always the Y plane (old saves byte-for-byte); extra BE
 * occupants add X/Z mid-planes (axis-exclusive, so at most one sheet
 * per axis and the three cross without sharing slots). Merging claims
 * the axis of the clicked face; the pane channel never mixes with
 * face-quadrant panel cells.
 */
public class ContourFlatPaneBlock extends ContourBlock {

	public ContourFlatPaneBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	public static List<float[]> boxesFor(BlockState state) {
		return List.of(new float[]{0f, 7f, 0f, 16f, 9f, 16f});
	}

	/** Mid-plane sheet for an axis: 0=X (x7..9), 1=Y (y7..9), 2=Z. */
	public static List<float[]> boxesForAxis(int axis) {
		return List.of(switch (axis) {
			case 0 -> new float[]{7f, 0f, 0f, 9f, 16f, 16f};
			case 2 -> new float[]{0f, 0f, 7f, 16f, 16f, 9f};
			default -> new float[]{0f, 7f, 0f, 16f, 9f, 16f};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			for (ContourBlockEntity.PaneOccupant occ : be.getPaneOccupants())
				for (float[] b : boxesForAxis(occ.axis()))
					s = Shapes.or(s,
						Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/**
	 * Merge eligibility: a flat-pane item clicked onto this cell claims
	 * the mid-plane on the clicked face's axis; replaceable only when
	 * that axis is still free (same-axis exclusive). Same clicked-cell
	 * reconstruction trap as ContourByteBlock applies.
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		if (!(ctx.getItemInHand().getItem() instanceof ContourBlockItem)
			|| ctx.getItemInHand().getItem() != asItem())
			return false;
		Vec3 click = ctx.getClickLocation();
		BlockPos clicked = BlockPos.containing(click.subtract(
			Vec3.atLowerCornerOf(ctx.getClickedFace().getNormal())
				.scale(1.0E-4)));
		if (!(ctx.getLevel().getBlockEntity(clicked)
			instanceof ContourBlockEntity be))
			return false;
		return !be.occupiedPaneAxes()
			.contains(ctx.getClickedFace().getAxis().ordinal());
	}

	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		net.minecraft.world.item.ItemStack stack, BlockState state,
		net.minecraft.world.level.Level level, BlockPos pos,
		net.minecraft.world.entity.player.Player player,
		net.minecraft.world.InteractionHand hand,
		net.minecraft.world.phys.BlockHitResult hit) {
		return usePaneOccupantItemOn(stack, state, level, pos, player, hand,
			hit);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return withWater(defaultBlockState(), ctx);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state;
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
