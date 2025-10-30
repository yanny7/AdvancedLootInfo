package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ClearMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_clear");

    public final int totalMessages;

    public ClearMessage(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public ClearMessage(FriendlyByteBuf buf) {
        totalMessages = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(totalMessages);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
