package com.yanny.advanced_loot_info.network;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public abstract class LootEntry {
    public final List<ILootFunction> functions;
    public final List<ILootCondition> conditions;

    LootEntry(FriendlyByteBuf buf) {
        functions = LootUtils.decodeFunction(buf);
        conditions = LootUtils.decodeCondition(buf);
    }

    LootEntry(List<ILootFunction> functions, List<ILootCondition> conditions) {
        this.functions = functions;
        this.conditions = conditions;
    }

    public abstract EntryType getType();

    public void encode(FriendlyByteBuf buf) {
        LootUtils.encodeFunction(buf, functions);
        LootUtils.encodeCondition(buf, conditions);
    }
}
