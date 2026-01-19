package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class StartMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_start");

    public final int totalMessages;

    public StartMessage(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public StartMessage(FriendlyByteBuf buf) {
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
