package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.*;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static TooltipNode andConditionTooltip(IServerUtils utils, AndCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinAndCondition) condition).getConditions());

        return GenericTooltipUtils.getConditionsTooltip(utils, conditions).build("ali.type.condition.and");
    }

    @NotNull
    public static TooltipNode anyBiomeCheckTooltip(IServerUtils utils, AnyBiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getBiomes()).build("ali.property.branch.biomes"))
                        .add(utils.getValueTooltip(utils, cond.getTags()).build("ali.property.branch.tags"))
                )
                .build("ali.type.condition.any_biome");
    }

    @NotNull
    public static TooltipNode anyDimensionTooltip(IServerUtils utils, AnyDimension condition) {
        MixinAnyDimension cond = (MixinAnyDimension) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, Arrays.asList(cond.getDimensions())).build("ali.property.branch.dimensions"))
                )
                .build("ali.type.condition.any_dimension");
    }

    @NotNull
    public static TooltipNode anyStructureTooltip(IServerUtils utils, AnyStructure condition) {
        MixinAnyStructure cond = (MixinAnyStructure) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getStructureLocators()).build("ali.property.branch.structures"))
                        .add(utils.getValueTooltip(utils, cond.getExact()).build("ali.property.value.exact"))
                )
                .build("ali.type.condition.any_structure");
    }

    @NotNull
    public static TooltipNode biomeCheckTooltip(IServerUtils utils, BiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getBiomes()).build("ali.property.branch.biomes"))
                        .add(utils.getValueTooltip(utils, cond.getTags()).build("ali.property.branch.tags"))
                )
                .build("ali.type.condition.biome");
    }

    @NotNull
    public static TooltipNode containsLootConditionTooltip(IServerUtils utils, ContainsLootCondition condition) {
        MixinContainsLootCondition cond = (MixinContainsLootCondition) condition;

        return TooltipBuilder.array((b) -> b
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                        .add(utils.getValueTooltip(utils, cond.getExact()).build("ali.property.value.exact"))
                )
                .build("ali.type.condition.match_loot");
    }

    @NotNull
    public static TooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.entity_predicate");
        } else if (cond.getParam() == LootContextParams.KILLER_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.killer_predicate");
        } else if (cond.getParam() == LootContextParams.DIRECT_KILLER_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.direct_killer_predicate");
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.block_predicate");
        } else {
            return TooltipNode.EMPTY_INSTANCE;
        }
    }

    @NotNull
    public static TooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, IntRange.range(cond.getMin(), cond.getMax())).build("ali.property.value.value"))
                )
                .build("ali.type.condition.light_level");
    }

    @NotNull
    public static TooltipNode lootItemConditionWrapperTooltip(IServerUtils utils, LootItemConditionWrapper condition) {
        return utils.getConditionTooltip(utils, ((MixinLootItemConditionWrapper) condition).getCondition());
    }

    @NotNull
    public static TooltipNode mainHandTableBonusTooltip(IServerUtils utils, MainHandTableBonus condition) {
        MixinMainHandTableBonus cond = (MixinMainHandTableBonus) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getEnchantment()).build("ali.property.value.enchantment"))
                        .add(utils.getValueTooltip(utils, Arrays.toString(cond.getValues())).build("ali.property.value.values"))
                )
                .build("ali.type.condition.random_chance_with_enchantment");
    }

    @NotNull
    public static TooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        switch (cond.getSlot()) {
            case MAINHAND -> {
                return TooltipBuilder.array((b) -> b
                                .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_mainhand");
            }
            case OFFHAND -> {
                return TooltipBuilder.array((b) -> b
                                .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_offhand");
            }
            default -> {
                return TooltipBuilder.array((b) -> b
                                .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                                .add(utils.getValueTooltip(utils, cond.getSlot()).build("ali.property.value.slot"))
                        )
                        .build("ali.type.condition.match_equipment_slot");
            }
        }
    }

    @NotNull
    public static TooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        MixinMatchKillerDistance cond = (MixinMatchKillerDistance) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.value.predicate"))
                )
                .build("ali.type.condition.distance_to_killer");
    }

    @NotNull
    public static TooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        MixinMatchPlayer cond = (MixinMatchPlayer) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.value.predicate"))
                )
                .build("ali.type.condition.match_player");
    }

    @NotNull
    public static TooltipNode notConditionTooltip(IServerUtils utils, NotCondition condition) {
        MixinNotCondition cond = (MixinNotCondition) condition;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getConditionTooltip(utils, (LootItemCondition) cond.getCondition()))
                )
                .build("ali.type.condition.not");
    }

    @NotNull
    public static TooltipNode orConditionTooltip(IServerUtils utils, OrCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinOrCondition) condition).getConditions());

        return GenericTooltipUtils.getConditionsTooltip(utils, conditions).build("ali.type.condition.or");
    }

    @NotNull
    public static TooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                )
                .build("ali.type.condition.player_predicate");
    }

    @NotNull
    public static TooltipNode wrapperDamageSourceConditionTooltip(IServerUtils utils, WrappedDamageSourceCondition condition) {
        MixinWrappedDamageSourceCondition cond = (MixinWrappedDamageSourceCondition) condition;
        List<String> sourceNames = cond.getSourceNames() != null ? Arrays.asList(cond.getSourceNames()) : null;

        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.branch.predicate"))
                        .add(utils.getValueTooltip(utils, sourceNames).build("ali.property.branch.source_names"))
                )
                .build("ali.type.condition.match_damage_source");
    }
}
