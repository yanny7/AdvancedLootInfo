package com.yanny.ali.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class GenericTooltipUtils {
    @NotNull
    public static TooltipBuilder getConditionsSectionTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            return TooltipBuilder.array((b) -> {
                b.add(TooltipBuilder.keyOnly("ali.util.advanced_loot_info.delimiter.conditions"));
                b.add(utils.getValueTooltip(utils, conditions));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getFunctionsSectionTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            return TooltipBuilder.array((b) -> {
                b.add(TooltipBuilder.keyOnly("ali.util.advanced_loot_info.delimiter.functions"));
                b.add(utils.getValueTooltip(utils, functions));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static <K, V> TooltipBuilder getMapTooltip(IServerUtils utils, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, TooltipBuilder> mapper) {
        if (!values.isEmpty()) {
            return TooltipBuilder.branch((b) -> values.entrySet().forEach((e) -> b.add(mapper.apply(utils, e))));
        }

        return TooltipBuilder.empty();
    }

    // MAP ENTRY

    @NotNull
    public static TooltipBuilder getStatsEntryTooltip(IServerUtils utils, Map.Entry<Stat<?>, MinMaxBounds.Ints> entry) {
        Stat<?> stat = entry.getKey();
        MinMaxBounds.Ints ints = entry.getValue();
        Object value = stat.getValue();

        if (value instanceof Item item) {
            TooltipBuilder itemTooltip = utils.getValueTooltip(utils, item);

            itemTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
            return itemTooltip.key(Lang.Value.ITEM);
        } else if (value instanceof Block block) {
            TooltipBuilder blockTooltip = utils.getValueTooltip(utils, block);

            blockTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
            return blockTooltip.key(Lang.Value.BLOCK);
        } else if (value instanceof EntityType<?> entityType) {
            TooltipBuilder entityTooltip = utils.getValueTooltip(utils, entityType);

            entityTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(stat.getType().getTranslationKey()), toString(ints)).build());
            return entityTooltip.key(Lang.Value.ENTITY_TYPE);
        } else if (value instanceof ResourceLocation resourceLocation) {
            TooltipBuilder locationTooltip = utils.getValueTooltip(utils, resourceLocation);

            locationTooltip.add(TooltipBuilder.keyValue(TooltipBuilder.translate(getTranslationKey(resourceLocation)), toString(ints)).build());
            return locationTooltip.key(Lang.Value.ID);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceLocation, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey().toString(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return TooltipBuilder.keyValue(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static TooltipBuilder getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.LIMIT));
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue().amplifier).build(Lang.Value.AMPLIFIER))
                .add(utils.getValueTooltip(utils, entry.getValue().duration).build(Lang.Value.DURATION))
                .add(utils.getValueTooltip(utils, entry.getValue().ambient).build(Lang.Value.IS_AMBIENT))
                .add(utils.getValueTooltip(utils, entry.getValue().visible).build(Lang.Value.IS_VISIBLE));
    }

    @NotNull
    public static TooltipBuilder getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Enchantment, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.LEVELS));
    }

    @NotNull
    public static TooltipBuilder getMobEffectDurationEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, NumberProvider> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()).build(Lang.Value.DURATION));
    }

    @NotNull
    public static TooltipBuilder getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        return utils.getValueTooltip(utils, entry.getKey())
                .add(utils.getValueTooltip(utils, entry.getValue()));
    }

    @NotNull
    public static String toString(MinMaxBounds.Doubles doubles) {
        Double min = doubles.getMin();
        Double max = doubles.getMax();

        if (min != null) {
            if (max != null) {
                if (!Objects.equals(min, max)) {
                    return String.format("%.1f-%.1f", min, max);
                } else {
                    return String.format("=%.1f", min);
                }
            } else {
                return String.format("≥%.1f", min);
            }
        } else {
            if (max != null) {
                return String.format("≤%.1f", max);
            }

            return "???";
        }
    }

    @NotNull
    public static String toString(MinMaxBounds.Ints ints) {
        Integer min = ints.getMin();
        Integer max = ints.getMax();

        if (min != null) {
            if (max != null) {
                if (!Objects.equals(min, max)) {
                    return String.format("%d-%d", min, max);
                } else {
                    return String.format("=%d", min);
                }
            } else {
                return String.format("≥%d", min);
            }
        } else {
            if (max != null) {
                return String.format("≤%d", max);
            }

            return "???";
        }
    }

    @NotNull
    private static String getTranslationKey(ResourceLocation location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
