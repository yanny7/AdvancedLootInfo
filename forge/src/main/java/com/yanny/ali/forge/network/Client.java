package com.yanny.ali.forge.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onLootInfo(msg);
        }

        contextSupplier.setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, CustomPayloadEvent.Context contextSupplier) {
        if (contextSupplier.getDirection().getReceptionSide().isClient()) {
            super.onClear(msg);
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
