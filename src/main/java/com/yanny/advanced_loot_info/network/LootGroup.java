package com.yanny.advanced_loot_info.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedList;
import java.util.List;

public class LootGroup extends LootEntry {
    private final List<LootEntry> entries;

    public LootGroup(List<LootEntry> entries, List<LootFunction> functions, List<LootCondition> conditions) {
        super(functions, conditions);
        this.entries = entries;
    }

    public LootGroup(FriendlyByteBuf buf) {
        super(buf);
        int count = buf.readInt();
        List<LootEntry> entries = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            EntryType entryType = buf.readEnum(EntryType.class);

            switch (entryType) {
                case GROUP -> entries.add(new LootGroup(buf));
                case INFO -> entries.add(new LootInfo(buf));
                case POOL -> entries.add(new LootPoolEntry(buf));
            }
        }

        this.entries = entries;
    }

    public List<LootEntry> entries() {
        return entries;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeInt(entries.size());

        for (LootEntry entry : entries) {
            buf.writeEnum(entry.getType());
            entry.encode(buf);
        }
    }

    @Override
    public EntryType getType() {
        return EntryType.GROUP;
    }

    @Override
    public String toString() {
        return entries().toString();
    }
}
