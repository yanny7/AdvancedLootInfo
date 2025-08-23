package com.yanny.ali.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

public class NetworkUtils {
    private static int messageId = 0;

    public static DistHolder<AbstractClient, AbstractServer> registerLootInfoPropagator(SimpleChannel channel) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return registerClientLootInfoPropagator(channel);
        } else {
            return registerServerLootInfoPropagator(channel);
        }
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerClientLootInfoPropagator(SimpleChannel channel) {
        Client client = new Client();
        Server server = new Server(channel);

        channel.messageBuilder(SyncLootTableMessage.class, getMessageId())
                .encoder(SyncLootTableMessage::encode)
                .decoder(SyncLootTableMessage::new)
                .consumerNetworkThread(client::onLootInfo)
                .add();
        channel.messageBuilder(SyncTradeMessage.class, getMessageId())
                .encoder(SyncTradeMessage::encode)
                .decoder(SyncTradeMessage::new)
                .consumerNetworkThread(client::onTradeInfo)
                .add();
        channel.messageBuilder(ClearMessage.class, getMessageId())
                .encoder(ClearMessage::encode)
                .decoder(ClearMessage::new)
                .consumerNetworkThread(client::onClear)
                .add();
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .encoder(DoneMessage::encode)
                .decoder(DoneMessage::new)
                .consumerNetworkThread(client::onDone)
                .add();
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerServerLootInfoPropagator(SimpleChannel channel) {
        Server server = new Server(channel);

        channel.messageBuilder(SyncLootTableMessage.class, getMessageId())
                .encoder(SyncLootTableMessage::encode)
                .decoder(SyncLootTableMessage::new)
                .consumerNetworkThread((msg, context) -> {})
                .add();
        channel.messageBuilder(SyncTradeMessage.class, getMessageId())
                .encoder(SyncTradeMessage::encode)
                .decoder(SyncTradeMessage::new)
                .consumerNetworkThread((msg, context) -> {})
                .add();
        channel.messageBuilder(ClearMessage.class, getMessageId())
                .encoder(ClearMessage::encode)
                .decoder(ClearMessage::new)
                .consumerNetworkThread((msg, context) -> {})
                .add();
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .encoder(DoneMessage::encode)
                .decoder(DoneMessage::new)
                .consumerNetworkThread((msg, context) -> {})
                .add();
        return new DistHolder<>(null, server);
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
