package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.DecimalFormat;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class ConditionTooltipUtils {
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.####");

    @NotNull
    public static ITooltipNode getAllOfTooltip(IServerUtils utils, AllOfCondition cond) {
        return getCollectionTooltip(utils, "ali.type.condition.all_of", cond.terms, utils::getConditionTooltip);
    }

    @NotNull
    public static ITooltipNode getAnyOfTooltip(IServerUtils utils, AnyOfCondition cond) {
        return getCollectionTooltip(utils, "ali.type.condition.any_of", cond.terms, utils::getConditionTooltip);
    }

    @NotNull
    public static ITooltipNode getBlockStatePropertyTooltip(IServerUtils utils, LootItemBlockStatePropertyCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.block_state_property"));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.block", cond.block(), RegistriesTooltipUtils::getBlockTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.properties", cond.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDamageSourcePropertiesTooltip(IServerUtils utils, DamageSourceCondition cond) {
        return getOptionalTooltip(utils, "ali.type.condition.damage_source_properties", cond.predicate(), GenericTooltipUtils::getDamageSourcePredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getEnchantActiveCheckTooltip(IServerUtils utils, EnchantmentActiveCheck cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.enchantment_active_check"));

        tooltip.add(getBooleanTooltip(utils, "ali.property.value.active", cond.active()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEntityPropertiesTooltip(IServerUtils utils, LootItemEntityPropertyCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.entity_properties"));

        tooltip.add(getEnumTooltip(utils, "ali.property.value.target", cond.entityTarget()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.predicate", cond.predicate(), GenericTooltipUtils::getEntityPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEntityScoresTooltip(IServerUtils utils, EntityHasScoreCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.entity_scores"));

        tooltip.add(getEnumTooltip(utils, "ali.property.value.target", cond.entityTarget()));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.scores", cond.scores(), GenericTooltipUtils::getIntRangeEntryTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getInvertedTooltip(IServerUtils utils, InvertedLootItemCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.inverted"));

        tooltip.add(utils.getConditionTooltip(utils, cond.term()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getKilledByPlayerTooltip(IServerUtils ignoredUtils, LootItemKilledByPlayerCondition ignoredCond) {
        return new TooltipNode(translatable("ali.type.condition.killed_by_player"));
    }

    @NotNull
    public static ITooltipNode getLocationCheckTooltip(IServerUtils utils, LocationCheck cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.location_check"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.location", cond.predicate(), GenericTooltipUtils::getLocationPredicateTooltip));

        if (!cond.offset().equals(BlockPos.ZERO)) {
            tooltip.add(getBlockPosTooltip(utils, "ali.property.multi.offset", cond.offset()));
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMatchToolTooltip(IServerUtils utils, MatchTool cond) {
        return getOptionalTooltip(utils, "ali.type.condition.match_tool", cond.predicate(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static ITooltipNode getRandomChanceTooltip(IServerUtils utils, LootItemRandomChanceCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.random_chance"), true);

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.chance", cond.chance()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getRandomChanceWithEnchantedBonusTooltip(IServerUtils utils, LootItemRandomChanceWithEnchantedBonusCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.random_chance_with_enchanted_bonus"), true);

        tooltip.add(getFloatTooltip(utils, "ali.property.value.unenchanted_chance", cond.unenchantedChance()));
        tooltip.add(getLevelBasedValueTooltip(utils, "ali.property.branch.enchanted_chance", cond.enchantedChance()));
        tooltip.add(getHolderTooltip(utils, "ali.property.value.enchantment", cond.enchantment(), RegistriesTooltipUtils::getEnchantmentTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getReferenceTooltip(IServerUtils utils, ConditionReference cond) {
        return getResourceKeyTooltip(utils, "ali.type.condition.reference", cond.name());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getSurvivesExplosionTooltip(IServerUtils ignoredUtils, ExplosionCondition ignoredCond) {
        return new TooltipNode(translatable("ali.type.condition.survives_explosion"));
    }

    @NotNull
    public static ITooltipNode getTableBonusTooltip(IServerUtils utils, BonusLevelTableCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.table_bonus"), true);
        List<String> list = cond.values().stream().mapToDouble(aFloat -> aFloat).mapToObj(FLOAT_FORMAT::format).toList();

        tooltip.add(getHolderTooltip(utils, "ali.property.value.enchantment", cond.enchantment(), RegistriesTooltipUtils::getEnchantmentTooltip));
        tooltip.add(getStringTooltip(utils, "ali.property.value.values", list.toString()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getTimeCheckTooltip(IServerUtils utils, TimeCheck cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.time_check"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.period", cond.period(), GenericTooltipUtils::getLongTooltip));
        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.value", cond.value()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getValueCheckTooltip(IServerUtils utils, ValueCheckCondition cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.value_check"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.provider", cond.provider()));
        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.range", cond.range()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getWeatherCheckTooltip(IServerUtils utils, WeatherCheck cond) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.weather_check"));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_raining", cond.isRaining(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_thundering", cond.isThundering(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }
}
