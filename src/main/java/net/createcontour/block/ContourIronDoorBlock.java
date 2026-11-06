package net.createcontour.block;

/**
 * Iron door variant: identical geometry, pairing and ledger semantics
 * to the wooden door, but hands cannot open it - redstone signal only.
 * The wrench still cycles FACING so a closed leaf can be re-hinged.
 */
public class ContourIronDoorBlock extends ContourDoorBlock {

	public ContourIronDoorBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected boolean canManualOpen() {
		return false;
	}
}
