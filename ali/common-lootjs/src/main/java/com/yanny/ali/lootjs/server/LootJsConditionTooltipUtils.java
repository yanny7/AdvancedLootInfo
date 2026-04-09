package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.lootjs.mixin.MixinCustomParamPredicate;
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
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, condition.biomes()).build("ali.property.branch.biomes"))
                .build("ali.type.condition.match_biome");
    }

    @NotNull
    public static ITooltipNode matchDimensionTooltip(IServerUtils utils, MatchDimension condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, Arrays.asList(condition.dimensions())).build("ali.property.branch.dimensions"))
                .build("ali.type.condition.match_dimension");
    }

    @NotNull
    public static ITooltipNode matchStructureTooltip(IServerUtils utils, MatchStructure condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, condition.structures()).build("ali.property.branch.structures"))
                .add(utils.getValueTooltip(utils, condition.exact()).build("ali.property.value.exact"))
                .build("ali.type.condition.match_structure");
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.match_entity_custom");
        } else if (cond.getParam() == LootContextParams.ATTACKING_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.match_attacker_custom");
        } else if (cond.getParam() == LootContextParams.DIRECT_ATTACKING_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.match_direct_attacker_custom");
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return BranchTooltipNode.branch()
                    .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                    .build("ali.type.condition.block_entity");
        } else {
            return EmptyTooltipNode.EMPTY;
        }
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, IntRange.range(condition.min(), condition.max())).build("ali.property.value.value"))
                .build("ali.type.condition.is_light_level");
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        switch (condition.slot()) {
            case MAINHAND -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_mainhand");
            }
            case OFFHAND -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_offhand");
            }
            case FEET -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_feet");
            }
            case LEGS -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_legs");
            }
            case CHEST -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_chest");
            }
            case HEAD -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .build("ali.type.condition.match_head");
            }
            default -> {
                return BranchTooltipNode.branch()
                        .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        .add(utils.getValueTooltip(utils, condition.slot()).build("ali.property.value.slot"))
                        .build("ali.type.condition.match_equipment_slot");
            }
        }
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, condition.predicate()).build("ali.property.value.predicate"))
                .build("ali.type.condition.match_distance");
    }

    @NotNull
    public static ITooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, condition.predicate()).build("ali.property.value.predicate"))
                .build("ali.type.condition.match_player");
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return BranchTooltipNode.branch()
                .add(LiteralTooltipNode.translatable("ali.property.value.detail_not_available"))
                .build("ali.type.condition.match_player_custom");
    }

    @NotNull
    public static ITooltipNode matchAnyInventorySlot(IServerUtils utils, MatchAnyInventorySlot condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, condition.filter()).build("ali.property.branch.predicate"))
                .add(utils.getValueTooltip(utils, condition.hotbar()).build("ali.property.value.hotbar"))
                .build("ali.type.condition.match_any_inventory_slot");
    }
}
