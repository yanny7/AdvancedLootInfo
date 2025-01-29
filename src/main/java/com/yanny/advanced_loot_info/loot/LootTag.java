package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LootTag extends LootEntry {
    public final TagKey<Item> tag;
    public final float chance;

    public LootTag(FriendlyByteBuf buf) {
        super(buf);
        tag = TagKey.create(Registries.ITEM, buf.readResourceLocation());
        chance = buf.readFloat();
    }

    public LootTag(TagKey<Item> tag, List<ILootFunction> functions, List<ILootCondition> conditions, float chance) {
        super(functions, conditions);
        this.tag = tag;
        this.chance = chance;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(tag.location());
        buf.writeFloat(chance);
    }

    @Override
    public EntryType getType() {
        return EntryType.TAG;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%s (%f)", tag, chance);
    }
}
