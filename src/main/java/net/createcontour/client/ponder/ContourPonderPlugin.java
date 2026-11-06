package net.createcontour.client.ponder;

import net.createcontour.CreateContour;
import net.createcontour.registry.ContourRegistries;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * Ponder plugin: registers Contour tutorial scenes against the items they
 * teach. Client-side only; discovered through PonderIndex in
 * ContourClient#onClientSetup.
 */
public class ContourPonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return CreateContour.MOD_ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		helper.addStoryBoard(ContourRegistries.CONTOUR_PLACER.getId(), "placer",
			ContourPonderScenes::placer);
		helper.addStoryBoard(ContourRegistries.CONTOUR_BLANK_ITEM.getId(), "placer",
			ContourPonderScenes::placer);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
	}

	@Override
	public void registerSharedText(SharedTextRegistrationHelper helper) {
	}
}
