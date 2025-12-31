package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class NetworkUtils {
    public static final ResourceLocation NEW_LOOT_INFO_ID = Utils.modLoc("new_loot_info");
    public static final ResourceLocation CLEAR_LOOT_INFO_ID = Utils.modLoc("clear_loot_info");
    public static final ResourceLocation DONE_LOOT_INFO_ID = Utils.modLoc("done_loot_info");

    public static void registerClient() {
        Client client = new Client();

        ClientPlayNetworking.registerGlobalReceiver(NEW_LOOT_INFO_ID, client::onLootInfo);
        ClientPlayNetworking.registerGlobalReceiver(CLEAR_LOOT_INFO_ID, client::onClear);
        ClientPlayNetworking.registerGlobalReceiver(DONE_LOOT_INFO_ID, client::onDone);
    }
}
