package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public abstract class LootEntry {
    public final List<ILootFunction> functions;
    public final List<ILootCondition> conditions;

    LootEntry(IContext context, FriendlyByteBuf buf) {
        functions = context.registry().decodeFunctions(context, buf);
        conditions = context.registry().decodeConditions(context, buf);
    }

    LootEntry(List<ILootFunction> functions, List<ILootCondition> conditions) {
        this.functions = functions;
        this.conditions = conditions;
    }

    public abstract EntryType getType();

    public void encode(IContext context, FriendlyByteBuf buf) {
        context.registry().encodeFunctions(context, buf, functions);
        context.registry().encodeConditions(context, buf, conditions);
    }
}
