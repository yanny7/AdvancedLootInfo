package com.yanny.ali.forge.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.ClearMessage;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Client extends AbstractClient {
    public void onLootInfo(LootDataChunkMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onLootInfo(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onClear(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }

    protected void onDone(DoneMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onDone(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }
}
