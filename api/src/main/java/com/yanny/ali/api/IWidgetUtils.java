package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface IWidgetUtils extends IClientUtils {
    Rect addSlotWidget(Item item, ILootEntry entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance,
                       RangeValue count, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions);

    Rect addSlotWidget(TagKey<Item> item, ILootEntry entry, int x, int y, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance,
                       RangeValue count, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> allFunctions, List<ILootCondition> allConditions);

}
