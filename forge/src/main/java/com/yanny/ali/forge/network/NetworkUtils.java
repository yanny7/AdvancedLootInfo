package com.yanny.ali.forge.network;

import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.BiConsumer;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(SimpleChannel channel) {
        Client client = new Client();

        //noinspection unchecked
        channel.messageBuilder(LootDataChunkMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, LootDataChunkMessage>)(Object) LootDataChunkMessage.CODEC)
                .consumerNetworkThread((BiConsumer<LootDataChunkMessage, CustomPayloadEvent.Context>) client::onLootDataChunk)
                .add();
        //noinspection unchecked
        channel.messageBuilder(StartMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, StartMessage>)(Object) StartMessage.CODEC)
                .consumerNetworkThread((BiConsumer<StartMessage, CustomPayloadEvent.Context>) client::onStart)
                .add();
        //noinspection unchecked
        channel.messageBuilder(DoneMessage.class, getMessageId())
                .codec((StreamCodec<FriendlyByteBuf, DoneMessage>)(Object) DoneMessage.CODEC)
                .consumerNetworkThread((BiConsumer<DoneMessage, CustomPayloadEvent.Context>) client::onDone)
                .add();
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
