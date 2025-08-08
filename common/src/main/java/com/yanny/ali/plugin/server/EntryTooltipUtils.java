package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.value;

public class EntryTooltipUtils {
    @Unmodifiable
    @NotNull
    public static List<ITooltipNode> getLootTableTooltip() {
        return List.of(new TooltipNode(translatable("ali.enum.group_type.all")));
    }

    @NotNull
    public static List<ITooltipNode> getReferenceTooltip(LootTableReference entry, float chance, int sumWeight) {
        List<ITooltipNode> tooltip = new ArrayList<>();

        tooltip.add(new TooltipNode(translatable("ali.enum.group_type.all")));
        tooltip.add(getQualityTooltip(entry.quality));
        tooltip.add(getChanceTooltip(getBaseMap((chance * entry.weight / sumWeight) * 100)));

        return tooltip;
    }

    @NotNull
    public static List<ITooltipNode> getLootPoolTooltip(RangeValue rolls, RangeValue bonusRolls) {
        List<ITooltipNode> tooltip = new ArrayList<>();

        tooltip.add(new TooltipNode(translatable("ali.enum.group_type.random")));
        tooltip.add(getRolls(rolls, bonusRolls));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static List<ITooltipNode> getAlternativesTooltip() {
        return List.of(new TooltipNode(translatable("ali.enum.group_type.alternatives")));
    }

    @NotNull
    public static List<ITooltipNode> getDynamicTooltip(DynamicLoot entry, float chance, int sumWeight) {
        List<ITooltipNode> tooltip = new ArrayList<>();

        tooltip.add(new TooltipNode(translatable("ali.enum.group_type.dynamic")));
        tooltip.add(getQualityTooltip(entry.quality));
        tooltip.add(getChanceTooltip(getBaseMap(chance * entry.weight / sumWeight * 100)));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static List<ITooltipNode>  getGroupTooltip() {
        return List.of(new TooltipNode(translatable("ali.enum.group_type.all")));
    }

    @Unmodifiable
    @NotNull
    public static List<ITooltipNode>  getSequentialTooltip() {
        return List.of(new TooltipNode(translatable("ali.enum.group_type.sequence")));
    }

    @NotNull
    public static List<ITooltipNode> getEmptyTooltip(IServerUtils utils, LootPoolSingletonContainer entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<ITooltipNode> tooltip = new ArrayList<>();
        List<LootItemFunction> allFunctions = new ArrayList<>(functions);
        List<LootItemCondition> allConditions = new ArrayList<>(conditions);

        allFunctions.addAll(Arrays.asList(entry.functions));
        allConditions.addAll(Arrays.asList(entry.conditions));

        float rawChance = chance * entry.weight / sumWeight;
        Map<Enchantment, Map<Integer, RangeValue>> chanceMap = TooltipUtils.getChance(utils, allConditions, rawChance);

        tooltip.add(new TooltipNode(Component.translatable("ali.enum.group_type.empty")));
        tooltip.add(getQualityTooltip(entry.quality));
        tooltip.add(getChanceTooltip(chanceMap));
        tooltip.addAll(GenericTooltipUtils.getConditionsTooltip(utils, allConditions));
        tooltip.addAll(GenericTooltipUtils.getFunctionsTooltip(utils, allFunctions));
        return tooltip;
    }

    @NotNull
    public static List<ITooltipNode> getSingletonTooltip(IServerUtils utils, LootPoolSingletonContainer entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = new ArrayList<>(functions);
        List<LootItemCondition> allConditions = new ArrayList<>(conditions);

        allFunctions.addAll(Arrays.asList(entry.functions));
        allConditions.addAll(Arrays.asList(entry.conditions));

        float rawChance = chance * entry.weight / sumWeight;
        Map<Enchantment, Map<Integer, RangeValue>> chanceMap = TooltipUtils.getChance(utils, allConditions, rawChance);
        Map<Enchantment, Map<Integer, RangeValue>> countMap = TooltipUtils.getCount(utils, allFunctions);

        return getTooltip(utils, entry.quality, chanceMap, countMap, allFunctions, allConditions);
    }

    @NotNull
    public static List<ITooltipNode> getTooltip(IServerUtils utils, int quality, Map<Enchantment, Map<Integer, RangeValue>> chance, Map<Enchantment, Map<Integer, RangeValue>> count,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<ITooltipNode> tooltip = new ArrayList<>();

        tooltip.add(getQualityTooltip(quality));
        tooltip.add(getChanceTooltip(chance));
        tooltip.add(getCountTooltip(count));
        tooltip.addAll(GenericTooltipUtils.getConditionsTooltip(utils, conditions));
        tooltip.addAll(GenericTooltipUtils.getFunctionsTooltip(utils, functions));

        return tooltip;
    }

    @NotNull
    @Unmodifiable
    public static ITooltipNode getQualityTooltip(int quality) {
        if (quality != LootPoolSingletonContainer.DEFAULT_QUALITY) {
            return new TooltipNode(translatable("ali.description.quality", value(quality)));
        }

        return new TooltipNode();
    }

    @NotNull
    public static ITooltipNode getChanceTooltip(Map<Enchantment, Map<Integer, RangeValue>> chance) {
        RangeValue defaultChance = chance.get(null).get(0);
        ITooltipNode tooltip;

        if (!defaultChance.isRange() && defaultChance.max() > 99.99999) {
            tooltip = new TooltipNode();
        } else {
            tooltip = new TooltipNode(translatable("ali.description.chance", value(chance.get(null).get(0), "%")));
        }

        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceEntry : chance.entrySet()) {
            Enchantment enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    RangeValue value = levelEntry.getValue();

                    tooltip.add(new TooltipNode(translatable(
                            "ali.description.chance_bonus",
                            value(value, "%"),
                            value(Component.translatable(enchantment.getDescriptionId())),
                            value(Component.translatable("enchantment.level." + level))
                    )));
                }
            }
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getCountTooltip(Map<Enchantment, Map<Integer, RangeValue>> count) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.description.count", value(count.get(null).get(0))));

        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceEntry : count.entrySet()) {
            Enchantment enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    RangeValue value = levelEntry.getValue();

                    tooltip.add(new TooltipNode(translatable(
                            "ali.description.count_bonus",
                            value(value),
                            value(Component.translatable(enchantment.getDescriptionId())),
                            value(Component.translatable("enchantment.level." + level))
                    )));
                }
            }
        }

        return tooltip;
    }

    @NotNull
    public static Map<Enchantment, Map<Integer, RangeValue>> getBaseMap(float value) {
        Map<Enchantment, Map<Integer, RangeValue>> map = new LinkedHashMap<>();

        map.put(null, Map.of(0, new RangeValue(value)));
        return map;
    }

    @NotNull
    public static Map<Enchantment, Map<Integer, RangeValue>> getBaseMap(float min, float max) {
        Map<Enchantment, Map<Integer, RangeValue>> map = new LinkedHashMap<>();

        map.put(null, Map.of(0, new RangeValue(min, max)));
        return map;
    }

    @NotNull
    public static ITooltipNode getRolls(RangeValue rolls, RangeValue bonusRolls) {
        return new TooltipNode(translatable("ali.description.rolls", value(getTotalRolls(rolls, bonusRolls).toIntString(), "x")));
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
