package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class EntryTooltipUtils {
    @NotNull
    public static TooltipNode getLootTableTooltip() {
        return TooltipBuilder.keyOnly("ali.enum.group_type.all").build();
    }

    @NotNull
    public static TooltipNode getReferenceTooltip(NestedLootTable entry, float chance, int sumWeight) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly("ali.enum.group_type.all"))
                        .add(getQualityTooltip(entry.quality))
                        .add(getChanceTooltip(getBaseMap((chance * entry.weight / sumWeight) * 100)))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getLootPoolTooltip(RangeValue rolls, RangeValue bonusRolls) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly("ali.enum.group_type.random"))
                        .add(getRolls(rolls, bonusRolls))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getAlternativesTooltip() {
        return TooltipBuilder.keyOnly("ali.enum.group_type.alternatives").build();
    }

    @NotNull
    public static TooltipNode getDynamicTooltip(IServerUtils utils, int quality, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly("ali.enum.group_type.dynamic"))
                        .add(getQualityTooltip(quality))
                        .add(getChanceTooltip(getBaseMap(chance * 100)))
                        .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                        .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getGroupTooltip() {
        return TooltipBuilder.keyOnly("ali.enum.group_type.all").build();
    }

    @NotNull
    public static TooltipNode getSequentialTooltip() {
        return TooltipBuilder.keyOnly("ali.enum.group_type.sequence").build();
    }

    @NotNull
    public static TooltipNode getEmptyTooltip(IServerUtils utils, int quality, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly("ali.enum.group_type.empty"))
                        .add(getQualityTooltip(quality))
                        .add(getChanceTooltip(chance))
                        .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                        .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getSlotTooltip(IServerUtils utils, SlotSource slotSource, int quality, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> b
                .add(TooltipBuilder.keyOnly("ali.enum.group_type.slot"))
                .add(utils.getSlotSourceTooltip(utils, slotSource))
                .add(getQualityTooltip(quality))
                .add(getChanceTooltip(chance))
                .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getTooltip(IServerUtils utils, int quality, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count,
                                               List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> b
                        .add(getQualityTooltip(quality))
                        .add(getChanceTooltip(chance))
                        .add(getCountTooltip(count))
                        .add(GenericTooltipUtils.getConditionsTooltip(utils, conditions))
                        .add(GenericTooltipUtils.getFunctionsTooltip(utils, functions))
                )
                .build();
    }

    @NotNull
    public static TooltipNode getQualityTooltip(int quality) {
        if (quality != LootPoolSingletonContainer.DEFAULT_QUALITY) {
            return TooltipBuilder.value(quality).build("ali.description.quality");
        }

        return TooltipNode.EMPTY_INSTANCE;
    }

    @NotNull
    public static TooltipNode getChanceTooltip(Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
        RangeValue defaultChance = chance.get(null).get(0);

        if (!defaultChance.isRange() && defaultChance.max() > 99.99999) {
            return TooltipNode.EMPTY_INSTANCE;
        }

        TooltipBuilder builder = TooltipBuilder.value(defaultChance, "%");

        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceEntry : chance.entrySet()) {
            Holder<Enchantment> enchantment = chanceEntry.getKey();

            if (enchantment != null) {
                Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    String key = ((TranslatableContents) enchantment.value().description().getContents()).getKey();
                    RangeValue value = levelEntry.getValue();

                    builder.add(TooltipBuilder.value(
                            value.toString() + "%",
                            TooltipBuilder.translate(key),
                            TooltipBuilder.translate("enchantment.level." + level)
                    ).build("ali.description.chance_bonus"));
                }
            }
        }

        return builder.build("ali.description.chance");
    }

    @NotNull
    public static TooltipNode getCountTooltip(Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
        RangeValue defaultCount = count.get(null).get(0);

        TooltipBuilder builder = TooltipBuilder.value(defaultCount);

        for (Map.Entry<Holder<Enchantment>, Map<Integer, RangeValue>> chanceEntry : count.entrySet()) {
            Holder<Enchantment> enchantment = chanceEntry.getKey();
            Map<Integer, RangeValue> levelMap = chanceEntry.getValue();

            if (enchantment != null) {
                for (Map.Entry<Integer, RangeValue> levelEntry : levelMap.entrySet()) {
                    int level = levelEntry.getKey();
                    String key = ((TranslatableContents) enchantment.value().description().getContents()).getKey();
                    RangeValue value = levelEntry.getValue();

                    builder.add(TooltipBuilder.value(
                            value,
                            TooltipBuilder.translate(key),
                            TooltipBuilder.translate("enchantment.level." + level)
                    ).build("ali.description.count_bonus"));
                }
            }
        }

        return builder.build("ali.description.count");
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
    public static TooltipNode getRolls(RangeValue rolls, RangeValue bonusRolls) {
        return TooltipBuilder.value(getTotalRolls(rolls, bonusRolls).toIntString(), "x").build("ali.description.rolls");
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return new RangeValue(bonusRolls).add(rolls);
        } else {
            return rolls;
        }
    }
}
