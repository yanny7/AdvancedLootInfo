package com.yanny.ali.neoforge.network;


import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage msg, PlayPayloadContext contextSupplier) {
        super.onLootInfo(msg);
    }

    public void onClear(ClearMessage msg, PlayPayloadContext contextSupplier) {
        super.onClear(msg);
    }

    public void onDone(DoneMessage msg, PlayPayloadContext contextSupplier) {
        super.onDone(msg);
    }
}
