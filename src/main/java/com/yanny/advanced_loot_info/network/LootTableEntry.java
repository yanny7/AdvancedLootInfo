package com.yanny.advanced_loot_info.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class LootTableEntry extends LootGroup {
    public LootTableEntry(List<LootPoolEntry> entries, List<LootFunction> functions) {
        super(GroupType.GROUP, entries, functions, List.of());
    }

    public LootTableEntry(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
    }

    @Override
    public List<? extends LootEntry> entries() {
        return super.entries();
    }

    @Override
    public EntryType getType() {
        return EntryType.TABLE;
    }
}
