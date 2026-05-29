package com.yanny.ali.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class EntryTooltipUtils {
    @NotNull
    public static TooltipBuilder getLootTableTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALL);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(NestedLootTable entry, float chance, int sumWeight) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(getQualityTooltip(entry.quality));
            b.add(getChanceTooltip(new EnchantedRanges((chance * entry.weight / sumWeight) * 100)));
        });
    }

    @NotNull
    public static TooltipBuilder getLootPoolTooltip(RangeValue rolls, RangeValue bonusRolls) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.RANDOM));
            b.add(getRolls(rolls, bonusRolls));
        });
    }

    @NotNull
    public static TooltipBuilder getAlternativesTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALTERNATIVES);
    }

    @NotNull
    public static TooltipBuilder getDynamicTooltip(IServerUtils utils, int quality, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.DYNAMIC));
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(new EnchantedRanges(chance * 100)));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getGroupTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALL);
    }

    @NotNull
    public static TooltipBuilder getSequentialTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.SEQUENCE);
    }

    @NotNull
    public static TooltipBuilder getEmptyTooltip(IServerUtils utils, int quality, EnchantedRanges chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.EMPTY));
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(chance));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getTooltip(IServerUtils utils, int quality, EnchantedRanges chance, EnchantedRanges count,
                                               List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(chance));
            b.add(getCountTooltip(count));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getQualityTooltip(int quality) {
        if (quality != LootPoolSingletonContainer.DEFAULT_QUALITY) {
            return TooltipBuilder.value(quality).key(Lang.Description.QUALITY);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getChanceTooltip(EnchantedRanges chance) {
        RangeValue defaultChance = chance.getUnenchantedValue();

        if (!defaultChance.isRange() && defaultChance.max() > 99.99999) {
            return TooltipBuilder.empty();
        }

        TooltipBuilder builder = TooltipBuilder.value(defaultChance, "%");

        chance.forEachEnchantment((enchantment, level, value) -> builder.add(TooltipBuilder.value(
                value + "%",
                TooltipBuilder.translate(((TranslatableContents) enchantment.description().getContents()).getKey()),
                TooltipBuilder.translate("enchantment.level." + level)
        ).build(Lang.Description.CHANCE_BONUS)));

        return builder.key(Lang.Description.CHANCE);
    }

    @NotNull
    public static TooltipBuilder getCountTooltip(EnchantedRanges count) {
        TooltipBuilder builder = TooltipBuilder.value(count.getUnenchantedValue());

        count.forEachEnchantment((enchantment, level, value) -> builder.add(TooltipBuilder.value(
                value,
                TooltipBuilder.translate(((TranslatableContents) enchantment.description().getContents()).getKey()),
                TooltipBuilder.translate("enchantment.level." + level)
        ).build(Lang.Description.COUNT_BONUS)));

        return builder.key(Lang.Description.COUNT);
    }

    @NotNull
    public static TooltipBuilder getRolls(RangeValue rolls, RangeValue bonusRolls) {
        return TooltipBuilder.value(getTotalRolls(rolls, bonusRolls).toIntString(), "x").key(Lang.Description.ROLLS);
    }

    private static RangeValue getTotalRolls(RangeValue rolls, RangeValue bonusRolls) {
        if (bonusRolls.min() > 0 || bonusRolls.max() > 0) {
            return bonusRolls.add(rolls);
        } else {
            return rolls;
        }
    }
}
