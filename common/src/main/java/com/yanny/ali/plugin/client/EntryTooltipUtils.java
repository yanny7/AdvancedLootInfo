package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class EntryTooltipUtils {
    @NotNull
    public static List<Component> getLootTableTooltip(int pad, int quality, float chance) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.enum.group_type.all")));
        components.addAll(getQualityTooltip(quality));
        components.addAll(getChanceTooltip(getBaseMap(chance)));

        return components;
    }

    @NotNull
    public static List<Component> getLootPoolTooltip(int pad, RangeValue rolls, RangeValue bonusRolls) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.enum.group_type.random")));
        components.add(getRolls(rolls, bonusRolls));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAlternativesTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.enum.group_type.alternatives")));
    }

    @NotNull
    public static List<Component> getDynamicTooltip(int pad, DynamicLoot entry, int sumWeight) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.enum.group_type.dynamic")));
        components.addAll(getQualityTooltip(entry.quality));
        components.addAll(getChanceTooltip(getBaseMap((float) entry.weight / sumWeight * 100)));

        return components;
    }

    @NotNull
    public static List<Component> getEmptyTooltip(IClientUtils utils, EmptyLootItem entry, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(entry.functions);
        allConditions.addAll(entry.conditions);

        float rawChance = (float) entry.weight / sumWeight;
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance = TooltipUtils.getChance(utils, allConditions, rawChance);

        return getTooltip(utils, entry, chance, getBaseMap(1), allFunctions, allConditions);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGroupTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.enum.group_type.all")));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getSequentialTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.enum.group_type.sequence")));
    }

    @NotNull
    public static List<Component> getTooltip(IClientUtils utils, LootPoolEntryContainer entry, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance,
                                             Map<Holder<Enchantment>, Map<Integer, RangeValue>> count, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<Component> components = new LinkedList<>();

        if (entry instanceof LootPoolSingletonContainer singletonEntry) {
            components.addAll(getQualityTooltip(singletonEntry.quality));
        }

        components.addAll(getChanceTooltip(chance));
        components.addAll(getCountTooltip(count));

        if (!conditions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.conditions"));
            components.addAll(GenericTooltipUtils.getConditionsTooltip(utils, 0, conditions));
        }
        if (!functions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.functions"));
            components.addAll(GenericTooltipUtils.getFunctionsTooltip(utils, 0, functions));
        }

        return components;
    }

    @NotNull
    @Unmodifiable
    public static List<Component> getQualityTooltip(int quality) {
        if (quality != 0) {
            return List.of(translatable("ali.description.quality", value(quality)));
        }

        return List.of();
    }

    @NotNull
    public static List<Component> getChanceTooltip(Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.description.chance", value(chance.get(null).get(0), "%")));

        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceEntry : chance.entrySet()) {
            Holder<Enchantment> enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    RangeValue value = levelEntry.getValue();

                    components.add(pad(1, translatable(
                            "ali.description.chance_bonus",
                            value(value, "%"),
                            value(enchantment.value().description()),
                            value(Component.translatable("enchantment.level." + level))
                    )));
                }
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getCountTooltip(Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        List<Component> components = new LinkedList<>();

        components.add(translatable("ali.description.count", value(count.get(null).get(0))));

        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceEntry : count.entrySet()) {
            Holder<Enchantment> enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    RangeValue value = levelEntry.getValue();

                    components.add(pad(1, translatable(
                            "ali.description.count_bonus",
                            value(value),
                            value(enchantment.value().description()),
                            value(Component.translatable("enchantment.level." + level))
                    )));
                }
            }
        }

        return components;
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getBaseMap(float value) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> map = new LinkedHashMap<>();

        map.put(null, Map.of(0, new RangeValue(value)));
        return map;
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getBaseMap(float min, float max) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> map = new LinkedHashMap<>();

        map.put(null, Map.of(0, new RangeValue(min, max)));
        return map;
    }

    @NotNull
    private static Component getRolls(RangeValue rolls, RangeValue bonusRolls) {
        return translatable("ali.description.rolls", value(getTotalRolls(rolls, bonusRolls).toIntString(), "x"));
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
