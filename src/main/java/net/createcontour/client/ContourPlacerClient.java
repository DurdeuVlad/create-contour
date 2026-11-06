package net.createcontour.client;

import net.minecraft.client.Minecraft;

/**
 * Client-side entry points for the placer UX. Referenced from the
 * common item class only inside a `level.isClientSide &&
 * FMLEnvironment.dist == Dist.CLIENT` guard, so this class is
 * never initialized on dedicated servers.
 */
public final class ContourPlacerClient {

	private ContourPlacerClient() {
	}

	public static void openPlacerScreen() {
		Minecraft.getInstance().setScreen(new ContourPlacerScreen());
	}
}
