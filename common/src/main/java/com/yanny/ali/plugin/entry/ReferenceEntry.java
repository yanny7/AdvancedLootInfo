package com.yanny.ali.plugin.entry;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinNestedLootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ReferenceEntry extends SingletonEntry {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<ResourceKey<LootTable>> name;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<LootTableEntry> lootTable;

    public ReferenceEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        Either<ResourceKey<LootTable>, LootTable> contents = ((MixinNestedLootTable) entry).getContents();

        if (contents.right().isPresent()) {
            lootTable = Optional.of(new LootTableEntry(context, contents.right().get()));
            name = Optional.empty();
        } else if (contents.left().isPresent()) {
            name = Optional.of(contents.left().get());

            ReloadableServerRegistries.Holder manager = context.lootDataManager();

            if (manager != null) {
                lootTable = Optional.of(new LootTableEntry(context, manager.getLootTable(contents.left().get())));
            } else {
                lootTable = Optional.empty();
            }
        } else {
            name = Optional.empty();
            lootTable = Optional.empty();
        }
    }

    public ReferenceEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readOptional((b) -> b.readResourceKey(Registries.LOOT_TABLE));
        lootTable = buf.readOptional((b) -> new LootTableEntry(context, b));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeOptional(name, FriendlyByteBuf::writeResourceKey);
        buf.writeOptional(lootTable, (f, l) -> l.encode(context, f));
    }

    @Override
    public @NotNull List<Item> collectItems() {
        return lootTable.map(LootTableEntry::collectItems).orElse(List.of());
    }
}
