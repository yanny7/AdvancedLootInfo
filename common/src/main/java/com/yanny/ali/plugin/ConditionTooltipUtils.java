package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.IUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.condition.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

public class ConditionTooltipUtils {
    @NotNull
    public static List<Component> getAllOfTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        AllOfAliCondition cond = (AllOfAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.all_of")));
        components.addAll(GenericTooltipUtils.getConditionsTooltip(utils, pad + 1, cond.terms));

        return components;
    }

    @NotNull
    public static List<Component> getAnyOfTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        AnyOfAliCondition cond = (AnyOfAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.any_of")));
        components.addAll(GenericTooltipUtils.getConditionsTooltip(utils, pad + 1, cond.terms));

        return components;
    }

    @NotNull
    public static List<Component> getBlockStatePropertyTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        BlockStatePropertyAliCondition cond = (BlockStatePropertyAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.block_state_property")));
        components.addAll(GenericTooltipUtils.getBlockTooltip(pad + 1, cond.block));
        components.addAll(GenericTooltipUtils.getStatePropertiesPredicateTooltip(pad + 1, cond.properties));

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePropertiesTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        DamageSourcePropertiesAliCondition cond = (DamageSourcePropertiesAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.damage_source_properties")));
        components.addAll(GenericTooltipUtils.getDamageSourcePredicateTooltip(pad + 1, cond.predicate));

        return components;
    }

    @NotNull
    public static List<Component> getEntityPropertiesTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        EntityPropertiesAliCondition cond = (EntityPropertiesAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.entity_properties")));
        components.addAll(getEnumTooltip(pad + 1, "ali.property.value.target", "target", cond.target));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.predicate", GenericTooltipUtils.getEntityPredicateTooltip(pad + 2, cond.predicate)));

        return components;
    }

    @NotNull
    public static List<Component> getEntityScoresTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        EntityScoresAliCondition cond = (EntityScoresAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.entity_scores")));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.target", "target", cond.target));

        if (!cond.scores.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.scores")));
            cond.scores.forEach((score, tuple) -> components.add(pad(pad + 2, keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getInvertedTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        InvertedAliCondition cond = (InvertedAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.inverted")));
        components.addAll(utils.getConditionTooltip(cond.term.getClass(), utils, pad + 1, cond.term));

        return components;
    }

    @NotNull
    public static List<Component> getKilledByPlayerTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.killed_by_player")));

        return components;
    }

    @NotNull
    public static List<Component> getLocationCheckTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        LocationCheckAliCondition cond = (LocationCheckAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.location_check")));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad + 1, "ali.property.branch.location", GenericTooltipUtils.getLocationPredicateTooltip(pad + 2, cond.predicate)));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad + 1, "ali.property.branch.offset", GenericTooltipUtils.getBlockPosTooltip(pad + 2, cond.offset)));

        return components;
    }

    @NotNull
    public static List<Component> getMatchToolTooltip(IUtils utils, int pad, ILootCondition condition) {
        MatchToolAliCondition cond = (MatchToolAliCondition) condition;
        return new LinkedList<>(GenericTooltipUtils.getComponentsTooltip(pad, "ali.type.condition.match_tool", GenericTooltipUtils.getItemPredicateTooltip(pad + 1, cond.predicate)));
    }

    @NotNull
    public static List<Component> getRandomChanceTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        RandomChanceAliCondition cond = (RandomChanceAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.random_chance")));
        components.addAll(GenericTooltipUtils.getFloatTooltip(pad + 1, "ali.property.value.probability", cond.probability));

        return components;
    }

    @NotNull
    public static List<Component> getRandomChanceWithLootingTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        RandomChanceWithLootingAliCondition cond = (RandomChanceWithLootingAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.random_chance_with_looting")));
        components.addAll(GenericTooltipUtils.getFloatTooltip(pad + 1, "ali.property.value.percent", cond.percent));
        components.addAll(GenericTooltipUtils.getFloatTooltip(pad + 1, "ali.property.value.multiplier", cond.multiplier));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(IUtils utils, int pad, ILootCondition condition) {
        ReferenceAliCondition cond = (ReferenceAliCondition) condition;
        return new LinkedList<>(GenericTooltipUtils.getResourceLocationTooltip(pad, "ali.type.condition.reference", cond.name));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getSurvivesExplosionTooltip(IUtils utils, int pad, ILootCondition condition) {
        return List.of(pad(pad, translatable("ali.type.condition.survives_explosion")));
    }

    @NotNull
    public static List<Component> getTableBonusTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        TableBonusAliCondition cond = (TableBonusAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.table_bonus")));
        components.addAll(GenericTooltipUtils.getEnchantmentTooltip(pad + 1, cond.enchantment));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.values", Arrays.toString(cond.values)));

        return components;
    }

    @NotNull
    public static List<Component> getTimeCheckTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        TimeCheckAliCondition cond = (TimeCheckAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.time_check")));
        components.addAll(GenericTooltipUtils.getLongTooltip(pad + 1, "ali.property.value.period", cond.period));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.value", RangeValue.rangeToString(cond.min, cond.max)));

        return components;
    }

    @NotNull
    public static List<Component> getValueCheckTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        ValueCheckAliCondition cond = (ValueCheckAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.value_check")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.provider", cond.provider));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.range", RangeValue.rangeToString(cond.min, cond.max)));

        return components;
    }

    @NotNull
    public static List<Component> getWeatherCheckTooltip(IUtils utils, int pad, ILootCondition condition) {
        List<Component> components = new LinkedList<>();
        WeatherCheckAliCondition cond = (WeatherCheckAliCondition) condition;

        components.add(pad(pad, translatable("ali.type.condition.weather_check")));

        if (cond.isRaining != null) {
            components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.is_raining", cond.isRaining));
        }

        if (cond.isThundering != null) {
            components.addAll(GenericTooltipUtils.getBooleanTooltip(pad + 1, "ali.property.value.is_thundering", cond.isThundering));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUnknownTooltip(IUtils utils, int pad, ILootCondition condition) {
        UnknownAliCondition cond = (UnknownAliCondition) condition;
        return List.of(pad(pad, translatable("ali.type.condition.unknown", cond.conditionType)));
    }
}
