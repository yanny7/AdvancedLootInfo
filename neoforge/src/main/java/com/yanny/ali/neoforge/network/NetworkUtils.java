package com.yanny.ali.neoforge.network;

import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkUtils {
    public static void registerClient(PayloadRegistrar registrar) {
        Client client = new Client();

        registrar.executesOn(HandlerThread.NETWORK).playToClient(LootDataChunkMessage.TYPE, LootDataChunkMessage.CODEC, client::onLootInfo);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(ClearMessage.TYPE, ClearMessage.CODEC, client::onClear);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(DoneMessage.TYPE, DoneMessage.CODEC, client::onDone);
    }
}
