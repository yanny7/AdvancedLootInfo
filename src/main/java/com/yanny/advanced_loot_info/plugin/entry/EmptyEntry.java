package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public class EmptyEntry extends SingletonEntry {
    public EmptyEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
    }

    public EmptyEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
    }
}
