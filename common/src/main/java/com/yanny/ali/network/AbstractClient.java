package com.yanny.ali.network;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractClient {
    public final List<InfoSyncLootTableMessage> lootEntries = new LinkedList<>();

    protected void onLootInfo(InfoSyncLootTableMessage msg) {
        lootEntries.add(msg);
    }

    protected void onClear(ClearMessage msg) {
        lootEntries.clear();
    }
}
