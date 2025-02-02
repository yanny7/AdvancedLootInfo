package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.LootEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public class UnknownEntry extends LootEntry {
    public UnknownEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
    }

    public UnknownEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
    }
}
