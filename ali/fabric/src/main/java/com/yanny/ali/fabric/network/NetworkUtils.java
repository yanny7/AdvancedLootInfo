package com.yanny.ali.fabric.network;

import com.yanny.ali.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class NetworkUtils {
    public static final ResourceLocation LOOT_DATA_CHUNK_ID = Utils.modLoc("loot_data_chunk");
    public static final ResourceLocation START_LOOT_INFO_ID = Utils.modLoc("start_loot_info");
    public static final ResourceLocation DONE_LOOT_INFO_ID = Utils.modLoc("done_loot_info");
    public static final ResourceLocation REQUEST_LOOT_DATA_ID = Utils.modLoc("request_loot_data");

    public static void registerClient(Client client) {
        ClientPlayNetworking.registerGlobalReceiver(LOOT_DATA_CHUNK_ID, client::onLootDataChunk);
        ClientPlayNetworking.registerGlobalReceiver(START_LOOT_INFO_ID, client::onStart);
        ClientPlayNetworking.registerGlobalReceiver(DONE_LOOT_INFO_ID, client::onDone);
    }

    public static void registerCommon(Server server) {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_LOOT_DATA_ID, server::onStartSendingLootData);
    }
}
