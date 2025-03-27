package com.yanny.ali.plugin;

import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.RangeValue;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.ali.plugin.GenericTooltipUtils.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ConditionTooltipUtils {
    @NotNull
    public static List<Component> getAllOfTooltip(int pad, List<ILootCondition> terms) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.all_of")));
        components.addAll(GenericTooltipUtils.getConditionsTooltip(pad + 1, terms));

        return components;
    }

    @NotNull
    public static List<Component> getAnyOfTooltip(int pad, List<ILootCondition> terms) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.any_of")));
        components.addAll(GenericTooltipUtils.getConditionsTooltip(pad + 1, terms));

        return components;
    }

    @NotNull
    public static List<Component> getBlockStatePropertyTooltip(int pad, Holder<Block> block, Optional<StatePropertiesPredicate> predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.block_state_property")));
        components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 1, block, GenericTooltipUtils::getBlockTooltip));
        components.addAll(GenericTooltipUtils.getOptionalTooltip(pad + 1, predicate, GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePropertiesTooltip(int pad, Optional<DamageSourcePredicate> predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.damage_source_properties")));
        components.addAll(GenericTooltipUtils.getOptionalTooltip(pad + 1, predicate, GenericTooltipUtils::getDamageSourcePredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityPropertiesTooltip(int pad, LootContext.EntityTarget target, Optional<EntityPredicate> predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.entity_properties")));
        components.addAll(getEnumTooltip(pad + 1, "ali.property.value.target", "target", Optional.of(target)));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.predicate", predicate, GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityScoresTooltip(int pad, LootContext.EntityTarget target, Map<String, Tuple<RangeValue, RangeValue>> scores) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.entity_scores")));
        components.addAll(GenericTooltipUtils.getEnumTooltip(pad + 1, "ali.property.value.target", "target", Optional.of(target)));

        if (!scores.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.scores")));
            scores.forEach((score, tuple) -> components.add(pad(pad + 2, keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getInvertedTooltip(int pad, ILootCondition term) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.inverted")));
        components.addAll(term.getTooltip(pad + 1));

        return components;
    }

    @NotNull
    public static List<Component> getKilledByPlayerTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.killed_by_player")));

        return components;
    }

    @NotNull
    public static List<Component> getLocationCheckTooltip(int pad, BlockPos offset, Optional<LocationPredicate> predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.location_check")));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad + 1, "ali.property.branch.location", predicate, GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(GenericTooltipUtils.getComponentsTooltip(pad + 1, "ali.property.branch.offset", offset, GenericTooltipUtils::getBlockPosTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getMatchToolTooltip(int pad, Optional<ItemPredicate> predicate) {
        return new LinkedList<>(GenericTooltipUtils.getComponentsTooltip(pad, "ali.type.condition.match_tool", predicate, GenericTooltipUtils::getItemPredicateTooltip));
    }

    @NotNull
    public static List<Component> getRandomChanceTooltip(int pad, float probability) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance")));
        components.addAll(GenericTooltipUtils.getFloatTooltip(pad + 1, "ali.property.value.probability", probability));

        return components;
    }

    @NotNull
    public static List<Component> getRandomChanceWithLootingTooltip(int pad, float percent, float multiplier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.random_chance_with_looting")));
        components.addAll(GenericTooltipUtils.getFloatTooltip(pad + 1, "ali.property.value.percent", percent));
        components.addAll(GenericTooltipUtils.getFloatTooltip(pad + 1, "ali.property.value.multiplier", multiplier));

        return components;
    }

    @NotNull
    public static List<Component> getReferenceTooltip(int pad, ResourceKey<LootItemCondition> name) {
        return new LinkedList<>(GenericTooltipUtils.getResourceKeyTooltip(pad, "ali.type.condition.reference", Optional.of(name)));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getSurvivesExplosionTooltip(int pad) {
        return List.of(pad(pad, translatable("ali.type.condition.survives_explosion")));
    }

    @NotNull
    public static List<Component> getTableBonusTooltip(int pad, Holder<Enchantment> enchantment, List<Float> values) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.table_bonus")));
        components.addAll(GenericTooltipUtils.getHolderTooltip(pad + 1, enchantment, GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.values", Arrays.toString(values.toArray(Float[]::new))));

        return components;
    }

    @NotNull
    public static List<Component> getTimeCheckTooltip(int pad, Optional<Long> period, RangeValue min, RangeValue max) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.time_check")));
        components.addAll(GenericTooltipUtils.getOptionalTooltip(pad + 1, "ali.property.value.period", period, GenericTooltipUtils::getLongTooltip));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.value", RangeValue.rangeToString(min, max)));

        return components;
    }

    @NotNull
    public static List<Component> getValueCheckTooltip(int pad, RangeValue provider, RangeValue min, RangeValue max) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.value_check")));
        components.addAll(GenericTooltipUtils.getRangeValueTooltip(pad + 1, "ali.property.value.provider", provider));
        components.addAll(GenericTooltipUtils.getStringTooltip(pad + 1, "ali.property.value.range", RangeValue.rangeToString(min, max)));

        return components;
    }

    @NotNull
    public static List<Component> getWeatherCheckTooltip(int pad, Optional<Boolean> isRaining, Optional<Boolean> isThundering) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.condition.weather_check")));
        components.addAll(GenericTooltipUtils.getOptionalTooltip(pad + 1, "ali.property.value.is_raining", isRaining, GenericTooltipUtils::getBooleanTooltip));
        components.addAll(GenericTooltipUtils.getOptionalTooltip(pad + 1, "ali.property.value.is_thundering", isThundering, GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

}
