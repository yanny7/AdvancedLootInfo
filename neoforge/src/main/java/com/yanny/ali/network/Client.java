package com.yanny.ali.network;


import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Client extends AbstractClient {
    public void onLootInfo(SyncLootTableMessage msg, IPayloadContext contextSupplier) {
        super.onLootInfo(msg);
    }

    public void onClear(ClearMessage msg, IPayloadContext contextSupplier) {
        super.onClear(msg);
    }

    public void onDone(DoneMessage msg, PlayPayloadContext contextSupplier) {
        super.onDone(msg);
    }
}
