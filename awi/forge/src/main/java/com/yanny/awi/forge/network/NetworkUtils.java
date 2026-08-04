package com.yanny.awi.forge.network;

import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client(channel);

        channel.registerMessage(getMessageId(), WorldgenDataChunkMessage.class, WorldgenDataChunkMessage::encode, WorldgenDataChunkMessage::new, client::onWorldgenDataChunk);
        channel.registerMessage(getMessageId(), StartMessage.class, StartMessage::encode, StartMessage::new, client::onStart);
        channel.registerMessage(getMessageId(), DoneMessage.class, DoneMessage::encode, DoneMessage::new, client::onDone);
    }

    public static void registerCommon(SimpleChannel channel, Server server) {
        channel.registerMessage(getMessageId(), RequestWorldgenDataMessage.class, RequestWorldgenDataMessage::encode, RequestWorldgenDataMessage::new, server::onStartSendingWorldgenData);
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
