package net.createcontour.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server codex selection request. `shape` is the shape ITEM id;
 * `material` is a block id, or `minecraft:air` for a blank (unpainted)
 * template. Server-side validation owns the outcome - a client can only
 * ever obtain TEMPLATE stacks through this path.
 */
public record CodexSelectPayload(ResourceLocation shape, ResourceLocation material)
	implements CustomPacketPayload {

	public static final ResourceLocation NO_MATERIAL =
		ResourceLocation.fromNamespaceAndPath("minecraft", "air");

	public static final Type<CodexSelectPayload> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath("create_contour", "codex_select"));

	public static final StreamCodec<RegistryFriendlyByteBuf, CodexSelectPayload>
		STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, CodexSelectPayload::shape,
			ResourceLocation.STREAM_CODEC, CodexSelectPayload::material,
			CodexSelectPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
