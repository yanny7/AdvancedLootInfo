package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootEntry;
import com.yanny.ali.mixin.MixinLootPoolEntryContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.List;

public abstract class LootEntry implements ILootEntry {
    public final List<ILootCondition> conditions;

    public LootEntry(IContext context, LootPoolEntryContainer entry) {
        conditions = context.utils().convertConditions(context, ((MixinLootPoolEntryContainer) entry).getConditions());
    }

    public LootEntry(IContext context, FriendlyByteBuf buf) {
        conditions = context.utils().decodeConditions(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeConditions(context, buf, conditions);
    }
}
