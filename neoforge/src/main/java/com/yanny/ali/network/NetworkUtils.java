package com.yanny.ali.network;

import net.neoforged.neoforge.network.simple.SimpleChannel;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client();

        channel.messageBuilder(LootDataChunkMessage.class, getMessageId())
                .encoder(LootDataChunkMessage::encode)
                .decoder(LootDataChunkMessage::new)
                .consumerNetworkThread(client::onLootInfo)
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
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
