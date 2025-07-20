package com.yanny.ali.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public class NetworkUtils {
    private static int messageId = 0;

    public static DistHolder<AbstractClient, AbstractServer> registerLootInfoPropagator(IPayloadRegistrar registrar) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return registerClientLootInfoPropagator(registrar);
        } else {
            return registerServerLootInfoPropagator(registrar);
        }
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerClientLootInfoPropagator(IPayloadRegistrar registrar) {
        Client client = new Client();
        Server server = new Server();

        registrar.play(
                SyncLootTableMessage.ID,
                SyncLootTableMessage::new,
                (handler) ->
                        handler.client(client::onLootInfo).server((msg, ctx) -> {})
        );
        registrar.play(
                ClearMessage.ID,
                ClearMessage::new,
                (handler) ->
                        handler.client(client::onClear).server((msg, ctx) -> {})
        );
        registrar.play(
                DoneMessage.ID,
                DoneMessage::new,
                (handler) ->
                        handler.client(client::onDone).server((msg, ctx) -> {})
        );
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerServerLootInfoPropagator(IPayloadRegistrar registrar) {
        Server server = new Server();

        registrar.play(
                SyncLootTableMessage.ID,
                SyncLootTableMessage::new,
                (handler) -> {}
        );
        registrar.play(
                ClearMessage.ID,
                ClearMessage::new,
                (handler) -> {}
        );
        registrar.play(
                DoneMessage.ID,
                DoneMessage::new,
                (handler) -> {}
        );
        return new DistHolder<>(null, server);
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
