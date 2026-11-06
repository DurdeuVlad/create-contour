package net.createcontour.network;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server sign line update. Server-side validation owns the
 * outcome: distance, BE type and per-line length are all re-checked;
 * a client can only write text to a sign it is actually near.
 */
public record SignTextPayload(BlockPos pos, List<String> lines)
	implements CustomPacketPayload {

	public static final Type<SignTextPayload> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath("create_contour", "sign_text"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SignTextPayload>
		STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SignTextPayload::pos,
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(4)),
			SignTextPayload::lines,
			SignTextPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
