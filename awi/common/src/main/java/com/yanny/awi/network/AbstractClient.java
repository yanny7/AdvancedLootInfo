package com.yanny.awi.network;

import com.yanny.awi.manager.PluginManager;

public abstract class AbstractClient {
    public static AbstractClient INSTANCE;

    protected AbstractClient() {
        INSTANCE = this;
    }

    protected void onWorldgenDataChunk(WorldgenDataChunkMessage msg) {
        PluginManager.getInstance().clientRegistry.addChunkData(msg.index(), msg.data());
    }

    protected void onStart(StartMessage msg) {
        PluginManager.getInstance().clientRegistry.startReceivingData(msg.totalMessages);
    }

    protected void onDone(DoneMessage ignoredMsg) {
        PluginManager.getInstance().clientRegistry.doneReceivingData();
    }

    public abstract void sendWorldgenDataToPlayer(RequestWorldgenDataMessage message);
}
