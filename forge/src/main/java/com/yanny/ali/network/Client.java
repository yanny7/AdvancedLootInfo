package com.yanny.ali.network;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Client extends AbstractClient {
    public void onLootInfo(InfoSyncLootTableMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        super.onLootInfo(msg);
        contextSupplier.get().setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        super.onClear(msg);
        contextSupplier.get().setPacketHandled(true);
    }
}
