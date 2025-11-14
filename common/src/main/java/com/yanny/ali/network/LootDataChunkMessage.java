package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record LootDataChunkMessage(int index, byte[] data) implements CustomPacketPayload {
    public static final ResourceLocation ID = Utils.modLoc("loot_table_sync");

    public LootDataChunkMessage(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(index);
        buf.writeByteArray(data);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
