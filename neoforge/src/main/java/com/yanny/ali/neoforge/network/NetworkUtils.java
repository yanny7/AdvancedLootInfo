package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class NetworkUtils {
    private static int messageId = 0;

    public static void registerClient(IPayloadRegistrar registrar) {
        Client client = new Client();

        registrar.play(
                LootDataChunkMessage.ID,
                LootDataChunkMessage::new,
                (handler) ->
                        handler.client(client::onLootInfo).server((msg, ctx) -> {})
        );
        registrar.play(
                ClearMessage.ID,
                ClearMessage::new,
                (handler) ->
                        handler.client(client::onClear).server((msg, ctx) -> {})
        );
        registrar.play(
                DoneMessage.ID,
                DoneMessage::new,
                (handler) ->
                        handler.client(client::onDone).server((msg, ctx) -> {})
        );
    }

    private static int getMessageId() {
        return ++messageId;
    }
}
