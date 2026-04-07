package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.lootjs.mixin.*;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getCollectionTooltip;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static ITooltipNode andConditionTooltip(IServerUtils utils, AndCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinAndCondition) condition).getConditions());

        return getCollectionTooltip(utils, conditions, utils::getConditionTooltip).build("ali.type.condition.and");
    }

    @NotNull
    public static ITooltipNode anyBiomeCheckTooltip(IServerUtils utils, AnyBiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getBiomes()).build("ali.property.branch.biomes"))
                .add(utils.getValueTooltip(utils, cond.getTags()).build("ali.property.branch.tags"))
                .build("ali.type.condition.any_biome");
    }

    @NotNull
    public static ITooltipNode anyDimensionTooltip(IServerUtils utils, AnyDimension condition) {
        MixinAnyDimension cond = (MixinAnyDimension) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, Arrays.asList(cond.getDimensions())).build("ali.property.branch.dimensions"))
                .build("ali.type.condition.any_dimension");
    }

    @NotNull
    public static ITooltipNode anyStructureTooltip(IServerUtils utils, AnyStructure condition) {
        MixinAnyStructure cond = (MixinAnyStructure) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getStructureLocators()).build("ali.property.branch.structures"))
                .add(utils.getValueTooltip(utils, cond.getExact()).build("ali.property.value.exact"))
                .build("ali.type.condition.any_structure");
    }

    @NotNull
    public static ITooltipNode biomeCheckTooltip(IServerUtils utils, BiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getBiomes()).build("ali.property.branch.biomes"))
                .add(utils.getValueTooltip(utils, cond.getTags()).build("ali.property.branch.tags"))
                .build("ali.type.condition.biome");
    }

    @NotNull
    public static ITooltipNode containsLootConditionTooltip(IServerUtils utils, ContainsLootCondition condition) {
        MixinContainsLootCondition cond = (MixinContainsLootCondition) condition;

        return BranchTooltipNode.branch()
                .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                .add(utils.getValueTooltip(utils, cond.getExact()).build("ali.property.value.exact"))
                .build("ali.type.condition.match_loot");
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.entity_predicate");
        } else if (cond.getParam() == LootContextParams.KILLER_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.killer_predicate");
        } else if (cond.getParam() == LootContextParams.DIRECT_KILLER_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.direct_killer_predicate");
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.block_predicate");
        } else {
            return EmptyTooltipNode.EMPTY;
        }
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, IntRange.range(cond.getMin(), cond.getMax())).build("ali.property.value.value"))
                .build("ali.type.condition.light_level");
    }

    @NotNull
    public static ITooltipNode lootItemConditionWrapperTooltip(IServerUtils utils, LootItemConditionWrapper condition) {
        return utils.getConditionTooltip(utils, ((MixinLootItemConditionWrapper) condition).getCondition());
    }

    @NotNull
    public static ITooltipNode mainHandTableBonusTooltip(IServerUtils utils, MainHandTableBonus condition) {
        MixinMainHandTableBonus cond = (MixinMainHandTableBonus) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getEnchantment()).build("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, Arrays.toString(cond.getValues())).build("ali.property.value.values"))
                .build("ali.type.condition.random_chance_with_enchantment");
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        switch (cond.getSlot()) {
            case MAINHAND -> {
                return BranchTooltipNode.branch()
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_mainhand");
            }
            case OFFHAND -> {
                return BranchTooltipNode.branch()
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_offhand");
            }
            default -> {
                return BranchTooltipNode.branch()
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).build("ali.property.value.item_filter"))
                        .add(utils.getValueTooltip(utils, cond.getSlot()).build("ali.property.value.slot"))
                        .build("ali.type.condition.match_equipment_slot");
            }
        }
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        MixinMatchKillerDistance cond = (MixinMatchKillerDistance) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.value.predicate"))
                .build("ali.type.condition.distance_to_killer");
    }

    @NotNull
    public static ITooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        MixinMatchPlayer cond = (MixinMatchPlayer) condition;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.value.predicate"))
                .build("ali.type.condition.match_player");
    }

    @NotNull
    public static ITooltipNode notConditionTooltip(IServerUtils utils, NotCondition condition) {
        MixinNotCondition cond = (MixinNotCondition) condition;

        return BranchTooltipNode.branch()
                .add(utils.getConditionTooltip(utils, (LootItemCondition) cond.getCondition()))
                .build("ali.type.condition.not");
    }

    @NotNull
    public static ITooltipNode orConditionTooltip(IServerUtils utils, OrCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinOrCondition) condition).getConditions());

        return getCollectionTooltip(utils, conditions, utils::getConditionTooltip).build("ali.type.condition.or");
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return BranchTooltipNode.branch()
                .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                .build("ali.type.condition.player_predicate");
    }

    @NotNull
    public static ITooltipNode wrapperDamageSourceConditionTooltip(IServerUtils utils, WrappedDamageSourceCondition condition) {
        MixinWrappedDamageSourceCondition cond = (MixinWrappedDamageSourceCondition) condition;
        List<String> sourceNames = cond.getSourceNames() != null ? Arrays.asList(cond.getSourceNames()) : null;

        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, cond.getPredicate()).build("ali.property.branch.predicate"))
                .add(utils.getValueTooltip(utils, sourceNames).build("ali.property.branch.source_names"))
                .build("ali.type.condition.match_damage_source");
    }
}
