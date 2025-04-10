package com.yanny.ali.plugin;

import com.yanny.ali.api.IUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

public class ConditionTooltipUtils {
    @NotNull
    public static List<Component> getAllOfTooltip(IUtils utils, int pad, AllOfCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.all_of")));
        components.addAll(getConditionsTooltip(utils, pad + 1, cond.terms));

        return components;
    }

    @NotNull
    public static List<Component> getAnyOfTooltip(IUtils utils, int pad, AnyOfCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.any_of")));
        components.addAll(getConditionsTooltip(utils, pad + 1, cond.terms));

        return components;
    }

    @NotNull
    public static List<Component> getBlockStatePropertyTooltip(IUtils utils, int pad, LootItemBlockStatePropertyCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.block_state_property")));
        components.addAll(getHolderTooltip(utils, pad + 1, cond.block(), GenericTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, cond.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePropertiesTooltip(IUtils utils, int pad, DamageSourceCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.damage_source_properties")));
        components.addAll(getOptionalTooltip(utils, pad + 1, cond.predicate(), GenericTooltipUtils::getDamageSourcePredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityPropertiesTooltip(IUtils utils, int pad, LootItemEntityPropertyCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.entity_properties")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", "target", Optional.of(cond.entityTarget())));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.predicate", cond.predicate(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityScoresTooltip(IUtils utils, int pad, EntityHasScoreCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.entity_scores")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.target", "target", Optional.of(cond.entityTarget())));

        if (!cond.scores().isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.scores")));
            cond.scores().forEach((score, range) -> {
                components.add(pad(pad + 2, translatable("ali.property.value.score", score)));
                components.addAll(getIntRangeTooltip(utils, pad + 3, "ali.property.value.limit", range));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getInvertedTooltip(IUtils utils, int pad, InvertedLootItemCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.inverted")));
        components.addAll(utils.getConditionTooltip(utils, pad + 1, cond.term()));

        return components;
    }

    @NotNull
    public static List<Component> getKilledByPlayerTooltip(IUtils utils, int pad, LootItemKilledByPlayerCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.killed_by_player")));

        return components;
    }

    @NotNull
    public static List<Component> getLocationCheckTooltip(IUtils utils, int pad, LocationCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.location_check")));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.location", cond.predicate(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.offset", cond.offset(), GenericTooltipUtils::getBlockPosTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getMatchToolTooltip(IUtils utils, int pad, MatchTool cond) {
        return new LinkedList<>(getComponentsTooltip(utils, pad, "ali.type.condition.match_tool", cond.predicate(), GenericTooltipUtils::getItemPredicateTooltip));
    }

    @NotNull
    public static List<Component> getRandomChanceTooltip(IUtils utils, int pad, LootItemRandomChanceCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance")));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.probability", cond.probability()));

        return components;
    }

    @NotNull
    public static List<Component> getRandomChanceWithLootingTooltip(IUtils utils, int pad, LootItemRandomChanceWithLootingCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance_with_looting")));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.percent", cond.percent()));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.multiplier", cond.lootingMultiplier()));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IUtils utils, int pad, ConditionReference cond) {
        return new LinkedList<>(getResourceKeyTooltip(utils, pad, "ali.type.condition.reference", cond.name()));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getSurvivesExplosionTooltip(IUtils utils, int pad, ExplosionCondition cond) {
        return List.of(pad(pad, translatable("ali.type.condition.survives_explosion")));
    }

    @NotNull
    public static List<Component> getTableBonusTooltip(IUtils utils, int pad, BonusLevelTableCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.table_bonus")));
        components.addAll(getHolderTooltip(utils, pad + 1, cond.enchantment(), GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.values", cond.values().toString()));

        return components;
    }

    @NotNull
    public static List<Component> getTimeCheckTooltip(IUtils utils, int pad, TimeCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.time_check")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.period", cond.period(), GenericTooltipUtils::getLongTooltip));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.value", cond.value()));

        return components;
    }

    @NotNull
    public static List<Component> getValueCheckTooltip(IUtils utils, int pad, ValueCheckCondition cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.value_check")));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.provider", utils.convertNumber(utils, cond.provider())));
        components.addAll(getIntRangeTooltip(utils, pad + 1, "ali.property.value.range", cond.range()));

        return components;
    }

    @NotNull
    public static List<Component> getWeatherCheckTooltip(IUtils utils, int pad, WeatherCheck cond) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.weather_check")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_raining", cond.isRaining(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_thundering", cond.isThundering(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }
}
