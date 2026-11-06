package net.createcontour.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Client-side entry points for sign UX. Referenced from the common
 * block class only inside a `level.isClientSide && FMLEnvironment.dist
 * == Dist.CLIENT` guard, so this class is never initialized on
 * dedicated servers.
 */
public final class SignClient {

	private SignClient() {
	}

	public static void openSignEditor(BlockPos pos) {
		Minecraft.getInstance().setScreen(new ContourSignEditScreen(pos));
	}
}
