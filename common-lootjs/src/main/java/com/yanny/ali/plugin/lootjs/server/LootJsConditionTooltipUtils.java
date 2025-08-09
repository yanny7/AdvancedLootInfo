package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.mixin.MixinCustomParamPredicate;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.yanny.ali.plugin.lootjs.server.LootJsGenericTooltipUtils.getItemFilterTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static ITooltipNode matchBiomeTooltip(IServerUtils utils, MatchBiome condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_biome"));

        tooltip.add(getHolderSetTooltip(utils, "ali.property.branch.biomes", "ali.property.value.null", condition.biomes(), RegistriesTooltipUtils::getBiomeTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchDimensionTooltip(IServerUtils utils, MatchDimension condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_dimension"));

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.dimensions", "ali.property.value.null", Arrays.asList(condition.dimensions()), GenericTooltipUtils::getResourceLocationTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchStructureTooltip(IServerUtils utils, MatchStructure condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_structure"));

        tooltip.add(getHolderSetTooltip(utils, "ali.property.branch.structures", "ali.property.value.null", condition.structures(), RegistriesTooltipUtils::getStructureTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.exact", condition.exact()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_entity_custom"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else if (cond.getParam() == LootContextParams.ATTACKING_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_attacker_custom"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else if (cond.getParam() == LootContextParams.DIRECT_ATTACKING_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_direct_attacker_custom"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.block_entity"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else {
            return new TooltipNode();
        }
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.is_light_level"));

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.value", IntRange.range(condition.min(), condition.max())));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        switch (condition.slot()) {
            case MAINHAND -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_mainhand"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));

                return tooltip;
            }
            case OFFHAND -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_offhand"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));

                return tooltip;
            }
            case FEET -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_feet"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));

                return tooltip;
            }
            case LEGS -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_legs"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));

                return tooltip;
            }
            case CHEST -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_chest"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));

                return tooltip;
            }
            case HEAD -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_head"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));

                return tooltip;
            }
            default -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_equipment_slot"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", condition.itemFilter()));
                tooltip.add(GenericTooltipUtils.getEnumTooltip(utils, "ali.property.value.slot", condition.slot()));

                return tooltip;
            }
        }
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_distance"));

        tooltip.add(getDistancePredicateTooltip(utils, "ali.property.value.predicate", condition.predicate()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_player"));

        tooltip.add(getEntityPredicateTooltip(utils, "ali.property.value.predicate", condition.predicate()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_player_custom"));

        tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchAnyInventorySlot(IServerUtils utils, MatchAnyInventorySlot condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_any_inventory_slot"));

        tooltip.add(getItemFilterTooltip(utils, "ali.property.branch.predicate", condition.filter()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.hotbar", condition.hotbar()));

        return tooltip;
    }
}
