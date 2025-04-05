package com.yanny.ali.plugin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IUtils;
import com.yanny.ali.api.RangeValue;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class GenericTooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    @NotNull
    public static MutableComponent translatableType(String prefix, Enum<?> type, Object... args) {
        return translatable(prefix + "." + type.name().toLowerCase(), args);
    }

    @NotNull
    public static MutableComponent translatable(String key, Object... args) {
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
    public static MutableComponent value(Object value) {
        if (value instanceof MutableComponent) {
            return ((MutableComponent) value).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
        } else {
            return Component.literal(value.toString()).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
        }
    }

    @NotNull
    public static MutableComponent value(Object value, String unit) {
        return Component.translatable("ali.util.advanced_loot_info.two_values", value.toString(), unit).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
    }

    @NotNull
    public static MutableComponent pair(Object value1, Object value2) {
        return Component.translatable("ali.util.advanced_loot_info.two_values_with_space", value1.toString(), value2.toString());
    }

    @NotNull
    public static MutableComponent pad(int count, Object arg) {
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
    public static MutableComponent keyValue(Object key, Object value) {
        return translatable("ali.util.advanced_loot_info.key_value", key instanceof MutableComponent ? key : Component.literal(key.toString()), value(value.toString()));
    }

    @NotNull
    public static List<Component> getTooltip(IUtils utils, LootPoolEntryContainer entry, RangeValue chance, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance,
                                             RangeValue count, @Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount, List<LootItemFunction> functions,
                                             List<LootItemCondition> conditions) {
        List<Component> components = new LinkedList<>();

        if (entry instanceof LootPoolSingletonContainer singletonEntry) {
            components.addAll(getQualityTooltip(singletonEntry));
        }

        components.addAll(getChanceTooltip(chance));
        components.addAll(getBonusChanceTooltip(bonusChance));

        components.addAll(getCountTooltip(count));
        components.addAll(getBonusCountTooltip(bonusCount));

        if (!conditions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.conditions"));
            components.addAll(getConditionsTooltip(utils, 0, conditions));
        }
        if (!functions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.functions"));
            components.addAll(getFunctionsTooltip(utils, 0, functions));
        }

        return components;
    }

    @NotNull
    public static List<Component> getConditionsTooltip(IUtils utils, int pad, List<LootItemCondition> conditions) {
        return conditions.stream().map((condition) -> utils.getConditionTooltip(condition.getClass(), utils, pad, condition)).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFunctionsTooltip(IUtils utils, int pad, List<LootItemFunction> functions) {
        return functions.stream().map((function) -> {
            List<Component> components = new LinkedList<>(utils.getFunctionTooltip(function.getClass(), utils, pad, function));

            if (function instanceof LootItemConditionalFunction conditionalFunction && conditionalFunction.predicates.length > 0) {
                components.add(pad(pad + 1, translatable("ali.property.branch.conditions")));
                components.addAll(getConditionsTooltip(utils, pad + 2, Arrays.asList(conditionalFunction.predicates)));
            }

            return components;
        }).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFormulaTooltip(IUtils utils, int pad, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(utils, pad, "ali.property.value.formula", formula.getType()));

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.extra_rounds", binomialWithBonusCount.extraRounds));
            components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.probability", binomialWithBonusCount.probability));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.bonus_multiplier", uniformBonusCount.bonusMultiplier));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNameSourceTooltip(IUtils utils, int pad, CopyNameFunction.NameSource source) {
        return getEnumTooltip(utils, pad, "ali.property.value.name_source", "name_source", source);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockTooltip(IUtils utils, int pad, Block block) {
        return List.of(pad(pad, translatable("ali.property.value.block", value(translatable(block.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPropertyTooltip(IUtils utils, int pad, Property<?> property) {
        return List.of(pad(pad, value(pair(property.getName(), property.getPossibleValues().toString()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(IUtils utils, int pad, Enchantment enchantment) {
        return List.of(pad(pad, translatable("ali.property.value.enchantment", value(translatable(enchantment.getDescriptionId())))));
    }

    @NotNull
    public static List<Component> getModifierTooltip(IUtils utils, int pad, SetAttributesFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.modifier")));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.name", modifier.name));
        components.addAll(getAttributeTooltip(utils, pad + 1, modifier.attribute));
        components.addAll(getOperationTooltip(utils, pad + 1, modifier.operation));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.amount", utils.convertNumber(utils, modifier.amount)));
        components.addAll(getOptionalTooltip(utils, pad + 1, modifier.id, GenericTooltipUtils::getUUIDTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.equipment_slots", Arrays.asList(modifier.slots), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(IUtils utils, int pad, Attribute attribute) {
        return List.of(pad(pad, translatable("ali.property.value.attribute", value(translatable(attribute.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getOperationTooltip(IUtils utils, int pad, AttributeModifier.Operation operation) {
        return List.of(pad(pad, translatable("ali.property.value.operation", value(operation.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(IUtils utils, int pad, UUID uuid) {
        return List.of(pad(pad, translatable("ali.property.value.uuid", value(uuid))));
    }

    @NotNull
    public static List<Component> getBannerPatternsTooltip(IUtils utils, int pad, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        List<Component> components = new LinkedList<>();

        if (!patterns.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.banner_patterns")));
            patterns.forEach((pair) -> {
                components.addAll(getHolderTooltip(utils, pad + 1, pair.getFirst(), GenericTooltipUtils::getBannerPatternTooltip));
                components.addAll(getEnumTooltip(utils, pad + 2, "ali.property.value.color", pair.getSecond()));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(IUtils utils, int pad, BannerPattern bannerPattern) {
        return List.of(pad(pad, translatable("ali.property.value.banner_pattern", value(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.getKey(bannerPattern))))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(IUtils utils, int pad, BlockEntityType<?> blockEntityType) {
        return List.of(pad(pad, translatable("ali.property.value.block_entity_type", value(Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType))))));
    }

    @NotNull
    public static List<Component> getPotionTooltip(IUtils utils, int pad, Potion potion) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.potion")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.mob_effects", potion.getEffects(), GenericTooltipUtils::getMobEffectInstanceTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstanceTooltip(IUtils utils, int pad, MobEffectInstance effectInstance) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMobEffectTooltip(utils, pad, effectInstance.getEffect()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.amplifier", effectInstance.getAmplifier()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.duration", effectInstance.getDuration()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.is_ambient", effectInstance.isAmbient()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.is_visible", effectInstance.isVisible()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.show_icon", effectInstance.showIcon()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(IUtils utils, int pad, MobEffect mobEffect) {
        return List.of(pad(pad, translatable("ali.property.value.mob_effect", value(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(mobEffect))))));
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(IUtils utils, int pad, StatePropertiesPredicate propertiesPredicate) {
        List<Component> components = new LinkedList<>();

        if (propertiesPredicate != StatePropertiesPredicate.ANY) {
            if (!propertiesPredicate.properties.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.branch.state_properties_predicate")));
                propertiesPredicate.properties.forEach((propertyMatcher) -> components.addAll(getPropertyMatcherTooltip(utils, pad + 1, propertyMatcher)));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(IUtils utils, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        List<Component> components = new LinkedList<>();

        if (propertyMatcher instanceof StatePropertiesPredicate.ExactPropertyMatcher matcher) {
            components.add(pad(pad, keyValue(matcher.name, matcher.value)));
        }
        if (propertyMatcher instanceof StatePropertiesPredicate.RangedPropertyMatcher matcher) {
            String min = matcher.minValue;
            String max = matcher.maxValue;

            if (min != null) {
                if (max != null) {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_both", matcher.name, min, max))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_gte", matcher.name, min))));
                }
            } else {
                if (max != null) {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_lte", matcher.name, max))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_any", matcher.name))));
                }
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePredicateTooltip(IUtils utils, int pad, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        if (damagePredicate != DamageSourcePredicate.ANY) {
            components.add(pad(pad, translatable("ali.property.branch.damage_source_predicate")));
            components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.tag_predicates", damagePredicate.tags, GenericTooltipUtils::getTagPredicateTooltip));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.direct_entity", getEntityPredicateTooltip(utils, pad + 2, damagePredicate.directEntity)));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.source_entity", getEntityPredicateTooltip(utils, pad + 2, damagePredicate.sourceEntity)));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagPredicateTooltip(IUtils utils, int pad, TagPredicate<T> tagPredicate) {
        return List.of(pad(pad, keyValue(tagPredicate.tag.location().toString(), tagPredicate.expected)));
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(IUtils utils, int pad, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        if (entityPredicate != EntityPredicate.ANY) {
            components.addAll(getEntityTypePredicateTooltip(utils, pad, entityPredicate.entityType));
            components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.distance_to_player", getDistancePredicateTooltip(utils, pad + 1, entityPredicate.distanceToPlayer)));
            components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.location", getLocationPredicateTooltip(utils, pad + 1, entityPredicate.location)));
            components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.stepping_on_location", getLocationPredicateTooltip(utils, pad + 1, entityPredicate.steppingOnLocation)));
            components.addAll(getMobEffectPredicateTooltip(utils, pad, entityPredicate.effects));
            components.addAll(getNbtPredicateTooltip(utils, pad, entityPredicate.nbt));
            components.addAll(getEntityFlagsPredicateTooltip(utils, pad, entityPredicate.flags));
            components.addAll(getEntityEquipmentPredicateTooltip(utils, pad, entityPredicate.equipment));
            components.addAll(getEntitySubPredicateTooltip(utils, pad, entityPredicate.subPredicate));
            components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.vehicle", getEntityPredicateTooltip(utils, pad + 1, entityPredicate.vehicle)));
            components.addAll(getComponentsTooltip(utils, pad, "ali.property.value.passenger", getEntityPredicateTooltip(utils, pad + 1, entityPredicate.passenger)));
            components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.targeted_entity", getEntityPredicateTooltip(utils, pad + 1, entityPredicate.targetedEntity)));
            components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.team", entityPredicate.team, GenericTooltipUtils::getStringTooltip));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(IUtils utils, int pad, EntityTypePredicate entityTypePredicate) {
        List<Component> components = new LinkedList<>();

        if (entityTypePredicate != EntityTypePredicate.ANY) {
            if (entityTypePredicate instanceof EntityTypePredicate.TypePredicate typePredicate) {
                components.addAll(getComponentTooltip(utils, pad, "ali.property.value.entity_type", value(typePredicate.type.getDescription())));
            }
            if (entityTypePredicate instanceof EntityTypePredicate.TagPredicate tagPredicate) {
                components.addAll(getComponentTooltip(utils, pad, "ali.property.value.entity_type", value(tagPredicate.tag.location())));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(IUtils utils, int pad, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        if (distancePredicate != DistancePredicate.ANY) {
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.x", distancePredicate.x));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.y", distancePredicate.y));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.z", distancePredicate.z));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.horizontal", distancePredicate.horizontal));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.absolute", distancePredicate.absolute));
        }

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(IUtils utils, int pad, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        if (locationPredicate != LocationPredicate.ANY) {
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.x", locationPredicate.x));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.y", locationPredicate.y));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.z", locationPredicate.z));
            components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.biome", locationPredicate.biome, GenericTooltipUtils::getResourceKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.structure", locationPredicate.structure, GenericTooltipUtils::getResourceKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.dimension", locationPredicate.dimension, GenericTooltipUtils::getResourceKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.smokey", locationPredicate.smokey, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getLightPredicateTooltip(utils, pad, locationPredicate.light));
            components.addAll(getBlockPredicateTooltip(utils, pad, locationPredicate.block));
            components.addAll(getFluidPredicateTooltip(utils, pad, locationPredicate.fluid));
        }

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(IUtils utils, int pad, LightPredicate lightPredicate) {
        List<Component> components = new LinkedList<>();

        if (lightPredicate != LightPredicate.ANY) {
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.light", lightPredicate.composite));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(IUtils utils, int pad, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        if (blockPredicate != BlockPredicate.ANY) {
            Set<Block> blocks = blockPredicate.blocks;

            components.add(pad(pad, translatable("ali.property.branch.block_predicate")));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", blockPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));

            if (blocks != null && !blocks.isEmpty()) {
                components.add(pad(pad + 1, translatable("ali.property.branch.blocks")));

                blocks.forEach((block) -> components.addAll(getBlockTooltip(utils, pad + 2, block)));
            }

            components.addAll(getStatePropertiesPredicateTooltip(utils, pad, blockPredicate.properties));
            components.addAll(getNbtPredicateTooltip(utils, pad, blockPredicate.nbt));
        }

        return components;
    }

    @NotNull
    public static List<Component> getNbtPredicateTooltip(IUtils utils, int pad, NbtPredicate nbtPredicate) {
        List<Component> components = new LinkedList<>();

        if (nbtPredicate != NbtPredicate.ANY) {
            if (nbtPredicate.tag != null) {
                components.add(pad(pad, translatable("ali.property.value.nbt", value(nbtPredicate.tag))));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(IUtils utils, int pad, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        if (fluidPredicate != FluidPredicate.ANY) {
            components.add(pad(pad, translatable("ali.property.branch.fluid_predicate")));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", fluidPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, fluidPredicate.fluid, GenericTooltipUtils::getFluidTooltip));
            components.addAll(getStatePropertiesPredicateTooltip(utils, pad + 1, fluidPredicate.properties));
        }

        return components;
    }

    @NotNull
    public static List<Component> getFluidTooltip(IUtils utils, int pad, Fluid fluid) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.value.fluid", value(translatable(BuiltInRegistries.FLUID.getKey(fluid).toString())))));

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(IUtils utils, int pad, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        if (mobEffectsPredicate != MobEffectsPredicate.ANY) {
            components.add(pad(pad, translatable("ali.property.branch.mob_effects")));

            mobEffectsPredicate.effects.forEach((effect, instancePredicate) -> {
                components.addAll(getMobEffectTooltip(utils, pad + 1, effect));
                components.addAll(getMobEffectInstancePredicateTooltip(utils, pad + 2, instancePredicate));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(IUtils utils, int pad, MobEffectsPredicate.MobEffectInstancePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.amplifier", predicate.amplifier));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.duration", predicate.duration));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_ambient", predicate.ambient, GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_visible", predicate.visible, GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(IUtils utils, int pad, EntityFlagsPredicate predicate) {
        List<Component> components = new LinkedList<>();

        if (predicate != EntityFlagsPredicate.ANY) {
            components.add(pad(pad, translatable("ali.property.branch.entity_flags")));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_on_fire", predicate.isOnFire, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_baby", predicate.isBaby, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_crouching", predicate.isCrouching, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_sprinting", predicate.isSprinting, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_swimming", predicate.isSwimming, GenericTooltipUtils::getBooleanTooltip));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(IUtils utils, int pad, EntityEquipmentPredicate predicate) {
        List<Component> components = new LinkedList<>();

        if (predicate != EntityEquipmentPredicate.ANY) {
            components.add(pad(pad, translatable("ali.property.branch.entity_equipment")));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.head", getItemPredicateTooltip(utils, pad + 2, predicate.head)));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.chest", getItemPredicateTooltip(utils, pad + 2, predicate.chest)));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.legs", getItemPredicateTooltip(utils, pad + 2, predicate.legs)));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.feet", getItemPredicateTooltip(utils, pad + 2, predicate.feet)));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.mainhand", getItemPredicateTooltip(utils, pad + 2, predicate.mainhand)));
            components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.offhand", getItemPredicateTooltip(utils, pad + 2, predicate.offhand)));
        }

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(IUtils utils, int pad, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        if (itemPredicate != ItemPredicate.ANY) {
            Set<Item> items = itemPredicate.items;
            EnchantmentPredicate[] enchantments = itemPredicate.enchantments;
            EnchantmentPredicate[] storedEnchantments = itemPredicate.storedEnchantments;

            components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.tag", itemPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));

            if (items != null && !items.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.branch.items")));

                items.forEach((item) -> components.addAll(getItemTooltip(utils, pad + 1, item)));
            }

            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.count", itemPredicate.count));
            components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.durability", itemPredicate.durability));

            if (enchantments.length > 0) {
                components.add(pad(pad, translatable("ali.property.branch.enchantments")));

                for (EnchantmentPredicate enchantment : enchantments) {
                    components.addAll(getEnchantmentPredicateTooltip(utils, pad + 1, enchantment));
                }
            }

            if (storedEnchantments.length > 0) {
                components.add(pad(pad, translatable("ali.property.branch.stored_enchantments")));

                for (EnchantmentPredicate enchantment : storedEnchantments) {
                    components.addAll(getEnchantmentPredicateTooltip(utils, pad + 1, enchantment));
                }
            }

            components.addAll(getOptionalTooltip(utils, pad, itemPredicate.potion, GenericTooltipUtils::getPotionTooltip));
            components.addAll(getNbtPredicateTooltip(utils, pad, itemPredicate.nbt));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(IUtils utils, int pad, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            components.addAll(getOptionalTooltip(utils, pad, enchantmentPredicate.enchantment, GenericTooltipUtils::getEnchantmentTooltip));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", enchantmentPredicate.level));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(IUtils utils, int pad, EntitySubPredicate entitySubPredicate) {
        List<Component> components = new LinkedList<>();

        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable("ali.property.branch.entity_sub_predicate", entry.getKey())));

                if (entitySubPredicate instanceof LighthingBoltPredicate boltPredicate) {
                    components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.blocks_on_fire", boltPredicate.blocksSetOnFire));
                    components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", getEntityPredicateTooltip(utils, pad + 2, boltPredicate.entityStruck)));
                } else if (entitySubPredicate instanceof FishingHookPredicate fishingHookPredicate) {
                    components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.in_open_water", fishingHookPredicate.inOpenWater));
                } else if (entitySubPredicate instanceof PlayerPredicate playerPredicate) {
                    components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", playerPredicate.level));
                    components.addAll(getOptionalTooltip(utils, pad + 1, playerPredicate.gameType, GenericTooltipUtils::getGameTypeTooltip));
                    components.addAll(getStatsTooltip(utils, pad + 1, playerPredicate.stats));
                    components.addAll(getRecipesTooltip(utils, pad + 1, playerPredicate.recipes));
                    components.addAll(getAdvancementsTooltip(utils, pad + 1, playerPredicate.advancements));
                } else if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                    components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.size", slimePredicate.size));
                } else {
                    JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                    if (jsonObject.has("variant")) {
                        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.variant", jsonObject.getAsJsonPrimitive("variant").getAsString()));
                    } else {
                        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.variant", jsonObject.getAsString()));
                    }
                }
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemTooltip(IUtils utils, int pad, Item item) {
        return List.of(pad(pad, translatable("ali.property.value.item", value(translatable(item.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypeTooltip(IUtils utils, int pad, GameType gameType) {
        return List.of(pad(pad, value(translatable("ali.property.value.game_type", gameType.getShortDisplayName()))));
    }

    @NotNull
    public static List<Component> getStatsTooltip(IUtils utils, int pad, Map<Stat<?>, MinMaxBounds.Ints> statIntsMap) {
        List<Component> components = new LinkedList<>();

        if (!statIntsMap.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.stats")));
            statIntsMap.forEach((stat, ints) -> {
                Object value = stat.getValue();

                if (value instanceof Item item) {
                    components.addAll(getItemTooltip(utils, pad + 1, item));
                } else if (value instanceof Block block) {
                    components.addAll(getBlockTooltip(utils, pad + 1, block));
                } else {
                    components.add(pad(pad + 1, value));
                }

                components.add(pad(pad + 2, keyValue(stat.getType().getDisplayName(), toString(ints))));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getRecipesTooltip(IUtils utils, int pad, Object2BooleanMap<ResourceLocation> recipes) {
        List<Component> components = new LinkedList<>();

        if (!recipes.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.recipes")));
            recipes.forEach((recipe, required) -> components.add(pad(pad + 1, keyValue(recipe.toString(), required))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getAdvancementsTooltip(IUtils utils, int pad, Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap) {
        List<Component> components = new LinkedList<>();

        if (!predicateMap.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.advancements")));
            predicateMap.forEach((advancement, predicate) -> {
                components.add(pad(pad + 1, advancement.toString()));

                if (predicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                    components.add(pad(pad + 2, translatable("ali.property.value.done", donePredicate.state)));
                } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                    criterionsPredicate.criterions.forEach((criterion, state) -> components.add(pad(pad + 2, keyValue(criterion, state))));
                }
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getBlockPosTooltip(IUtils utils, int pad, BlockPos pos) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.x", pos.getX()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.y", pos.getY()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.z", pos.getZ()));

        return components;
    }

    @NotNull
    public static List<Component> getCopyOperationTooltip(IUtils utils, int pad, CopyNbtFunction.CopyOperation copyOperation) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.operation")));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.source", copyOperation.sourcePathText));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.target", copyOperation.targetPathText));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.merge_strategy", copyOperation.op));

        return components;
    }

    // HELPERS

    @Unmodifiable
    @NotNull
    public static List<Component> getRangeValueTooltip(IUtils utils, int pad, String key, RangeValue value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBooleanTooltip(IUtils utils, int pad, String key, Boolean value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntegerTooltip(IUtils utils, int pad, String key, int value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLongTooltip(IUtils utils, int pad, String key, Long value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getStringTooltip(IUtils utils, int pad, String key, String value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFloatTooltip(IUtils utils, int pad, String key, Float value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(IUtils utils, int pad, String key, String enumName, Enum<?> value) {
        return List.of(pad(pad, translatable(key, value(translatableType("ali.enum." + enumName, value)))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(IUtils utils, int pad, String key, Enum<?> value) {
        return List.of(pad(pad, translatable(key, value(value.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(IUtils utils, int pad, Enum<?> value) {
        return List.of(pad(pad, value(value.name())));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getResourceLocationTooltip(IUtils utils, int pad, String key, ResourceLocation value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getResourceKeyTooltip(IUtils utils, int pad, String key, ResourceKey<T> value) {
        return List.of(pad(pad, translatable(key, value(value.location()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTagKeyTooltip(IUtils utils, int pad, String key, TagKey<?> value) {
        return getResourceLocationTooltip(utils, pad, key, value.location());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(IUtils utils, int pad, String key, Component component) {
        return List.of(pad(pad, translatable(key, value(component))));
    }

    @NotNull
    public static List<Component> getComponentsTooltip(IUtils utils, int pad, String key, List<Component> componentList) {
        List<Component> components = new LinkedList<>();

        if (!componentList.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(componentList);
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(IUtils utils, int pad, String key, MinMaxBounds.Ints ints) {
        List<Component> components = new LinkedList<>();

        if (ints != MinMaxBounds.Ints.ANY) {
            components.add(pad(pad, translatable(key, value(toString(ints)))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(IUtils utils, int pad, String key, MinMaxBounds.Doubles doubles) {
        List<Component> components = new LinkedList<>();

        if (doubles != MinMaxBounds.Doubles.ANY) {
            components.add(pad(pad, translatable(key, value(toString(doubles)))));
        }

        return components;
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(IUtils utils, int pad, @Nullable T optional, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        if (optional != null) {
            return mapper.apply(utils, pad, optional);
        } else {
            return List.of();
        }
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(IUtils utils, int pad, String key, @Nullable T optional, QuadFunction<IUtils, Integer, String, T, List<Component>> mapper) {
        if (optional != null) {
            return mapper.apply(utils, pad, key, optional);
        } else {
            return List.of();
        }
    }

    @NotNull
    public static <T> List<Component> getHolderTooltip(IUtils utils, int pad, Holder<T> holder, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        return mapper.apply(utils, pad, holder.value());
    }

    @NotNull
    public static <T> List<Component> getCollectionTooltip(IUtils utils, int pad, String key, Collection<T> values, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        if (!values.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            values.forEach((value) -> components.addAll(mapper.apply(utils, pad + 1, value)));
        }

        return components;
    }

    // PRIVATE

    @NotNull
    private static String toString(MinMaxBounds.Doubles doubles) {
        Double min = doubles.getMin();
        Double max = doubles.getMax();

        if (min != null) {
            if (max != null) {
                if (!Objects.equals(min, max)) {
                    return String.format("%.1f-%.1f", min, max);
                } else {
                    return String.format("=%.1f", min);
                }
            } else {
                return String.format("≥%.1f", min);
            }
        } else {
            if (max != null) {
                return String.format("≤%.1f", max);
            }

            return "???";
        }
    }

    @NotNull
    private static String toString(MinMaxBounds.Ints ints) {
        Integer min = ints.getMin();
        Integer max = ints.getMax();

        if (min != null) {
            if (max != null) {
                if (!Objects.equals(min, max)) {
                    return String.format("%d-%d", min, max);
                } else {
                    return String.format("=%d", min);
                }
            } else {
                return String.format("≥%d", min);
            }
        } else {
            if (max != null) {
                return String.format("≤%d", max);
            }

            return "???";
        }
    }

    @NotNull
    @Unmodifiable
    private static List<Component> getQualityTooltip(LootPoolSingletonContainer entry) {
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
    private static List<Component> getBonusChanceTooltip(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance) {
        List<Component> components = new LinkedList<>();

        if (bonusChance != null) {
            bonusChance.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                    components.add(pad(1, translatable(
                            "ali.description.chance_bonus",
                            value(entry.getValue(), "%"),
                            Component.translatable(bonusChance.getFirst().getDescriptionId()),
                            Component.translatable("enchantment.level." + entry.getKey())
                    )))
            );
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCountTooltip(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        List<Component> components = new LinkedList<>();

        if (bonusCount != null) {
            bonusCount.getSecond().entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach((entry) ->
                    components.add(pad(1, translatable(
                            "ali.description.count_bonus",
                            value(entry.getValue()),
                            Component.translatable(bonusCount.getFirst().getDescriptionId()),
                            Component.translatable("enchantment.level." + entry.getKey())
                    )))
            );
        }

        return components;
    }

    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
