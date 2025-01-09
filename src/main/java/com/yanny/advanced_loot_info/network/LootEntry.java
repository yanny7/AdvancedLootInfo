package com.yanny.advanced_loot_info.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public abstract class LootEntry {
    public final List<LootFunction> functions;
    public final List<LootCondition> conditions;

    LootEntry(FriendlyByteBuf buf) {
        functions = LootFunction.decode(buf);
        conditions = LootCondition.decode(buf);
    }

    LootEntry(List<LootFunction> functions, List<LootCondition> conditions) {
        this.functions = functions;
        this.conditions = conditions;
    }

    public abstract EntryType getType();

    public void encode(FriendlyByteBuf buf) {
        LootFunction.encode(buf, functions);
        LootCondition.encode(buf, conditions);
    }
}
