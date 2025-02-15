package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootEntry;
import com.yanny.ali.mixin.MixinCompositeEntryBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public abstract class CompositeEntry extends LootEntry {
    public final List<ILootEntry> children;

    public CompositeEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        children = context.utils().convertEntries(context, ((MixinCompositeEntryBase) entry).getChildren());
    }

    public CompositeEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        children = context.utils().decodeEntries(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        context.utils().encodeEntries(context, buf, children);
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        List<Item> items = new LinkedList<>();

        for (ILootEntry child : children) {
            items.addAll(child.collectItems());
        }

        return items;
    }
}
