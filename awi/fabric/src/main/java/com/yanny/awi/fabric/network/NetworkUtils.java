package com.yanny.awi.fabric.network;

import com.yanny.awi.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class NetworkUtils {
    public static final ResourceLocation WORLDGEN_DATA_CHUNK_ID = Utils.modLoc("worldgen_data_chunk");
    public static final ResourceLocation START_WORLDGEN_INFO_ID = Utils.modLoc("start_worldgen_info");
    public static final ResourceLocation DONE_WORLDGEN_INFO_ID = Utils.modLoc("done_worldgen_info");
    public static final ResourceLocation REQUEST_WORLDGEN_DATA_ID = Utils.modLoc("request_worldgen_data");

    public static void registerClient(Client client) {
        ClientPlayNetworking.registerGlobalReceiver(WORLDGEN_DATA_CHUNK_ID, client::onWorldgenDataChunk);
        ClientPlayNetworking.registerGlobalReceiver(START_WORLDGEN_INFO_ID, client::onStart);
        ClientPlayNetworking.registerGlobalReceiver(DONE_WORLDGEN_INFO_ID, client::onDone);
    }

    public static void registerCommon(Server server) {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_WORLDGEN_DATA_ID, server::onStartSendingWorldgenData);
    }
}
