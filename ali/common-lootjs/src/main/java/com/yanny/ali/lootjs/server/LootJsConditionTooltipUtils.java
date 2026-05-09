package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
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

        return utils.getValueTooltip(utils, conditions).key("ali.type.condition.and");
    }

    @NotNull
    public static TooltipBuilder anyBiomeCheckTooltip(IServerUtils utils, AnyBiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getBiomes()).build("ali.property.branch.biomes"));
            b.add(utils.getValueTooltip(utils, cond.getTags()).build("ali.property.branch.tags"));
        }).key("ali.type.condition.any_biome");
    }

    @NotNull
    public static TooltipBuilder anyDimensionTooltip(IServerUtils utils, AnyDimension condition) {
        MixinAnyDimension cond = (MixinAnyDimension) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getDimensions()).build("ali.property.branch.dimensions")))
                .key("ali.type.condition.any_dimension");
    }

    @NotNull
    public static TooltipBuilder anyStructureTooltip(IServerUtils utils, AnyStructure condition) {
        MixinAnyStructure cond = (MixinAnyStructure) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getStructureLocators()).build("ali.property.branch.structures"));
            b.add(utils.getValueTooltip(utils, cond.getExact()).build("ali.property.value.exact"));
        }).key("ali.type.condition.any_structure");
    }

    @NotNull
    public static TooltipBuilder biomeCheckTooltip(IServerUtils utils, BiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getBiomes()).build("ali.property.branch.biomes"));
            b.add(utils.getValueTooltip(utils, cond.getTags()).build("ali.property.branch.tags"));
        }).key("ali.type.condition.biome");
    }

    @NotNull
    public static TooltipBuilder containsLootConditionTooltip(IServerUtils utils, ContainsLootCondition condition) {
        MixinContainsLootCondition cond = (MixinContainsLootCondition) condition;

        return TooltipBuilder.array((b) -> {
            b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"));
            b.add(utils.getValueTooltip(utils, cond.getExact()).build("ali.property.value.exact"));
        }).key("ali.type.condition.match_loot");
    }

    @NotNull
    public static TooltipBuilder customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available")))
                    .key("ali.type.condition.entity_predicate");
        } else if (cond.getParam() == LootContextParams.KILLER_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available")))
                    .key("ali.type.condition.killer_predicate");
        } else if (cond.getParam() == LootContextParams.DIRECT_KILLER_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available")))
                    .key("ali.type.condition.direct_killer_predicate");
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available")))
                    .key("ali.type.condition.block_predicate");
        } else {
            return TooltipBuilder.empty();
        }
    }

    @NotNull
    public static TooltipBuilder isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, IntRange.range(cond.getMin(), cond.getMax())).build("ali.property.value.value")))
                .key("ali.type.condition.light_level");
    }

    @NotNull
    public static TooltipBuilder lootItemConditionWrapperTooltip(IServerUtils utils, LootItemConditionWrapper condition) {
        return utils.getValueTooltip(utils, ((MixinLootItemConditionWrapper) condition).getCondition());
    }

    @NotNull
    public static TooltipBuilder mainHandTableBonusTooltip(IServerUtils utils, MainHandTableBonus condition) {
        MixinMainHandTableBonus cond = (MixinMainHandTableBonus) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getEnchantment()).build("ali.property.value.enchantment"));
            b.add(utils.getValueTooltip(utils, Arrays.toString(cond.getValues())).build("ali.property.value.values"));
        }).key("ali.type.condition.random_chance_with_enchantment");
    }

    @NotNull
    public static TooltipBuilder getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        switch (cond.getSlot()) {
            case MAINHAND -> {
                return TooltipBuilder.array((b) -> b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter")))
                        .key("ali.type.condition.match_mainhand");
            }
            case OFFHAND -> {
                return TooltipBuilder.array((b) -> b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter")))
                        .key("ali.type.condition.match_offhand");
            }
            default -> {
                return TooltipBuilder.array((b) -> {
                    b.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"));
                    b.add(utils.getValueTooltip(utils, cond.getSlot()).build("ali.property.value.slot"));
                }).key("ali.type.condition.match_equipment_slot");
            }
        }
    }

    @NotNull
    public static TooltipBuilder matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        MixinMatchKillerDistance cond = (MixinMatchKillerDistance) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.value.predicate")))
                .key("ali.type.condition.distance_to_killer");
    }

    @NotNull
    public static TooltipBuilder matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        MixinMatchPlayer cond = (MixinMatchPlayer) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.value.predicate")))
                .key("ali.type.condition.match_player");
    }

    @NotNull
    public static TooltipBuilder notConditionTooltip(IServerUtils utils, NotCondition condition) {
        MixinNotCondition cond = (MixinNotCondition) condition;

        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, (LootItemCondition) cond.getCondition())))
                .key("ali.type.condition.not");
    }

    @NotNull
    public static TooltipBuilder orConditionTooltip(IServerUtils utils, OrCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinOrCondition) condition).getConditions());

        return utils.getValueTooltip(utils, conditions).key("ali.type.condition.or");
    }

    @NotNull
    public static TooltipBuilder playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available")))
                .key("ali.type.condition.player_predicate");
    }

    @NotNull
    public static TooltipBuilder wrapperDamageSourceConditionTooltip(IServerUtils utils, WrappedDamageSourceCondition condition) {
        MixinWrappedDamageSourceCondition cond = (MixinWrappedDamageSourceCondition) condition;

        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.branch.predicate"));
            b.add(utils.getValueTooltip(utils, cond.getSourceNames()).build("ali.property.branch.source_names"));
        }).key("ali.type.condition.match_damage_source");
    }
}
