package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinLootTableReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReferenceEntry extends SingletonEntry {
    public final ResourceLocation name;
    @Nullable public final LootTableEntry lootTable;

    public ReferenceEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        name = ((MixinLootTableReference) entry).getName();

        LootDataManager manager = context.lootDataManager();

        if (manager != null) {
            LootTable table = manager.getElement(LootDataType.TABLE, name);

            if (table != null) {
                lootTable = new LootTableEntry(context, table);
            } else {
                lootTable = null;
            }
        } else {
            lootTable = null;
        }
    }

    public ReferenceEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readResourceLocation();

        if (buf.readBoolean()) {
            lootTable = new LootTableEntry(context, buf);
        } else {
            lootTable = null;
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(name);
        buf.writeBoolean(lootTable != null);

        if (lootTable != null) {
            lootTable.encode(context, buf);
        }
    }

    @Override
    public @NotNull List<Item> collectItems() {
        if (lootTable != null) {
            return lootTable.collectItems();
        } else {
            return List.of();
        }
    }
}
