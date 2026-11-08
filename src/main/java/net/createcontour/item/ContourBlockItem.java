package net.createcontour.item;

import java.util.List;
import net.createcontour.block.ContourBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.material.MaterialPolicy;
import net.createcontour.material.MaterialPolicy.Reservation;
import net.createcontour.material.MaterialSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Contour shape item. Items are always BLANK - paint lives on the world
 * block only, and breaking refunds the shape + the material item. The
 * `contour_*` payload keys remain parseable solely for legacy stacks
 * from before the drop rule changed; nothing mints them anymore.
 * `contour_material` = full BlockState compound, `contour_material_id` =
 * legacy string fallback, `contour_paid` (0..1), `contour_facing`,
 * `contour_top`.
 */
public class ContourBlockItem extends BlockItem {

	public ContourBlockItem(Block block) {
		super(block, new Properties());
	}

	// --- payload access ---

	@Nullable
	public static BlockState storedMaterial(HolderLookup.Provider provider, ItemStack stack) {
		CompoundTag tag = payloadTag(stack);
		try {
			BlockState s = null;
			if (tag.contains("contour_material"))
				s = MaterialSafety.normalize(NbtUtils.readBlockState(
					provider.lookupOrThrow(Registries.BLOCK), tag.getCompound("contour_material")));
			else if (tag.contains("contour_material_id")) {
				ResourceLocation id = ResourceLocation.parse(tag.getString("contour_material_id"));
				if (BuiltInRegistries.BLOCK.containsKey(id))
					s = MaterialSafety.normalize(BuiltInRegistries.BLOCK.get(id).defaultBlockState());
			}
			// validated payload only: invalid/air/removed/unsupported/neutral
			// stored states carry no material authority.
			return s != null && MaterialSafety.isAccepted(s) && !MaterialSafety.isNeutral(s)
				? s : null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/** True when the frame carries an EXPLICIT stored material key (even
	 *  an invalid one) vs a blank item with no stored payload. */
	private static boolean hasExplicitStored(ItemStack stack) {
		CompoundTag tag = payloadTag(stack);
		return tag.contains("contour_material") || tag.contains("contour_material_id");
	}

	public static int storedPaid(ItemStack stack) {
		return Math.max(0, Math.min(1, payloadTag(stack).getInt("contour_paid")));
	}

	private static CompoundTag payloadTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? new CompoundTag() : data.copyTag();
	}

	public static void writePayload(ItemStack stack, BlockState material,
		int paidUnits, Direction facing, boolean top) {
		CompoundTag tag = new CompoundTag();
		tag.put("contour_material", NbtUtils.writeBlockState(material));
		tag.putInt("contour_paid", Math.max(0, Math.min(1, paidUnits)));
		tag.putString("contour_facing", facing.name());
		tag.putBoolean("contour_top", top);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.merge(tag));
	}

