package net.createcontour.client;

import net.createcontour.item.ContourBlockItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Item-side resolve: reads the full painted BlockState from the held
 * item's contour payload and returns a short-lived bound model for that
 * material (no static cache, no custom packets). A blank item resolves to
 * the neutral bound model unchanged.
 */
public class ContourItemOverrides extends ItemOverrides {

	@FunctionalInterface
	public interface BoundModelFactory {
		BakedModel create(BakedModel original, BlockState boundState,
			BlockState boundMaterial);
	}

	private final BakedModel original;
	private final BlockState boundState;
	private final BoundModelFactory factory;

	public ContourItemOverrides(BakedModel original, BlockState boundState,
		BoundModelFactory factory) {
		this.original = original;
		this.boundState = boundState;
		this.factory = factory;
	}

	@Override
	public BakedModel resolve(BakedModel model, ItemStack stack,
		@Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		HolderLookup.Provider provider = level == null ? null : level.registryAccess();
		if (provider == null || !(model instanceof ContourModel))
			return model;
		BlockState material = ContourBlockItem.storedMaterial(provider, stack);
		if (material == null)
			return model;
		return factory.create(original, boundState, material);
	}
}
