package com.yanny.ali.forge.network;

import com.yanny.ali.network.AbstractClient;
import com.yanny.ali.network.DoneMessage;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.StartMessage;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Client extends AbstractClient {
    public void onLootDataChunk(LootDataChunkMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onLootDataChunk(msg);
        }

        contextSupplier.get().setPacketHandled(true);
    }

    public void onStart(StartMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection().getReceptionSide().isClient()) {
            super.onStart(msg);
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
