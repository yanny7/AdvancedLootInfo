package com.yanny.ali.forge.network;

import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.RequestLootDataMessage;
import com.yanny.ali.network.StartMessage;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client(channel);

        channel.registerMessage(getMessageId(), LootDataChunkMessage.class, LootDataChunkMessage::encode, LootDataChunkMessage::new, client::onLootDataChunk);
        channel.registerMessage(getMessageId(), StartMessage.class, StartMessage::encode, StartMessage::new, client::onStart);
        channel.registerMessage(getMessageId(), DoneMessage.class, DoneMessage::encode, DoneMessage::new, client::onDone);
    }

    public static void registerCommon(SimpleChannel channel, Server server) {
        channel.registerMessage(getMessageId(), RequestLootDataMessage.class, RequestLootDataMessage::encode, RequestLootDataMessage::new, server::onStartSendingLootData);
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
