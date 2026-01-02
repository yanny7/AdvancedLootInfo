package com.yanny.ali.neoforge.network;


import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.neoforged.neoforge.network.NetworkEvent;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage msg, NetworkEvent.Context contextSupplier) {
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
