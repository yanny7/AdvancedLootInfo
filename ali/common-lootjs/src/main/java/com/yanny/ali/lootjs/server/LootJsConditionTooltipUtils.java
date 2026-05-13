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
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, condition.biomes()).build("ali.property.branch.biomes")))
                .key("ali.type.condition.match_biome");
    }

    @NotNull
    public static TooltipNode matchDimensionTooltip(IServerUtils utils, MatchDimension condition) {
        return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, Arrays.asList(condition.dimensions())).build("ali.property.branch.dimensions"))
                )
                .build("ali.type.condition.match_dimension");
    }

    @NotNull
    public static TooltipNode matchStructureTooltip(IServerUtils utils, MatchStructure condition) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, condition.structures()).build("ali.property.branch.structures"))
                        .add(utils.getValueTooltip(utils, condition.exact()).build("ali.property.value.exact"))
                )
                .build("ali.type.condition.match_structure");
    }

    @NotNull
    public static TooltipBuilder customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.match_entity_custom");
        } else if (cond.getParam() == LootContextParams.ATTACKING_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.match_attacker_custom");
        } else if (cond.getParam() == LootContextParams.DIRECT_ATTACKING_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.match_direct_attacker_custom");
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            return TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                    )
                    .build("ali.type.condition.block_entity");
        } else {
            return TooltipBuilder.empty();
        }
    }

    @NotNull
    public static TooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, IntRange.range(condition.min(), condition.max())).build("ali.property.value.value"))
                )
                .build("ali.type.condition.is_light_level");
    }

    @NotNull
    public static TooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        switch (condition.slot()) {
            case MAINHAND -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_mainhand");
            }
            case OFFHAND -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_offhand");
            }
            case FEET -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_feet");
            }
            case LEGS -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_legs");
            }
            case CHEST -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_chest");
            }
            case HEAD -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                        )
                        .build("ali.type.condition.match_head");
            }
            default -> {
                return TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, condition.itemFilter()).build("ali.property.value.item_filter"))
                                .add(utils.getValueTooltip(utils, condition.slot()).build("ali.property.value.slot"))
                        )
                        .build("ali.type.condition.match_equipment_slot");
            }
        }
    }

    @NotNull
    public static TooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, condition.predicate()).build("ali.property.value.predicate"))
                )
                .build("ali.type.condition.match_distance");
    }

    @NotNull
    public static TooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, condition.predicate()).build("ali.property.value.predicate"))
                )
                .build("ali.type.condition.match_player");
    }

    @NotNull
    public static TooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        return TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly("ali.property.value.detail_not_available"))
                )
                .build("ali.type.condition.match_player_custom");
    }

    @NotNull
    public static TooltipNode matchAnyInventorySlot(IServerUtils utils, MatchAnyInventorySlot condition) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, condition.filter()).build("ali.property.branch.predicate"))
                        .add(utils.getValueTooltip(utils, condition.hotbar()).build("ali.property.value.hotbar"))
                )
                .build("ali.type.condition.match_any_inventory_slot");
    }
}
