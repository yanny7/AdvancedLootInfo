package com.yanny.ali.network;


import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage msg, IPayloadContext contextSupplier) {
        super.onLootInfo(msg);
    }

    public void onClear(ClearMessage msg, IPayloadContext contextSupplier) {
        super.onClear(msg);
    }

    public void onDone(DoneMessage msg, IPayloadContext contextSupplier) {
        super.onDone(msg);
    }
}
