package com.yanny.ali.network;

import net.minecraft.network.FriendlyByteBuf;

public class StartMessage {
    public final int totalMessages;

    public StartMessage(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public StartMessage(FriendlyByteBuf buf) {
        totalMessages = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(totalMessages);
    }
}
