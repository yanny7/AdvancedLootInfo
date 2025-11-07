package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.mixin.*;
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

        return getCollectionTooltip(utils, conditions, utils::getConditionTooltip).key("ali.type.condition.and");
    }

    @NotNull
    public static ITooltipNode anyBiomeCheckTooltip(IServerUtils utils, AnyBiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return BranchTooltipNode.branch("ali.type.condition.any_biome")
                .add(utils.getValueTooltip(utils, cond.getBiomes()).key("ali.property.branch.biomes"))
                .add(utils.getValueTooltip(utils, cond.getTags()).key("ali.property.branch.tags"));
    }

    @NotNull
    public static ITooltipNode anyDimensionTooltip(IServerUtils utils, AnyDimension condition) {
        MixinAnyDimension cond = (MixinAnyDimension) condition;

        return BranchTooltipNode.branch("ali.type.condition.any_dimension")
                .add(utils.getValueTooltip(utils, Arrays.asList(cond.getDimensions())).key("ali.property.branch.dimensions"));
    }

    @NotNull
    public static ITooltipNode anyStructureTooltip(IServerUtils utils, AnyStructure condition) {
        MixinAnyStructure cond = (MixinAnyStructure) condition;

        return BranchTooltipNode.branch("ali.type.condition.any_structure")
                .add(utils.getValueTooltip(utils, cond.getStructureLocators()).key("ali.property.branch.structures"))
                .add(utils.getValueTooltip(utils, cond.getExact()).key("ali.property.value.exact"));
    }

    @NotNull
    public static ITooltipNode biomeCheckTooltip(IServerUtils utils, BiomeCheck condition) {
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        return BranchTooltipNode.branch("ali.type.condition.biome")
                .add(utils.getValueTooltip(utils, cond.getBiomes()).key("ali.property.branch.biomes"))
                .add(utils.getValueTooltip(utils, cond.getTags()).key("ali.property.branch.tags"));
    }

    @NotNull
    public static ITooltipNode containsLootConditionTooltip(IServerUtils utils, ContainsLootCondition condition) {
        MixinContainsLootCondition cond = (MixinContainsLootCondition) condition;

        return BranchTooltipNode.branch("ali.type.condition.match_loot")
                .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).key("ali.property.value.item_filter"))
                .add(utils.getValueTooltip(utils, cond.getExact()).key("ali.property.value.exact"));
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.entity_predicate")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else if (cond.getParam() == LootContextParams.KILLER_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.killer_predicate")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else if (cond.getParam() == LootContextParams.DIRECT_KILLER_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.direct_killer_predicate")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.block_predicate")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else {
            return EmptyTooltipNode.EMPTY;
        }
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        return BranchTooltipNode.branch("ali.type.condition.light_level")
                .add(utils.getValueTooltip(utils, IntRange.range(cond.getMin(), cond.getMax())).key("ali.property.value.value"));
    }

    @NotNull
    public static ITooltipNode lootItemConditionWrapperTooltip(IServerUtils utils, LootItemConditionWrapper condition) {
        return utils.getConditionTooltip(utils, ((MixinLootItemConditionWrapper) condition).getCondition());
    }

    @NotNull
    public static ITooltipNode mainHandTableBonusTooltip(IServerUtils utils, MainHandTableBonus condition) {
        MixinMainHandTableBonus cond = (MixinMainHandTableBonus) condition;

        return BranchTooltipNode.branch("ali.type.condition.random_chance_with_enchantment")
                .add(utils.getValueTooltip(utils, cond.getEnchantment()).key("ali.property.value.enchantment"))
                .add(utils.getValueTooltip(utils, Arrays.toString(cond.getValues())).key("ali.property.value.values"));
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        switch (cond.getSlot()) {
            case MAINHAND -> {
                return BranchTooltipNode.branch("ali.type.condition.match_mainhand")
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).key("ali.property.value.item_filter"));
            }
            case OFFHAND -> {
                return BranchTooltipNode.branch("ali.type.condition.match_offhand")
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).key("ali.property.value.item_filter"));
            }
            default -> {
                return BranchTooltipNode.branch("ali.type.condition.match_equipment_slot")
                        .add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, cond.getPredicate()).key("ali.property.value.item_filter"))
                        .add(utils.getValueTooltip(utils, cond.getSlot()).key("ali.property.value.slot"));
            }
        }
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        MixinMatchKillerDistance cond = (MixinMatchKillerDistance) condition;

        return BranchTooltipNode.branch("ali.type.condition.distance_to_killer")
                .add(utils.getValueTooltip(utils, cond.getPredicate()).key("ali.property.value.predicate"));
    }

    @NotNull
    public static ITooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        MixinMatchPlayer cond = (MixinMatchPlayer) condition;

        return BranchTooltipNode.branch("ali.type.condition.match_player")
                .add(utils.getValueTooltip(utils, cond.getPredicate()).key("ali.property.value.predicate"));
    }

    @NotNull
    public static ITooltipNode notConditionTooltip(IServerUtils utils, NotCondition condition) {
        MixinNotCondition cond = (MixinNotCondition) condition;

        return BranchTooltipNode.branch("ali.type.condition.not")
                .add(utils.getConditionTooltip(utils, (LootItemCondition) cond.getCondition()));
    }

    @NotNull
    public static ITooltipNode orConditionTooltip(IServerUtils utils, OrCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinOrCondition) condition).getConditions());

        return getCollectionTooltip(utils, conditions, utils::getConditionTooltip).key("ali.type.condition.or");
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return BranchTooltipNode.branch("ali.type.condition.player_predicate")
                .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
    }

    @NotNull
    public static ITooltipNode wrapperDamageSourceConditionTooltip(IServerUtils utils, WrappedDamageSourceCondition condition) {
        MixinWrappedDamageSourceCondition cond = (MixinWrappedDamageSourceCondition) condition;
        List<String> sourceNames = cond.getSourceNames() != null ? Arrays.asList(cond.getSourceNames()) : null;

        return BranchTooltipNode.branch("ali.type.condition.match_damage_source")
                .add(utils.getValueTooltip(utils, cond.getPredicate()).key("ali.property.branch.predicate"))
                .add(utils.getValueTooltip(utils, sourceNames).key("ali.property.branch.source_names"));
    }
}
