package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public class GroupEntry extends CompositeEntry {
    public GroupEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
    }

    public GroupEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
    }
}
