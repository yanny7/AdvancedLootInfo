package com.yanny.ali.network;

import net.minecraft.network.FriendlyByteBuf;

public class ClearMessage {
    public final int totalMessages;

    public ClearMessage(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public ClearMessage(FriendlyByteBuf buf) {
        totalMessages = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(totalMessages);
    }
}
