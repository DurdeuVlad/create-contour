package net.createcontour.material;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.config.ContourConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Atomic server-authoritative paint accounting.
 *
 * - consumeMaterial ON + survival: a paint that changes the material costs
 *   one matching material item per host shape unit. OFF or creative: zero.
 * - Same-material (same block) repaint/re-axis: free, ledger unchanged.
 * - INVALID input (null/air/recursive/BE/unregistered) returns false and
 *   leaves material, ledger and inventory untouched - clearing paint is a
 *   separate explicit action, never a side effect of bad input.
 * - Old paid units are refunded as items only AFTER a successful
 *   different-material repaint.
 */
public final class MaterialPolicy {

	private MaterialPolicy() {
	}

	public static boolean sameBaseMaterial(BlockState a, BlockState b) {
		return a != null && b != null && a.getBlock() == b.getBlock();
	}

	// --- escrow: reserve EXACT fragments, refund on failure, keep on success ---

	/**
	 * Escrow for placement/gesture billing: holds copies (with ALL
	 * components/data) of the exact item fragments taken from the player's
	 * inventory (main + offhand). One-shot: once refunded it inerts.
	 */
	public static final class Reservation {
		private record Fragment(int slot, ItemStack stack) {
		}

		private final Player player;
		private final List<Fragment> fragments = new ArrayList<>();
		private boolean refunded;

		private Reservation(Player player) {
			this.player = player;
		}

		public int totalCount() {
			return fragments.stream().mapToInt(f -> f.stack().getCount()).sum();
		}
	}

	private static int countMatching(Player player, Item item) {
		int total = 0;
		for (ItemStack s : player.getInventory().items)
			if (s.is(item))
				total += s.getCount();
		if (player.getOffhandItem().is(item))
			total += player.getOffhandItem().getCount();
		return total;
	}

	/**
	 * Two-pass escrow: returns null when fewer than `count` matching items
	 * exist (inventory untouched), else shrinks real stacks and returns
	 * fragments preserving every component of the source stacks.
	 */
	@Nullable
	public static Reservation reserve(@Nullable Player player, Item item, int count) {
		Reservation r = new Reservation(player);
		if (count <= 0)
			return r; // zero-cost escrow is always granted (and empty)
		if (player == null || countMatching(player, item) < count)
			return null;
		int need = count;
		var items = player.getInventory().items;
		for (int slot = 0; slot < items.size() && need > 0; slot++) {
			ItemStack s = items.get(slot);
			if (s.is(item)) {
				int take = Math.min(need, s.getCount());
				r.fragments.add(new Reservation.Fragment(slot,
					s.copyWithCount(take))); // BEFORE shrink: keeps data + slot
				s.shrink(take);
				need -= take;
			}
		}
		ItemStack off = player.getOffhandItem();
		if (need > 0 && off.is(item)) {
			int take = Math.min(need, off.getCount());
			r.fragments.add(new Reservation.Fragment(
				net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND,
				off.copyWithCount(take)));
			off.shrink(take);
		}
		player.getInventory().setChanged();
		return r;
	}

	/**
	 * Returns every reserved fragment once: fills partial compatible
	 * stacks first, drops ONLY the true remainder - no duplication on a
	 * partially-full inventory. Null/empty/already-refunded is a no-op.
	 */
	public static void refund(@Nullable Reservation r) {
		if (r == null || r.refunded || r.player == null)
			return;
		r.refunded = true;
		for (var f : r.fragments) {
			ItemStack remaining = f.stack().copy();
			// Restore the ORIGIN slot first via canonical Inventory slot
			// mapping (covers main AND offhand indices): empty slot gets
			// the fragment, compatible stack fills up to its capacity, an
			// incompatible/modified slot is never overwritten.
			var inv = r.player.getInventory();
			ItemStack current = inv.getItem(f.slot());
			int limit = inv.getMaxStackSize(remaining);
			if (current.isEmpty()) {
				inv.setItem(f.slot(), remaining.split(limit));
			} else if (ItemStack.isSameItemSameComponents(current, remaining)) {
				int space = limit - current.getCount();
				if (space > 0)
					current.grow(remaining.split(space).getCount());
			}
			// fallback normal merge, then drop only the true remainder
			inv.add(remaining);
			if (!remaining.isEmpty())
				r.player.drop(remaining, false);
		}
		r.fragments.clear();
		r.player.getInventory().setChanged();
	}

	// --- gesture-time paint ---

	/**
	 * Paint `requested` onto `be`. The ORIGINAL candidate is validated
	 * (normalize -> isAccepted) BEFORE any mutation; invalid input returns
	 * false with zero side effects. Null-player callers may only apply
	 * materials that cost nothing. Uses the same Reservation API as
	 * placement - reserve, mutate, commit (no refund on success).
	 */
	public static boolean paint(Level level, ContourBlockEntity be,
		@Nullable Player player, @Nullable BlockState requested) {
		if (level == null || level.isClientSide || be == null || be.isFrozen())
			return false;
		if (requested == null
			|| !MaterialSafety.isAccepted(MaterialSafety.normalize(requested)))
			return false;
		BlockState sanitized = MaterialSafety.sanitize(requested);
		BlockState old = be.getMaterial();
		if (sameBaseMaterial(old, sanitized)) {
			be.setMaterial(sanitized); // free re-axis, ledger untouched
			return true;
		}
		int required = ContourConfig.consumeMaterial() ? be.hostUnits() : 0;
		boolean creative = player != null && player.getAbilities().instabuild;
		if (required > 0 && player == null)
			return false; // automation may not mint free materials
		Item matItem = sanitized.getBlock().asItem();
		Reservation res = null;
		if (required > 0 && !creative) {
			if (matItem.getDefaultInstance().isEmpty())
				return false;
			res = reserve(player, matItem, required);
			if (res == null)
				return false;
		}
		int oldPaid = be.getPaidUnits();
		BlockState oldMaterial = old;
		// Single atomic write + refresh, never partial-field notify.
		be.applyPayload(sanitized, required > 0 && !creative ? required : 0,
			be.getFacing(), be.isTop());
		if (oldPaid > 0 && !MaterialSafety.isNeutral(oldMaterial)
			&& player != null && !creative) {
			ItemStack refund = new ItemStack(oldMaterial.getBlock().asItem(), oldPaid);
			if (!player.getInventory().add(refund))
				player.drop(refund, false);
		}
		return true;
	}

	/**
	 * Slab-merge accounting: preserve the old paid ledger and add the
	 * incoming item's paid unit (or the freshly reserved cost), capped at
	 * host units. Server-side, after a successful merge only.
	 */
	public static void onSlabMerge(ContourBlockEntity be, int incomingPaid) {
		if (be == null || be.isFrozen())
			return;
		be.setPaidUnits(Math.min(be.hostUnits(),
			be.getPaidUnits() + Math.max(0, Math.min(1, incomingPaid))));
	}
}
