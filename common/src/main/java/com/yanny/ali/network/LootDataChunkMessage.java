package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record LootDataChunkMessage(int index, byte[] data) implements CustomPacketPayload {
    public static final Type<LootDataChunkMessage> TYPE = new Type<>(Utils.modLoc("loot_chunk_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootDataChunkMessage> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            LootDataChunkMessage::index,
            ByteBufCodecs.BYTE_ARRAY,
            LootDataChunkMessage::data,
            LootDataChunkMessage::new
    );

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
