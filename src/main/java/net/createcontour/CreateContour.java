package net.createcontour;

import net.createcontour.config.ContourConfig;
import net.createcontour.registry.ContourRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * Create: Contour - copycat-style shape blocks for Create.
 *
 * Common-side bootstrap: registers the three first shapes, their block
 * entity type, creative tab and the SERVER-authoritative config.
 * Client-side model wrappers and QoL gestures land in later batches.
 */
@Mod(CreateContour.MOD_ID)
public final class CreateContour {

	public static final String MOD_ID = "create_contour";

	public CreateContour(IEventBus modEventBus, ModContainer container) {
		ContourRegistries.register(modEventBus);
		container.registerConfig(ModConfig.Type.SERVER, ContourConfig.SERVER_SPEC);
		modEventBus.addListener((net.neoforged.neoforge.event.RegisterGameTestsEvent e) ->
			e.register(net.createcontour.gametest.ContourGameTests.class));
	}
}
