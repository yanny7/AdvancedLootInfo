package com.yanny.ali.api;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Map;

public interface IWidgetUtils extends IClientUtils {
    Rect addSlotWidget(Item item, LootPoolEntryContainer entry, int x, int y, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                       Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions);

    Rect addSlotWidget(TagKey<Item> item, LootPoolEntryContainer entry, int x, int y, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                       Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> allFunctions, List<LootItemCondition> allConditions);

}
