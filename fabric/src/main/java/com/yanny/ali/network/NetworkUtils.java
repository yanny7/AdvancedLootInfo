package com.yanny.ali.network;

import com.yanny.ali.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NetworkUtils {
    public static final ResourceLocation NEW_LOOT_INFO_ID = Utils.modLoc("new_loot_info");
    public static final ResourceLocation CLEAR_LOOT_INFO_ID = Utils.modLoc("clear_loot_info");

    public static DistHolder<Client, Server> registerLootInfoPropagator() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return registerClientLootInfoPropagator();
        } else {
            return registerServerLootInfoPropagator();
        }
    }

    @NotNull
    private static DistHolder<Client, Server> registerClientLootInfoPropagator() {
        Client client = new Client();
        Server server = new Server();

        ClientPlayNetworking.registerGlobalReceiver(NEW_LOOT_INFO_ID, client::onLootInfo);
        ClientPlayNetworking.registerGlobalReceiver(CLEAR_LOOT_INFO_ID, client::onClear);
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<Client, Server> registerServerLootInfoPropagator() {
        Server server = new Server();
        return new DistHolder<>(null, server);
    }
}
