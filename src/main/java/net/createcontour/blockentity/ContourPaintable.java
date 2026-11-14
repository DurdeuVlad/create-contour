package net.createcontour.blockentity;

import net.createcontour.material.MaterialSafety;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The owned material + paid-ledger surface shared by every Contour host
 * block entity. {@link ContourBlockEntity} (copycat family) and kinetic
 * hosts ({@link ContourCogwheelBlockEntity}) both implement it, so
 * MaterialPolicy billing and ContourBlockItem placement/paint paths treat
 * them identically. All mutators must refuse work while the host is
 * frozen on an unknown future payload.
 */
public interface ContourPaintable {

	/** Sanitized stored material; the neutral sentinel when unpainted. */
	BlockState getMaterial();

	default boolean hasCustomMaterial() {
		return !MaterialSafety.isNeutral(getMaterial());
	}

	void setMaterial(BlockState material);

	/** Paid ledger units (0..hostUnits), zeroed when material is neutral. */
	int getPaidUnits();

	void setPaidUnits(int units);

	/** Host shape units this BE's single paint surface represents. */
	int hostUnits();

	/**
	 * Material items billed for painting this host (ADR-019): defaults
	 * to {@link #hostUnits()} - a 1:1 charge per host unit. Resizable
	 * shapes (contour_layers) override toward whole-item fractions.
	 */
	default int materialUnits() {
		return hostUnits();
	}

	/** Canonical facing used for item-payload round trips. */
	Direction getFacing();

	/** Canonical top flag used for item-payload round trips. */
	boolean isTop();

	/** True while a preserved unknown-future payload blocks all mutation. */
	boolean isFrozen();

	void setCanonical(Direction facing, boolean top);

	/**
	 * Atomic write of material + paid + canonical orientation in one
	 * settle, followed by sync/light/model refresh. Refused when frozen.
	 */
	void applyPayload(BlockState material, int paidUnits, Direction facing,
		boolean top);
}
