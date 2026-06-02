package com.yanny.awi.network;

import net.minecraft.network.FriendlyByteBuf;

public record WorldgenDataChunkMessage(int index, byte[] data) {
    public WorldgenDataChunkMessage(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readByteArray());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
        buf.writeByteArray(data);
    }
}
