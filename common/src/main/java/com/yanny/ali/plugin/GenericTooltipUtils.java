package com.yanny.ali.plugin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.*;
import com.yanny.ali.plugin.function.SetAttributesAliFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class GenericTooltipUtils {
    @NotNull
    public static List<Component> getConditionsTooltip(int pad, List<ILootCondition> conditions) {
        return conditions.stream().map((condition) -> condition.getTooltip(pad)).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFunctionsTooltip(int pad, List<ILootFunction> functions) {
        return functions.stream().map((function) -> function.getTooltip(pad)).flatMap(Collection::stream).toList();

        //TODO conditional function
    }

    @NotNull
    public static List<Component> getFormulaTooltip(int pad, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(pad, "ali.property.common.formula", formula.getType()));

        if (formula.getType() == ApplyBonusCount.BinomialWithBonusCount.TYPE) {
            components.addAll(getIntegerTooltip(pad + 1, "ali.property.common.extra_rounds", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getExtraRounds()));
            components.addAll(getFloatTooltip(pad + 1, "ali.property.common.probability", ((MixinApplyBonusCount.BinomialWithBonusCount) formula).getProbability()));
        } else if (formula.getType() == ApplyBonusCount.UniformBonusCount.TYPE) {
            components.addAll(getIntegerTooltip(pad + 1, "ali.property.common.bonus_multiplier", ((MixinApplyBonusCount.UniformBonusCount) formula).getBonusMultiplier()));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNameSourceTooltip(int pad, CopyNameFunction.NameSource source) {
        return getEnumTooltip(pad, "ali.property.common.name_source", "name_source", source);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockTooltip(int pad, Block block) {
        return List.of(pad(pad, translatable("ali.property.common.block", value(translatable(block.getDescriptionId())))));
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
    public static List<Component> getEnchantmentsTooltip(int pad, List<Enchantment> enchantments) {
        List<Component> components = new LinkedList<>();

        if (!enchantments.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.enchantments")));
            enchantments.forEach((enchantment) -> components.addAll(getEnchantmentTooltip(pad + 1, enchantment)));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(int pad, @Nullable Enchantment enchantment) {
        if (enchantment != null) {
            return List.of(pad(pad, translatable("ali.property.common.enchantment", value(translatable(enchantment.getDescriptionId())))));
        } else {
            return List.of();
        }
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

        components.addAll(getStringTooltip(pad + 1, "ali.property.common.name", modifier.name()));
        components.addAll(getAttributeTooltip(pad + 1, modifier.attribute()));
        components.addAll(getOperationTooltip(pad + 1, modifier.operation()));
        components.addAll(getRangeValueTooltip(pad + 1, "ali.property.common.amount", modifier.amount()));

        if (modifier.id() != null) {
            components.addAll(getUUIDTooltip(pad + 1, modifier.id()));
        }

        components.addAll(getEquipmentSlotsTooltip(pad + 1, Arrays.asList(modifier.slots())));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(int pad, Attribute attribute) {
        return List.of(pad(pad, translatable("ali.property.common.attribute", value(translatable(attribute.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getOperationTooltip(int pad, AttributeModifier.Operation operation) {
        return List.of(pad(pad, translatable("ali.property.common.operation", value(operation.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(int pad, UUID uuid) {
        return List.of(pad(pad, translatable("ali.property.common.uuid", value(uuid))));
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
    public static List<Component> getBlockEntityTypeTooltip(int pad, BlockEntityType<?> blockEntityType) {
        return List.of(pad(pad, translatable("ali.property.common.block_entity_type", value(Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType))))));
    }

    @NotNull
    public static List<Component> getPotionTooltip(int pad, @Nullable Potion potion) {
        List<Component> components = new LinkedList<>();

        if (potion != null) {
            components.add(pad(pad, translatable("ali.property.common.potion")));
            components.addAll(getMobEffectInstancesTooltip(pad + 1, potion.getEffects()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancesTooltip(int pad, List<MobEffectInstance> mobEffectInstances) {
        List<Component> components = new LinkedList<>();

        if (!mobEffectInstances.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.mob_effects")));
            mobEffectInstances.forEach((effectInstance) -> {
                components.addAll(getMobEffectTooltip(pad + 1, effectInstance.getEffect()));
                components.addAll(getIntegerTooltip(pad + 2, "ali.property.common.amplifier", effectInstance.getAmplifier()));
                components.addAll(getIntegerTooltip(pad + 2, "ali.property.common.duration", effectInstance.getDuration()));
                components.addAll(getBooleanTooltip(pad + 2, "ali.property.common.is_ambient", effectInstance.isAmbient()));
                components.addAll(getBooleanTooltip(pad + 2, "ali.property.common.is_visible", effectInstance.isVisible()));
                components.addAll(getBooleanTooltip(pad + 2, "ali.property.common.show_icon", effectInstance.showIcon()));
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(int pad, MobEffect mobEffect) {
        return List.of(pad(pad, translatable("ali.property.common.mob_effect", value(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(mobEffect))))));
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(int pad, StatePropertiesPredicate propertiesPredicate) {
        List<Component> components = new LinkedList<>();

        if (propertiesPredicate != StatePropertiesPredicate.ANY) {
            MixinStatePropertiesPredicate predicate = (MixinStatePropertiesPredicate) propertiesPredicate;
            List<StatePropertiesPredicate.PropertyMatcher> matchers = predicate.getProperties();

            if (!matchers.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.common.state_properties_predicate")));
                matchers.forEach((propertyMatcher) -> components.addAll(getPropertyMatcherTooltip(pad + 1, propertyMatcher)));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        List<Component> components = new LinkedList<>();

        if (propertyMatcher instanceof MixinStatePropertiesPredicate.ExactPropertyMatcher matcher) {
            components.add(pad(pad, keyValue(matcher.getName(), matcher.getValue())));
        }
        if (propertyMatcher instanceof MixinStatePropertiesPredicate.RangedPropertyMatcher matcher) {
            String min = matcher.getMinValue();
            String max = matcher.getMaxValue();

            if (min != null) {
                if (max != null) {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_both", matcher.getName(), min, max))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_gte", matcher.getName(), min))));
                }
            } else {
                if (max != null) {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_lte", matcher.getName(), max))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.common.ranged_property_any", matcher.getName()))));
                }
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePredicateTooltip(int pad, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        if (damagePredicate != DamageSourcePredicate.ANY) {
            MixinDamageSourcePredicate predicate = (MixinDamageSourcePredicate) damagePredicate;
            List<TagPredicate<DamageType>> tagPredicates = predicate.getTags();

            components.add(pad(pad, translatable("ali.property.common.damage_source_predicate")));
            components.addAll(getTagPredicatesTooltip(pad + 1, tagPredicates));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.direct_entity", getEntityPredicateTooltip(pad + 2, predicate.getDirectEntity())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.source_entity", getEntityPredicateTooltip(pad + 2, predicate.getSourceEntity())));
        }

        return components;
    }

    @NotNull
    public static <T> List<Component> getTagPredicatesTooltip(int pad, List<TagPredicate<T>> tagPredicates) {
        List<Component> components = new LinkedList<>();

        if (!tagPredicates.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.tag_predicates")));
            tagPredicates.forEach((tagPredicate) -> {
                MixinTagPredicate<?> predicate = (MixinTagPredicate<?>) tagPredicate;

                components.add(pad(pad + 1, keyValue(predicate.getTag().location().toString(), predicate.getExpected())));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(int pad, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        if (entityPredicate != EntityPredicate.ANY) {
            MixinEntityPredicate predicate = (MixinEntityPredicate) entityPredicate;

            components.addAll(getEntityTypePredicateTooltip(pad, predicate.getEntityType()));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.distance_to_player", getDistancePredicateTooltip(pad + 1, predicate.getDistanceToPlayer())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.location", getLocationPredicateTooltip(pad + 1, predicate.getLocation())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.stepping_on_location", getLocationPredicateTooltip(pad + 1, predicate.getSteppingOnLocation())));
            components.addAll(getMobEffectPredicateTooltip(pad, predicate.getEffects()));
            components.addAll(getNbtPredicateTooltip(pad, predicate.getNbt()));
            components.addAll(getEntityFlagsPredicateTooltip(pad, predicate.getFlags()));
            components.addAll(getEntityEquipmentPredicateTooltip(pad, predicate.getEquipment()));
            components.addAll(getEntitySubPredicateTooltip(pad, predicate.getSubPredicate()));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.vehicle", getEntityPredicateTooltip(pad + 1, predicate.getVehicle())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.passenger", getEntityPredicateTooltip(pad + 1, predicate.getPassenger())));
            components.addAll(getComponentsTooltip(pad, "ali.property.common.targeted_entity", getEntityPredicateTooltip(pad + 1, predicate.getTargetedEntity())));
            components.addAll(getStringTooltip(pad, "ali.property.common.team", predicate.getTeam()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(int pad, EntityTypePredicate entityTypePredicate) {
        List<Component> components = new LinkedList<>();

        if (entityTypePredicate != EntityTypePredicate.ANY) {
            if (entityTypePredicate instanceof MixinEntityTypePredicate.TypePredicate typePredicate) {
                components.addAll(getComponentTooltip(pad, "ali.property.common.entity_type", value(typePredicate.getType().getDescription())));
            }
            if (entityTypePredicate instanceof MixinEntityTypePredicate.TagPredicate tagPredicate) {
                components.addAll(getComponentTooltip(pad, "ali.property.common.entity_type", value(tagPredicate.getTag().location())));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(int pad, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        if (distancePredicate != DistancePredicate.ANY) {
            MixinDistancePredicate predicate = (MixinDistancePredicate) distancePredicate;

            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.x", predicate.getX()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.y", predicate.getY()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.z", predicate.getZ()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.horizontal", predicate.getHorizontal()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.absolute", predicate.getAbsolute()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(int pad, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        if (locationPredicate != LocationPredicate.ANY) {
            MixinLocationPredicate predicate = (MixinLocationPredicate) locationPredicate;

            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.x", predicate.getX()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.y", predicate.getY()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.z", predicate.getZ()));
            components.addAll(getResourceKeyTooltip(pad, "ali.property.common.biome", predicate.getBiome()));
            components.addAll(getResourceKeyTooltip(pad, "ali.property.common.structure", predicate.getStructure()));
            components.addAll(getResourceKeyTooltip(pad, "ali.property.common.dimension", predicate.getDimension()));
            components.addAll(getBooleanTooltip(pad, "ali.property.common.smokey", predicate.getSmokey()));
            components.addAll(getLightPredicateTooltip(pad, predicate.getLight()));
            components.addAll(getBlockPredicateTooltip(pad, predicate.getBlock()));
            components.addAll(getFluidPredicateTooltip(pad, predicate.getFluid()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(int pad, LightPredicate lightPredicate) {
        List<Component> components = new LinkedList<>();

        if (lightPredicate != LightPredicate.ANY) {
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.light", ((MixinLightPredicate) lightPredicate).getComposite()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(int pad, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        if (blockPredicate != BlockPredicate.ANY) {
            MixinBlockPredicate predicate = (MixinBlockPredicate) blockPredicate;

            components.add(pad(pad, translatable("ali.property.common.block_predicate")));
            components.addAll(getTagKeyTooltip(pad + 1, "ali.property.common.tag", predicate.getTag()));

            if (predicate.getBlocks() != null) {
                components.add(pad(pad + 1, translatable("ali.property.common.blocks")));

                predicate.getBlocks().forEach((block) -> components.addAll(getBlockTooltip(pad + 2, block)));
            }

            components.addAll(getStatePropertiesPredicateTooltip(pad, predicate.getProperties()));
            components.addAll(getNbtPredicateTooltip(pad, predicate.getNbt()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getNbtPredicateTooltip(int pad, NbtPredicate nbtPredicate) {
        List<Component> components = new LinkedList<>();

        if (nbtPredicate != NbtPredicate.ANY) {
            components.add(pad(pad, translatable("ali.property.common.nbt", value(((MixinNbtPredicate) nbtPredicate).getTag()))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(int pad, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        if (fluidPredicate != FluidPredicate.ANY) {
            MixinFluidPredicate predicate = (MixinFluidPredicate) fluidPredicate;

            components.add(pad(pad, translatable("ali.property.common.fluid_predicate")));
            components.addAll(getTagKeyTooltip(pad + 1, "ali.property.common.tag", predicate.getTag()));
            components.addAll(getFluidTooltip(pad + 1, predicate.getFluid()));
            components.addAll(getStatePropertiesPredicateTooltip(pad + 1, predicate.getProperties()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getFluidTooltip(int pad, @Nullable Fluid fluid) {
        List<Component> components = new LinkedList<>();

        if (fluid != null) {
            components.add(pad(pad, translatable("ali.property.common.fluid", value(translatable(BuiltInRegistries.FLUID.getKey(fluid).toString())))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(int pad, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        if (mobEffectsPredicate != MobEffectsPredicate.ANY) {
            MixinMobEffectPredicate predicate = (MixinMobEffectPredicate) mobEffectsPredicate;

            components.add(pad(pad, translatable("ali.property.common.mob_effects")));

            predicate.getEffects().forEach((effect, instancePredicate) -> {
                components.addAll(getMobEffectTooltip(pad + 1, effect));
                components.addAll(getMobEffectInstancePredicateTooltip(pad + 2, instancePredicate));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(int pad, MobEffectsPredicate.MobEffectInstancePredicate mobEffectInstancePredicate) {
        List<Component> components = new LinkedList<>();
        MixinMobEffectPredicate.MobEffectInstancePredicate predicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) mobEffectInstancePredicate;

        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.amplifier", predicate.getAmplifier()));
        components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.duration", predicate.getDuration()));
        components.addAll(getBooleanTooltip(pad, "ali.property.common.is_ambient", predicate.getAmbient()));
        components.addAll(getBooleanTooltip(pad, "ali.property.common.is_visible", predicate.getVisible()));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(int pad, EntityFlagsPredicate entityFlagsPredicate) {
        List<Component> components = new LinkedList<>();

        if (entityFlagsPredicate != EntityFlagsPredicate.ANY) {
            MixinEntityFlagsPredicate predicate = (MixinEntityFlagsPredicate) entityFlagsPredicate;

            components.add(pad(pad, translatable("ali.property.common.entity_flags")));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_on_fire", predicate.getIsOnFire()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_baby", predicate.getIsBaby()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_crouching", predicate.getIsCrouching()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_sprinting", predicate.getIsSprinting()));
            components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.is_swimming", predicate.getIsSwimming()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(int pad, EntityEquipmentPredicate entityEquipmentPredicate) {
        List<Component> components = new LinkedList<>();

        if (entityEquipmentPredicate != EntityEquipmentPredicate.ANY) {
            MixinEntityEquipmentPredicate predicate = (MixinEntityEquipmentPredicate) entityEquipmentPredicate;

            components.add(pad(pad, translatable("ali.property.common.entity_equipment")));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.head", getItemPredicateTooltip(pad + 2, predicate.getHead())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.chest", getItemPredicateTooltip(pad + 2, predicate.getChest())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.legs", getItemPredicateTooltip(pad + 2, predicate.getLegs())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.feet", getItemPredicateTooltip(pad + 2, predicate.getFeet())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.mainhand", getItemPredicateTooltip(pad + 2, predicate.getMainhand())));
            components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.offhand", getItemPredicateTooltip(pad + 2, predicate.getOffhand())));
        }

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(int pad, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        if (itemPredicate != ItemPredicate.ANY) {
            MixinItemPredicate predicate = (MixinItemPredicate) itemPredicate;
            Set<Item> items = predicate.getItems();
            EnchantmentPredicate[] enchantments = predicate.getEnchantments();
            EnchantmentPredicate[] storedDnchantments = predicate.getEnchantments();

            components.addAll(getTagKeyTooltip(pad, "ali.property.common.tag", predicate.getTag()));

            if (items != null && !items.isEmpty()) {
                components.add(pad(pad, translatable("ali.property.common.items")));

                items.forEach((item) -> components.addAll(getItemTooltip(pad + 1, item)));
            }

            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.count", predicate.getCount()));
            components.addAll(getMinMaxBoundsTooltip(pad, "ali.property.common.durability", predicate.getDurability()));

            if (enchantments.length > 0) {
                components.add(pad(pad, translatable("ali.property.common.enchantments")));

                for (EnchantmentPredicate enchantment : enchantments) {
                    components.addAll(getEnchantmentPredicateTooltip(pad + 1, enchantment));
                }
            }

            if (storedDnchantments.length > 0) {
                components.add(pad(pad, translatable("ali.property.common.stored_enchantments")));

                for (EnchantmentPredicate enchantment : storedDnchantments) {
                    components.addAll(getEnchantmentPredicateTooltip(pad + 1, enchantment));
                }
            }

            components.addAll(getPotionTooltip(pad, predicate.getPotion()));
            components.addAll(getNbtPredicateTooltip(pad, predicate.getNbt()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(int pad, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            MixinEnchantmentPredicate predicate = (MixinEnchantmentPredicate) enchantmentPredicate;

            components.addAll(getEnchantmentTooltip(pad, predicate.getEnchantment()));
            components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.level", predicate.getLevel()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(int pad, EntitySubPredicate entitySubPredicate) {
        List<Component> components = new LinkedList<>();

        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable("ali.property.common.entity_sub_predicate", entry.getKey())));

                if (entitySubPredicate instanceof LighthingBoltPredicate predicate) {
                    MixinLighthingBoltPredicate boltPredicate = (MixinLighthingBoltPredicate) predicate;

                    components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.blocks_on_fire", boltPredicate.getBlocksSetOnFire()));
                    components.addAll(getComponentsTooltip(pad + 1, "ali.property.common.stuck_entity", getEntityPredicateTooltip(pad + 2, boltPredicate.getEntityStruck())));
                } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                    MixinFishingHookPredicate fishingHookPredicate = (MixinFishingHookPredicate) predicate;

                    components.addAll(getBooleanTooltip(pad + 1, "ali.property.common.in_open_water", fishingHookPredicate.isInOpenWater()));
                } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                    MixinPlayerPredicate playerPredicate = (MixinPlayerPredicate) predicate;

                    components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.level", playerPredicate.getLevel()));
                    components.addAll(getGameTypeTooltip(pad + 1, playerPredicate.getGameType()));
                    components.addAll(getStatsTooltip(pad + 1, playerPredicate.getStats()));
                    components.addAll(getRecipesTooltip(pad + 1, playerPredicate.getRecipes()));
                    components.addAll(getAdvancementsTooltip(pad + 1, playerPredicate.getAdvancements()));
                } else if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                    MixinSlimePredicate predicate = (MixinSlimePredicate) slimePredicate;

                    components.addAll(getMinMaxBoundsTooltip(pad + 1, "ali.property.common.size", predicate.getSize()));
                } else {
                    JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                    if (jsonObject.has("variant")) {
                        components.addAll(getStringTooltip(pad + 1, "ali.property.common.variant", jsonObject.getAsJsonPrimitive("variant").getAsString()));
                    } else {
                        components.addAll(getStringTooltip(pad + 1, "ali.property.common.variant", jsonObject.getAsString()));
                    }
                }
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemTooltip(int pad, Item item) {
        return List.of(pad(pad, translatable("ali.property.common.item", value(translatable(item.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypeTooltip(int pad, @Nullable GameType gameType) {
        if (gameType != null) {
            return List.of(pad(pad, value(translatable("ali.property.common.game_type", gameType.getShortDisplayName()))));
        } else {
            return List.of();
        }
    }

    @NotNull
    public static List<Component> getStatsTooltip(int pad, Map<Stat<?>, MinMaxBounds.Ints> statIntsMap) {
        List<Component> components = new LinkedList<>();

        if (!statIntsMap.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.common.stats")));
            statIntsMap.forEach((stat, ints) -> {
                Object value = stat.getValue();

                if (value instanceof Item item) {
                    components.addAll(getItemTooltip(pad + 1, item));
                } else if (value instanceof Block block) {
                    components.addAll(getBlockTooltip(pad + 1, block));
                } else {
                    components.add(pad(pad + 1, value));
                }

                components.add(pad(pad + 2, keyValue(stat.getType().getDisplayName(), toString((MixinMinMaxBounds.Ints) ints))));
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
                    components.add(pad(pad + 2, translatable("ali.property.common.done", ((MixinPlayerPredicate.AdvancementDonePredicate) donePredicate).getState())));
                } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                    MixinPlayerPredicate.AdvancementCriterionsPredicate mixPredicate = (MixinPlayerPredicate.AdvancementCriterionsPredicate) criterionsPredicate;

                    mixPredicate.getCriterions().forEach((criterion, state) -> components.add(pad(pad + 2, keyValue(criterion, state))));
                }
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getBlockPosTooltip(int pad, BlockPos pos) {
        List<Component> components = new LinkedList<>();

        if (!pos.equals(BlockPos.ZERO)) {
            components.addAll(getIntegerTooltip(pad, "ali.property.common.x", pos.getX()));
            components.addAll(getIntegerTooltip(pad, "ali.property.common.y", pos.getY()));
            components.addAll(getIntegerTooltip(pad, "ali.property.common.z", pos.getZ()));
        }

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
    public static List<Component> getBooleanTooltip(int pad, String key, @Nullable Boolean value) {
        if (value != null) {
            return List.of(pad(pad, translatable(key, value(value))));
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntegerTooltip(int pad, String key, int value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLongTooltip(int pad, String key, @Nullable Long value) {
        if (value != null) {
            return List.of(pad(pad, translatable(key, value(value))));
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getStringTooltip(int pad, String key, @Nullable String value) {
        if (value != null) {
            return List.of(pad(pad, translatable(key, value(value))));
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFloatTooltip(int pad, String key, @Nullable Float value) {
        if (value != null) {
            return List.of(pad(pad, translatable(key, value(value))));
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(int pad, String key, String enumName, Enum<?> value) {
        return List.of(pad(pad, translatable(key, value(translatableType("ali.enum." + enumName, value)))));
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
    public static <T> List<Component> getResourceKeyTooltip(int pad, String key, @Nullable ResourceKey<T> value) {
        if (value != null) {
            return List.of(pad(pad, translatable(key, value(value.location()))));
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTagKeyTooltip(int pad, String key, @Nullable TagKey<?> value) {
        if (value != null) {
            return getResourceLocationTooltip(pad, key, value.location());
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(int pad, String key, Component component) {
        return List.of(pad(pad, translatable(key, value(component))));
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
            MixinMinMaxBounds.Ints absolute = (MixinMinMaxBounds.Ints) ints;
            components.add(pad(pad, translatable(key, value(toString(absolute)))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(int pad, String key, MinMaxBounds.Doubles ints) {
        List<Component> components = new LinkedList<>();

        if (ints != MinMaxBounds.Doubles.ANY) {
            MixinMinMaxBounds.Doubles absolute = (MixinMinMaxBounds.Doubles) ints;
            components.add(pad(pad, translatable(key, value(toString(absolute)))));
        }

        return components;
    }

    // PRIVATE

    @NotNull
    private static String toString(MixinMinMaxBounds.Doubles doubles) {
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
                return String.format("<%.1f", max);
            }

            return "???";
        }
    }

    @NotNull
    private static String toString(MixinMinMaxBounds.Ints ints) {
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
                return String.format("<%d", max);
            }

            return "???";
        }
    }
}
