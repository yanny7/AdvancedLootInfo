package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.LootEntry;
import com.yanny.advanced_loot_info.mixin.MixinCompositeEntryBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public abstract class CompositeEntry extends LootEntry {
    public final List<LootEntry> children;

    public CompositeEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        children = context.registry().convertEntries(context, ((MixinCompositeEntryBase) entry).getChildren());
    }

    public CompositeEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        children = context.registry().decodeEntries(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        context.registry().encodeEntries(context, buf, children);
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        List<Item> items = new LinkedList<>();

        for (LootEntry child : children) {
            items.addAll(child.collectItems());
        }

        return items;
    }
}
