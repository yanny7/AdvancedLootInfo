package com.yanny.ali.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class NetworkUtils {
    public static DistHolder<AbstractClient, AbstractServer> registerLootInfoPropagator() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return registerClientLootInfoPropagator();
        } else {
            return registerServerLootInfoPropagator();
        }
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerClientLootInfoPropagator() {
        Client client = new Client();
        Server server = new Server();

        PayloadTypeRegistry.playS2C().register(LootDataChunkMessage.TYPE, LootDataChunkMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearMessage.TYPE, ClearMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(DoneMessage.TYPE, DoneMessage.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(LootDataChunkMessage.TYPE, client::onLootInfo);
        ClientPlayNetworking.registerGlobalReceiver(ClearMessage.TYPE, client::onClear);
        ClientPlayNetworking.registerGlobalReceiver(DoneMessage.TYPE, client::onDone);
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerServerLootInfoPropagator() {
        Server server = new Server();

        PayloadTypeRegistry.playS2C().register(LootDataChunkMessage.TYPE, LootDataChunkMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearMessage.TYPE, ClearMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(DoneMessage.TYPE, DoneMessage.CODEC);
        return new DistHolder<>(null, server);
    }
}
