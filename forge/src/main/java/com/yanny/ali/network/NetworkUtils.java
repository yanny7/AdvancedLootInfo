package com.yanny.ali.network;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.BiConsumer;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client();

        channel.messageBuilder(LootDataChunkMessage.class, getMessageId())
                .encoder(LootDataChunkMessage::encode)
                .decoder(LootDataChunkMessage::new)
                .consumerNetworkThread((BiConsumer<LootDataChunkMessage, CustomPayloadEvent.Context>) client::onLootInfo)
                .add();
        channel.messageBuilder(ClearMessage.class, getMessageId())
                .encoder(ClearMessage::encode)
                .decoder(ClearMessage::new)
                .consumerNetworkThread((BiConsumer<ClearMessage, CustomPayloadEvent.Context>) client::onClear)
                .add();
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .encoder(DoneMessage::encode)
                .decoder(DoneMessage::new)
                .consumerNetworkThread((BiConsumer<DoneMessage, CustomPayloadEvent.Context>) client::onDone)
                .add();
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
