package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client();

        channel.messageBuilder(LootDataChunkMessage.class, getMessageId())
                .encoder(LootDataChunkMessage::encode)
                .decoder(LootDataChunkMessage::new)
                .consumerNetworkThread(client::onLootDataChunk)
                .add();
        channel.messageBuilder(StartMessage.class, getMessageId())
                .encoder(StartMessage::encode)
                .decoder(StartMessage::new)
                .consumerNetworkThread(client::onStart)
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
