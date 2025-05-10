package com.yanny.ali.plugin.client;

import com.yanny.ali.api.IClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.*;

public class ConditionTooltipUtils {
    @NotNull
    public static List<Component> getAllOfTooltip(IClientUtils utils, int pad, AllOfCondition cond) {
        return getCollectionTooltip(utils, pad, "ali.type.condition.all_of", cond.terms, utils::getConditionTooltip);
    }

    @NotNull
    public static List<Component> getAnyOfTooltip(IClientUtils utils, int pad, AnyOfCondition cond) {
        return getCollectionTooltip(utils, pad, "ali.type.condition.any_of", cond.terms, utils::getConditionTooltip);
    }

    @NotNull
    public static List<Component> getBlockStatePropertyTooltip(IClientUtils utils, int pad, LootItemBlockStatePropertyCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.block_state_property")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.block", cond.block(), RegistriesTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.properties", cond.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePropertiesTooltip(IClientUtils utils, int pad, DamageSourceCondition cond) {
        return getOptionalTooltip(utils, pad, "ali.type.condition.damage_source_properties", cond.predicate(), GenericTooltipUtils::getDamageSourcePredicateTooltip);
    }

    @NotNull
    public static List<Component> getEnchantActiveCheckTooltip(IClientUtils utils, int pad, EnchantmentActiveCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.enchantment_active_check")));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.active", cond.active()));

        return components;
    }

    @NotNull
    public static List<Component> getEntityPropertiesTooltip(IClientUtils utils, int pad, LootItemEntityPropertyCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.entity_properties")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", cond.entityTarget()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.predicate", cond.predicate(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityScoresTooltip(IClientUtils utils, int pad, EntityHasScoreCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.entity_scores")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", cond.entityTarget()));

        if (!cond.scores().isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.scores")));
            cond.scores().forEach((score, range) -> {
                components.add(pad(pad + 2, translatable("ali.property.value.null", score)));
                components.addAll(getIntRangeTooltip(utils, pad + 3, "ali.property.value.limit", range));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getInvertedTooltip(IClientUtils utils, int pad, InvertedLootItemCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.inverted")));
        components.addAll(utils.getConditionTooltip(utils, pad + 1, cond.term()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getKilledByPlayerTooltip(IClientUtils ignoredUtils, int pad, LootItemKilledByPlayerCondition ignoredCond) {
        return List.of(pad(pad, translatable("ali.type.condition.killed_by_player")));
    }

    @NotNull
    public static List<Component> getLocationCheckTooltip(IClientUtils utils, int pad, LocationCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.location_check")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.location", cond.predicate(), GenericTooltipUtils::getLocationPredicateTooltip));

        if (!cond.offset().equals(BlockPos.ZERO)) {
            components.addAll(getBlockPosTooltip(utils, pad + 1, "ali.property.multi.offset", cond.offset()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMatchToolTooltip(IClientUtils utils, int pad, MatchTool cond) {
        return getOptionalTooltip(utils, pad, "ali.type.condition.match_tool", cond.predicate(), GenericTooltipUtils::getItemPredicateTooltip);
    }

    @NotNull
    public static List<Component> getRandomChanceTooltip(IClientUtils utils, int pad, LootItemRandomChanceCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.chance", cond.chance()));

        return components;
    }

    @NotNull
    public static List<Component> getRandomChanceWithEnchantedBonusTooltip(IClientUtils utils, int pad, LootItemRandomChanceWithEnchantedBonusCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance_with_enchanted_bonus")));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.unenchanted_chance", cond.unenchantedChance()));
        components.addAll(getLevelBasedValueTooltip(utils, pad + 1, "ali.property.branch.enchanted_chance", cond.enchantedChance()));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.enchantment", cond.enchantment(), RegistriesTooltipUtils::getEnchantmentTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getReferenceTooltip(IClientUtils utils, int pad, ConditionReference cond) {
        return getResourceKeyTooltip(utils, pad, "ali.type.condition.reference", cond.name());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getSurvivesExplosionTooltip(IClientUtils ignoredUtils, int pad, ExplosionCondition ignoredCond) {
        return List.of(pad(pad, translatable("ali.type.condition.survives_explosion")));
    }

    @NotNull
    public static List<Component> getTableBonusTooltip(IClientUtils utils, int pad, BonusLevelTableCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.table_bonus")));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.enchantment", cond.enchantment(), RegistriesTooltipUtils::getEnchantmentTooltip));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.values", cond.values().toString()));

        return components;
    }

    @NotNull
    public static List<Component> getTimeCheckTooltip(IClientUtils utils, int pad, TimeCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.time_check")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.period", cond.period(), GenericTooltipUtils::getLongTooltip));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.value", cond.value()));

        return components;
    }

    @NotNull
    public static List<Component> getValueCheckTooltip(IClientUtils utils, int pad, ValueCheckCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.value_check")));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.provider", cond.provider()));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.range", cond.range()));

        return components;
    }

    @NotNull
    public static List<Component> getWeatherCheckTooltip(IClientUtils utils, int pad, WeatherCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.weather_check")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_raining", cond.isRaining(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_thundering", cond.isThundering(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }
}
