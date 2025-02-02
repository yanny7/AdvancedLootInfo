package com.yanny.advanced_loot_info.api;

import com.yanny.advanced_loot_info.mixin.MixinLootPoolEntryContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.List;

public abstract class LootEntry implements ILootEntry {
    public final List<ILootCondition> conditions;

    public LootEntry(IContext context, LootPoolEntryContainer entry) {
        conditions = context.registry().convertConditions(context, ((MixinLootPoolEntryContainer) entry).getConditions());
    }

    public LootEntry(IContext context, FriendlyByteBuf buf) {
        conditions = context.registry().decodeConditions(context, buf);
    }

    public void encode(IContext context, FriendlyByteBuf buf) {
        context.registry().encodeConditions(context, buf, conditions);
    }
}
