package com.yanny.ali.neoforge.network;


import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Client extends AbstractClient {
    public void onLootDataChunk(LootDataChunkMessage msg, IPayloadContext contextSupplier) {
        super.onLootDataChunk(msg);
    }

    public void onStart(StartMessage msg, IPayloadContext contextSupplier) {
        super.onStart(msg);
    }

    public void onDone(DoneMessage msg, IPayloadContext contextSupplier) {
        super.onDone(msg);
    }
}