	public static void writePaid(ItemStack stack, int paid) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack,
			t -> t.putInt("contour_paid", Math.max(0, Math.min(1, paid))));
	}

	/** Removes ONLY our keys - never other mods' CUSTOM_DATA content. */
	public static void clearPayload(ItemStack stack) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> {
			t.remove("contour_material");
			t.remove("contour_material_id");
			t.remove("contour_paid");
			t.remove("contour_facing");
			t.remove("contour_top");
		});
	}

	// --- gestures ---

	/**
	 * A BlockPlaceContext carrying the material of the ORIGINAL clicked
	 * block, captured before any world mutation. Ordinary
	 * BlockPlaceContext callers fall back to sampling the clicked block.
	 */
	public static class ContourPlaceContext extends BlockPlaceContext {
		@Nullable
		public final BlockState lookMaterial;

		public ContourPlaceContext(UseOnContext context) {
			super(context);
			BlockState target = context.getLevel()
				.getBlockState(context.getClickedPos());
			// A Contour target contributes its BE paint (appearance), not its
			// own recursive host block.
			if (target.getBlock() instanceof ContourBlock
				&& context.getLevel().getBlockEntity(context.getClickedPos())
					instanceof ContourBlockEntity be
				&& !MaterialSafety.isNeutral(be.getMaterial())) {
				this.lookMaterial = be.getMaterial();
				return;
			}
			BlockState norm = MaterialSafety.normalize(target);
			this.lookMaterial = MaterialSafety.isAccepted(norm) ? norm : null;
		}
	}

	/**
	 * Shape items never carry paint: sneak or not, every useOn is a
	 * plain placement. ContourPlaceContext still captures the clicked
	 * block's look for paint-at-placement QoL.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return place(new ContourPlaceContext(context));
	}

	private static BlockState offhandMaterial(BlockPlaceContext context, Player player) {
		if (player == null)
			return null;
		ItemStack off = player.getOffhandItem();
		if (off.getItem() instanceof BlockItem bi) {
			BlockState s = MaterialSafety.sanitize(bi.getBlock().defaultBlockState());
			return MaterialSafety.isNeutral(s) ? null : s;
		}
		return null;
	}

	private static Direction parseFacing(CompoundTag tag) {
		try {
			Direction d = Direction.valueOf(tag.getString("contour_facing"));
			return d.getAxis().isHorizontal() ? d : Direction.NORTH;
		} catch (IllegalArgumentException e) {
			return Direction.NORTH;
		}
	}

	/**
	 * Incoming paint resolution BEFORE any world/stack mutation:
	 * stored > offhand > clicked-block look > neutral.
	 */
	public static BlockState resolveIncoming(BlockPlaceContext context,
		ItemStack held, Player player) {
		// An explicit stored payload is authoritative - even when invalid:
		// it never retargets to offhand/look (conservation). Blank items
		// keep the offhand > look QoL precedence.
		if (hasExplicitStored(held))
			return storedMaterial(context.getLevel().registryAccess(), held);
		BlockState off = offhandMaterial(context, player);
		if (off != null)
			return off;
		if (context instanceof ContourPlaceContext cpc && cpc.lookMaterial != null)
			return cpc.lookMaterial;
		// Fallback (also hit by canBeReplaced inside the ctor, before
		// lookMaterial is assigned): sample the clicked block, and read a
		// Contour target's BE paint rather than its recursive host block.
		BlockPos pos = context.getClickedPos();
		BlockState clicked = context.getLevel().getBlockState(pos);
		if (clicked.getBlock() instanceof ContourBlock
			&& context.getLevel().getBlockEntity(pos) instanceof ContourBlockEntity be
			&& !MaterialSafety.isNeutral(be.getMaterial()))
			return be.getMaterial();
		BlockState norm = MaterialSafety.normalize(clicked);
		return MaterialSafety.isAccepted(norm) ? norm : null;
	}

	/**
	 * Escrowed placement: all billing decisions and inventory mutations
	 * happen BEFORE super.place; failure refunds the exact fragments.
	 */
	@Override
	public InteractionResult place(BlockPlaceContext context) {
		Level level = context.getLevel();
		ItemStack held = context.getItemInHand();
		Player player = context.getPlayer();
		// BlockPlaceContext already resolves the placement cell
		// (clicked pos if replaceable, else adjacent) - do NOT re-offset.
		BlockPos placePos = context.getClickedPos();

		// --- pre-mutation snapshot ---
		ItemStack frameSnapshot = held.copy();
		BlockState preMaterial = storedMaterial(level.registryAccess(), held);
		int prePaid = storedPaid(held);
		BlockState beforeState = level.getBlockState(placePos);
		BlockEntity beforeBe = level.getBlockEntity(placePos);
		CompoundTag beforeBeTag = beforeBe == null ? null
			: beforeBe.saveWithFullMetadata(level.registryAccess());
		BlockState mergeMaterial = null;
		Direction mergeFacing = null;
		boolean mergeTop = false;
		boolean mergeFrozen = false;
		if ((beforeState.getBlock() instanceof net.createcontour.block.ContourSlabBlock
			|| beforeState.getBlock() instanceof net.createcontour.block.ContourLayersBlock
			|| beforeState.getBlock() instanceof net.createcontour.block.ContourHalfLayerBlock
			|| beforeState.getBlock() instanceof net.createcontour.block.ContourSliceBlock)
			&& beforeBe instanceof ContourBlockEntity mbe) {
			mergeMaterial = mbe.getMaterial();
			mergeFacing = mbe.getFacing();
			mergeTop = mbe.isTop();
			mergeFrozen = mbe.isFrozen();
		}
		boolean merge = mergeMaterial != null;
		if (mergeFrozen)
			return InteractionResult.FAIL; // frozen future payloads: no merge
		int mergePaid = merge && beforeBe instanceof ContourBlockEntity mbe2
			? mbe2.getPaidUnits() : 0;

		// --- resolve incoming paint + billing ---
		// Invalid/unregistered stored payloads resolve to a free neutral
		// placement (paid 0) - they never mint credit or charge.
		BlockState incoming = level.isClientSide ? null
			: resolveIncoming(context, held, player);
		boolean creative = player != null && player.getAbilities().instabuild;
		// PREPAID transfer requires real validated material + credit AND a
		// non-creative placement - creative never seeds free credit.
		boolean paidTransfer = !creative && preMaterial != null
			&& !MaterialSafety.isNeutral(preMaterial) && prePaid > 0;
		// A template (unpaid, non-neutral incoming material) with consume ON
		// + survival reserves ONE unit up front - for fresh paint AND merge.
		int cost = !level.isClientSide && !creative && !paidTransfer
			&& ContourConfig.consumeMaterial() && incoming != null
			&& !MaterialSafety.isNeutral(incoming) ? 1 : 0;
		Reservation res = null;
		if (cost > 0) {
			if (player == null)
				return InteractionResult.FAIL;
			res = MaterialPolicy.reserve(player, incoming.getBlock().asItem(), cost);
			if (res == null)
				return InteractionResult.FAIL; // no world/stack mutation yet
		}

		// --- issue #4: Byte octant merge - click a free octant of an
		// existing byte cell to add an occupant; no setBlock at all. ---
		if (beforeState.getBlock() instanceof net.createcontour.block.ContourByteBlock
			&& getBlock() == beforeState.getBlock()
			&& beforeBe instanceof ContourBlockEntity tbe) {
			if (level.isClientSide)
				return InteractionResult.sidedSuccess(true);
			if (tbe.isFrozen()) {
				MaterialPolicy.refund(res);
				return InteractionResult.FAIL;
			}
			int octant = net.createcontour.block.ContourByteBlock
				.octantAt(context.getClickLocation(), placePos);
			if (!tbe.occupiedOctants().contains(octant)
				&& tbe.addByteOccupant(octant,
					incoming != null ? incoming
						: MaterialSafety.neutral(),
					paidTransfer ? Math.min(prePaid, 1)
						: (creative ? 0 : cost))) {
				held.shrink(1);
				return InteractionResult.sidedSuccess(false);
			}
			MaterialPolicy.refund(res);
			return InteractionResult.FAIL;
		}

		InteractionResult result = super.place(context);
		if (!result.consumesAction()) {
			MaterialPolicy.refund(res);
			return result;
		}
		if (level.isClientSide)
			return result;

		BlockEntity be = level.getBlockEntity(placePos);
		if (!(be instanceof ContourBlockEntity cbe)) {
			// Placement succeeded but our BE is missing: roll back world,
			// frame (full components) and reservation exactly.
			level.setBlock(placePos, beforeState, Block.UPDATE_ALL);
			if (beforeBeTag != null && level.getBlockEntity(placePos) != null)
				level.getBlockEntity(placePos).loadWithComponents(beforeBeTag,
					level.registryAccess());
			MaterialPolicy.refund(res);
			if (player != null)
				player.setItemInHand(context.getHand(), frameSnapshot);
			return InteractionResult.FAIL;
		}

		boolean top = payloadTag(frameSnapshot).contains("contour_top")
			? payloadTag(frameSnapshot).getBoolean("contour_top") : cbe.isTop();
		Direction facing = payloadTag(frameSnapshot).contains("contour_facing")
			? parseFacing(payloadTag(frameSnapshot)) : cbe.getFacing();
		BlockState placed = level.getBlockState(placePos);

		if (merge && placed.getBlock() == beforeState.getBlock()) {
			// Same-block merge (slab->double, layers/slice +1): rebuild
			// owned state from the PRE-MUTATION snapshot (never the
			// post-super imported BE data) + incoming paid.
			int incomingPaid = paidTransfer ? 1 : res != null ? res.totalCount() : 0;
			cbe.applyPayload(mergeMaterial, mergePaid + incomingPaid,
				mergeFacing, mergeTop);
			return result;
		}
		if (paidTransfer) {
			// PREPAID transfer: ledger moves once, no charge.
			cbe.applyPayload(preMaterial, 1, facing, top);
		} else if (incoming != null && !MaterialSafety.isNeutral(incoming)) {
			cbe.applyPayload(incoming, res != null ? 1 : 0, facing, top);
		} else {
			// Neutral placement: imported vanilla BE data must not leave
			// operational credit behind.
			cbe.applyPayload(MaterialSafety.neutral(), 0, facing, top);
		}
		return result;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
		List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable(
			"item.create_contour.tooltip.gestures"));
	}
}
