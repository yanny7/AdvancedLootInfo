package com.yanny.ali.network;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Objects;

public class Client extends AbstractClient {
    public void onLootInfo(InfoSyncLootTableMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onLootInfo(msg, ((ClientPacketListener) Objects.requireNonNull(contextSupplier.getConnection().getPacketListener())).registryAccess());
        contextSupplier.setPacketHandled(true);
    }

    public void onClear(ClearMessage msg, CustomPayloadEvent.Context contextSupplier) {
        super.onClear(msg);
        contextSupplier.setPacketHandled(true);
    }
}
