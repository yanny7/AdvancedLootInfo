package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinLootItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemEntry extends SingletonEntry {
    public final Item item;

    public ItemEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        item = ((MixinLootItem) entry).getItem();
    }

    public ItemEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        item = ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(ForgeRegistries.ITEMS.getKey(item));
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        return List.of(item);
    }
}
