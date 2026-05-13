package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.lootjs.mixin.*;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static TooltipBuilder andConditionTooltip(IServerUtils utils, AndCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinAndCondition) condition).getConditions());

        return utils.getValueTooltip(utils, conditions).key(Lang.Conditions.AND);
    }

    @NotNull
    public static TooltipBuilder anyBiomeCheckTooltip(IServerUtils utils, AnyBiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getBiomes()).build(Lang.Branch.BIOMES));
            b.add(utils.getValueTooltip(utils, cond.getTags()).build(Lang.Branch.TAGS));
        }).key(Lang.Conditions.ANY_BIOME);
    }

    @NotNull
    public static TooltipBuilder anyDimensionTooltip(IServerUtils utils, AnyDimension condition) {
        MixinAnyDimension cond = (MixinAnyDimension) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getDimensions()).build(Lang.Branch.DIMENSIONS)))
                .key(Lang.Conditions.ANY_DIMENSION);
    }

    @NotNull
    public static TooltipBuilder anyStructureTooltip(IServerUtils utils, AnyStructure condition) {
        MixinAnyStructure cond = (MixinAnyStructure) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getStructureLocators()).build(Lang.Branch.STRUCTURES));
            b.add(utils.getValueTooltip(utils, cond.getExact()).build(Lang.Value.EXACT));
        }).key(Lang.Conditions.ANY_STRUCTURE);
    }

    @NotNull
    public static TooltipBuilder biomeCheckTooltip(IServerUtils utils, BiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getBiomes()).build(Lang.Branch.BIOMES));
            b.add(utils.getValueTooltip(utils, cond.getTags()).build(Lang.Branch.TAGS));
        }).key(Lang.Conditions.BIOME);
    }

    @NotNull
    public static TooltipBuilder containsLootConditionTooltip(IServerUtils utils, ContainsLootCondition condition) {
        MixinContainsLootCondition cond = (MixinContainsLootCondition) condition;

        return TooltipBuilder.array((b) -> {
            b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build(Lang.Value.ITEM_FILTER));
            b.add(utils.getValueTooltip(utils, cond.getExact()).build(Lang.Value.EXACT));
        }).key(Lang.Conditions.MATCH_LOOT);
    }

    @NotNull
    public static TooltipBuilder customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.ENTITY_PREDICATE);
        } else if (cond.getParam() == LootContextParams.KILLER_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.KILLER_PREDICATE);
        } else if (cond.getParam() == LootContextParams.DIRECT_KILLER_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.DIRECT_KILLER_PREDICATE);
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.BLOCK_PREDICATE);
        } else {
            return TooltipBuilder.empty();
        }
    }

    @NotNull
    public static TooltipBuilder isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, IntRange.range(cond.getMin(), cond.getMax())).build(Lang.Value.VALUE)))
                .key(Lang.Conditions.LIGHT_LEVEL);
    }

    @NotNull
    public static TooltipBuilder lootItemConditionWrapperTooltip(IServerUtils utils, LootItemConditionWrapper condition) {
        return utils.getValueTooltip(utils, ((MixinLootItemConditionWrapper) condition).getCondition());
    }

    @NotNull
    public static TooltipBuilder mainHandTableBonusTooltip(IServerUtils utils, MainHandTableBonus condition) {
        MixinMainHandTableBonus cond = (MixinMainHandTableBonus) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getEnchantment()).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, Arrays.toString(cond.getValues())).build(Lang.Value.VALUES));
        }).key(Lang.Conditions.RANDOM_CHANCE_WITH_ENCHANTMENT);
    }

    @NotNull
    public static TooltipBuilder getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        switch (cond.getSlot()) {
            case MAINHAND -> {
                return TooltipBuilder.array((b) -> b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_MAINHAND);
            }
            case OFFHAND -> {
                return TooltipBuilder.array((b) -> b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_OFFHAND);
            }
            default -> {
                return TooltipBuilder.array((b) -> {
                    b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build(Lang.Value.ITEM_FILTER));
                    b.add(utils.getValueTooltip(utils, cond.getSlot()).build(Lang.Value.SLOT));
                }).key(Lang.Conditions.MATCH_EQUIPMENT_SLOT);
            }
        }
    }

    @NotNull
    public static TooltipBuilder matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        MixinMatchKillerDistance cond = (MixinMatchKillerDistance) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getPredicate()).build(Lang.Branch.PREDICATE)))
                .key(Lang.Conditions.DISTANCE_TO_KILLER);
    }

    @NotNull
    public static TooltipBuilder matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        MixinMatchPlayer cond = (MixinMatchPlayer) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getPredicate()).build(Lang.Branch.PREDICATE)))
                .key(Lang.Conditions.MATCH_PLAYER);
    }

    @NotNull
    public static TooltipBuilder notConditionTooltip(IServerUtils utils, NotCondition condition) {
        MixinNotCondition cond = (MixinNotCondition) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, (LootItemCondition) cond.getCondition())))
                .key(Lang.Conditions.NOT);
    }

    @NotNull
    public static TooltipBuilder orConditionTooltip(IServerUtils utils, OrCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinOrCondition) condition).getConditions());

        return utils.getValueTooltip(utils, conditions).key(Lang.Conditions.OR);
    }

    @NotNull
    public static TooltipBuilder playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                .key(Lang.Conditions.PLAYER_PREDICATE);
    }

    @NotNull
    public static TooltipBuilder wrapperDamageSourceConditionTooltip(IServerUtils utils, WrappedDamageSourceCondition condition) {
        MixinWrappedDamageSourceCondition cond = (MixinWrappedDamageSourceCondition) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getPredicate()).build(Lang.Branch.PREDICATE));
            b.add(utils.getValueTooltip(utils, cond.getSourceNames()).build(Lang.Branch.SOURCE_NAMES));
        }).key(Lang.Conditions.MATCH_DAMAGE_SOURCE);
    }
}
