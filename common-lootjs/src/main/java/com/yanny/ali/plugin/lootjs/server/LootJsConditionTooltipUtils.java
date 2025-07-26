package com.yanny.ali.plugin.lootjs.server;

import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.mixin.*;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class LootJsConditionTooltipUtils {
    @NotNull
    public static ITooltipNode andConditionTooltip(IServerUtils utils, AndCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinAndCondition) condition).getConditions());

        return getCollectionTooltip(utils, "ali.type.condition.and", conditions, utils::getConditionTooltip);
    }

    @NotNull
    public static ITooltipNode anyBiomeCheckTooltip(IServerUtils utils, AnyBiomeCheck condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.any_biome"));
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.biomes", "ali.property.value.null", cond.getBiomes(), GenericTooltipUtils::getResourceKeyTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.tags", "ali.property.value.null", cond.getTags(), GenericTooltipUtils::getTagKeyTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode anyDimensionTooltip(IServerUtils utils, AnyDimension condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.any_dimension"));
        MixinAnyDimension cond = (MixinAnyDimension) condition;

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.dimensions", "ali.property.value.null", Arrays.asList(cond.getDimensions()), GenericTooltipUtils::getResourceLocationTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode anyStructureTooltip(IServerUtils utils, AnyStructure condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.any_structure"));
        MixinAnyStructure cond = (MixinAnyStructure) condition;

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.structures", "ali.property.value.null", cond.getStructureLocators(), LootJsGenericTooltipUtils::getStructureLocatorTooltip));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.exact", cond.getExact()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode biomeCheckTooltip(IServerUtils utils, BiomeCheck condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.biome"));
        MixinBiomeCheck cond = (MixinBiomeCheck) condition;

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.biomes", "ali.property.value.null", cond.getBiomes(), GenericTooltipUtils::getResourceKeyTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.tags", "ali.property.value.null", cond.getTags(), GenericTooltipUtils::getTagKeyTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode containsLootConditionTooltip(IServerUtils utils, ContainsLootCondition condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_loot"));
        MixinContainsLootCondition cond = (MixinContainsLootCondition) condition;

        tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", cond.getPredicate()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.exact", cond.getExact()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode customParamPredicateTooltip(IServerUtils ignoredUtils, CustomParamPredicate<?> condition) {
        MixinCustomParamPredicate<?> cond = (MixinCustomParamPredicate<?>) condition;

        if (cond.getParam() == LootContextParams.THIS_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.entity_predicate"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else if (cond.getParam() == LootContextParams.KILLER_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.killer_predicate"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else if (cond.getParam() == LootContextParams.DIRECT_KILLER_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.direct_killer_predicate"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else if (cond.getParam() == LootContextParams.BLOCK_ENTITY) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.block_predicate"));

            tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

            return tooltip;
        } else {
            return TooltipNode.EMPTY;
        }
    }

    @NotNull
    public static ITooltipNode isLightLevelTooltip(IServerUtils utils, IsLightLevel condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.light_level"));
        MixinIsLightLevel cond = (MixinIsLightLevel) condition;

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.value", IntRange.range(cond.getMin(), cond.getMax())));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode lootItemConditionWrapperTooltip(IServerUtils utils, LootItemConditionWrapper condition) {
        return utils.getConditionTooltip(utils, ((MixinLootItemConditionWrapper) condition).getCondition());
    }

    @NotNull
    public static ITooltipNode mainHandTableBonusTooltip(IServerUtils utils, MainHandTableBonus condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.random_chance_with_enchantment"));
        MixinMainHandTableBonus cond = (MixinMainHandTableBonus) condition;

        tooltip.add(RegistriesTooltipUtils.getEnchantmentTooltip(utils, "ali.property.value.enchantment", cond.getEnchantment()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.values", Arrays.toString(cond.getValues())));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMatchEquipmentSlotTooltip(IServerUtils utils, MatchEquipmentSlot condition) {
        MixinMatchEquipmentSlot cond = (MixinMatchEquipmentSlot) condition;

        switch (cond.getSlot()) {
            case MAINHAND -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_mainhand"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", cond.getPredicate()));

                return tooltip;
            }
            case OFFHAND -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_offhand"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", cond.getPredicate()));

                return tooltip;
            }
            default -> {
                ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_equipment_slot"));

                tooltip.add(LootJsGenericTooltipUtils.getItemFilterTooltip(utils, "ali.property.value.item_filter", cond.getPredicate()));
                tooltip.add(GenericTooltipUtils.getEnumTooltip(utils, "ali.property.value.slot", cond.getSlot()));

                return tooltip;
            }
        }
    }

    @NotNull
    public static ITooltipNode matchKillerDistanceTooltip(IServerUtils utils, MatchKillerDistance condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.distance_to_killer"));
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
    public static ITooltipNode notConditionTooltip(IServerUtils utils, NotCondition condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.not"));
        MixinNotCondition cond = (MixinNotCondition) condition;

        tooltip.add(utils.getConditionTooltip(utils, (LootItemCondition) cond.getCondition()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode orConditionTooltip(IServerUtils utils, OrCondition condition) {
        //noinspection unchecked
        List<LootItemCondition> conditions = (List<LootItemCondition>)(List<?>)List.of(((MixinOrCondition) condition).getConditions());

        return getCollectionTooltip(utils, "ali.type.condition.or", conditions, utils::getConditionTooltip);
    }

    @NotNull
    public static ITooltipNode playerParamPredicateTooltip(IServerUtils ignoredUtils, PlayerParamPredicate ignoredCondition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.player_predicate"));

        tooltip.add(new TooltipNode(translatable("ali.property.value.detail_not_available")));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode wrapperDamageSourceConditionTooltip(IServerUtils utils, WrappedDamageSourceCondition condition) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.match_damage_source"));
        MixinWrappedDamageSourceCondition cond = (MixinWrappedDamageSourceCondition) condition;
        List<String> sourceNames = cond.getSourceNames() != null ? Arrays.asList(cond.getSourceNames()) : null;

        tooltip.add(getDamageSourcePredicateTooltip(utils, "ali.property.branch.predicate", cond.getPredicate()));
        tooltip.add(getOptionalCollectionTooltip(utils, "ali.property.branch.source_names", "ali.property.value.null", sourceNames, GenericTooltipUtils::getStringTooltip));

        return tooltip;
    }
}
