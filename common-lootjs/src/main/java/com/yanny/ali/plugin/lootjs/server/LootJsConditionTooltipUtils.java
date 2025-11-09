package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.mixin.MixinCustomParamPredicate;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static ITooltipNode matchBiomeTooltip(IServerUtils utils, MatchBiome condition) {
        return BranchTooltipNode.branch("ali.type.condition.match_biome")
               .add(utils.getValueTooltip(utils, condition.biomes()).key("ali.property.branch.biomes"));
    }

    @NotNull
    public static ITooltipNode matchDimensionTooltip(IServerUtils utils, MatchDimension condition) {
        return BranchTooltipNode.branch("ali.type.condition.match_dimension")
               .add(utils.getValueTooltip(utils, Arrays.asList(condition.dimensions())).key("ali.property.branch.dimensions"));
    }

    @NotNull
    public static ITooltipNode matchStructureTooltip(IServerUtils utils, MatchStructure condition) {
        return BranchTooltipNode.branch("ali.type.condition.match_structure")
               .add(utils.getValueTooltip(utils, condition.structures()).key("ali.property.branch.structures"))
               .add(utils.getValueTooltip(utils, condition.exact()).key("ali.property.value.exact"));
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.match_entity_custom")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else if (cond.getParam() == LootContextParams.ATTACKING_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.match_attacker_custom")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else if (cond.getParam() == LootContextParams.DIRECT_ATTACKING_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.match_direct_attacker_custom")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return BranchTooltipNode.branch("ali.type.condition.block_entity")
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
        } else {
            return EmptyTooltipNode.EMPTY;
        }
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        return BranchTooltipNode.branch("ali.type.condition.is_light_level")
               .add(utils.getValueTooltip(utils, IntRange.range(condition.min(), condition.max())).key("ali.property.value.value"));
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        switch (condition.slot()) {
            case MAINHAND -> {
                return BranchTooltipNode.branch("ali.type.condition.match_mainhand")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"));
            }
            case OFFHAND -> {
                return BranchTooltipNode.branch("ali.type.condition.match_offhand")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"));
            }
            case FEET -> {
                return BranchTooltipNode.branch("ali.type.condition.match_feet")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"));
            }
            case LEGS -> {
                return BranchTooltipNode.branch("ali.type.condition.match_legs")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"));
            }
            case CHEST -> {
                return BranchTooltipNode.branch("ali.type.condition.match_chest")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"));
            }
            case HEAD -> {
                return BranchTooltipNode.branch("ali.type.condition.match_head")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"));
            }
            default -> {
                return BranchTooltipNode.branch("ali.type.condition.match_equipment_slot")
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).key("ali.property.value.item_filter"))
                        .add(utils.getValueTooltip(utils, condition.slot()).key("ali.property.value.slot"));
            }
        }
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        return BranchTooltipNode.branch("ali.type.condition.match_distance")
               .add(utils.getValueTooltip(utils, condition.predicate()).key("ali.property.value.predicate"));
    }

    @NotNull
    public static ITooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        return BranchTooltipNode.branch("ali.type.condition.match_player")
               .add(utils.getValueTooltip(utils, condition.predicate()).key("ali.property.value.predicate"));
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return BranchTooltipNode.branch("ali.type.condition.match_player_custom")
               .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"));
    }

    @NotNull
    public static ITooltipNode matchAnyInventorySlot(IServerUtils utils, MatchAnyInventorySlot condition) {
        return BranchTooltipNode.branch("ali.type.condition.match_any_inventory_slot")
               .add(utils.getValueTooltip(utils, condition.filter()).key("ali.property.branch.predicate"))
               .add(utils.getValueTooltip(utils, condition.hotbar()).key("ali.property.value.hotbar"));
    }
}
