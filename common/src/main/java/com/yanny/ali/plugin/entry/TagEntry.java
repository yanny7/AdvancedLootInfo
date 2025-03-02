package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinTagEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
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
        return BuiltInRegistries.ITEM.getTag(item).map((tag) -> tag.stream().map(Holder::value).toList()).orElse(List.of());
    }
}
