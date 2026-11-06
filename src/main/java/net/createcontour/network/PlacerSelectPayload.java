package net.createcontour.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server placer selection request. `shape` is the shape
 * ITEM id; the server validates it resolves to a ContourBlockItem
 * before writing it onto the held gun stack.
 */
public record PlacerSelectPayload(ResourceLocation shape)
	implements CustomPacketPayload {

	public static final Type<PlacerSelectPayload> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath("create_contour", "placer_select"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlacerSelectPayload>
		STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, PlacerSelectPayload::shape,
			PlacerSelectPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
