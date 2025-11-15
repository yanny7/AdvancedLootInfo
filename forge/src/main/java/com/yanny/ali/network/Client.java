package com.yanny.ali.network;

import net.minecraftforge.event.network.CustomPayloadEvent;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onLootInfo(msg);
        contextSupplier.setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onClear(msg);
        contextSupplier.setPacketHandled(true);
    }

    protected void onDone(DoneMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onDone(msg);
        contextSupplier.setPacketHandled(true);
    }
}
