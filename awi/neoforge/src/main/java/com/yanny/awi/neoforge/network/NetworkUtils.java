package com.yanny.awi.neoforge.network;

import com.yanny.awi.network.DoneMessage;
import com.yanny.awi.network.RequestWorldgenDataMessage;
import com.yanny.awi.network.StartMessage;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkUtils {
    public static void registerClient(PayloadRegistrar registrar, Client client) {
        registrar.executesOn(HandlerThread.NETWORK).playToClient(WorldgenDataChunkMessage.TYPE, WorldgenDataChunkMessage.CODEC, client::onWorldgenDataChunk);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(StartMessage.TYPE, StartMessage.CODEC, client::onStart);
        registrar.executesOn(HandlerThread.NETWORK).playToClient(DoneMessage.TYPE, DoneMessage.CODEC, client::onDone);
    }

    public static void registerCommon(PayloadRegistrar registrar, Server server) {
        registrar.executesOn(HandlerThread.NETWORK).playToServer(RequestWorldgenDataMessage.TYPE, RequestWorldgenDataMessage.CODEC, server::onStartSendingWorldgenData);
    }
}
