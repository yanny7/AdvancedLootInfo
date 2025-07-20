package com.yanny.ali.network;


import net.neoforged.neoforge.network.NetworkEvent;

public class Client extends AbstractClient {
    public void onLootInfo(SyncLootTableMessage msg, NetworkEvent.Context contextSupplier) {
        super.onLootInfo(msg);
        contextSupplier.setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, NetworkEvent.Context contextSupplier) {
        super.onClear(msg);
        contextSupplier.setPacketHandled(true);
    }

    public void onDone(DoneMessage msg, NetworkEvent.Context contextSupplier) {
        super.onDone(msg);
        contextSupplier.setPacketHandled(true);
    }
}
