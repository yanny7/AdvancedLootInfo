package com.yanny.emi_loot_addon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class LootInfo extends LootEntry {
    public final ResourceLocation item;
    public final float chance;

    public LootInfo(FriendlyByteBuf buf) {
        super(buf);
        item = buf.readResourceLocation();
        chance = buf.readFloat();
    }

    public LootInfo(ResourceLocation item, List<LootFunction> functions, List<LootCondition> conditions, float chance) {
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
        return EntryType.INFO;
    }

    @Override
    public String toString() {
        return String.format("%s (%f)", item, chance);
    }
}
