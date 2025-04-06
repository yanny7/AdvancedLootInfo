package com.yanny.ali.plugin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IUtils;
import com.yanny.ali.api.RangeValue;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.TypedDataComponent;
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
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
    public static List<Component> getConditionsTooltip(IUtils utils, int pad, List<LootItemCondition> conditions) {
        return conditions.stream().map((condition) -> utils.getConditionTooltip(utils, pad, condition)).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFunctionsTooltip(IUtils utils, int pad, List<LootItemFunction> functions) {
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
    public static List<Component> getFormulaTooltip(IUtils utils, int pad, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(utils, pad, "ali.property.value.formula", formula.getType().id()));

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount(int extraRounds, float probability)) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.extra_rounds", extraRounds));
            components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.probability", probability));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount(int bonusMultiplier)) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.bonus_multiplier", bonusMultiplier));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNameSourceTooltip(IUtils utils, int pad, CopyNameFunction.NameSource source) {
        return getEnumTooltip(utils, pad, "ali.property.value.name_source", "name_source", Optional.of(source));
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
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.name", modifier.name()));
        components.addAll(getHolderTooltip(utils, pad + 1, modifier.attribute(), GenericTooltipUtils::getAttributeTooltip));
        components.addAll(getOperationTooltip(utils, pad + 1, modifier.operation()));
        components.addAll(getRangeValueTooltip(utils, pad + 1, "ali.property.value.amount", utils.convertNumber(utils, modifier.amount())));
        components.addAll(getOptionalTooltip(utils, pad + 1, modifier.id(), GenericTooltipUtils::getUUIDTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.equipment_slots", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

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
    public static List<Component> getBannerPatternLayersTooltip(IUtils utils, int pad, BannerPatternLayers bannerPatternLayers) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.banner_patterns", bannerPatternLayers.layers(), GenericTooltipUtils::getBannerPatternLayerTooltip);
    }

    @NotNull
    public static List<Component> getBannerPatternLayerTooltip(IUtils utils, int pad, BannerPatternLayers.Layer layer) {
        return getHolderTooltip(utils, pad, layer.pattern(), (u, p, b) -> getBannerPatternTooltip(u, p, b, layer.color()));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(IUtils utils, int pad, BannerPattern bannerPattern, DyeColor color) {
        return List.of(pad(pad, translatable("ali.property.value.banner_pattern", value(translatable(bannerPattern.translationKey() + "." + color.getName())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(IUtils utils, int pad, BlockEntityType<?> blockEntityType) {
        ResourceLocation location = Objects.requireNonNull(blockEntityType.builtInRegistryHolder()).unwrapKey().orElseThrow().location();
        return List.of(pad(pad, translatable("ali.property.value.block_entity_type", value(location))));
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

        components.addAll(getHolderTooltip(utils, pad, effectInstance.getEffect(), GenericTooltipUtils::getMobEffectTooltip));
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
        return getCollectionTooltip(utils, pad, "ali.property.branch.state_properties_predicate", propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(IUtils utils, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        List<Component> components = new LinkedList<>();
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
            components.add(pad(pad, keyValue(name, value)));
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> minValue, Optional<String> maxValue)) {
            if (minValue.isPresent()) {
                if (maxValue.isPresent()) {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_both", name, minValue.get(), maxValue.get()))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_gte", name, minValue.get()))));
                }
            } else {
                if (maxValue.isPresent()) {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_lte", name, maxValue.get()))));
                } else {
                    components.add(pad(pad, value(translatable("ali.property.value.ranged_property_any", name))));
                }
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDamageSourcePredicateTooltip(IUtils utils, int pad, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.damage_source_predicate")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.tag_predicates", damagePredicate.tags(), GenericTooltipUtils::getTagPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.direct_entity", damagePredicate.directEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.source_entity", damagePredicate.sourceEntity(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagPredicateTooltip(IUtils utils, int pad, TagPredicate<T> tagPredicate) {
        return List.of(pad(pad, keyValue(tagPredicate.tag().location(), tagPredicate.expected())));
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(IUtils utils, int pad, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.entityType(), GenericTooltipUtils::getEntityTypePredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer(), GenericTooltipUtils::getDistancePredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.location", entityPredicate.location(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.stepping_on_location", entityPredicate.steppingOnLocation(), GenericTooltipUtils::getLocationPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.subPredicate(), GenericTooltipUtils::getEntitySubPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.value.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(IUtils utils, int pad, EntityTypePredicate entityTypePredicate) {
        return getHolderSetTooltip(utils, pad, "ali.property.branch.entity_types", entityTypePredicate.types(), GenericTooltipUtils::getEntityTypeTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEntityTypeTooltip(IUtils utils, int pad, EntityType<?> entityType) {
        return getComponentTooltip(utils, pad, "ali.property.value.entity_type", value(translatable(entityType.getDescriptionId())));
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(IUtils utils, int pad, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.x", distancePredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.y", distancePredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.z", distancePredicate.z()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.horizontal", distancePredicate.horizontal()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.absolute", distancePredicate.absolute()));

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(IUtils utils, int pad, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, locationPredicate.position(), GenericTooltipUtils::getPositionPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.branch.biomes", locationPredicate.biomes(),
                (u, p, s, h) -> getHolderSetTooltip(u, p, s, h, "ali.property.value.biome")));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.branch.structures", locationPredicate.structures(),
                (u, p, s, h) -> getHolderSetTooltip(u, p, s, h, "ali.property.value.structure")));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad, locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getPositionPredicateTooltip(IUtils utils, int pad, LocationPredicate.PositionPredicate positionPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.position")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", positionPredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", positionPredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", positionPredicate.z()));

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(IUtils utils, int pad, LightPredicate lightPredicate) {
        return getMinMaxBoundsTooltip(utils, pad, "ali.property.value.light", lightPredicate.composite());
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(IUtils utils, int pad, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.block_predicate")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.blocks", blockPredicate.blocks(), GenericTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad, blockPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, blockPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPredicateTooltip(IUtils utils, int pad, NbtPredicate nbtPredicate) {
        return List.of(pad(pad, translatable("ali.property.value.nbt", value(nbtPredicate.tag()))));
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(IUtils utils, int pad, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.fluid_predicate")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.fluids", fluidPredicate.fluids(), GenericTooltipUtils::getFluidTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, fluidPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFluidTooltip(IUtils utils, int pad, Fluid fluid) {
        return List.of(pad(pad, translatable("ali.property.value.fluid", value(translatable(BuiltInRegistries.FLUID.getKey(fluid).toString())))));
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(IUtils utils, int pad, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.mob_effects")));
        mobEffectsPredicate.effectMap().forEach((effect, instancePredicate) -> {
            components.addAll(getHolderTooltip(utils, pad + 1, effect, GenericTooltipUtils::getMobEffectTooltip));
            components.addAll(getMobEffectInstancePredicateTooltip(utils, pad + 2, instancePredicate));
        });

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(IUtils utils, int pad, MobEffectsPredicate.MobEffectInstancePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.amplifier", predicate.amplifier()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.duration", predicate.duration()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_ambient", predicate.ambient(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_visible", predicate.visible(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(IUtils utils, int pad, EntityFlagsPredicate entityFlagsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.entity_flags")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_on_fire", entityFlagsPredicate.isOnFire(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_baby", entityFlagsPredicate.isBaby(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_crouching", entityFlagsPredicate.isCrouching(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_sprinting", entityFlagsPredicate.isSprinting(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_swimming", entityFlagsPredicate.isSwimming(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(IUtils utils, int pad, EntityEquipmentPredicate entityEquipmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.entity_equipment")));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.head", entityEquipmentPredicate.head(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.chest", entityEquipmentPredicate.chest(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.legs", entityEquipmentPredicate.legs(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.feet", entityEquipmentPredicate.feet(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.mainhand", entityEquipmentPredicate.mainhand(), GenericTooltipUtils::getItemPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.offhand", entityEquipmentPredicate.offhand(), GenericTooltipUtils::getItemPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(IUtils utils, int pad, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.items", itemPredicate.items(), GenericTooltipUtils::getItemTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.count", itemPredicate.count()));
        components.addAll(getDataComponentPredicateTooltip(utils, pad, itemPredicate.components()));
        //fixme item sub predicates

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(IUtils utils, int pad, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderTooltip(utils, pad, enchantmentPredicate.enchantment(), GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", enchantmentPredicate.level()));

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(IUtils utils, int pad, EntitySubPredicate entitySubPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.value.entity_sub_predicate", BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE.getKey(entitySubPredicate.codec()))));

        switch (entitySubPredicate) {
            case LightningBoltPredicate(MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck) -> {
                components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.blocks_on_fire", blocksSetOnFire));
                components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", entityStruck, GenericTooltipUtils::getEntityPredicateTooltip));
            }
            case FishingHookPredicate(Optional<Boolean> inOpenWater) ->
                    components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.in_open_water", inOpenWater, GenericTooltipUtils::getBooleanTooltip));
            case PlayerPredicate(
                    MinMaxBounds.Ints level, Optional<GameType> gameType, List<PlayerPredicate.StatMatcher<?>> stats,
                    Object2BooleanMap<ResourceLocation> recipes,
                    Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements,
                    Optional<EntityPredicate> lookingAt
            ) -> {
                components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", level));
                components.addAll(getOptionalTooltip(utils, pad + 1, gameType, GenericTooltipUtils::getGameTypeTooltip));
                components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stats", stats, GenericTooltipUtils::getStatMatcherTooltip));
                components.addAll(getRecipesTooltip(utils, pad + 1, recipes));
                components.addAll(getAdvancementsTooltip(utils, pad + 1, advancements));
                components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.looking_at", lookingAt,
                        (u, p, s, c) -> getComponentsTooltip(u, p, s, c, GenericTooltipUtils::getEntityPredicateTooltip)));
            }
            case SlimePredicate(MinMaxBounds.Ints size) ->
                    components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.common.size", size));
            default ->
                    EntitySubPredicate.CODEC.encodeStart(JsonOps.INSTANCE, entitySubPredicate).result().ifPresent((element) -> {
                        JsonObject jsonObject = element.getAsJsonObject();

                        if (jsonObject.has("variant")) {
                            components.add(pad(pad + 1, translatable("ali.property.common.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                        } else {
                            components.add(pad(pad + 1, translatable("ali.property.common.variant", jsonObject.toString())));
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
    public static List<Component> getStatMatcherTooltip(IUtils utils, int pad, PlayerPredicate.StatMatcher<?> stat) {
        List<Component> components = new LinkedList<>();
        Holder<?> value = stat.value();

        if (value.value() instanceof Item item) {
            components.addAll(getItemTooltip(utils, pad, item));
        } else if (value.value() instanceof Block block) {
            components.addAll(getBlockTooltip(utils, pad, block));
        } else {
            components.add(pad(pad, value.value().toString()));
        }

        components.add(pad(pad + 1, keyValue(stat.type().getDisplayName(), toString(stat.range()))));

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

                if (predicate instanceof PlayerPredicate.AdvancementDonePredicate(boolean state)) {
                    components.add(pad(pad + 2, translatable("ali.property.value.done", state)));
                } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions)) {
                    criterions.forEach((criterion, state) -> components.add(pad(pad + 2, keyValue(criterion, state))));
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
    public static List<Component> getMapDecorationTypeTooltip(IUtils utils, int pad, MapDecorationType decorationType) {
        List<Component> components = new LinkedList<>();

        components.addAll(getResourceLocationTooltip(utils, pad, "ali.property.value.asset_id", decorationType.assetId()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.color", decorationType.mapColor()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_on_item_frame", decorationType.showOnItemFrame()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.exploration_map_element", decorationType.explorationMapElement()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.track_count", decorationType.trackCount()));

        return components;
    }

    @NotNull
    public static List<Component> getListOperationTooltip(IUtils utils, int pad, ListOperation operation) {
        List<Component> components = new LinkedList<>(getEnumTooltip(utils, pad, "ali.property.value.list_operation", operation.mode()));

        if (operation instanceof ListOperation.Insert(int offset)) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.offset", offset));
        } else if (operation instanceof ListOperation.ReplaceSection(int offset, Optional<Integer> size)) {
            components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.offset", offset));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.size", size, GenericTooltipUtils::getIntegerTooltip));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getContainerComponentManipulatorTooltip(IUtils utils, int pad, ContainerComponentManipulator<?> component) {
        return getStringTooltip(utils, pad, "ali.property.value.component", component.type().toString());
    }

    @NotNull
    public static List<Component> getCopyOperationTooltip(IUtils utils, int pad, CopyCustomDataFunction.CopyOperation copyOperation) {
        List<Component> components = new LinkedList<>();

        components.addAll(GenericTooltipUtils.getNbtPathTooltip(utils, pad, "ali.property.value.source_path", copyOperation.sourcePath()));
        components.addAll(GenericTooltipUtils.getNbtPathTooltip(utils, pad, "ali.property.value.target_path", copyOperation.targetPath()));
        components.addAll(GenericTooltipUtils.getEnumTooltip(utils, pad, "ali.property.value.merge_strategy", copyOperation.op()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPathTooltip(IUtils utils, int pad, String key, NbtPathArgument.NbtPath provider) {
        return getStringTooltip(utils, pad, key, provider.asString());
    }

    @NotNull
    public static List<Component> getDataComponentPredicateTooltip(IUtils utils, int pad, DataComponentPredicate dataComponentPredicate) {
        if (dataComponentPredicate != DataComponentPredicate.EMPTY) {
            return getCollectionTooltip(utils, pad, "ali.property.branch.component_predicates", dataComponentPredicate.expectedComponents, GenericTooltipUtils::getTypedDataComponentTooltip);
        } else {
            return List.of();
        }
    }

    @NotNull
    public static List<Component> getTypedDataComponentTooltip(IUtils utils, int pad, TypedDataComponent<?> typedDataComponent) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.value.component", keyValue(typedDataComponent.type().toString(), typedDataComponent.value()))));

        return components;
    }

    // HELPERS

    @NotNull
    public static Component translatableType(String prefix, Enum<?> type, Object... args) {
        return translatable(prefix + "." + type.name().toLowerCase(), args);
    }

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
    public static List<Component> getRangeValueTooltip(IUtils utils, int pad, String key, RangeValue value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @NotNull
    public static List<Component> getIntRangeTooltip(IUtils utils, int pad, String key, IntRange range) {
        return List.of(pad(pad, translatable(key, value(RangeValue.rangeToString(utils.convertNumber(utils, range.min), utils.convertNumber(utils, range.max))))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBooleanTooltip(IUtils utils, int pad, String key, Boolean value) {
        return List.of(pad(pad, translatable(key, value(value))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntegerTooltip(IUtils utils, int pad, String key, Integer value) {
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
    public static <T extends Enum<T>> List<Component> getEnumTooltip(IUtils utils, int pad, String key, String enumName, Optional<T> value) {
        return value.map((v) -> List.of(pad(pad, translatable(key, value(translatableType("ali.enum." + enumName, v)))))).orElse(List.of());
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
    public static <T> List<Component> getTagKeyTooltip(IUtils utils, int pad, String key, TagKey<T> value) {
        return getResourceLocationTooltip(utils, pad, key, value.location());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(IUtils utils, int pad, String key, Component component) {
        return List.of(pad(pad, translatable(key, value(component))));
    }

    @NotNull
    public static <T> List<Component> getComponentsTooltip(IUtils utils, int pad, String key, T value, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        List<Component> componentList = mapper.apply(utils, pad + 1, value);

        if (!componentList.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(componentList);
        }

        return components;
    }
    @NotNull
    public static <T> List<Component> getComponentsTooltip(IUtils utils, int pad, String key, Optional<T> value, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        List<Component> componentList = getOptionalTooltip(utils, pad + 1, value, mapper);

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
    public static <T> List<Component> getOptionalTooltip(IUtils utils, int pad, Optional<T> optional, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(utils, pad, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(IUtils utils, int pad, String key, Optional<T> optional, QuadFunction<IUtils, Integer, String, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(utils, pad, key, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderTooltip(IUtils utils, int pad, Optional<Holder<T>> optional, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        return optional.map((holder) -> getHolderTooltip(utils, pad, holder, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderSetTooltip(IUtils utils, int pad, String key, Optional<HolderSet<T>> optional, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        return optional.map((holderSet) -> getHolderSetTooltip(utils, pad, key, holderSet, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getHolderTooltip(IUtils utils, int pad, Holder<T> holder, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
        return mapper.apply(utils, pad, holder.value());
    }

    @NotNull
    public static <T> List<Component> getHolderSetTooltip(IUtils utils, int pad, String key, HolderSet<T> holderSet, TriFunction<IUtils, Integer, T, List<Component>> mapper) {
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
                holderSet.forEach((holder) -> components.addAll(getHolderTooltip(utils, pad + 1, holder, mapper)));
            }
        });

        return components;
    }

    @NotNull
    public static <T> List<Component> getHolderSetTooltip(IUtils utils, int pad, String key, HolderSet<T> holderSet, String value) {
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
                holderSet.forEach((holder) -> components.addAll(getResourceKeyTooltip(utils, pad + 1, value, holder.unwrapKey().orElseThrow())));
            }
        });

        return components;
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
