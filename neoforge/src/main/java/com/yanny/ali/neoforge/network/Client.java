package com.yanny.ali.neoforge.network;


import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.neoforged.neoforge.network.NetworkEvent;

public class Client extends AbstractClient {
    public void onLootDataChunk(LootDataChunkMessage msg, NetworkEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onLootDataChunk(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    public void onStart(StartMessage msg, NetworkEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onStart(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    public void onDone(DoneMessage msg, NetworkEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onDone(msg);
        }

        contextSupplier.setPacketHandled(true);
    }
}
