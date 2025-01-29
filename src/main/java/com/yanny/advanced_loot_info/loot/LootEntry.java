package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.manager.PluginManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public abstract class LootEntry {
    public final List<ILootFunction> functions;
    public final List<ILootCondition> conditions;

    LootEntry(FriendlyByteBuf buf) {
        functions = PluginManager.REGISTRY.decodeFunctions(buf);
        conditions = PluginManager.REGISTRY.decodeConditions(buf);
    }

    LootEntry(List<ILootFunction> functions, List<ILootCondition> conditions) {
        this.functions = functions;
        this.conditions = conditions;
    }

    public abstract EntryType getType();

    public void encode(FriendlyByteBuf buf) {
        PluginManager.REGISTRY.encodeFunctions(buf, functions);
        PluginManager.REGISTRY.encodeConditions(buf, conditions);
    }
}
