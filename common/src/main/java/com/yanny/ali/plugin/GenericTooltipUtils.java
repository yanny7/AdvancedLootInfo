package com.yanny.ali.plugin;

import com.google.gson.JsonObject;
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
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

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
            components.addAll(getConditionsTooltip(1, conditions));
        }
        if (!functions.isEmpty()) {
            components.add(translatable("ali.util.advanced_loot_info.delimiter.functions"));
            components.addAll(getFunctionsTooltip(1, functions));
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
                components.add(pad(pad + 1, translatable("ali.property.common.conditions")));
                components.addAll(getConditionsTooltip(pad + 2, conditionalFunction.conditions));
            }

            return components;
        }).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFormulaTooltip(int pad, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(pad, "ali.property.common.formula", formula.getType().id()));

        if (formula.getType() == ApplyBonusCount.BinomialWithBonusCount.TYPE) {
            components.addAll(getIntegerTooltip(pad + 1, "ali.property.common.extra_rounds", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getExtraRounds()));
            components.addAll(getFloatTooltip(pad + 1, "ali.property.common.probability", Optional.of(((MixinApplyBonusCount.BinomialWithBonusCount) formula).getProbability())));
        } else if (formula.getType() == ApplyBonusCount.UniformBonusCount.TYPE) {
            components.addAll(getIntegerTooltip(pad + 1, "ali.property.common.bonus_multiplier", ((MixinApplyBonusCount.UniformBonusCount) formula).getBonusMultiplier()));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNameSourceTooltip(int pad, CopyNameFunction.NameSource source) {
        return getEnumTooltip(pad, "ali.property.common.name_source", "name_source", Optional.of(source));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockTooltip(int pad, Holder<Block> block) {
        return List.of(pad(pad, translatable("ali.property.common.block", value(translatable(block.value().getDescriptionId())))));
    }

    @NotNull
    public static List<Component> getPropertiesTooltip(int pad, Set<Property<?>> properties) {
        List<Component> components = new LinkedList<>();

        if (!properties.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.properties")));
            properties.forEach((property) -> components.addAll(getPropertyTooltip(pad + 1, property)));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPropertyTooltip(int pad, Property<?> property) {
        return List.of(pad(pad, value(pair(property.getName(), property.getPossibleValues().toString()))));
    }

    @NotNull
    public static List<Component> getEnchantmentsTooltip(int pad, Optional<HolderSet<Enchantment>> enchantments) {
        List<Component> components = new LinkedList<>();

        enchantments.ifPresent((e) -> {
            if (e.size() > 0) {
                components.add(pad(pad, translatable("ali.property.common.enchantments")));
                e.forEach((enchantment) -> components.addAll(getEnchantmentTooltip(pad + 1, Optional.of(enchantment))));
            }
        });

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(int pad, Optional<Holder<Enchantment>> enchantment) {
        return enchantment.map((e) -> List.of(pad(pad, translatable("ali.property.common.enchantment", value(translatable(e.value().getDescriptionId())))))).orElse(List.of());
    }

    @NotNull
    public static List<Component> getModifiersTooltip(int pad, List<SetAttributesAliFunction.Modifier> modifiers) {
        List<Component> components = new LinkedList<>();

        if (!modifiers.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.modifiers")));
            modifiers.forEach((modifier) -> components.addAll(getModifierTooltip(pad + 1, modifier)));
        }

        return components;
    }

    @NotNull
    public static List<Component> getModifierTooltip(int pad, SetAttributesAliFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.common.modifier")));

        components.addAll(getStringTooltip(pad + 1, "ali.property.common.name", Optional.ofNullable(modifier.name())));
        components.addAll(getAttributeTooltip(pad + 1, modifier.attribute()));
        components.addAll(getOperationTooltip(pad + 1, modifier.operation()));
        components.addAll(getRangeValueTooltip(pad + 1, "ali.property.common.amount", modifier.amount()));
        components.addAll(getUUIDTooltip(pad + 1, modifier.id()));

        components.addAll(getEquipmentSlotsTooltip(pad + 1, modifier.slots()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(int pad, Holder<Attribute> attribute) {
        return List.of(pad(pad, translatable("ali.property.common.attribute", value(translatable(attribute.value().getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getOperationTooltip(int pad, AttributeModifier.Operation operation) {
        return List.of(pad(pad, translatable("ali.property.common.operation", value(operation.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(int pad, Optional<UUID> uuid) {
        return uuid.map((u) -> List.of(pad(pad, translatable("ali.property.common.uuid", value(u))))).orElse(List.of());
    }

    @NotNull
    public static List<Component> getEquipmentSlotsTooltip(int pad, List<EquipmentSlot> equipmentSlots) {
        List<Component> components = new LinkedList<>();

        if (!equipmentSlots.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.equipment_slots")));
            equipmentSlots.forEach((slot) -> components.addAll(getEnumTooltip(pad + 1, slot)));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBannerPatternsTooltip(int pad, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        List<Component> components = new LinkedList<>();

        if (!patterns.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.banner_patterns")));
            patterns.forEach((pair) -> {
                components.addAll(getBannerPatternTooltip(pad + 1, pair.getFirst().value()));
                components.addAll(getEnumTooltip(pad + 2, "ali.property.common.color", pair.getSecond()));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(int pad, BannerPattern bannerPattern) {
        return List.of(pad(pad, translatable("ali.property.common.banner_pattern", value(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.getKey(bannerPattern))))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(int pad, Holder<BlockEntityType<?>> blockEntityType) {
        ResourceLocation location = Objects.requireNonNull(blockEntityType.value().builtInRegistryHolder()).unwrapKey().orElseThrow().location();
        return List.of(pad(pad, translatable("ali.property.common.block_entity_type", value(location))));
    }

    @NotNull
    public static List<Component> getPotionTooltip(int pad, Optional<Holder<Potion>> potion) {
        List<Component> components = new LinkedList<>();

        potion.ifPresent((p) -> {
            components.add(pad(pad, translatable("ali.property.common.potion")));
            components.addAll(getMobEffectInstancesTooltip(pad + 1, p.value().getEffects()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancesTooltip(int pad, List<MobEffectInstance> mobEffectInstances) {
        List<Component> components = new LinkedList<>();

        if (!mobEffectInstances.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.mob_effects")));
            mobEffectInstances.forEach((effectInstance) -> {
                components.addAll(getMobEffectTooltip(pad + 1, Holder.direct(effectInstance.getEffect())));
                components.addAll(getIntegerTooltip(pad + 2, "ali.property.common.amplifier", effectInstance.getAmplifier()));
                components.addAll(getIntegerTooltip(pad + 2, "ali.property.common.duration", effectInstance.getDuration()));
                components.addAll(getBooleanTooltip(pad + 2, "ali.property.common.is_ambient", Optional.of(effectInstance.isAmbient())));
                components.addAll(getBooleanTooltip(pad + 2, "ali.property.common.is_visible", Optional.of(effectInstance.isVisible())));
                components.addAll(getBooleanTooltip(pad + 2, "ali.property.common.show_icon", Optional.of(effectInstance.showIcon())));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(int pad, Holder<MobEffect> mobEffect) {
        return List.of(pad(pad, translatable("ali.property.common.mob_effect", value(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(mobEffect.value()))))));
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(int pad, Optional<StatePropertiesPredicate> propertiesPredicate) {
        List<Component> components = new LinkedList<>();

        propertiesPredicate.ifPresent((predicate) -> {
            List<StatePropertiesPredicate.PropertyMatcher> matchers = predicate.properties();

            if (!matchers.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.common.state_properties_predicate")));
                matchers.forEach((propertyMatcher) -> components.addAll(getPropertyMatcherTooltip(pad + 1, propertyMatcher)));
            }
        });

        return components;
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
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_both", name, min.get(), max.get()))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_gte", name, min.get()))));
                }
            } else {
                if (max.isPresent()) {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_lte", name, max.get()))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_any", name))));
                }
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePredicateTooltip(int pad, Optional<DamageSourcePredicate> damagePredicate) {
        List<Component> components = new LinkedList<>();

        damagePredicate.ifPresent((predicate) -> {
            List<TagPredicate<DamageType>> tagPredicates = predicate.tags();

            components.add(pad(pad, translatable("ali.property.common.damage_source_predicate")));
            components.addAll(getTagPredicatesTooltip(pad + 1, tagPredicates));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.direct_entity", getEntityPredicateTooltip(pad + 2, predicate.directEntity())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.source_entity", getEntityPredicateTooltip(pad + 2, predicate.sourceEntity())));
        });

        return components;
    }

    @NotNull
    public static <T> List<Component> getTagPredicatesTooltip(int pad, List<TagPredicate<T>> tagPredicates) {
        List<Component> components = new LinkedList<>();

        if (!tagPredicates.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.tag_predicates")));
            tagPredicates.forEach((tagPredicate) -> {
                components.add(pad(pad + 1, keyValue(tagPredicate.tag().location().toString(), tagPredicate.expected())));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(int pad, Optional<EntityPredicate> entityPredicate) {
        List<Component> components = new LinkedList<>();

        entityPredicate.ifPresent((predicate) -> {
            components.addAll(getEntityTypePredicateTooltip(pad, predicate.entityType()));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.distance_to_player", getDistancePredicateTooltip(pad + 1, predicate.distanceToPlayer())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.location", getLocationPredicateTooltip(pad + 1, predicate.location())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.stepping_on_location", getLocationPredicateTooltip(pad + 1, predicate.steppingOnLocation())));
            components.addAll(getMobEffectPredicateTooltip(pad, predicate.effects()));
            components.addAll(getNbtPredicateTooltip(pad, predicate.nbt()));
            components.addAll(getEntityFlagsPredicateTooltip(pad, predicate.flags()));
            components.addAll(getEntityEquipmentPredicateTooltip(pad, predicate.equipment()));
            components.addAll(getEntitySubPredicateTooltip(pad, predicate.subPredicate()));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.vehicle", getEntityPredicateTooltip(pad + 1, predicate.vehicle())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.passenger", getEntityPredicateTooltip(pad + 1, predicate.passenger())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.targeted_entity", getEntityPredicateTooltip(pad + 1, predicate.targetedEntity())));
            components.addAll(getStringTooltip(pad, "ali.property.common.team", predicate.team()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(int pad, Optional<EntityTypePredicate> entityTypePredicate) {
        List<Component> components = new LinkedList<>();

        entityTypePredicate.ifPresent((predicate) -> {
            HolderSet<EntityType<?>> holderSet = predicate.types();
            Optional<TagKey<EntityType<?>>> tagKey = holderSet.unwrapKey();

            if (holderSet.size() > 0 || tagKey.isPresent()) {
                components.add(pad(pad, translatable("ali.property.common.entity_types")));
                holderSet.forEach((type) -> {
                    components.addAll(getComponentTooltip(pad + 1, "ali.property.common.entity_type", Optional.of(value(translatable(type.value().getDescriptionId())))));
                });
                components.addAll(getTagKeyTooltip(pad + 1, "ali.property.common.entity_type", tagKey));
            }
        });

        return components;
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(int pad, Optional<DistancePredicate> distancePredicate) {
        List<Component> components = new LinkedList<>();

        distancePredicate.ifPresent((predicate) -> {
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.x", predicate.x()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.y", predicate.y()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.z", predicate.z()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.horizontal", predicate.horizontal()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.absolute", predicate.absolute()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(int pad, Optional<LocationPredicate> locationPredicate) {
        List<Component> components = new LinkedList<>();

        locationPredicate.ifPresent((predicate) -> {
            components.addAll(getPositionPredicateTooltip(pad, predicate.position()));
            components.addAll(getResourceKeyTooltip(pad, "ali.property.common.biome", predicate.biome()));
            components.addAll(getResourceKeyTooltip(pad, "ali.property.common.structure", predicate.structure()));
            components.addAll(getResourceKeyTooltip(pad, "ali.property.common.dimension", predicate.dimension()));
            components.addAll(getBooleanTooltip(pad, "ali.property.common.smokey", predicate.smokey()));
            components.addAll(getLightPredicateTooltip(pad, predicate.light()));
            components.addAll(getBlockPredicateTooltip(pad, predicate.block()));
            components.addAll(getFluidPredicateTooltip(pad, predicate.fluid()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getPositionPredicateTooltip(int pad, Optional<LocationPredicate.PositionPredicate> positionPredicate) {
        List<Component> components = new LinkedList<>();

        positionPredicate.ifPresent((predicate) -> {
            components.add(pad(pad, translatable("ali.property.common.position")));
            components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.x", predicate.x()));
            components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.y", predicate.y()));
            components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.z", predicate.z()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(int pad, Optional<LightPredicate> lightPredicate) {
        return lightPredicate.map((predicate) -> getMinMaxBoundsTooltip(pad, "ali.property.common.light", predicate.composite())).orElse(List.of());
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(int pad, Optional<BlockPredicate> blockPredicate) {
        List<Component> components = new LinkedList<>();

        blockPredicate.ifPresent((predicate) -> {
            components.add(pad(pad, translatable("ali.property.common.block_predicate")));
            components.addAll(getTagKeyTooltip(pad + 1, "ali.property.common.tag", predicate.tag()));
            predicate.blocks().ifPresent((blocks) -> {
                if (blocks.size() > 0) {
                    components.add(pad(pad + 1, translatable("ali.property.common.blocks")));
                    blocks.forEach((block) -> components.addAll(getBlockTooltip(pad + 2, block)));
                }
            });

            components.addAll(getStatePropertiesPredicateTooltip(pad, predicate.properties()));
            components.addAll(getNbtPredicateTooltip(pad, predicate.nbt()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getNbtPredicateTooltip(int pad, Optional<NbtPredicate> nbtPredicate) {
        return nbtPredicate.map((predicate) -> List.of(pad(pad, translatable("ali.property.common.nbt", value(predicate.tag()))))).orElse(List.of());
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(int pad, Optional<FluidPredicate> fluidPredicate) {
        List<Component> components = new LinkedList<>();

        fluidPredicate.ifPresent((predicate) -> {
            components.add(pad(pad, translatable("ali.property.common.fluid_predicate")));
            components.addAll(getTagKeyTooltip(pad + 1, "ali.property.common.tag", predicate.tag()));
            components.addAll(getFluidTooltip(pad + 1, predicate.fluid()));
            components.addAll(getStatePropertiesPredicateTooltip(pad + 1, predicate.properties()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getFluidTooltip(int pad, Optional<Holder<Fluid>> fluid) {
        return fluid.map((f) -> List.of(pad(pad, translatable("ali.property.common.fluid", value(translatable(BuiltInRegistries.FLUID.getKey(f.value()).toString())))))).orElse(List.of());
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(int pad, Optional<MobEffectsPredicate> mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        mobEffectsPredicate.ifPresent((predicate) -> {
            components.add(pad(pad, translatable("ali.property.common.mob_effects")));

            predicate.effectMap().forEach((effect, instancePredicate) -> {
                components.addAll(getMobEffectTooltip(pad + 1, effect));
                components.addAll(getMobEffectInstancePredicateTooltip(pad + 2, instancePredicate));
            });
        });

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(int pad, MobEffectsPredicate.MobEffectInstancePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.amplifier", predicate.amplifier()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.duration", predicate.duration()));
        components.addAll(getBooleanTooltip(pad, "ali.property.common.is_ambient", predicate.ambient()));
        components.addAll(getBooleanTooltip(pad, "ali.property.common.is_visible", predicate.visible()));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(int pad, Optional<EntityFlagsPredicate> entityFlagsPredicate) {
        List<Component> components = new LinkedList<>();

        entityFlagsPredicate.ifPresent((predicate) -> {
            components.add(pad(pad, translatable("ali.property.common.entity_flags")));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_on_fire", predicate.isOnFire()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_baby", predicate.isBaby()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_crouching", predicate.isCrouching()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_sprinting", predicate.isSprinting()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_swimming", predicate.isSwimming()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(int pad, Optional<EntityEquipmentPredicate> entityEquipmentPredicate) {
        List<Component> components = new LinkedList<>();

        entityEquipmentPredicate.ifPresent((predicate) -> {
            components.add(pad(pad, translatable("ali.property.common.entity_equipment")));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.head", getItemPredicateTooltip(pad + 2, predicate.head())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.chest", getItemPredicateTooltip(pad + 2, predicate.chest())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.legs", getItemPredicateTooltip(pad + 2, predicate.legs())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.feet", getItemPredicateTooltip(pad + 2, predicate.feet())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.mainhand", getItemPredicateTooltip(pad + 2, predicate.mainhand())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.offhand", getItemPredicateTooltip(pad + 2, predicate.offhand())));
        });

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(int pad, Optional<ItemPredicate> itemPredicate) {
        List<Component> components = new LinkedList<>();

        itemPredicate.ifPresent((predicate) -> {
            Optional<HolderSet<Item>> items = predicate.items();
            List<EnchantmentPredicate> enchantments = predicate.enchantments();
            List<EnchantmentPredicate> storedEnchantments = predicate.storedEnchantments();

            components.addAll(getTagKeyTooltip(pad, "ali.property.common.tag", predicate.tag()));

            items.ifPresent((i) -> {
                if (i.size() > 0) {
                    components.add(pad(pad, translatable("ali.property.common.items")));
                    i.forEach((item) -> components.addAll(getItemTooltip(pad + 1, item)));
                }
            });

            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.count", predicate.count()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.durability", predicate.durability()));

            if (!enchantments.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.common.enchantments")));

                for (EnchantmentPredicate enchantment : enchantments) {
                    components.addAll(getEnchantmentPredicateTooltip(pad + 1, Optional.ofNullable(enchantment)));
                }
            }

            if (!storedEnchantments.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.common.stored_enchantments")));

                for (EnchantmentPredicate enchantment : storedEnchantments) {
                    components.addAll(getEnchantmentPredicateTooltip(pad + 1, Optional.ofNullable(enchantment)));
                }
            }

            components.addAll(getPotionTooltip(pad, predicate.potion()));
            components.addAll(getNbtPredicateTooltip(pad, predicate.nbt()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(int pad, Optional<EnchantmentPredicate> enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        enchantmentPredicate.ifPresent((predicate) -> {
            components.addAll(getEnchantmentTooltip(pad, predicate.enchantment()));
            components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.level", predicate.level()));
        });

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(int pad, Optional<EntitySubPredicate> entitySubPredicate) {
        List<Component> components = new LinkedList<>();

        entitySubPredicate.ifPresent((subPredicate) -> {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == subPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable("ali.property.common.entity_sub_predicate", entry.getKey())));

                if (subPredicate instanceof LightningBoltPredicate predicate) {
                    components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.blocks_on_fire", predicate.blocksSetOnFire()));
                    components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.stuck_entity", getEntityPredicateTooltip(pad + 2, predicate.entityStruck())));
                } else if (subPredicate instanceof FishingHookPredicate predicate) {
                    components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.in_open_water", predicate.inOpenWater()));
                } else if (subPredicate instanceof PlayerPredicate predicate) {
                    components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.level", predicate.level()));
                    components.addAll(getGameTypeTooltip(pad + 1, predicate.gameType()));
                    components.addAll(getStatsTooltip(pad + 1, predicate.stats()));
                    components.addAll(getRecipesTooltip(pad + 1, predicate.recipes()));
                    components.addAll(getAdvancementsTooltip(pad + 1, predicate.advancements()));
                } else if (subPredicate instanceof SlimePredicate predicate) {
                    components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.size", predicate.size()));
                } else {
                    EntitySubPredicate.CODEC.encodeStart(JsonOps.INSTANCE, subPredicate).result().ifPresent((element) -> {
                        JsonObject jsonObject = element.getAsJsonObject();

                        if (jsonObject.has("variant")) {
                            components.add(pad(pad + 1, translatable("ali.property.common.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                        } else {
                            components.add(pad(pad + 1, translatable("ali.property.common.variant", jsonObject.toString())));
                        }
                    });
                }
            });
        });

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemTooltip(int pad, Holder<Item> item) {
        return List.of(pad(pad, translatable("ali.property.common.item", value(translatable(item.value().getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypeTooltip(int pad, Optional<GameType> gameType) {
        return gameType.map((g) -> List.of(pad(pad, value(translatable("ali.property.common.game_type", g.getShortDisplayName()))))).orElse(List.of());
    }

    @NotNull
    public static List<Component> getStatsTooltip(int pad, List<PlayerPredicate.StatMatcher<?>> statMatchers) {
        List<Component> components = new LinkedList<>();

        if (!statMatchers.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.stats")));
            statMatchers.forEach((stat) -> {
                Holder<?> value = stat.value();

                if (value.value() instanceof Item) {
                    components.addAll(getItemTooltip(pad + 1, (Holder<Item>) value));
                } else if (value.value() instanceof Block) {
                    components.addAll(getBlockTooltip(pad + 1, (Holder<Block>) value));
                } else {
                    components.add(pad(pad + 1, value.value().toString()));
                }

                components.add(pad(pad + 2, keyValue(stat.type().getDisplayName(), toString(stat.range()))));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getRecipesTooltip(int pad, Object2BooleanMap<ResourceLocation> recipes) {
        List<Component> components = new LinkedList<>();

        if (!recipes.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.recipes")));
            recipes.forEach((recipe, required) -> components.add(pad(pad + 1, keyValue(recipe.toString(), required))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getAdvancementsTooltip(int pad, Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap) {
        List<Component> components = new LinkedList<>();

        if (!predicateMap.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.advancements")));
            predicateMap.forEach((advancement, predicate) -> {
                components.add(pad(pad + 1, advancement.toString()));

                if (predicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                    components.add(pad(pad + 2, translatable("ali.property.common.done", donePredicate.state())));
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

        components.addAll(getIntegerTooltip(pad, "ali.property.common.x", pos.getX()));
        components.addAll(getIntegerTooltip(pad, "ali.property.common.y", pos.getY()));
        components.addAll(getIntegerTooltip(pad, "ali.property.common.z", pos.getZ()));

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
    public static List<Component> getBooleanTooltip(int pad, String key, Optional<Boolean> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(v))))).orElse(List.of());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntegerTooltip(int pad, String key, int value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLongTooltip(int pad, String key, Optional<Long> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(v))))).orElse(List.of());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getStringTooltip(int pad, String key, Optional<String> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(v))))).orElse(List.of());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFloatTooltip(int pad, String key, Optional<Float> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(v))))).orElse(List.of());
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
    public static <T> List<Component> getResourceKeyTooltip(int pad, String key, Optional<ResourceKey<T>> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(v.location()))))).orElse(List.of());
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagKeyTooltip(int pad, String key, Optional<TagKey<T>> value) {
        return value.map((v) -> getResourceLocationTooltip(pad, key, v.location())).orElse(List.of());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(int pad, String key, Optional<Component> component) {
        return component.map((c) -> List.of(pad(pad, translatable(key, value(c))))).orElse(List.of());
    }

    @NotNull
    public static List<Component> getComponentsTooltip(int pad, String key, List<Component> componentList) {
        List<Component> components = new LinkedList<>();

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
            return max.map(aDouble -> String.format("<%.1f", aDouble)).orElse("???");
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
            return max.map(integer -> String.format("<%d", integer)).orElse("???");

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
