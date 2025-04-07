package com.yanny.ali.network;

import net.minecraftforge.event.network.CustomPayloadEvent;

public class Client extends AbstractClient {
    public void onLootInfo(InfoSyncLootTableMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onLootInfo(msg);
        contextSupplier.setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onClear(msg);
        contextSupplier.setPacketHandled(true);
    }
}
