package com.yanny.ali.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public class NetworkUtils {
    public static DistHolder<AbstractClient, AbstractServer> registerLootInfoPropagator(PayloadRegistrar registrar) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return registerClientLootInfoPropagator(registrar);
        } else {
            return registerServerLootInfoPropagator(registrar);
        }
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerClientLootInfoPropagator(PayloadRegistrar registrar) {
        Client client = new Client();
        Server server = new Server();

        registrar.executesOn(HandlerThread.NETWORK).playToClient(InfoSyncLootTableMessage.TYPE, InfoSyncLootTableMessage.CODEC, client::onLootInfo);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(ClearMessage.TYPE, ClearMessage.CODEC, client::onClear);
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerServerLootInfoPropagator(PayloadRegistrar registrar) {
        Server server = new Server();

        registrar.playToClient(InfoSyncLootTableMessage.TYPE, InfoSyncLootTableMessage.CODEC, (a, b) -> {});
        registrar.playToClient(ClearMessage.TYPE, ClearMessage.CODEC, (a, b) -> {});
        return new DistHolder<>(null, server);
    }
}
