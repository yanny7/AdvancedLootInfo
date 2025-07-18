package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.modifier.LootAction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.List;

public interface IGroupedLootActionAccessor {
    List<LootAction> getActions();

    ItemFilter getContainsLootFilter();

    NumberProvider getRolls();

    List<LootItemFunction> getFunctions();

    List<LootItemCondition> ali_$getInjectedConditions();
}
