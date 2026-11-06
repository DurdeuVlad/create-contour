package net.createcontour.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * SERVER-authoritative config: both values affect shared state
 * (paid-material accounting and transform semantics), so the server spec
 * keeps all clients consistent.
 */
public final class ContourConfig {

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec.BooleanValue CONSUME_MATERIAL = BUILDER
		.comment("When true, first material application consumes one matching",
			"material item per host shape unit (survival).")
		.define("consumeMaterial", true);

	public static final ModConfigSpec.BooleanValue ROTATE_TEXTURES = BUILDER
		.comment("When true, painted material block-state axes rotate with",
			"structure transforms (Create contraption rotate/mirror).")
		.define("rotateTextures", true);

	public static final ModConfigSpec SERVER_SPEC = BUILDER.build();

	public static boolean consumeMaterial() {
		return CONSUME_MATERIAL.get();
	}

	public static boolean rotateTextures() {
		return ROTATE_TEXTURES.get();
	}

	private ContourConfig() {
	}
}
