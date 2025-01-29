package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class LootItem extends LootEntry {
    public final ResourceLocation item;
    public final float chance;

    public LootItem(FriendlyByteBuf buf) {
        super(buf);
        item = buf.readResourceLocation();
        chance = buf.readFloat();
    }

    public LootItem(ResourceLocation item, List<ILootFunction> functions, List<ILootCondition> conditions, float chance) {
        super(functions, conditions);
        this.item = item;
        this.chance = chance;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(item);
        buf.writeFloat(chance);
    }

    @Override
    public EntryType getType() {
        return EntryType.ITEM;
    }

    @Override
    public String toString() {
        return String.format("%s (%f)", item, chance);
    }
}
