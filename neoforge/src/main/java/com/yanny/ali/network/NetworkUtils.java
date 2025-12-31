package com.yanny.ali.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.jetbrains.annotations.NotNull;

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
