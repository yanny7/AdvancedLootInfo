package com.yanny.advanced_loot_info.network;

import com.yanny.advanced_loot_info.api.ILootFunction;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public abstract class LootEntry {
    public final List<ILootFunction> functions;
    public final List<LootCondition> conditions;

    LootEntry(FriendlyByteBuf buf) {
        functions = LootUtils.decode(buf);
        conditions = LootCondition.decode(buf);
    }

    LootEntry(List<ILootFunction> functions, List<LootCondition> conditions) {
        this.functions = functions;
        this.conditions = conditions;
    }

    public abstract EntryType getType();

    public void encode(FriendlyByteBuf buf) {
        LootUtils.encode(buf, functions);
        LootCondition.encode(buf, conditions);
    }
}
