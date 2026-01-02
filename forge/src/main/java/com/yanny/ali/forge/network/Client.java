package com.yanny.ali.forge.network;

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
