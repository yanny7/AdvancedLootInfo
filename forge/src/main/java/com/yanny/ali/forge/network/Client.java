package com.yanny.ali.forge.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class Client extends AbstractClient {
    public void onLootDataChunk(LootDataChunkMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onLootDataChunk(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    public void onStart(StartMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onStart(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    protected void onDone(DoneMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onDone(msg);
        }

        contextSupplier.setPacketHandled(true);
    }
}
