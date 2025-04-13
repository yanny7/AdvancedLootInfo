package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
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
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Predicate;

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
    public static List<Component> getFormulaTooltip(IClientUtils utils, int pad, ApplyBonusCount.Formula formula) {
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
    public static List<Component> getBlockTooltip(IClientUtils ignoredUtils, int pad, Block block) {
        return List.of(pad(pad, translatable("ali.property.value.block", value(translatable(block.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPropertyTooltip(IClientUtils ignoredUtils, int pad, Property<?> property) {
        return List.of(pad(pad, value(pair(property.getName(), property.getPossibleValues().toString()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEnchantmentTooltip(IClientUtils utils, int pad, Enchantment enchantment) {
        return List.of(pad(pad, translatable("ali.property.value.enchantment", value(enchantment.description()))));
    }

    @NotNull
    public static List<Component> getModifierTooltip(IClientUtils utils, int pad, SetAttributesFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.modifier")));
        components.addAll(getHolderTooltip(utils, pad + 1, modifier.attribute(), GenericTooltipUtils::getAttributeTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.operation", modifier.operation()));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.amount", modifier.amount()));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.id", modifier.id()));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.equipment_slots", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getAttributeTooltip(IClientUtils ignoredUtils, int pad, Attribute attribute) {
        return List.of(pad(pad, translatable("ali.property.value.attribute", value(translatable(attribute.getDescriptionId())))));
    }

    @NotNull
    public static List<Component> getBannerPatternLayersTooltip(IClientUtils utils, int pad, BannerPatternLayers bannerPatternLayers) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.banner_patterns", bannerPatternLayers.layers(), GenericTooltipUtils::getBannerPatternLayerTooltip);
    }

    @NotNull
    public static List<Component> getBannerPatternLayerTooltip(IClientUtils utils, int pad, BannerPatternLayers.Layer layer) {
        return getHolderTooltip(utils, pad, layer.pattern(), (u, p, b) -> getBannerPatternTooltip(u, p, b, layer.color()));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(IClientUtils ignoredUtils, int pad, BannerPattern bannerPattern, DyeColor color) {
        return List.of(pad(pad, translatable("ali.property.value.banner_pattern", value(translatable(bannerPattern.translationKey() + "." + color.getName())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockEntityTypeTooltip(IClientUtils ignoredUtils, int pad, BlockEntityType<?> blockEntityType) {
        ResourceLocation location = Objects.requireNonNull(blockEntityType.builtInRegistryHolder()).unwrapKey().orElseThrow().location();
        return List.of(pad(pad, translatable("ali.property.value.block_entity_type", value(location))));
    }

    @NotNull
    public static List<Component> getPotionTooltip(IClientUtils utils, int pad, Potion potion) {
        return getResourceLocationTooltip(utils, pad, "ali.property.value.potion", Objects.requireNonNull(BuiltInRegistries.POTION.getKey(potion)));

    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMobEffectTooltip(IClientUtils utils, int pad, MobEffect mobEffect) {
        return List.of(pad(pad, translatable("ali.property.value.mob_effect", value(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(mobEffect))))));
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(IClientUtils utils, int pad, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.state_properties_predicate", propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(IClientUtils utils, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
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
    public static List<Component> getDamageSourcePredicateTooltip(IClientUtils utils, int pad, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.damage_source_predicate")));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.tag_predicates", damagePredicate.tags(), GenericTooltipUtils::getTagPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.direct_entity", damagePredicate.directEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad + 1, "ali.property.branch.source_entity", damagePredicate.sourceEntity(), GenericTooltipUtils::getEntityPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagPredicateTooltip(IClientUtils utils, int pad, TagPredicate<T> tagPredicate) {
        return List.of(pad(pad, keyValue(tagPredicate.tag().location(), tagPredicate.expected())));
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(IClientUtils utils, int pad, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.entityType(), GenericTooltipUtils::getEntityTypePredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer(), GenericTooltipUtils::getDistancePredicateTooltip));
        components.addAll(getLocationWrapperTooltip(utils, pad, "ali.property.branch.location", entityPredicate.location()));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.entity_sub_predicate", entityPredicate.subPredicate(), (TriFunction<IClientUtils, Integer, EntitySubPredicate, List<Component>>) utils::getEntitySubPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.value.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getComponentsTooltip(utils, pad, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(IClientUtils utils, int pad, EntityTypePredicate entityTypePredicate) {
        return getHolderSetTooltip(utils, pad, "ali.property.branch.entity_types", entityTypePredicate.types(), GenericTooltipUtils::getEntityTypeTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getEntityTypeTooltip(IClientUtils utils, int pad, EntityType<?> entityType) {
        return getComponentTooltip(utils, pad, "ali.property.value.entity_type", value(translatable(entityType.getDescriptionId())));
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(IClientUtils utils, int pad, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.x", distancePredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.y", distancePredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.z", distancePredicate.z()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.horizontal", distancePredicate.horizontal()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.absolute", distancePredicate.absolute()));

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(IClientUtils utils, int pad, String key, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, locationPredicate.position(), GenericTooltipUtils::getPositionPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.biomes", locationPredicate.biomes(),
                (u, p, s, h) -> getHolderSetTooltip(u, p, s, h, "ali.property.value.biome")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.structures", locationPredicate.structures(),
                (u, p, s, h) -> getHolderSetTooltip(u, p, s, h, "ali.property.value.structure")));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getPositionPredicateTooltip(IClientUtils utils, int pad, LocationPredicate.PositionPredicate positionPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.position")));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", positionPredicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", positionPredicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", positionPredicate.z()));

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(IClientUtils utils, int pad, LightPredicate lightPredicate) {
        return getMinMaxBoundsTooltip(utils, pad, "ali.property.value.light", lightPredicate.composite());
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(IClientUtils utils, int pad, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.block_predicate")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.blocks", blockPredicate.blocks(), GenericTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad, blockPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad, blockPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPredicateTooltip(IClientUtils utils, int pad, NbtPredicate nbtPredicate) {
        return List.of(pad(pad, translatable("ali.property.value.nbt", value(nbtPredicate.tag()))));
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(IClientUtils utils, int pad, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.fluid_predicate")));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.fluids", fluidPredicate.fluids(), GenericTooltipUtils::getFluidTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, fluidPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getFluidTooltip(IClientUtils utils, int pad, Fluid fluid) {
        return getResourceLocationTooltip(utils, pad, "ali.property.value.fluid", BuiltInRegistries.FLUID.getKey(fluid));
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(IClientUtils utils, int pad, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.mob_effects")));
        mobEffectsPredicate.effectMap().forEach((effect, instancePredicate) -> {
            components.addAll(getHolderTooltip(utils, pad + 1, effect, GenericTooltipUtils::getMobEffectTooltip));
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
    public static List<Component> getEntityFlagsPredicateTooltip(IClientUtils utils, int pad, EntityFlagsPredicate entityFlagsPredicate) {
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
    public static List<Component> getEntityEquipmentPredicateTooltip(IClientUtils utils, int pad, EntityEquipmentPredicate entityEquipmentPredicate) {
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
    public static List<Component> getItemPredicateTooltip(IClientUtils utils, int pad, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.items", itemPredicate.items(), GenericTooltipUtils::getItemTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.count", itemPredicate.count()));
        components.addAll(getDataComponentPredicateTooltip(utils, pad, itemPredicate.components()));

        if (!itemPredicate.subPredicates().isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.item_predicates")));
            itemPredicate.subPredicates().forEach((type, predicate) -> components.addAll(utils.getItemSubPredicateTooltip(utils, pad +1, type, predicate)));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(IClientUtils utils, int pad, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.enchantments", enchantmentPredicate.enchantments(), GenericTooltipUtils::getEnchantmentTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", enchantmentPredicate.level()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getItemTooltip(IClientUtils utils, int pad, Item item) {
        return List.of(pad(pad, translatable("ali.property.value.item", value(translatable(item.getDescriptionId())))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypeTooltip(IClientUtils utils, int pad, GameTypePredicate gameType) {
        return getCollectionTooltip(utils, pad, "ali.property.branch.game_types", gameType.types(), GenericTooltipUtils::getEnumTooltip);
    }

    @NotNull
    public static List<Component> getStatMatcherTooltip(IClientUtils utils, int pad, PlayerPredicate.StatMatcher<?> stat) {
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
    public static List<Component> getRecipesTooltip(IClientUtils ignoredUtils, int pad, Object2BooleanMap<ResourceLocation> recipes) {
        List<Component> components = new LinkedList<>();

        if (!recipes.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.recipes")));
            recipes.forEach((recipe, required) -> components.add(pad(pad + 1, keyValue(recipe.toString(), required))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getAdvancementsTooltip(IClientUtils ignoredUtils, int pad, Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap) {
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
    public static List<Component> getBlockPosTooltip(IClientUtils utils, int pad, BlockPos pos) {
        List<Component> components = new LinkedList<>();

        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.x", pos.getX()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.y", pos.getY()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.z", pos.getZ()));

        return components;
    }

    @NotNull
    public static List<Component> getMapDecorationTypeTooltip(IClientUtils utils, int pad, MapDecorationType decorationType) {
        List<Component> components = new LinkedList<>();

        components.addAll(getResourceLocationTooltip(utils, pad, "ali.property.value.asset_id", decorationType.assetId()));
        components.addAll(getIntegerTooltip(utils, pad, "ali.property.value.color", decorationType.mapColor()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.show_on_item_frame", decorationType.showOnItemFrame()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.exploration_map_element", decorationType.explorationMapElement()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.track_count", decorationType.trackCount()));

        return components;
    }

    @NotNull
    public static List<Component> getListOperationTooltip(IClientUtils utils, int pad, ListOperation operation) {
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
    public static List<Component> getContainerComponentManipulatorTooltip(IClientUtils utils, int pad, ContainerComponentManipulator<?> component) {
        return getDataComponentTypeTooltip(utils, pad, component.type());
    }

    @NotNull
    public static List<Component> getCopyOperationTooltip(IClientUtils utils, int pad, CopyCustomDataFunction.CopyOperation copyOperation) {
        List<Component> components = new LinkedList<>();

        components.addAll(GenericTooltipUtils.getNbtPathTooltip(utils, pad, "ali.property.value.source_path", copyOperation.sourcePath()));
        components.addAll(GenericTooltipUtils.getNbtPathTooltip(utils, pad, "ali.property.value.target_path", copyOperation.targetPath()));
        components.addAll(GenericTooltipUtils.getEnumTooltip(utils, pad, "ali.property.value.merge_strategy", copyOperation.op()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPathTooltip(IClientUtils utils, int pad, String key, NbtPathArgument.NbtPath provider) {
        return getStringTooltip(utils, pad, key, provider.asString());
    }

    @NotNull
    public static List<Component> getDataComponentPredicateTooltip(IClientUtils utils, int pad, DataComponentPredicate dataComponentPredicate) {
        if (dataComponentPredicate != DataComponentPredicate.EMPTY) {
            return getCollectionTooltip(utils, pad, "ali.property.branch.component_predicates", dataComponentPredicate.expectedComponents, GenericTooltipUtils::getTypedDataComponentTooltip);
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTypedDataComponentTooltip(IClientUtils utils, int pad, TypedDataComponent<?> typedDataComponent) {
        return List.of(pad(pad, translatable("ali.property.value.component", keyValue(typedDataComponent.type().toString(), typedDataComponent.value()))));
    }

    @NotNull
    public static <A, B extends Predicate<A>> List<Component> getCollectionPredicateTooltip(IClientUtils utils, int pad, String key, CollectionPredicate<A, B> predicate,
                                                                                            TriFunction<IClientUtils, Integer, B, List<Component>> subPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.contains", predicate.contains(),
                (u, i, s, c) -> getCollectionTooltip(u, i, s, c.unpack(), subPredicate)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.counts", predicate.counts(),
                (u, i, s, c) -> getCollectionTooltip(u, i, s, c.unpack(),
                        (u1, i1, s1) -> {
                            List<Component> comps = new LinkedList<>();

                            comps.addAll(subPredicate.apply(u1, i1, s1.test()));
                            comps.addAll(getMinMaxBoundsTooltip(u1, i1, "ali.property.value.count", s1.count()));

                            return comps;
                        })));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.size", predicate.size(), GenericTooltipUtils::getMinMaxBoundsTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getFireworkPredicateTooltip(IClientUtils utils, int pad, ItemFireworkExplosionPredicate.FireworkPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.shape", predicate.shape(), GenericTooltipUtils::getEnumTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.trail", predicate.trail(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.twinkle", predicate.twinkle(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPagePredicateTooltip(IClientUtils ignoredUtils, int pad, ItemWritableBookPredicate.PagePredicate predicate) {
        return List.of(pad(pad, translatable("ali.property.value.page", value(predicate.contents()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPagePredicateTooltip(IClientUtils ignoredUtils, int pad, ItemWrittenBookPredicate.PagePredicate predicate) {
        return List.of(pad(pad, translatable("ali.property.value.page", value(predicate.contents()))));
    }

    @NotNull
    public static List<Component> getEntryPredicateTooltip(IClientUtils utils, int pad, ItemAttributeModifiersPredicate.EntryPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderSetTooltip(utils, pad, "ali.property.branch.attributes", predicate.attribute(), GenericTooltipUtils::getAttributeTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.id", predicate.id(), GenericTooltipUtils::getResourceLocationTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.amount", predicate.amount()));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.operation", predicate.operation(), GenericTooltipUtils::getEnumTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.slot", predicate.slot(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getTrimMaterialTooltip(IClientUtils utils, int pad, TrimMaterial material) {
        List<Component> components = new LinkedList<>();

        components.addAll(getStringTooltip(utils, pad, "ali.property.value.asset_name", material.assetName()));
        components.addAll(getHolderTooltip(utils, pad, material.ingredient(), GenericTooltipUtils::getItemTooltip));
        components.addAll(getFloatTooltip(utils, pad, "ali.property.value.model_index", material.itemModelIndex()));
        components.addAll(getComponentTooltip(utils, pad, "ali.property.value.description", material.description()));

        return components;
    }

    @NotNull
    public static List<Component> getTrimPatternTooltip(IClientUtils utils, int pad, TrimPattern material) {
        List<Component> components = new LinkedList<>();

        components.addAll(getResourceLocationTooltip(utils, pad, "ali.property.value.asset_id", material.assetId()));
        components.addAll(getHolderTooltip(utils, pad, material.templateItem(), GenericTooltipUtils::getItemTooltip));
        components.addAll(getComponentTooltip(utils, pad, "ali.property.value.description", material.description()));
        components.addAll(getBooleanTooltip(utils, pad, "ali.property.value.decal", material.decal()));

        return components;
    }

    @NotNull
    public static List<Component> getDataComponentPatchTooltip(IClientUtils utils, int pad, DataComponentPatch data) {
        List<Component> components = new LinkedList<>();

        if (!data.map.isEmpty()) {
            components.add(pad(pad, translatable("ali.property.branch.components")));
            data.map.forEach((type, value) -> components.addAll(getDataComponentTypeTooltip(utils, pad + 1, type)));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getDataComponentTypeTooltip(IClientUtils utils, int pad, DataComponentType<?> data) {
        return getResourceLocationTooltip(utils, pad, "ali.property.value.type", Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(data)));
    }

    @NotNull
    public static List<Component> getFireworkExplosionTooltip(IClientUtils utils, int pad, FireworkExplosion data) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.explosion")));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.shape", data.shape()));
        components.addAll(getIntListTooltip(utils, pad + 1, "ali.property.value.colors", data.colors()));
        components.addAll(getIntListTooltip(utils, pad + 1, "ali.property.value.fade_colors", data.fadeColors()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.has_trail", data.hasTrail()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.has_twinkle", data.hasTwinkle()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getIntListTooltip(IClientUtils utils, int pad, String key, IntList data) {
        return getStringTooltip(utils, pad, key, data.toString());
    }

    @NotNull
    public static <T> List<Component> getFilterableTooltip(IClientUtils utils, int pad, String key, Filterable<T> data,
                                                           QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(mapper.apply(utils, pad + 1, "ali.property.value.raw", data.raw()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.filtered", data.filtered(), mapper));

        return components;
    }

    @NotNull
    public static List<Component> getLevelBasedValueTooltip(IClientUtils utils, int pad, String key, LevelBasedValue levelBasedValue) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));

        switch (levelBasedValue) {
            case LevelBasedValue.Constant(float value) ->
                    components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.constant", value));
            case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                components.add(pad(pad + 1, translatable("ali.property.branch.clamped")));
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.value.value", value));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.min", min));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.max", max));
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                components.add(pad(pad + 1, translatable("ali.property.branch.fraction")));
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.value.numerator", numerator));
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.value.denominator", denominator));
            }
            case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> {
                components.add(pad(pad + 1, translatable("ali.property.branch.linear")));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.base", base));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.per_level", perLevelAboveFirst));
            }
            case LevelBasedValue.LevelsSquared(float added) -> {
                components.add(pad(pad + 1, translatable("ali.property.branch.level_squared")));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.added", added));
            }
            case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                components.add(pad(pad + 1, translatable("ali.property.branch.lookup")));
                components.addAll(getStringTooltip(utils, pad + 2, "ali.property.value.values", values.toString()));
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.value.fallback", fallback));
            }
            default -> {
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getLocationWrapperTooltip(IClientUtils utils, int pad, String key, EntityPredicate.LocationWrapper locationWrapper) {
        List<Component> components = new LinkedList<>();

        if (locationWrapper.located().isPresent() || locationWrapper.affectsMovement().isPresent() || locationWrapper.steppingOn().isPresent()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.located", locationWrapper.located(), GenericTooltipUtils::getLocationPredicateTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.stepping_on_location", locationWrapper.steppingOn(), GenericTooltipUtils::getLocationPredicateTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.affects_movement", locationWrapper.affectsMovement(), GenericTooltipUtils::getLocationPredicateTooltip));
        }

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
    public static List<Component> getIntegerTooltip(IClientUtils utils, int pad, String key, Integer value) {
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
    public static <T> List<Component> getResourceKeyTooltip(IClientUtils utils, int pad, String key, ResourceKey<T> value) {
        return List.of(pad(pad, translatable(key, value(value.location()))));
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagKeyTooltip(IClientUtils utils, int pad, String key, TagKey<T> value) {
        return getResourceLocationTooltip(utils, pad, key, value.location());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getComponentTooltip(IClientUtils ignoredUtils, int pad, String key, Component component) {
        return List.of(pad(pad, translatable(key, value(component))));
    }

    @NotNull
    public static <T> List<Component> getComponentsTooltip(IClientUtils utils, int pad, String key, T value, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        List<Component> componentList = mapper.apply(utils, pad + 1, value);

        if (!componentList.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(componentList);
        }

        return components;
    }

    @NotNull
    public static <T> List<Component> getComponentsTooltip(IClientUtils utils, int pad, String key, Optional<T> value, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();
        List<Component> componentList = getOptionalTooltip(utils, pad + 1, value, mapper);

        if (!componentList.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            components.addAll(componentList);
        }

        return components;
    }

    @NotNull
    public static List<Component> getMinMaxBoundsTooltip(IClientUtils utils, int pad, String key, MinMaxBounds.Ints ints) {
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
    public static <T> List<Component> getOptionalTooltip(IClientUtils utils, int pad, Optional<T> optional, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(utils, pad, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalTooltip(IClientUtils utils,int pad, String key, Optional<T> optional, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        return optional.map((value) -> mapper.apply(utils, pad, key, value)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderTooltip(IClientUtils utils, int pad, Optional<Holder<T>> optional, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        return optional.map((holder) -> getHolderTooltip(utils, pad, holder, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getOptionalHolderSetTooltip(IClientUtils utils, int pad, String key, Optional<HolderSet<T>> optional, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        return optional.map((holderSet) -> getHolderSetTooltip(utils, pad, key, holderSet, mapper)).orElse(List.of());
    }

    @NotNull
    public static <T> List<Component> getHolderTooltip(IClientUtils utils, int pad, Holder<T> holder, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        return mapper.apply(utils, pad, holder.value());
    }

    @NotNull
    public static <T> List<Component> getHolderSetTooltip(IClientUtils utils, int pad, String key, HolderSet<T> holderSet, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
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
    public static <T> List<Component> getHolderSetTooltip(IClientUtils utils, int pad, String key, HolderSet<T> holderSet, String value) {
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
    public static <T> List<Component> getCollectionTooltip(IClientUtils utils, int pad, String key, Collection<T> values, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        if (!values.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            values.forEach((value) -> components.addAll(mapper.apply(utils, pad + 1, value)));
        }

        return components;
    }

    @NotNull
    public static <T, P extends Collection<T>> List<Component> getCollectionTooltip(IClientUtils utils, int pad, String key, Optional<P> values, TriFunction<IClientUtils, Integer, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        values.ifPresent((v) -> {
            if (!v.isEmpty()) {
                components.add(pad(pad, translatable(key)));
                v.forEach((value) -> components.addAll(mapper.apply(utils, pad + 1, value)));
            }
        });

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
