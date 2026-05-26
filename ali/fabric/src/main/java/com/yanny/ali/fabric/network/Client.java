package com.yanny.ali.fabric.network;

import com.yanny.ali.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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

    @Override
    public void sendLootDataToPlayer(RequestLootDataMessage message) {
        if (ClientPlayNetworking.canSend(NetworkUtils.REQUEST_LOOT_DATA_ID)) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            message.encode(buf);
            ClientPlayNetworking.send(NetworkUtils.REQUEST_LOOT_DATA_ID, buf);
        }
    }
}
