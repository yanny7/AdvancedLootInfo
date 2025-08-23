package com.yanny.ali.network;


import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class Client extends AbstractClient {
    public void onLootInfo(SyncLootTableMessage msg, PlayPayloadContext contextSupplier) {
        super.onLootInfo(msg);
    }

    public void onTradeInfo(SyncTradeMessage msg, PlayPayloadContext contextSupplier) {
        super.onTradeInfo(msg);
    }

    public void onClear(ClearMessage msg, PlayPayloadContext contextSupplier) {
        super.onClear(msg);
    }

    public void onDone(DoneMessage msg, PlayPayloadContext contextSupplier) {
        super.onDone(msg);
    }
}
