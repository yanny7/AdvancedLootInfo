package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.loot.LootTableEntry;
import com.yanny.advanced_loot_info.mixin.MixinLootTableReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.Nullable;

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
}
