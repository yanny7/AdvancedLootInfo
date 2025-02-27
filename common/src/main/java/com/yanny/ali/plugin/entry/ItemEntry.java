package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinLootItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemEntry extends SingletonEntry {
    public final Item item;

    public ItemEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        item = ((MixinLootItem) entry).getItem().value();
    }

    public ItemEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        return List.of(item);
    }
}
