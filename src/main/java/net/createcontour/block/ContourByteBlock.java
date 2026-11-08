package net.createcontour.block;

import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Quarter-block mini cube: one 8px octant per block space. Placement
 * hugs the clicked face at the clicked quadrant; the wrench cycles the
 * eight octants. (Multi-byte-per-cell is a deferred multipart family.)
 */
public class ContourByteBlock extends ContourBlock {

	public enum Octant implements StringRepresentable {
		X0Y0Z0("x0y0z0", 0, 0, 0), X1Y0Z0("x1y0z0", 1, 0, 0),
		X0Y1Z0("x0y1z0", 0, 1, 0), X1Y1Z0("x1y1z0", 1, 1, 0),
		X0Y0Z1("x0y0z1", 0, 0, 1), X1Y0Z1("x1y0z1", 1, 0, 1),
		X0Y1Z1("x0y1z1", 0, 1, 1), X1Y1Z1("x1y1z1", 1, 1, 1);

		private final String name;
		public final int x, y, z;

		Octant(String name, int x, int y, int z) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final EnumProperty<Octant> OCTANT = EnumProperty.create("octant", Octant.class);

	public ContourByteBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(OCTANT, Octant.X0Y0Z0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(OCTANT));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		Octant o = state.getValue(OCTANT);
		VoxelShape shape = Block.box(o.x * 8, o.y * 8, o.z * 8,
			o.x * 8 + 8, o.y * 8 + 8, o.z * 8 + 8);
		if (level.getBlockEntity(pos) instanceof ContourBlockEntity be)
			for (ContourBlockEntity.ByteOccupant occ : be.getByteOccupants()) {
				Octant eo = Octant.values()[occ.octant()];
				shape = net.minecraft.world.phys.shapes.Shapes.or(shape,
					Block.box(eo.x * 8, eo.y * 8, eo.z * 8,
						eo.x * 8 + 8, eo.y * 8 + 8, eo.z * 8 + 8));
			}
		return shape;
	}

	/** Octant index (0-7, matching Octant.ordinal) from a hit position. */
	public static int octantAt(Vec3 hit, BlockPos cell) {
		int ox = hit.x - cell.getX() > 0.5 ? 1 : 0;
		int oy = hit.y - cell.getY() > 0.5 ? 1 : 0;
		int oz = hit.z - cell.getZ() > 0.5 ? 1 : 0;
		for (Octant o : Octant.values())
			if (o.x == ox && o.y == oy && o.z == oz)
				return o.ordinal();
		return 0;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
		if (!(ctx.getItemInHand().getItem() instanceof ContourBlockItem)
			|| ctx.getItemInHand().getItem() != asItem())
			return false;
		// NOTE: getClickedPos() is unreliable here - this very call is
		// what BlockPlaceContext uses to decide it, so it may already
		// return the relative (adjacent) position. Reconstruct the
		// clicked cell from the hit point instead (nudge opposite the
		// clicked face so we land inside the hit cell).
		Vec3 click = ctx.getClickLocation();
		BlockPos clicked = BlockPos.containing(click.subtract(
			Vec3.atLowerCornerOf(ctx.getClickedFace().getNormal())
				.scale(1.0E-4)));
		if (!(ctx.getLevel().getBlockEntity(clicked)
			instanceof ContourBlockEntity be))
			return false;
		return !be.occupiedOctants().contains(octantAt(click, clicked));
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	/**
	 * Octant nearest the click: the face axis snaps to the side adjacent
	 * to the clicked block; the other two axes come from the hit point's
	 * in-face coordinates.
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();
		double fx = hit.x - p.getX(), fy = hit.y - p.getY(), fz = hit.z - p.getZ();
		int ox = face.getAxis() == Direction.Axis.X ? (face.getStepX() < 0 ? 1 : 0) : (fx > 0.5 ? 1 : 0);
		int oy = face.getAxis() == Direction.Axis.Y ? (face.getStepY() < 0 ? 1 : 0) : (fy > 0.5 ? 1 : 0);
		int oz = face.getAxis() == Direction.Axis.Z ? (face.getStepZ() < 0 ? 1 : 0) : (fz > 0.5 ? 1 : 0);
		for (Octant o : Octant.values())
			if (o.x == ox && o.y == oy && o.z == oz)
				return withWater(defaultBlockState().setValue(OCTANT, o), ctx);
		return withWater(defaultBlockState(), ctx);
	}

	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(
		net.minecraft.world.item.ItemStack stack, BlockState state,
		net.minecraft.world.level.Level level, BlockPos pos,
		net.minecraft.world.entity.player.Player player,
		net.minecraft.world.InteractionHand hand,
		net.minecraft.world.phys.BlockHitResult hit) {
		// Paint the TARGETED octant: occupant #0 delegates to the normal
		// path; extra occupants get the same reserve/replace/refund
		// accounting against their own paid credit.
		int o = octantAt(hit.getLocation(), pos);
		if (o == state.getValue(OCTANT).ordinal())
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		if (!(level.getBlockEntity(pos) instanceof ContourBlockEntity be))
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		int idx = be.byteOccupantIndex(o);
		if (idx < 0)
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		if (level.isClientSide)
			return getAcceptedBlockState(level, pos, stack, hit.getDirection()) != null
				? net.minecraft.world.ItemInteractionResult.sidedSuccess(true)
				: net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		BlockState accepted = getAcceptedBlockState(level, pos, stack,
			hit.getDirection());
		if (accepted == null)
			return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		ContourBlockEntity.ByteOccupant occ = be.getByteOccupants().get(idx);
		BlockState newMat = net.createcontour.material.MaterialSafety
			.sanitize(accepted);
		boolean sameBase = occ.material() != null && occ.material().getBlock()
			== newMat.getBlock();
		boolean creative = player.getAbilities().instabuild;
		boolean consume = net.createcontour.config.ContourConfig.consumeMaterial()
			&& !sameBase && !creative;
		net.createcontour.material.MaterialPolicy.Reservation res = null;
		if (consume) {
			res = net.createcontour.material.MaterialPolicy.reserve(player,
				newMat.getBlock().asItem(), 1);
			if (res == null)
				return net.minecraft.world.ItemInteractionResult.FAIL;
		}
		be.paintByteOccupant(idx, newMat, consume ? 1 : (sameBase ? occ.paid() : 0));
		if (!sameBase && occ.paid() > 0 && occ.material() != null
			&& !net.createcontour.material.MaterialSafety.isNeutral(occ.material())
			&& !creative) {
			net.minecraft.world.item.ItemStack refund = new net.minecraft.world.item.ItemStack(
				occ.material().getBlock().asItem(), occ.paid());
			if (!player.getInventory().add(refund))
				player.drop(refund, false);
		}
		return net.minecraft.world.ItemInteractionResult.sidedSuccess(false);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		// Occupant #0 cycles to the next FREE octant - landing on an
		// extra occupant's slot would collapse two units into one.
		if (be == null)
			return state.cycle(OCTANT);
		java.util.Set<Integer> occupied = be.occupiedOctants();
		Octant o = state.getValue(OCTANT);
		for (int i = 0; i < 7; i++) {
			o = Octant.values()[(o.ordinal() + 1) % Octant.values().length];
			if (!occupied.contains(o.ordinal()))
				return state.setValue(OCTANT, o);
		}
		return state; // all other octants occupied: nothing to cycle to
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
