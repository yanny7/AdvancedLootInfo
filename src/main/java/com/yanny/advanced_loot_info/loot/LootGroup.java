package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedList;
import java.util.List;

public class LootGroup extends LootEntry {
    private final List<? extends LootEntry> entries;
    private final GroupType groupType;
    private final float chance;
    private final int quality;

    public LootGroup(GroupType groupType, List<? extends LootEntry> entries, List<ILootFunction> functions, List<ILootCondition> conditions, float chance, int quality) {
        super(functions, conditions);
        this.groupType = groupType;
        this.entries = entries;
        this.chance = chance;
        this.quality = quality;
    }

    public LootGroup(FriendlyByteBuf buf) {
        super(buf);

        groupType = buf.readEnum(GroupType.class);
        chance = buf.readFloat();
        quality = buf.readInt();

        int count = buf.readInt();
        List<LootEntry> entries = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            EntryType entryType = buf.readEnum(EntryType.class);

            switch (entryType) {
                case GROUP -> entries.add(new LootGroup(buf));
                case ITEM -> entries.add(new LootItem(buf));
                case TAG -> entries.add(new LootTag(buf));
                case POOL -> entries.add(new LootPoolEntry(buf));
                case TABLE -> entries.add(new LootTableEntry(buf));
            }
        }

        this.entries = entries;
    }

    public List<? extends LootEntry> entries() {
        return entries;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public float getChance() {
        return chance;
    }

    public int getQuality() {
        return quality;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeEnum(groupType);
        buf.writeFloat(chance);
        buf.writeInt(quality);
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
