package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class LootTableEntry extends LootGroup {
    public LootTableEntry(List<LootPoolEntry> entries, List<ILootFunction> functions, float chance, int quality) {
            super(GroupType.ALL, entries, functions, List.of(), chance, quality);
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
