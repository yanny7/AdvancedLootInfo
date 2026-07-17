package com.yanny.awi.network;

import com.yanny.awi.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record WorldgenDataChunkMessage(int index, byte[] data) implements CustomPacketPayload {
    public static final Type<WorldgenDataChunkMessage> TYPE = new Type<>(Utils.modLoc("worldgen_data_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorldgenDataChunkMessage> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            WorldgenDataChunkMessage::index,
            ByteBufCodecs.BYTE_ARRAY,
            WorldgenDataChunkMessage::data,
            WorldgenDataChunkMessage::new
    );

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
