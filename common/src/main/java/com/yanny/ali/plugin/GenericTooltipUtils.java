package com.yanny.ali.plugin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootEntry;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import com.yanny.ali.plugin.entry.SingletonEntry;
import com.yanny.ali.plugin.function.LootConditionalAliFunction;
import com.yanny.ali.plugin.function.SetAttributesAliFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GenericTooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    @NotNull
    public static Component translatableType(String prefix, Enum<?> type, Object... args) {
        return translatable(prefix + "." + type.name().toLowerCase(), args);
    }

    @NotNull
    public static Component translatable(String key, Object... args) {
        return Component.translatable(key, Arrays.stream(args).map((arg) -> {
            if (arg instanceof MutableComponent) {
                return arg;
            } else if (arg != null) {
                return Component.literal(arg.toString());
            } else {
                return Component.literal("null");
            }
        }).toArray()).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static Component value(Object value) {
        if (value instanceof MutableComponent) {
            return ((MutableComponent) value).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
        } else {
            return Component.literal(value.toString()).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
        }
    }

    @NotNull
    public static Component value(Object value, String unit) {
        return Component.translatable("ali.util.advanced_loot_info.two_values", value, unit).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
    }

    @NotNull
    public static Component pair(Object value1, Object value2) {
        return Component.translatable("ali.util.advanced_loot_info.two_values_with_space", value1, value2);
    }

    @NotNull
    public static Component pad(int count, Object arg) {
        if (count > 0) {
            return pair(Component.translatable("ali.util.advanced_loot_info.pad." + count), arg);
        } else {
            if (arg instanceof MutableComponent mutableComponent) {
                return mutableComponent;
            } else {
                return Component.literal(arg.toString());
            }
        }
    }

    @NotNull
    public static Component keyValue(Object key, Object value) {
        return translatable("ali.util.advanced_loot_info.key_value", key instanceof MutableComponent ? key : Component.literal(key.toString()), value(value));
    }

    @NotNull
    public static List<Component> getTooltip(ILootEntry entry, RangeValue chance, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance,
                                             RangeValue count, Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount, List<ILootFunction> functions,
                                             List<ILootCondition> conditions) {
        List<Component> components = new LinkedList<>();

        if (entry instanceof SingletonEntry singletonEntry) {
            components.addAll(getQualityTooltip(singletonEntry));
        }

        components.addAll(getChanceTooltip(chance));
        components.addAll(getBonusChanceTooltip(bonusChance));

        components.addAll(getCountTooltip(count));
        components.addAll(getBonusCountTooltip(bonusCount));

        if (!conditions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.conditions"));
            components.addAll(getConditionsTooltip(0, conditions));
        }
        if (!functions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.functions"));
            components.addAll(getFunctionsTooltip(0, functions));
        }

        return components;
    }

    @NotNull
    public static List<Component> getConditionsTooltip(int pad, List<ILootCondition> conditions) {
        return conditions.stream().map((condition) -> condition.getTooltip(pad)).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFunctionsTooltip(int pad, List<ILootFunction> functions) {
        return functions.stream().map((function) -> {
            List<Component> components = new LinkedList<>(function.getTooltip(pad));

            if (function instanceof LootConditionalAliFunction conditionalFunction && !conditionalFunction.conditions.isEmpty()) {
                components.add(pad(pad + 1, translatable("ali.property.branch.conditions")));
                components.addAll(getConditionsTooltip(pad + 2, conditionalFunction.conditions));
            }

            return components;
        }).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFormulaTooltip(int pad, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(pad, "ali.property.value.formula", formula.getType().id()));

        if (formula.getType() == ApplyBonusCount.BinomialWithBonusCount.TYPE) {
            components.addAll(getIntegerTooltip(pad + 1, "ali.property.value.extra_rounds", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getExtraRounds()));
            components.addAll(getFloatTooltip(pad + 1, "ali.property.value.probability", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getProbability()));
        } else if (formula.getType() == ApplyBonusCount.UniformBonusCount.TYPE) {
            components.addAll(getIntegerTooltip(pad + 1, "ali.property.value.bonus_multiplier", ((MixinApplyBonusCount.UniformBonusCount) formula).getBonusMultiplier()));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNameSourceTooltip(int pad, CopyNameFunction.NameSource source) {
        return getEnumTooltip(pad, "ali.property.value.name_source", "name_source", Optional.of(source));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockTooltip(int pad, Block block) {
        return List.of(pad(pad, translatable("ali.property.value.block", value(translatable(block.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPropertyTooltip(int pad, Property<?> property) {
        return List.of(pad(pad, value(pair(property.getName(), property.getPossibleValues().toString()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(int pad, Enchantment enchantment) {
        return List.of(pad(pad, translatable("ali.property.value.enchantment", value(translatable(enchantment.getDescriptionId())))));
    }

    @NotNull
    public static List<Component> getModifierTooltip(int pad, SetAttributesAliFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.modifier")));
        components.addAll(getStringTooltip(pad + 1, "ali.property.value.name", modifier.name()));
        components.addAll(getHolderTooltip(pad + 1, modifier.attribute(), GenericTooltipUtils::getAttributeTooltip));
        components.addAll(getOperationTooltip(pad + 1, modifier.operation()));
        components.addAll(getRangeValueTooltip(pad + 1, "ali.property.value.amount", modifier.amount()));
        components.addAll(getOptionalTooltip(pad + 1, modifier.id(), GenericTooltipUtils::getUUIDTooltip));
        components.addAll(getCollectionTooltip(pad + 1, "ali.property.branch.equipment_slots", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(int pad, Attribute attribute) {
        return List.of(pad(pad, translatable("ali.property.value.attribute", value(translatable(attribute.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getOperationTooltip(int pad, AttributeModifier.Operation operation) {
        return List.of(pad(pad, translatable("ali.property.value.operation", value(operation.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(int pad, UUID uuid) {
        return List.of(pad(pad, translatable("ali.property.value.uuid", value(uuid))));
    }

    @NotNull
    public static List<Component> getBannerPatternTooltip(int pad, Pair<Holder<BannerPattern>, DyeColor> pair) {
        List<Component> components = new LinkedList<>();

        components.addAll(getHolderTooltip(pad, pair.getFirst(), GenericTooltipUtils::getBannerPatternTooltip));
        components.addAll(getEnumTooltip(pad + 1, "ali.property.value.color", pair.getSecond()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(int pad, BannerPattern bannerPattern) {
        return List.of(pad(pad, translatable("ali.property.value.banner_pattern", value(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.getKey(bannerPattern))))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(int pad, BlockEntityType<?> blockEntityType) {
        ResourceLocation location = Objects.requireNonNull(blockEntityType.builtInRegistryHolder()).unwrapKey().orElseThrow().location();
        return List.of(pad(pad, translatable("ali.property.value.block_entity_type", value(location))));
    }

    @NotNull
    public static List<Component> getPotionTooltip(int pad, Potion potion) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.potion")));
        components.addAll(getCollectionTooltip(pad + 1, "ali.property.branch.mob_effects", potion.getEffects(), GenericTooltipUtils::getMobEffectInstanceTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstanceTooltip(int pad, MobEffectInstance effectInstance) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMobEffectTooltip(pad, effectInstance.getEffect()));
        components.addAll(getIntegerTooltip(pad + 1, "ali.property.value.amplifier", effectInstance.getAmplifier()));
        components.addAll(getIntegerTooltip(pad + 1, "ali.property.value.duration", effectInstance.getDuration()));
        components.addAll(getBooleanTooltip(pad + 1, "ali.property.value.is_ambient", effectInstance.isAmbient()));
        components.addAll(getBooleanTooltip(pad + 1, "ali.property.value.is_visible", effectInstance.isVisible()));
        components.addAll(getBooleanTooltip(pad + 1, "ali.property.value.show_icon", effectInstance.showIcon()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(int pad, MobEffect mobEffect) {
        return List.of(pad(pad, translatable("ali.property.value.mob_effect", value(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(mobEffect))))));
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(int pad, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(pad, "ali.property.branch.state_properties_predicate", propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        List<Component> components = new LinkedList<>();
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher matcher) {
            components.add(pad(pad, keyValue(name, matcher.value())));
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher matcher) {
            Optional<String> min = matcher.minValue();
            Optional<String> max = matcher.maxValue();

            if (min.isPresent()) {
                if (max.isPresent()) {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_both", name, min.get(), max.get()))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_gte", name, min.get()))));
                }
            } else {
                if (max.isPresent()) {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_lte", name, max.get()))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_any", name))));
                }
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePredicateTooltip(int pad, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.damage_source_predicate")));
        components.addAll(getCollectionTooltip(pad + 1, "ali.property.branch.tag_predicates", damagePredicate.tags(), GenericTooltipUtils::getTagPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.direct_entity", damagePredicate.directEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.source_entity", damagePredicate.sourceEntity(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagPredicateTooltip(int pad, TagPredicate<T> tagPredicate) {
        return List.of(pad(pad, keyValue(tagPredicate.tag().location(), tagPredicate.expected())));
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(int pad, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(pad, entityPredicate.entityType(), GenericTooltipUtils::getEntityTypePredicateTooltip));
        components.addAll(getComponentsTooltip(pad, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer(), GenericTooltipUtils::getDistancePredicateTooltip));
        components.addAll(getComponentsTooltip(pad, "ali.property.branch.location", entityPredicate.location(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getComponentsTooltip(pad, "ali.property.branch.stepping_on_location", entityPredicate.steppingOnLocation(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, entityPredicate.subPredicate(), GenericTooltipUtils::getEntitySubPredicateTooltip));
        components.addAll(getComponentsTooltip(pad, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(pad, "ali.property.value.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(pad, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(int pad, EntityTypePredicate entityTypePredicate) {
        return getHolderSetTooltip(pad, "ali.property.branch.entity_types", entityTypePredicate.types(), GenericTooltipUtils::getEntityTypeTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEntityTypeTooltip(int pad, EntityType<?> entityType) {
        return getComponentTooltip(pad, "ali.property.value.entity_type", value(translatable(entityType.getDescriptionId())));
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(int pad, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.x", distancePredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.y", distancePredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.z", distancePredicate.z()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.horizontal", distancePredicate.horizontal()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.absolute", distancePredicate.absolute()));

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(int pad, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(pad, locationPredicate.position(), GenericTooltipUtils::getPositionPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.biome", locationPredicate.biome(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.structure", locationPredicate.structure(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(pad, locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getPositionPredicateTooltip(int pad, LocationPredicate.PositionPredicate positionPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.position")));
        components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.x", positionPredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.y", positionPredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.z", positionPredicate.z()));

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(int pad, LightPredicate lightPredicate) {
        return getMinMaxBoundsTooltip(pad, "ali.property.value.light", lightPredicate.composite());
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(int pad, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.block_predicate")));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.tag", blockPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderSetTooltip(pad + 1, "ali.property.branch.blocks", blockPredicate.blocks(), GenericTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(pad, blockPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));
        components.addAll(getOptionalTooltip(pad, blockPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPredicateTooltip(int pad, NbtPredicate nbtPredicate) {
        return List.of(pad(pad, translatable("ali.property.value.nbt", value(nbtPredicate.tag()))));
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(int pad, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.fluid_predicate")));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.tag", fluidPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderTooltip(pad + 1, fluidPredicate.fluid(), GenericTooltipUtils::getFluidTooltip));
        components.addAll(getOptionalTooltip(pad + 1, fluidPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFluidTooltip(int pad, Fluid fluid) {
        return List.of(pad(pad, translatable("ali.property.value.fluid", value(translatable(BuiltInRegistries.FLUID.getKey(fluid).toString())))));
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(int pad, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.mob_effects")));
        mobEffectsPredicate.effectMap().forEach((effect, instancePredicate) -> {
            components.addAll(getHolderTooltip(pad + 1, effect, GenericTooltipUtils::getMobEffectTooltip));
            components.addAll(getMobEffectInstancePredicateTooltip(pad + 2, instancePredicate));
        });

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(int pad, MobEffectsPredicate.MobEffectInstancePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.amplifier", predicate.amplifier()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.duration", predicate.duration()));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.is_ambient", predicate.ambient(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(pad, "ali.property.value.is_visible", predicate.visible(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(int pad, EntityFlagsPredicate entityFlagsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.entity_flags")));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.is_on_fire", entityFlagsPredicate.isOnFire(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.is_baby", entityFlagsPredicate.isBaby(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.is_crouching", entityFlagsPredicate.isCrouching(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.is_sprinting", entityFlagsPredicate.isSprinting(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.is_swimming", entityFlagsPredicate.isSwimming(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(int pad, EntityEquipmentPredicate entityEquipmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.entity_equipment")));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.head", entityEquipmentPredicate.head(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.chest", entityEquipmentPredicate.chest(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.legs", entityEquipmentPredicate.legs(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.feet", entityEquipmentPredicate.feet(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.mainhand", entityEquipmentPredicate.mainhand(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.offhand", entityEquipmentPredicate.offhand(), GenericTooltipUtils::getItemPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(int pad, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(pad, "ali.property.value.tag", itemPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderSetTooltip(pad, "ali.property.branch.items", itemPredicate.items(), GenericTooltipUtils::getItemTooltip));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.count", itemPredicate.count()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.value.durability", itemPredicate.durability()));
        components.addAll(getCollectionTooltip(pad, "ali.property.branch.enchantments", itemPredicate.enchantments(), GenericTooltipUtils::getEnchantmentPredicateTooltip));
        components.addAll(getCollectionTooltip(pad, "ali.property.branch.stored_enchantments", itemPredicate.storedEnchantments(), GenericTooltipUtils::getEnchantmentPredicateTooltip));
        components.addAll(getOptionalHolderTooltip(pad, itemPredicate.potion(), GenericTooltipUtils::getPotionTooltip));
        components.addAll(getOptionalTooltip(pad, itemPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(int pad, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderTooltip(pad, enchantmentPredicate.enchantment(), GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.level", enchantmentPredicate.level()));

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(int pad, EntitySubPredicate entitySubPredicate) {
        List<Component> components = new LinkedList<>();
        Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

        optional.ifPresent((entry) -> {
            components.add(pad(pad, translatable("ali.property.branch.entity_sub_predicate", entry.getKey())));

            if (entitySubPredicate instanceof LightningBoltPredicate predicate) {
                components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
                components.addAll(getComponentsTooltip(pad + 1, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));
            } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                components.addAll(getOptionalTooltip(pad + 1, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));
            } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.level", predicate.level()));
                components.addAll(getOptionalTooltip(pad + 1, predicate.gameType(), GenericTooltipUtils::getGameTypeTooltip));
                components.addAll(getCollectionTooltip(pad + 1, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
                components.addAll(getRecipesTooltip(pad + 1, predicate.recipes()));
                components.addAll(getAdvancementsTooltip(pad + 1, predicate.advancements()));
            } else if (entitySubPredicate instanceof SlimePredicate predicate) {
                components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.value.size", predicate.size()));
            } else {
                EntitySubPredicate.CODEC.encodeStart(JsonOps.INSTANCE, entitySubPredicate).result().ifPresent((element) -> {
                    JsonObject jsonObject = element.getAsJsonObject();

                    if (jsonObject.has("variant")) {
                        components.add(pad(pad + 1, translatable("ali.property.value.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                    } else {
                        components.add(pad(pad + 1, translatable("ali.property.value.variant", jsonObject.toString())));
                    }
                });
            }
        });

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemTooltip(int pad, Item item) {
        return List.of(pad(pad, translatable("ali.property.value.item", value(translatable(item.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypeTooltip(int pad, GameType gameType) {
        return List.of(pad(pad, value(translatable("ali.property.value.game_type", gameType.getShortDisplayName()))));
    }

    @NotNull
    public static List<Component> getStatMatcherTooltip(int pad, PlayerPredicate.StatMatcher<?> stat) {
        List<Component> components = new LinkedList<>();
        Holder<?> value = stat.value();

        if (value.value() instanceof Item item) {
            components.addAll(getItemTooltip(pad, item));
        } else if (value.value() instanceof Block block) {
            components.addAll(getBlockTooltip(pad, block));
        } else {
            components.add(pad(pad, value.value().toString()));
        }

        components.add(pad(pad + 1, keyValue(stat.type().getDisplayName(), toString(stat.range()))));

        return components;
    }

    @NotNull
    public static List<Component> getRecipesTooltip(int pad, Object2BooleanMap<ResourceLocation> recipes) {
        List<Component> components = new LinkedList<>();

        if (!recipes.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.recipes")));
            recipes.forEach((recipe, required) -> components.add(pad(pad + 1, keyValue(recipe.toString(), required))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getAdvancementsTooltip(int pad, Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap) {
        List<Component> components = new LinkedList<>();

        if (!predicateMap.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.advancements")));
            predicateMap.forEach((advancement, predicate) -> {
                components.add(pad(pad + 1, advancement.toString()));

                if (predicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                    components.add(pad(pad + 2, translatable("ali.property.value.done", donePredicate.state())));
                } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                    criterionsPredicate.criterions().forEach((criterion, state) -> components.add(pad(pad + 2, keyValue(criterion, state))));
                }
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getBlockPosTooltip(int pad, BlockPos pos) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(pad, "ali.property.value.x", pos.getX()));
        components.addAll(getIntegerTooltip(pad, "ali.property.value.y", pos.getY()));
        components.addAll(getIntegerTooltip(pad, "ali.property.value.z", pos.getZ()));

        return components;
    }

    // HELPERS

    @Unmodifiable
    @NotNull
    public static List<Component> getRangeValueTooltip(int pad, String key, RangeValue value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBooleanTooltip(int pad, String key, Boolean value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntegerTooltip(int pad, String key, int value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLongTooltip(int pad, String key, Long value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getStringTooltip(int pad, String key, String value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFloatTooltip(int pad, String key, Float value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static <T extends Enum<T>> List<Component> getEnumTooltip(int pad, String key, String enumName, Optional<T> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(translatableType("ali.enum." + enumName, v)))))).orElse(List.of());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(int pad, String key, Enum<?> value) {
        return List.of(pad(pad, translatable(key, value(value.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(int pad, Enum<?> value) {
        return List.of(pad(pad, value(value.name())));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getResourceLocationTooltip(int pad, String key, ResourceLocation value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getResourceKeyTooltip(int pad, String key, ResourceKey<T> value) {
        return List.of(pad(pad, translatable(key, value(value.location()))));
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagKeyTooltip(int pad, String key, TagKey<T> value) {
        return getResourceLocationTooltip(pad, key, value.location());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(int pad, String key, Component component) {
        return List.of(pad(pad, translatable(key, value(component))));
    }

    @NotNull
    public static <T> List<Component> getComponentsTooltip(int pad, String key, T value, BiFunction<Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        List<Component> componentList = mapper.apply(pad + 1, value);

        if (!componentList.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(componentList);
        }

        return components;
    }
    @NotNull
    public static <T> List<Component> getComponentsTooltip(int pad, String key, Optional<T> value, BiFunction<Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        List<Component> componentList = getOptionalTooltip(pad + 1, value, mapper);

        if (!componentList.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(componentList);
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(int pad, String key, MinMaxBounds.Ints ints) {
        List<Component> components = new LinkedList<>();

        if (ints != MinMaxBounds.Ints.ANY) {
            components.add(pad(pad, translatable(key, value(toString(ints)))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(int pad, String key, MinMaxBounds.Doubles doubles) {
        List<Component> components = new LinkedList<>();

        if (doubles != MinMaxBounds.Doubles.ANY) {
            components.add(pad(pad, translatable(key, value(toString(doubles)))));
        }

        return components;
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(int pad, Optional<T> optional, BiFunction<Integer, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(pad, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(int pad, String key, Optional<T> optional, TriFunction<Integer, String, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(pad, key, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderTooltip(int pad, Optional<Holder<T>> optional, BiFunction<Integer, T, List<Component>> mapper) {
        return optional.map((holder) -> getHolderTooltip(pad, holder, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderSetTooltip(int pad, String key, Optional<HolderSet<T>> optional, BiFunction<Integer, T, List<Component>> mapper) {
        return optional.map((holderSet) -> getHolderSetTooltip(pad, key, holderSet, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getHolderTooltip(int pad, Holder<T> holder, BiFunction<Integer, T, List<Component>> mapper) {
        return mapper.apply(pad, holder.value());
    }

    @NotNull
    public static <T> List<Component> getHolderSetTooltip(int pad, String key, HolderSet<T> holderSet, BiFunction<Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        Either<TagKey<T>, List<Holder<T>>> either = holderSet.unwrap();
        Optional<TagKey<T>> left = either.left();
        Optional<List<Holder<T>>> right = either.right();

        if (left.isPresent() || !right.orElse(List.of()).isEmpty()) {
            components.add(pad(pad, translatable(key)));
        }

        left.ifPresent((tagKey) -> components.addAll(getTagKeyTooltip(pad + 1, "ali.property.value.tag", tagKey)));
        right.ifPresent((list) -> {
            if (!list.isEmpty()) {
                holderSet.forEach((holder) -> components.addAll(getHolderTooltip(pad + 1, holder, mapper)));
            }
        });

        return components;
    }

    @NotNull
    public static <T> List<Component> getCollectionTooltip(int pad, String key, Collection<T> values, BiFunction<Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        if (!values.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            values.forEach((value) -> components.addAll(mapper.apply(pad + 1, value)));
        }

        return components;
    }

    // PRIVATE

    @NotNull
    private static String toString(MinMaxBounds.Doubles doubles) {
        Optional<Double> min = doubles.min();
        Optional<Double> max = doubles.max();

        if (min.isPresent()) {
            if (max.isPresent()) {
                if (!Objects.equals(min, max)) {
                    return String.format("%.1f-%.1f", min.get(), max.get());
                } else {
                    return String.format("=%.1f", min.get());
                }
            } else {
                return String.format("≥%.1f", min.get());
            }
        } else {
            return max.map(aDouble -> String.format("≤%.1f", aDouble)).orElse("???");
        }
    }

    @NotNull
    private static String toString(MinMaxBounds.Ints ints) {
        Optional<Integer> min = ints.min();
        Optional<Integer> max = ints.max();

        if (min.isPresent()) {
            if (max.isPresent()) {
                if (!Objects.equals(min, max)) {
                    return String.format("%d-%d", min.get(), max.get());
                } else {
                    return String.format("=%d", min.get());
                }
            } else {
                return String.format("≥%d", min.get());
            }
        } else {
            return max.map(integer -> String.format("≤%d", integer)).orElse("???");

        }
    }

    @NotNull
    @Unmodifiable
    private static List<Component> getQualityTooltip(SingletonEntry entry) {
        if (entry.quality != 0) {
            return List.of(translatable("ali.description.quality", entry.quality));
        }

        return List.of();
    }

    @Unmodifiable
    @NotNull
    private static List<Component> getCountTooltip(RangeValue count) {
        return List.of(translatable("ali.description.count", value(count)));
    }

    @Unmodifiable
    @NotNull
    private static List<Component> getChanceTooltip(RangeValue chance) {
        return List.of(translatable("ali.description.chance", value(chance, "%")));
    }

    @NotNull
    private static List<Component> getBonusChanceTooltip(Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusChance) {
        List<Component> components = new LinkedList<>();

        bonusChance.ifPresent((pair) -> pair.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                components.add(pad(1, translatable(
                        "ali.description.chance_bonus",
                        value(entry.getValue(), "%"),
                        Component.translatable(pair.getFirst().value().getDescriptionId()),
                        Component.translatable("enchantment.level." + entry.getKey())
                )))
        ));

        return components;
    }

    @NotNull
    private static List<Component> getBonusCountTooltip(Optional<Pair<Holder<Enchantment>, Map<Integer, RangeValue>>> bonusCount) {
        List<Component> components = new LinkedList<>();

        bonusCount.ifPresent((pair) -> pair.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                components.add(pad(1, translatable(
                        "ali.description.count_bonus",
                        value(entry.getValue()),
                        Component.translatable(pair.getFirst().value().getDescriptionId()),
                        Component.translatable("enchantment.level." + entry.getKey())
                )))
        ));

        return components;
    }
}
