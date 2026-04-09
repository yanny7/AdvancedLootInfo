package com.yanny.ali.plugin.mods;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public interface IEntry {
    IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions);
}
