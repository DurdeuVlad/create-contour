package net.createcontour.material;

import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Central material validation for Contour shapes. Acceptance rule:
 * a real registered non-air block that is not itself a copycat, carries no
 * block entity, is not a fluid block, and has a non-empty selection outline
 * (getShape) evaluated in a context-free EmptyBlockGetter (world-aware
 * blocks would throw on a null context). Appearance eligibility is NOT
 * physical blocking: torch/leaf-style blocks with an empty collision shape
 * but a real selection outline ARE valid paint. A waterlogged source keeps
 * its material identity via WATERLOGGED=false so it appears non-fluid.
 */
public final class MaterialSafety {

	private MaterialSafety() {
	}

	private static final ResourceLocation NEUTRAL_ID =
		ResourceLocation.fromNamespaceAndPath("create", "copycat_base");

	public static BlockState neutral() {
		return BuiltInRegistries.BLOCK.get(NEUTRAL_ID).defaultBlockState();
	}

	public static boolean isNeutral(BlockState state) {
		return state == null
			|| state.getBlock() == BuiltInRegistries.BLOCK.get(NEUTRAL_ID);
	}

	public static boolean isAccepted(BlockState state) {
		if (state == null || state.isAir())
			return false;
		if (BuiltInRegistries.BLOCK.getKey(state.getBlock())
			.equals(BuiltInRegistries.BLOCK.getDefaultKey()))
			return false;
		if (state.getBlock() instanceof CopycatBlock)
			return false;
		if (state.getBlock() instanceof EntityBlock)
			return false;
		if (state.getBlock() instanceof LiquidBlock
			|| state.getFluidState().getType() instanceof FlowingFluid)
			return false;
		try {
			// Selection outline (visual), not physical collision: blocks
			// like leaves/torch collide empty but paint visually.
			if (state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
				.isEmpty())
				return false;
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}

	/**
	 * Normalize a sampled/parsed material: clears WATERLOGGED so wet
	 * sources read as their dry appearance, then validates.
	 */
	public static BlockState normalize(BlockState state) {
		if (state == null)
			return neutral();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED))
			state = state.setValue(BlockStateProperties.WATERLOGGED, false);
		return state;
	}

	/** Rejected/invalid candidates normalize to the neutral sentinel. */
	public static BlockState sanitize(BlockState state) {
		state = normalize(state);
		return isAccepted(state) ? state : neutral();
	}
}
