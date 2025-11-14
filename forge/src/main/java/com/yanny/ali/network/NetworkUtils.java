package com.yanny.ali.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

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

        channel.messageBuilder(LootDataChunkMessage.class, getMessageId())
                .encoder(LootDataChunkMessage::write)
                .decoder(LootDataChunkMessage::new)
                .consumerNetworkThread((BiConsumer<LootDataChunkMessage, CustomPayloadEvent.Context>) client::onLootInfo)
                .add();
        channel.messageBuilder(ClearMessage.class, getMessageId())
                .encoder(ClearMessage::write)
                .decoder(ClearMessage::new)
                .consumerNetworkThread((BiConsumer<ClearMessage, CustomPayloadEvent.Context>) client::onClear)
                .add();
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .encoder(DoneMessage::write)
                .decoder(DoneMessage::new)
                .consumerNetworkThread((BiConsumer<DoneMessage, CustomPayloadEvent.Context>) client::onDone)
                .add();
        return new DistHolder<>(client, server);
    }

    @NotNull
    private static DistHolder<AbstractClient, AbstractServer> registerServerLootInfoPropagator(SimpleChannel channel) {
        Server server = new Server(channel);

        channel.messageBuilder(LootDataChunkMessage.class, getMessageId())
                .encoder(LootDataChunkMessage::write)
                .decoder(LootDataChunkMessage::new)
                .add();
        channel.messageBuilder(ClearMessage.class, getMessageId())
                .encoder(ClearMessage::write)
                .decoder(ClearMessage::new)
                .add();
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .encoder(DoneMessage::write)
                .decoder(DoneMessage::new)
                .add();
        return new DistHolder<>(null, server);
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
