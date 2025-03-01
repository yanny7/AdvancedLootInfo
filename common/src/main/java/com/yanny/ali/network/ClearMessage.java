package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ClearMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_clear");

    public ClearMessage() {
    }

    public ClearMessage(FriendlyByteBuf buf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
