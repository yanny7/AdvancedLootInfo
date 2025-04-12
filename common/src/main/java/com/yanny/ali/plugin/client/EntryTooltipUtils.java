package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class EntryTooltipUtils {
    @NotNull
    public static List<Component> getLootTableTooltip(int pad, int quality, float chance) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.enum.group_type.all")));
        components.addAll(getQualityTooltip(quality));
        components.addAll(getChanceTooltip(new RangeValue(chance)));

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
        components.addAll(getChanceTooltip(new RangeValue((float) entry.weight / sumWeight * 100)));

        return components;
    }

    @NotNull
    public static List<Component> getEmptyTooltip(IClientUtils utils, EmptyLootItem entry, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = new LinkedList<>(functions);
        List<LootItemCondition> allConditions = new LinkedList<>(conditions);

        allFunctions.addAll(Arrays.asList(entry.functions));
        allConditions.addAll(Arrays.asList(entry.conditions));

        float rawChance = (float) entry.weight / sumWeight;
        RangeValue chance = TooltipUtils.getChance(allConditions, rawChance);
        Pair<Enchantment, Map<Integer, RangeValue>> bonusChance = TooltipUtils.getBonusChance(allConditions, rawChance);

        return new LinkedList<>(getTooltip(utils, entry, chance, bonusChance, new RangeValue(), null, allFunctions, allConditions));
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
    public static List<Component> getTooltip(IClientUtils utils, LootPoolEntryContainer entry, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
                                             RangeValue count, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<LootItemFunction> functions,
                                             List<LootItemCondition> conditions) {
        List<Component> components = new LinkedList<>();

        if (entry instanceof LootPoolSingletonContainer singletonEntry) {
            components.addAll(getQualityTooltip(singletonEntry.quality));
        }

        components.addAll(getChanceTooltip(chance));
        components.addAll(getBonusChanceTooltip(bonusChance));

        components.addAll(getCountTooltip(count));
        components.addAll(getBonusCountTooltip(bonusCount));

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

    /*
     * PRIVATE
     */

    @NotNull
    @Unmodifiable
    private static List<Component> getQualityTooltip(int quality) {
        if (quality != 0) {
            return List.of(translatable("ali.description.quality", value(quality)));
        }

        return List.of();
    }

    @Unmodifiable
    @NotNull
    private static List<Component> getCountTooltip(RangeValue count) {
        return List.of(translatable("ali.description.count", value(count)));
    }

    @Unmodifiable
    @NotNull
    private static List<Component> getChanceTooltip(RangeValue chance) {
        return List.of(translatable("ali.description.chance", value(chance, "%")));
    }

    @NotNull
    private static List<Component> getBonusChanceTooltip(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance) {
        List<Component> components = new LinkedList<>();

        if (bonusChance != null) {
            bonusChance.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                    components.add(pad(1, translatable(
                            "ali.description.chance_bonus",
                            value(entry.getValue(), "%"),
                            Component.translatable(bonusChance.getFirst().getDescriptionId()),
                            Component.translatable("enchantment.level." + entry.getKey())
                    )))
            );
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCountTooltip(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        List<Component> components = new LinkedList<>();

        if (bonusCount != null) {
            bonusCount.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                    components.add(pad(1, translatable(
                            "ali.description.count_bonus",
                            value(entry.getValue()),
                            Component.translatable(bonusCount.getFirst().getDescriptionId()),
                            Component.translatable("enchantment.level." + entry.getKey())
                    )))
            );
        }

        return components;
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
