package com.yanny.ali.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.lootjs.mixin.MixinCustomParamPredicate;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static TooltipBuilder matchBiomeTooltip(IServerUtils utils, MatchBiome condition) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.biomes()).build(Lang.Branch.BIOMES)))
                .key(Lang.Conditions.MATCH_BIOME);
    }

    @NotNull
    public static TooltipBuilder matchDimensionTooltip(IServerUtils utils, MatchDimension condition) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, Arrays.asList(condition.dimensions())).build(Lang.Branch.DIMENSIONS)))
                .key(Lang.Conditions.MATCH_DIMENSION);
    }

    @NotNull
    public static TooltipBuilder matchStructureTooltip(IServerUtils utils, MatchStructure condition) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, condition.structures()).build(Lang.Branch.STRUCTURES));
            b.add(utils.getValueTooltip(utils, condition.exact()).build(Lang.Value.EXACT));
        }).key(Lang.Conditions.MATCH_STRUCTURE);
    }

    @NotNull
    public static TooltipBuilder customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.MATCH_ENTITY_CUSTOM);
        } else if (cond.getParam() == LootContextParams.ATTACKING_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.MATCH_ATTACKER_CUSTOM);
        } else if (cond.getParam() == LootContextParams.DIRECT_ATTACKING_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.MATCH_DIRECT_ATTACKER_CUSTOM);
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                    .key(Lang.Conditions.BLOCK_ENTITY);
        } else {
            return TooltipBuilder.empty();
        }
    }

    @NotNull
    public static TooltipBuilder isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, IntRange.range(condition.min(), condition.max())).build(Lang.Value.VALUE)))
                .key(Lang.Conditions.IS_LIGHT_LEVEL);
    }

    @NotNull
    public static TooltipBuilder getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        switch (condition.slot()) {
            case MAINHAND -> {
                return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_MAINHAND);
            }
            case OFFHAND -> {
                return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_OFFHAND);
            }
            case FEET -> {
                return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_FEET);
            }
            case LEGS -> {
                return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_LEGS);
            }
            case CHEST -> {
                return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_CHEST);
            }
            case HEAD -> {
                return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER)))
                        .key(Lang.Conditions.MATCH_HEAD);
            }
            default -> {
                return TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, condition.itemFilter()).build(Lang.Value.ITEM_FILTER));
                    b.add(utils.getValueTooltip(utils, condition.slot()).build(Lang.Value.SLOT));
                }).key(Lang.Conditions.MATCH_EQUIPMENT_SLOT);
            }
        }
    }

    @NotNull
    public static TooltipBuilder matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.predicate()).build(Lang.Branch.PREDICATE)))
                .key(Lang.Conditions.MATCH_DISTANCE);
    }

    @NotNull
    public static TooltipBuilder matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.predicate()).build(Lang.Branch.PREDICATE)))
                .key(Lang.Conditions.MATCH_PLAYER);
    }

    @NotNull
    public static TooltipBuilder playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return TooltipBuilder.array((b) -> b.add(TooltipBuilder.keyOnly(Lang.Error.DETAIL_NOT_AVAILABLE)))
                .key(Lang.Conditions.MATCH_PLAYER_CUSTOM);
    }

    @NotNull
    public static TooltipBuilder matchAnyInventorySlot(IServerUtils utils, MatchAnyInventorySlot condition) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, condition.filter()).build(Lang.Branch.PREDICATE));
            b.add(utils.getValueTooltip(utils, condition.hotbar()).build(Lang.Value.HOTBAR));
        }).key(Lang.Conditions.MATCH_ANY_INVENTORY_SLOT);
    }
}
