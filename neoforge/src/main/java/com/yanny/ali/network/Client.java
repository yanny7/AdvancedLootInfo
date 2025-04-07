package com.yanny.ali.network;


import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Client extends AbstractClient {
    public void onLootInfo(InfoSyncLootTableMessage msg, IPayloadContext contextSupplier) {
        super.onLootInfo(msg);
    }

    public void onClear(ClearMessage msg, IPayloadContext contextSupplier) {
        super.onClear(msg);
    }
}
