package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkUtils {
    public static void registerClient(PayloadRegistrar registrar) {
        Client client = new Client();

        registrar.executesOn(HandlerThread.NETWORK).playToClient(LootDataChunkMessage.TYPE, LootDataChunkMessage.CODEC, client::onLootDataChunk);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(StartMessage.TYPE, StartMessage.CODEC, client::onStart);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(DoneMessage.TYPE, DoneMessage.CODEC, client::onDone);
    }
}
