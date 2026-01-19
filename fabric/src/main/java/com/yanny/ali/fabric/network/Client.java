package com.yanny.ali.fabric.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class Client extends AbstractClient {
    public void onLootDataChunk(Minecraft ignoredClient, ClientPacketListener ignoredHandler, FriendlyByteBuf buf, PacketSender ignoredResponseSender) {
        super.onLootDataChunk(new LootDataChunkMessage(buf));
    }

    public void onStart(Minecraft ignoredClient, ClientPacketListener ignoredHandler, FriendlyByteBuf buf, PacketSender ignoredResponseSender) {
        super.onStart(new StartMessage(buf));
    }

    public void onDone(Minecraft ignoredClient, ClientPacketListener ignoredHandler, FriendlyByteBuf buf, PacketSender ignoredResponseSender) {
        super.onDone(new DoneMessage(buf));
    }
}
