package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class Client extends AbstractClient {
    public void onLootInfo(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        super.onLootInfo(new LootDataChunkMessage(buf));
    }

    public void onClear(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        super.onClear(new ClearMessage(buf));
    }

    public void onDone(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        super.onDone(new DoneMessage(buf));
    }
}
