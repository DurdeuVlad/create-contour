package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialPolicy;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Paintable half slab: BOTTOM/TOP halves and a same-material DOUBLE merge. */
public class ContourSlabBlock extends ContourBlock {

	public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;

	private static final VoxelShape BOTTOM_SHAPE = Block.box(0, 0, 0, 16, 8, 16);
	private static final VoxelShape TOP_SHAPE = Block.box(0, 8, 0, 16, 16, 16);

	public ContourSlabBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false)
			.setValue(TYPE, SlabType.BOTTOM));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(TYPE));
	}

	@Override
	public int hostUnits(BlockState state) {
		return state.getValue(TYPE) == SlabType.DOUBLE ? 2 : 1;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		return switch (state.getValue(TYPE)) {
			case TOP -> TOP_SHAPE;
			case DOUBLE -> Shapes.block();
			default -> BOTTOM_SHAPE;
		};
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return state.getValue(TYPE) != SlabType.DOUBLE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		BlockState existing = ctx.getLevel().getBlockState(pos);
		if (existing.is(this) && existing.getValue(TYPE) != SlabType.DOUBLE) {
			// Merge into the clicked slab only when the paint matches.
			if (!sameMaterial(ctx, pos))
				return null;
			return existing.setValue(TYPE, SlabType.DOUBLE)
				.setValue(WATERLOGGED, false);
		}
		SlabType half = ctx.getClickLocation().y - pos.getY() > 0.5
			? SlabType.TOP : SlabType.BOTTOM;
		// Clicking the side face of a slab-like target aims for its half.
		if (existing.getBlock() instanceof SlabBlock
			&& existing.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP)
			half = SlabType.TOP;
		return withWater(defaultBlockState().setValue(TYPE, half), ctx);
	}

	/**
	 * Merge requires same paint: the incoming material follows the full
	 * precedence (stored payload > offhand > neutral). Blank-held merges
	 * only onto an unpainted host; any material mismatch rejects so the
	 * click falls through to normal adjacent placement.
	 */
	/** "Same paint" = same FULL BlockState (block + every property). */
	static boolean sameFullState(BlockState a, BlockState b) {
		return a != null && b != null && a.getBlock() == b.getBlock()
			&& a.getValues().equals(b.getValues());
	}

	private boolean sameMaterial(BlockPlaceContext ctx, BlockPos pos) {
		BlockState incoming = incomingMaterial(ctx);
		if (ctx.getLevel().getBlockEntity(pos) instanceof ContourBlockEntity be) {
			if (be.isFrozen())
				return false; // frozen future payloads never merge
			BlockState target = be.getMaterial();
			if (incoming == null)
				return MaterialSafety.isNeutral(target);
			return sameFullState(target, incoming);
		}
		return incoming == null;
	}

	public static BlockState incomingMaterial(BlockPlaceContext ctx) {
		return ContourBlockItem.resolveIncoming(ctx, ctx.getItemInHand(), ctx.getPlayer());
	}

	/**
	 * Merge eligibility: same item + not already double + same FULL paint.
	 * Any paint mismatch returns false so placement falls through to the
	 * normal adjacent cell instead of hard-failing on the clicked pos.
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		return state.getValue(TYPE) != SlabType.DOUBLE
			&& ctx.getItemInHand().is(asItem())
			&& sameMaterial(ctx, ctx.getClickedPos());
	}

	@Override
	protected Block nextInFamily() {
		return ContourRegistries.CONTOUR_VERTICAL_SLAB.get();
	}

	@Override
	protected BlockState carryOrientation(BlockState state, Direction facing, boolean top) {
		return state.setValue(TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
	}
}
