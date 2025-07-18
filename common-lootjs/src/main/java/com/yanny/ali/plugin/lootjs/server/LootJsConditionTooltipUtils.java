package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.mixin.*;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.yanny.ali.plugin.lootjs.server.LootJsGenericTooltipUtils.getItemFilterTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static ITooltipNode matchBiomeTooltip(IServerUtils utils, MatchBiome condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_biome"));
        MixinMatchBiome cond = (MixinMatchBiome) condition;

        tooltip.add(getHolderSetTooltip(utils, "ali.property.branch.biomes", "ali.property.value.null", cond.getBiomes(), RegistriesTooltipUtils::getBiomeTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchDimensionTooltip(IServerUtils utils, MatchDimension condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_dimension"));
        MixinMatchDimension cond = (MixinMatchDimension) condition;

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.dimensions", "ali.property.value.null", Arrays.asList(cond.getDimensions()), GenericTooltipUtils::getResourceLocationTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> ignoredCondition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.custom_param_predicate"));

        tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.is_light_level"));
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.value", IntRange.range(cond.getMin(), cond.getMax())));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_equipment_slot"));
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        tooltip.add(getItemFilterTooltip(utils, "ali.property.value.item_filter", cond.getItemFilter()));
        tooltip.add(GenericTooltipUtils.getEnumTooltip(utils, "ali.property.value.slot", cond.getSlot()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_killer_distance"));
        MixinMatchKillerDistance cond = (MixinMatchKillerDistance) condition;

        tooltip.add(getDistancePredicateTooltip(utils, "ali.property.value.predicate", cond.getPredicate()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode matchPlayerTooltip(IServerUtils utils, MatchPlayer condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_player"));
        MixinMatchPlayer cond = (MixinMatchPlayer) condition;

        tooltip.add(getEntityPredicateTooltip(utils, "ali.property.value.predicate", cond.getPredicate()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.player_param_predicate"));

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
