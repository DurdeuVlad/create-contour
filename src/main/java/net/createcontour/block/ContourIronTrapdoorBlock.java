package net.createcontour.block;

/**
 * Iron trapdoor variant: identical geometry and ledger semantics to
 * the wooden trapdoor, but hands cannot open it - redstone signal only.
 * The wrench still cycles FACING so a closed plate can be re-hinged.
 */
public class ContourIronTrapdoorBlock extends ContourTrapdoorBlock {

	public ContourIronTrapdoorBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected boolean canManualOpen() {
		return false;
	}
}
