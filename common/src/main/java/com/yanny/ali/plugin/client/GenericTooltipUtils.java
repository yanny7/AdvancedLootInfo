package com.yanny.ali.plugin.client;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
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
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GenericTooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    @NotNull
    public static List<Component> getConditionsTooltip(IClientUtils utils, int pad, List<LootItemCondition> conditions) {
        return conditions.stream().map((condition) -> utils.getConditionTooltip(utils, pad, condition)).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFunctionsTooltip(IClientUtils utils, int pad, List<LootItemFunction> functions) {
        return functions.stream().map((function) -> {
            List<Component> components = new LinkedList<>(utils.getFunctionTooltip(utils, pad, function));

            if (function instanceof LootItemConditionalFunction conditionalFunction && !conditionalFunction.predicates.isEmpty()) {
                components.add(pad(pad + 1, translatable("ali.property.branch.conditions")));
                components.addAll(getConditionsTooltip(utils, pad + 2, conditionalFunction.predicates));
            }

            return components;
        }).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFormulaTooltip(IClientUtils utils, int pad, String key, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(utils, pad, key, formula.getType().id()));

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.extra_rounds", binomialWithBonusCount.extraRounds()));
            components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.probability", binomialWithBonusCount.probability()));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.bonus_multiplier", uniformBonusCount.bonusMultiplier()));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockTooltip(IClientUtils ignoredUtils, int pad, String key, Block block) {
        return List.of(pad(pad, translatable(key, value(translatable(block.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPropertyTooltip(IClientUtils utils, int pad, String key, Property<?> property) {
        return getStringTooltip(utils, pad, key, property.getName());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(IClientUtils ignoredUtils, int pad, String key, Enchantment enchantment) {
        return List.of(pad(pad, translatable(key, value(translatable(enchantment.getDescriptionId())))));
    }

    @NotNull
    public static List<Component> getModifierTooltip(IClientUtils utils, int pad, String key, SetAttributesFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.name", modifier.name()));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.attribute", modifier.attribute(), GenericTooltipUtils::getAttributeTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.operation", modifier.operation()));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.amount", modifier.amount()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.uuid", modifier.id(), GenericTooltipUtils::getUUIDTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.equipment_slots", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(IClientUtils ignoredUtils, int pad, String key, Attribute attribute) {
        return List.of(pad(pad, translatable(key, value(translatable(attribute.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(IClientUtils utils, int pad, String key, UUID uuid) {
        return List.of(pad(pad, translatable("ali.property.value.uuid", value(uuid))));
    }

    @NotNull
    public static List<Component> getBannerPatternTooltip(IClientUtils utils, int pad, Pair<Holder<BannerPattern>, DyeColor> pair) {
        List<Component> components = new LinkedList<>();

        components.addAll(getHolderTooltip(utils, pad, "ali.property.value.banner_pattern", pair.getFirst(), GenericTooltipUtils::getBannerPatternTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.color", pair.getSecond()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(IClientUtils utils, int pad, String key, BannerPattern bannerPattern) {
        return getBuiltInRegistryTooltip(utils, pad, "ali.property.value.banner_pattern", BuiltInRegistries.BANNER_PATTERN, bannerPattern);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(IClientUtils utils, int pad, String key, BlockEntityType<?> blockEntityType) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPotionTooltip(IClientUtils utils, int pad, String key, Potion potion) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.POTION, potion);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(IClientUtils utils, int pad, String key, MobEffect mobEffect) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.MOB_EFFECT, mobEffect);
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(IClientUtils utils, int pad, String key, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(utils, pad, key, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(IClientUtils ignoredUtils, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
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
    public static List<Component> getDamageSourcePredicateTooltip(IClientUtils utils, int pad, String key, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.tag_predicates", damagePredicate.tags(), GenericTooltipUtils::getTagPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.direct_entity", damagePredicate.directEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.source_entity", damagePredicate.sourceEntity(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagPredicateTooltip(IClientUtils utils, int pad, TagPredicate<T> tagPredicate) {
        return List.of(pad(pad, keyValue(tagPredicate.tag().location(), tagPredicate.expected())));
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(IClientUtils utils, int pad, String key, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.entity_types", entityPredicate.entityType(), GenericTooltipUtils::getEntityTypePredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer(), GenericTooltipUtils::getDistancePredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.location", entityPredicate.location(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.stepping_on_location", entityPredicate.steppingOnLocation(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.mob_effects", entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.nbt", entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.entity_flags", entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.entity_equipment", entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.entity_sub_predicate", entityPredicate.subPredicate(), GenericTooltipUtils::getEntitySubPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(IClientUtils utils, int pad, String key, EntityTypePredicate entityTypePredicate) {
        return getHolderSetTooltip(utils, pad, key, "ali.property.value.null", entityTypePredicate.types(), GenericTooltipUtils::getEntityTypeTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEntityTypeTooltip(IClientUtils utils, int pad, String key, EntityType<?> entityType) {
        return getComponentTooltip(utils, pad, key, value(translatable(entityType.getDescriptionId())));
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(IClientUtils utils, int pad, String key, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", distancePredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", distancePredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", distancePredicate.z()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.horizontal", distancePredicate.horizontal()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.absolute", distancePredicate.absolute()));

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(IClientUtils utils, int pad, String key, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.position", locationPredicate.position(), GenericTooltipUtils::getPositionPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.biome", locationPredicate.biome(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.structure", locationPredicate.structure(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.light", locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.block_predicate", locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.fluid_predicate", locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getPositionPredicateTooltip(IClientUtils utils, int pad, String key, LocationPredicate.PositionPredicate positionPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", positionPredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", positionPredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", positionPredicate.z()));

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(IClientUtils utils, int pad, String key, LightPredicate lightPredicate) {
        return getMinMaxBoundsTooltip(utils, pad, key, lightPredicate.composite());
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(IClientUtils utils, int pad, String key, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", blockPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.blocks", "ali.property.value.null", blockPredicate.blocks(), GenericTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.branch.state_properties_predicate", blockPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.nbt", blockPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPredicateTooltip(IClientUtils utils, int pad, String key, NbtPredicate nbtPredicate) {
        return List.of(pad(pad, translatable(key, value(nbtPredicate.tag()))));
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(IClientUtils utils, int pad, String key, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", fluidPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderTooltip(utils, pad + 1, "ali.property.value.fluid", fluidPredicate.fluid(), GenericTooltipUtils::getFluidTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.state_properties_predicate", fluidPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFluidTooltip(IClientUtils utils, int pad, String key, Fluid fluid) {
        return getBuiltInRegistryTooltip(utils, pad, key, BuiltInRegistries.FLUID, fluid);
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(IClientUtils utils, int pad, String key, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.mob_effects")));
        mobEffectsPredicate.effectMap().forEach((effect, instancePredicate) -> {
            components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.mob_effect", effect, GenericTooltipUtils::getMobEffectTooltip));
            components.addAll(getMobEffectInstancePredicateTooltip(utils, pad + 2, instancePredicate));
        });

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(IClientUtils utils, int pad, MobEffectsPredicate.MobEffectInstancePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.amplifier", predicate.amplifier()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.duration", predicate.duration()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_ambient", predicate.ambient(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_visible", predicate.visible(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(IClientUtils utils, int pad, String key, EntityFlagsPredicate entityFlagsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_on_fire", entityFlagsPredicate.isOnFire(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_baby", entityFlagsPredicate.isBaby(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_crouching", entityFlagsPredicate.isCrouching(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_sprinting", entityFlagsPredicate.isSprinting(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_swimming", entityFlagsPredicate.isSwimming(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(IClientUtils utils, int pad, String key, EntityEquipmentPredicate entityEquipmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.head", entityEquipmentPredicate.head(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.chest", entityEquipmentPredicate.chest(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.legs", entityEquipmentPredicate.legs(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.feet", entityEquipmentPredicate.feet(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.mainhand", entityEquipmentPredicate.mainhand(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.offhand", entityEquipmentPredicate.offhand(), GenericTooltipUtils::getItemPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(IClientUtils utils, int pad, String key, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", itemPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.items", "ali.property.value.null", itemPredicate.items(), GenericTooltipUtils::getItemTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.count", itemPredicate.count()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.durability", itemPredicate.durability()));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.enchantments", itemPredicate.enchantments(), GenericTooltipUtils::getEnchantmentPredicateTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stored_enchantments", itemPredicate.storedEnchantments(), GenericTooltipUtils::getEnchantmentPredicateTooltip));
        components.addAll(getOptionalHolderTooltip(utils, pad + 1, "ali.property.value.potion", itemPredicate.potion(), GenericTooltipUtils::getPotionTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.nbt", itemPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(IClientUtils utils, int pad, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderTooltip(utils, pad, "ali.property.value.enchantment", enchantmentPredicate.enchantment(), GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", enchantmentPredicate.level()));

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, String key, EntitySubPredicate entitySubPredicate) {
        List<Component> components = new LinkedList<>();
        Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

        optional.ifPresent((entry) -> {
            components.add(pad(pad, translatable(key, entry.getKey())));

            if (entitySubPredicate instanceof LightningBoltPredicate predicate) {
                components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
                components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));
            } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));
            } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", predicate.level()));
                components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.game_type", predicate.gameType(), GenericTooltipUtils::getGameTypeTooltip));
                components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
                components.addAll(getRecipesTooltip(utils, pad + 1, "ali.property.branch.recipes", predicate.recipes()));
                components.addAll(getAdvancementsTooltip(utils, pad + 1, "ali.property.branch.advancements", predicate.advancements()));
            } else if (entitySubPredicate instanceof SlimePredicate predicate) {
                components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.size", predicate.size()));
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
    public static List<Component> getItemTooltip(IClientUtils ignoredUtils, int pad, String key, Item item) {
        return List.of(pad(pad, translatable(key, value(translatable(item.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypeTooltip(IClientUtils utils, int pad, String key, GameType gameType) {
        return List.of(pad(pad, value(translatable(key, gameType.getShortDisplayName()))));
    }

    @NotNull
    public static List<Component> getStatMatcherTooltip(IClientUtils utils, int pad, PlayerPredicate.StatMatcher<?> stat) {
        List<Component> components = new LinkedList<>();
        Holder<?> value = stat.value();

        if (value.value() instanceof Item item) {
            components.addAll(getItemTooltip(utils, pad, "ali.property.value.item", item));
        } else if (value.value() instanceof Block block) {
            components.addAll(getBlockTooltip(utils, pad, "ali.property.value.block", block));
        } else {
            components.add(pad(pad, value.value().toString()));
        }

        components.add(pad(pad + 1, keyValue(stat.type().getDisplayName(), toString(stat.range()))));

        return components;
    }

    @NotNull
    public static List<Component> getRecipesTooltip(IClientUtils ignoredUtils, int pad, String key, Object2BooleanMap<ResourceLocation> recipes) {
        List<Component> components = new LinkedList<>();

        if (!recipes.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            recipes.forEach((recipe, required) -> components.add(pad(pad + 1, keyValue(recipe.toString(), required))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getAdvancementsTooltip(IClientUtils ignoredUtils, int pad, String key, Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap) {
        List<Component> components = new LinkedList<>();

        if (!predicateMap.isEmpty()) {
            components.add(pad(pad, translatable(key)));
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
    public static List<Component> getBlockPosTooltip(IClientUtils utils, int pad, String key, BlockPos pos) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.x", pos.getX()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.y", pos.getY()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.z", pos.getZ()));

        return components;
    }

    @NotNull
    public static List<Component> getCopyOperationTooltip(IClientUtils utils, int pad, String key, CopyNbtFunction.CopyOperation copyOperation) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.source", copyOperation.sourcePath().string()));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.target", copyOperation.targetPath().string()));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.merge_strategy", copyOperation.op()));

        return components;
    }

    @NotNull
    public static List<Component> getEffectEntryTooltip(IClientUtils utils, int pad, SetStewEffectFunction.EffectEntry entry) {
        List<Component> components = new LinkedList<>();

        components.addAll(getHolderTooltip(utils, pad, "ali.property.value.mob_effect", entry.effect(), GenericTooltipUtils::getMobEffectTooltip));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.duration", entry.duration()));

        return components;
    }

    // HELPERS

    @NotNull
    public static Component translatable(String key, Object... args) {
        return Component.translatable(key, Arrays.stream(args).map(GenericTooltipUtils::convertObject).toArray()).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static Component value(Object value) {
        return convertObject(value).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
    }

    @NotNull
    public static Component value(Object value, String unit) {
        return Component.translatable("ali.util.advanced_loot_info.two_values", convertObject(value), unit).withStyle(PARAM_STYLE, ChatFormatting.BOLD);
    }

    @NotNull
    public static Component pair(Object value1, Object value2) {
        return Component.translatable("ali.util.advanced_loot_info.two_values_with_space", convertObject(value1), convertObject(value2));
    }

    @NotNull
    public static Component pad(int count, Object arg) {
        if (count > 0) {
            return pair(Component.translatable("ali.util.advanced_loot_info.pad." + count), convertObject(arg));
        } else {
            return convertObject(arg);
        }
    }

    @NotNull
    public static Component keyValue(Object key, Object value) {
        return translatable("ali.util.advanced_loot_info.key_value", convertObject(key), value(value));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNumberProviderTooltip(IClientUtils utils, int pad, String key, NumberProvider value) {
        return List.of(pad(pad, translatable(key, value(utils.convertNumber(utils, value)))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntRangeTooltip(IClientUtils utils, int pad, String key, IntRange range) {
        return List.of(pad(pad, translatable(key, value(RangeValue.rangeToString(utils.convertNumber(utils, range.min), utils.convertNumber(utils, range.max))))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBooleanTooltip(IClientUtils utils, int pad, String key, Boolean value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntegerTooltip(IClientUtils ignoredUtils, int pad, String key, int value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLongTooltip(IClientUtils ignoredUtils, int pad, String key, Long value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getStringTooltip(IClientUtils utils, int pad, String key, String value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFloatTooltip(IClientUtils ignoredUtils, int pad, String key, Float value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(IClientUtils ignoredUtils, int pad, String key, Enum<?> value) {
        return List.of(pad(pad, translatable(key, value(value.name()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnumTooltip(IClientUtils utils, int pad, Enum<?> value) {
        return List.of(pad(pad, value(value.name())));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getResourceLocationTooltip(IClientUtils ignoredUtils, int pad, String key, ResourceLocation value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getBuiltInRegistryTooltip(IClientUtils utils, int pad, String key, Registry<T> registry, T value) {
        return getResourceLocationTooltip(utils, pad, key, Objects.requireNonNull(registry.getKey(value)));
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getResourceKeyTooltip(IClientUtils utils, int pad, String key, ResourceKey<T> value) {
        return List.of(pad(pad, translatable(key, value(value.location()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTagKeyTooltip(IClientUtils utils, int pad, String key, TagKey<?> value) {
        return getResourceLocationTooltip(utils, pad, key, value.location());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(IClientUtils ignoredUtils, int pad, String key, Component component) {
        return List.of(pad(pad, translatable(key, value(component))));
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(IClientUtils ignoredUtils, int pad, String key, MinMaxBounds.Ints ints) {
        List<Component> components = new LinkedList<>();

        if (ints != MinMaxBounds.Ints.ANY) {
            components.add(pad(pad, translatable(key, value(toString(ints)))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(IClientUtils ignoredUtils, int pad, String key, MinMaxBounds.Doubles doubles) {
        List<Component> components = new LinkedList<>();

        if (doubles != MinMaxBounds.Doubles.ANY) {
            components.add(pad(pad, translatable(key, value(toString(doubles)))));
        }

        return components;
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(IClientUtils utils, int pad, String key, Optional<T> optional, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(utils, pad, key, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderTooltip(IClientUtils utils, int pad, String key, Optional<Holder<T>> optional, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        return optional.map((holder) -> getHolderTooltip(utils, pad, key, holder, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderSetTooltip(IClientUtils utils, int pad, String key, String value, Optional<HolderSet<T>> optional, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        return optional.map((holderSet) -> getHolderSetTooltip(utils, pad, key, value, holderSet, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getHolderTooltip(IClientUtils utils, int pad, String key, Holder<T> holder, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        return mapper.apply(utils, pad, key, holder.value());
    }

    @NotNull
    public static <T> List<Component> getHolderSetTooltip(IClientUtils utils, int pad, String key, String value, HolderSet<T> holderSet, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        Either<TagKey<T>, List<Holder<T>>> either = holderSet.unwrap();
        Optional<TagKey<T>> left = either.left();
        Optional<List<Holder<T>>> right = either.right();

        if (left.isPresent() || !right.orElse(List.of()).isEmpty()) {
            components.add(pad(pad, translatable(key)));
        }

        left.ifPresent((tagKey) -> components.addAll(getTagKeyTooltip(utils, pad + 1, "ali.property.value.tag", tagKey)));
        right.ifPresent((list) -> {
            if (!list.isEmpty()) {
                holderSet.forEach((holder) -> components.addAll(getHolderTooltip(utils, pad + 1, value, holder, mapper)));
            }
        });

        return components;
    }

    @NotNull
    public static <T> List<Component> getCollectionTooltip(IClientUtils utils, int pad, String key, Collection<T> values, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        if (!values.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            values.forEach((value) -> components.addAll(mapper.apply(utils, pad + 1, value)));
        }

        return components;
    }

    @NotNull
    public static <T> List<Component> getCollectionTooltip(IClientUtils utils, int pad, String key, String value, Collection<T> values, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        if (!values.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            values.forEach((v) -> components.addAll(mapper.apply(utils, pad + 1, value, v)));
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
    private static MutableComponent convertObject(@Nullable Object object) {
        if (object instanceof MutableComponent component) {
            return component;
        } else if (object != null) {
            return Component.literal(object.toString());
        } else {
            return Component.literal("null");
        }
    }

    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
