package com.yanny.ali.network;


import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public class Client extends AbstractClient {
    public void onLootInfo(InfoSyncLootTableMessage msg, IPayloadContext contextSupplier) {
        super.onLootInfo(msg, ((ClientPacketListener) Objects.requireNonNull(contextSupplier.connection().getPacketListener())).registryAccess());
    }

    public void onClear(ClearMessage msg, IPayloadContext contextSupplier) {
        super.onClear(msg);
    }
}
