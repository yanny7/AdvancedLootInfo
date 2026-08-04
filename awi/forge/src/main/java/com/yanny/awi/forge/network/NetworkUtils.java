package com.yanny.awi.forge.network;

import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.BiConsumer;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client(channel);

        //noinspection unchecked
        channel.messageBuilder(WorldgenDataChunkMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, WorldgenDataChunkMessage>) (Object) WorldgenDataChunkMessage.CODEC)
                .consumerNetworkThread((BiConsumer<WorldgenDataChunkMessage, CustomPayloadEvent.Context>) client::onWorldgenDataChunk)
                .add();
        //noinspection unchecked
        channel.messageBuilder(StartMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, StartMessage>) (Object) StartMessage.CODEC)
                .consumerNetworkThread((BiConsumer<StartMessage, CustomPayloadEvent.Context>) client::onStart)
                .add();
        //noinspection unchecked
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, DoneMessage>) (Object) DoneMessage.CODEC)
                .consumerNetworkThread((BiConsumer<DoneMessage, CustomPayloadEvent.Context>) client::onDone)
                .add();
    }

    public static void registerCommon(SimpleChannel channel, Server server) {
        //noinspection unchecked
        channel.messageBuilder(RequestWorldgenDataMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, RequestWorldgenDataMessage>) (Object) RequestWorldgenDataMessage.CODEC)
                .consumerNetworkThread((BiConsumer<RequestWorldgenDataMessage, CustomPayloadEvent.Context>) server::onStartSendingWorldgenData)
                .add();
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
