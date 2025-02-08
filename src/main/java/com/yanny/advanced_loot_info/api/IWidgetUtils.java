package com.yanny.advanced_loot_info.api;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.plugin.entry.SingletonEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IWidgetUtils extends IClientUtils {
    Rect addSlotWidget(Item item, SingletonEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
                       RangeValue count, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions);

    Rect addSlotWidget(TagKey<Item> item, SingletonEntry entry, int x, int y, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
                       RangeValue count, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions);

}
