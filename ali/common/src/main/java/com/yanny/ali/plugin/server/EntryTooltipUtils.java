package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class EntryTooltipUtils {
    @NotNull
    public static ITooltipNode getLootTableTooltip() {
        return LiteralTooltipNode.translatable("ali.enum.group_type.all");
    }

    @NotNull
    public static ITooltipNode getReferenceTooltip(LootTableReference entry, float chance, int sumWeight) {
        return ArrayTooltipNode.array()
                .add(LiteralTooltipNode.translatable("ali.enum.group_type.all"))
                .add(getQualityTooltip(entry.quality))
                .add(getChanceTooltip(getBaseMap((chance * entry.weight / sumWeight) * 100)))
                .build();
    }

    @NotNull
    public static ITooltipNode getLootPoolTooltip(RangeValue rolls, RangeValue bonusRolls) {
        return ArrayTooltipNode.array()
                .add(LiteralTooltipNode.translatable("ali.enum.group_type.random"))
                .add(getRolls(rolls, bonusRolls))
                .build();
    }

    @NotNull
    public static ITooltipNode getAlternativesTooltip() {
        return LiteralTooltipNode.translatable("ali.enum.group_type.alternatives");
    }

    @NotNull
    public static ITooltipNode getDynamicTooltip(IServerUtils utils, int quality, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return ArrayTooltipNode.array()
                .add(LiteralTooltipNode.translatable("ali.enum.group_type.dynamic"))
                .add(getQualityTooltip(quality))
                .add(getChanceTooltip(getBaseMap(chance * 100)))
                .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                .build();
    }

    @NotNull
    public static ITooltipNode getGroupTooltip() {
        return LiteralTooltipNode.translatable("ali.enum.group_type.all");
    }

    @NotNull
    public static ITooltipNode getSequentialTooltip() {
        return LiteralTooltipNode.translatable("ali.enum.group_type.sequence");
    }

    @NotNull
    public static ITooltipNode getEmptyTooltip(IServerUtils utils, int quality, Map<Enchantment, Map<Integer, RangeValue>> chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return ArrayTooltipNode.array()
                .add(LiteralTooltipNode.translatable("ali.enum.group_type.empty"))
                .add(getQualityTooltip(quality))
                .add(getChanceTooltip(chance))
                .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                .build();
    }

    @NotNull
    public static ITooltipNode getTooltip(IServerUtils utils, int quality, Map<Enchantment, Map<Integer, RangeValue>> chance, Map<Enchantment, Map<Integer, RangeValue>> count,
                                               List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return ArrayTooltipNode.array()
                .add(getQualityTooltip(quality))
                .add(getChanceTooltip(chance))
                .add(getCountTooltip(count))
                .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                .build();
    }

    @NotNull
    public static ITooltipNode getQualityTooltip(int quality) {
        if (quality != LootPoolSingletonContainer.DEFAULT_QUALITY) {
            return ValueTooltipNode.value(quality).build("ali.description.quality");
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getChanceTooltip(Map<Enchantment, Map<Integer, RangeValue>> chance) {
        RangeValue defaultChance = chance.get(null).get(0);

        if (!defaultChance.isRange() && defaultChance.max() > 99.99999) {
            return EmptyTooltipNode.EMPTY;
        }

        IKeyTooltipNode tooltip = ValueTooltipNode.value(chance.get(null).get(0), "%");

        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceEntry : chance.entrySet()) {
            Enchantment enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    RangeValue value = levelEntry.getValue();

                    tooltip.add(ValueTooltipNode.value(
                            value.toString() + "%",
                            ValueTooltipNode.translate(enchantment.getDescriptionId()),
                            ValueTooltipNode.translate("enchantment.level." + level)
                    ).build("ali.description.chance_bonus"));
                }
            }
        }

        return tooltip.build("ali.description.chance");
    }

    @NotNull
    public static ITooltipNode getCountTooltip(Map<Enchantment, Map<Integer, RangeValue>> count) {
        IKeyTooltipNode tooltip = ValueTooltipNode.value(count.get(null).get(0));

        for (Map.Entry<Enchantment, Map<Integer, RangeValue>> chanceEntry : count.entrySet()) {
            Enchantment enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    RangeValue value = levelEntry.getValue();

                    tooltip.add(ValueTooltipNode.value(
                            value,
                            ValueTooltipNode.translate(enchantment.getDescriptionId()),
                            ValueTooltipNode.translate("enchantment.level." + level)
                    ).build("ali.description.count_bonus"));
                }
            }
        }

        return tooltip.build("ali.description.count");
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
        return ValueTooltipNode.value(getTotalRolls(rolls, bonusRolls).toIntString(), "x").build("ali.description.rolls");
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
