package net.createcontour.client;

import net.minecraft.client.Minecraft;

/**
 * Client-side entry points for codex UX. Referenced from the common item
 * class only inside a `level.isClientSide && FMLEnvironment.dist ==
 * Dist.CLIENT` guard, so this class is never initialized on dedicated
 * servers (no DistExecutor exists on NeoForge 21.1).
 */
public final class CodexClient {

	private CodexClient() {
	}

	public static void openCodexScreen() {
		Minecraft.getInstance().setScreen(new ContourCodexScreen());
	}
}
