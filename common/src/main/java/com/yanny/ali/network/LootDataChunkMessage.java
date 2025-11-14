package com.yanny.ali.network;

import net.minecraft.network.FriendlyByteBuf;

public record LootDataChunkMessage(int index, byte[] data) {
    public LootDataChunkMessage(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readByteArray());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
        buf.writeByteArray(data);
    }
}
