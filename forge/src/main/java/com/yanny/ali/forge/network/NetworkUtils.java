package com.yanny.ali.forge.network;

import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
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
                .consumerNetworkThread((BiConsumer<LootDataChunkMessage, CustomPayloadEvent.Context>) client::onLootDataChunk)
                .add();
        channel.messageBuilder(StartMessage.class, getMessageId())
                .encoder(StartMessage::encode)
                .decoder(StartMessage::new)
                .consumerNetworkThread((BiConsumer<StartMessage, CustomPayloadEvent.Context>) client::onStart)
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
