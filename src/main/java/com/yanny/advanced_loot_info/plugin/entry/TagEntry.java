package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinTagEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TagEntry extends SingletonEntry {
    public final TagKey<Item> item;
    public final boolean expand;

    public TagEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        item = ((MixinTagEntry) entry).getTag();
        expand = ((MixinTagEntry) entry).getExpand();
    }

    public TagEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        item = TagKey.create(Registries.ITEM, buf.readResourceLocation());
        expand = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(item.location());
        buf.writeBoolean(expand);
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();

        if (tagManager != null) {
            return tagManager.getTag(item).stream().toList();
        } else {
            return List.of();
        }
    }
}
