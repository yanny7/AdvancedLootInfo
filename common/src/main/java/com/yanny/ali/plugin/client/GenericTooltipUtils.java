package com.yanny.ali.plugin.client;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
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

import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.*;

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
    public static List<Component> getPropertyTooltip(IClientUtils utils, int pad, String key, Property<?> property) {
        return getStringTooltip(utils, pad, key, property.getName());
    }

    @NotNull
    public static List<Component> getModifierTooltip(IClientUtils utils, int pad, String key, SetAttributesFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.attribute", modifier.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.operation", modifier.operation()));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.amount", modifier.amount()));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.id", modifier.id()));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.equipment_slots", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(IClientUtils ignoredUtils, int pad, String key, UUID uuid) {
        return List.of(pad(pad, translatable(key, value(uuid))));
    }

    @NotNull
    public static List<Component> getBannerPatternLayersTooltip(IClientUtils utils, int pad, String key, BannerPatternLayers bannerPatternLayers) {
        return getCollectionTooltip(utils, pad, key, "ali.property.value.null", bannerPatternLayers.layers(), GenericTooltipUtils::getBannerPatternLayerTooltip);
    }

    @NotNull
    public static List<Component> getBannerPatternLayerTooltip(IClientUtils utils, int pad, String key, BannerPatternLayers.Layer layer) {
        return getHolderTooltip(utils, pad, key, layer.pattern(), (u, p, s, b) -> getBannerPatternTooltip(u, p, s, b, layer.color()));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBannerPatternTooltip(IClientUtils ignoredUtils, int pad, String key, BannerPattern bannerPattern, DyeColor color) {
        return List.of(pad(pad, translatable(key, value(translatable(bannerPattern.translationKey() + "." + color.getName())))));
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(IClientUtils utils, int pad, String key, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(utils, pad, key, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
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
    public static List<Component> getDamageSourcePredicateTooltip(IClientUtils utils, int pad, String key, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.tag_predicates", damagePredicate.tags(), GenericTooltipUtils::getTagPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.direct_entity", damagePredicate.directEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.source_entity", damagePredicate.sourceEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_direct", damagePredicate.isDirect(), GenericTooltipUtils::getBooleanTooltip));

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
        components.addAll(getLocationWrapperTooltip(utils, pad + 1, "ali.property.branch.location", entityPredicate.location()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.movement", entityPredicate.movement(), GenericTooltipUtils::getMovementPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.periodic_tick", entityPredicate.periodicTick(), GenericTooltipUtils::getIntegerTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.mob_effects", entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.nbt", entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.entity_flags", entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.entity_equipment", entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, entityPredicate.subPredicate(), (u, i, p) -> {
            List<Component> list = new LinkedList<>();

            list.add(pad(i, translatable("ali.property.branch.entity_sub_predicate")));
            list.addAll(utils.getEntitySubPredicateTooltip(u, i + 1, p));

            return list;
        }));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(IClientUtils utils, int pad, String key, EntityTypePredicate entityTypePredicate) {
        return getHolderSetTooltip(utils, pad, key, "ali.property.value.null", entityTypePredicate.types(), RegistriesTooltipUtils::getEntityTypeTooltip);
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
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.biomes", "ali.property.value.null", locationPredicate.biomes(), RegistriesTooltipUtils::getBiomeTooltip));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.structures", "ali.property.value.null", locationPredicate.structures(), RegistriesTooltipUtils::getStructureTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.light", locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.block_predicate", locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.fluid_predicate", locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.can_see_sky", locationPredicate.canSeeSky(), GenericTooltipUtils::getBooleanTooltip));

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
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.blocks", "ali.property.value.null", blockPredicate.blocks(), RegistriesTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.state_properties_predicate", blockPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.nbt", blockPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

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
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.fluids", "ali.property.value.null", fluidPredicate.fluids(), RegistriesTooltipUtils::getFluidTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.state_properties_predicate", fluidPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(IClientUtils utils, int pad, String key, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.property.branch.mob_effects")));
        mobEffectsPredicate.effectMap().forEach((effect, instancePredicate) -> {
            components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.mob_effect", effect, RegistriesTooltipUtils::getMobEffectTooltip));
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
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_on_ground", entityFlagsPredicate.isOnGround(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_on_fire", entityFlagsPredicate.isOnFire(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_baby", entityFlagsPredicate.isBaby(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_crouching", entityFlagsPredicate.isCrouching(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_sprinting", entityFlagsPredicate.isSprinting(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_swimming", entityFlagsPredicate.isSwimming(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_flying", entityFlagsPredicate.isFlying(), GenericTooltipUtils::getBooleanTooltip));

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
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.items", "ali.property.value.null", itemPredicate.items(), RegistriesTooltipUtils::getItemTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.count", itemPredicate.count()));
        components.addAll(getDataComponentPredicateTooltip(utils, pad + 1, "ali.property.branch.component_predicates", itemPredicate.components()));

        if (!itemPredicate.subPredicates().isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.branch.item_predicates")));
            itemPredicate.subPredicates().forEach((type, predicate) -> components.addAll(utils.getItemSubPredicateTooltip(utils, pad + 2, type, predicate)));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(IClientUtils utils, int pad, String key, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getOptionalHolderSetTooltip(utils, pad, key, "ali.property.value.null", enchantmentPredicate.enchantments(), RegistriesTooltipUtils::getEnchantmentTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", enchantmentPredicate.level()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getGameTypePredicateTooltip(IClientUtils utils, int pad, String key, GameTypePredicate gameType) {
        return getCollectionTooltip(utils, pad, key, gameType.types(), GenericTooltipUtils::getEnumTooltip);
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
    public static List<Component> getRecipesTooltip(IClientUtils ignoredUtils, int pad, String key, Object2BooleanMap<ResourceKey<Recipe<?>>> recipes) {
        List<Component> components = new LinkedList<>();

        if (!recipes.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            recipes.forEach((recipe, required) -> components.add(pad(pad + 1, keyValue(recipe.location().toString(), required))));
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
    public static List<Component> getBlockPosTooltip(IClientUtils utils, int pad, String key, BlockPos pos) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.x", pos.getX()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.y", pos.getY()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.z", pos.getZ()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getMapDecorationTypeTooltip(IClientUtils utils, int pad, String key, MapDecorationType decorationType) {
        return RegistriesTooltipUtils.getMapDecorationTypeTooltip(utils, pad, key, decorationType);
    }

    @NotNull
    public static List<Component> getListOperationTooltip(IClientUtils utils, int pad, String key, ListOperation operation) {
        List<Component> components = new LinkedList<>(getEnumTooltip(utils, pad, key, operation.mode()));

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
    public static List<Component> getContainerComponentManipulatorTooltip(IClientUtils utils, int pad, String key, ContainerComponentManipulator<?> component) {
        return getDataComponentTypeTooltip(utils, pad, key, component.type());
    }

    @NotNull
    public static List<Component> getCopyOperationTooltip(IClientUtils utils, int pad, String key, CopyCustomDataFunction.CopyOperation copyOperation) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(GenericTooltipUtils.getNbtPathTooltip(utils, pad + 1, "ali.property.value.source_path", copyOperation.sourcePath()));
        components.addAll(GenericTooltipUtils.getNbtPathTooltip(utils, pad + 1, "ali.property.value.target_path", copyOperation.targetPath()));
        components.addAll(GenericTooltipUtils.getEnumTooltip(utils, pad + 1, "ali.property.value.merge_strategy", copyOperation.op()));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getNbtPathTooltip(IClientUtils utils, int pad, String key, NbtPathArgument.NbtPath provider) {
        return getStringTooltip(utils, pad, key, provider.asString());
    }

    @NotNull
    public static List<Component> getDataComponentPredicateTooltip(IClientUtils utils, int pad, String key, DataComponentPredicate dataComponentPredicate) {
        if (dataComponentPredicate != DataComponentPredicate.EMPTY) {
            return getCollectionTooltip(utils, pad, key, "ali.property.value.component", dataComponentPredicate.expectedComponents, GenericTooltipUtils::getTypedDataComponentTooltip);
        } else {
            return List.of();
        }
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTypedDataComponentTooltip(IClientUtils utils, int pad, String key, TypedDataComponent<?> typedDataComponent) {
        List<Component> components = new LinkedList<>();

        components.addAll(getDataComponentTypeTooltip(utils, pad, key, typedDataComponent.type()));
        components.addAll(utils.getDataComponentTypeTooltip(utils, pad + 1, typedDataComponent.type(), typedDataComponent.value()));

        return components;
    }

    @NotNull
    public static <A, B extends Predicate<A>> List<Component> getCollectionPredicateTooltip(IClientUtils utils, int pad, String key, String value, Optional<CollectionPredicate<A, B>> optional,
                                                                                            QuadFunction<IClientUtils, Integer, String, B, List<Component>> subPredicate) {
        List<Component> components = new LinkedList<>();

        optional.ifPresent((predicate) -> {
            components.add(pad(pad, translatable(key)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.contains", predicate.contains(),
                    (u, i, s, c) -> getCollectionTooltip(u, i, s, value, c.unpack(), subPredicate)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.branch.counts", predicate.counts(),
                    (u, i, s, c) -> getCollectionTooltip(u, i, s, c.unpack(),
                            (u1, i1, s1) -> {
                                List<Component> comps = new LinkedList<>();

                                comps.addAll(subPredicate.apply(u1, i1, value, s1.test()));
                                comps.addAll(getMinMaxBoundsTooltip(u1, i1 + 1, "ali.property.value.count", s1.count()));

                                return comps;
                            })));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.size", predicate.size(), GenericTooltipUtils::getMinMaxBoundsTooltip));
        });

        return components;
    }

    @NotNull
    public static List<Component> getFireworkPredicateTooltip(IClientUtils utils, int pad, String key, ItemFireworkExplosionPredicate.FireworkPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.shape", predicate.shape(), GenericTooltipUtils::getEnumTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.trail", predicate.trail(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.twinkle", predicate.twinkle(), GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPagePredicateTooltip(IClientUtils ignoredUtils, int pad, String key, ItemWritableBookPredicate.PagePredicate predicate) {
        return List.of(pad(pad, translatable(key, value(predicate.contents()))));
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getPagePredicateTooltip(IClientUtils ignoredUtils, int pad, String key, ItemWrittenBookPredicate.PagePredicate predicate) {
        return List.of(pad(pad, translatable(key, value(predicate.contents()))));
    }

    @NotNull
    public static List<Component> getEntryPredicateTooltip(IClientUtils utils, int pad, String key, ItemAttributeModifiersPredicate.EntryPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalHolderSetTooltip(utils, pad + 1, "ali.property.branch.attributes", "ali.property.value.null", predicate.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.id", predicate.id(), GenericTooltipUtils::getResourceLocationTooltip));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.amount", predicate.amount()));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.operation", predicate.operation(), GenericTooltipUtils::getEnumTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.slot", predicate.slot(), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getDataComponentPatchTooltip(IClientUtils utils, int pad, String key, DataComponentPatch data) {
        List<Component> components = new LinkedList<>();

        if (!data.map.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            data.map.forEach((type, value) -> {
                components.addAll(getDataComponentTypeTooltip(utils, pad + 1, "ali.property.value.null", type));
                components.addAll(getOptionalTooltip(utils, pad + 2, value, (u, i, v) -> utils.getDataComponentTypeTooltip(utils, pad + 2, type, v)));

                if (value.isEmpty()) {
                    components.add(pad(pad + 2, translatable("ali.util.advanced_loot_info.removed")));
                }
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getFireworkExplosionTooltip(IClientUtils utils, int pad, String key, FireworkExplosion data) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
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
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.branch.value", value));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.min", min));
                components.addAll(getFloatTooltip(utils, pad + 2, "ali.property.value.max", max));
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                components.add(pad(pad + 1, translatable("ali.property.branch.fraction")));
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.branch.numerator", numerator));
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.branch.denominator", denominator));
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
                components.addAll(getLevelBasedValueTooltip(utils, pad + 2, "ali.property.branch.fallback", fallback));
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

    @NotNull
    public static List<Component> getMovementPredicateTooltip(IClientUtils utils, int pad, String key, MovementPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", predicate.x()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", predicate.y()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", predicate.z()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.speed", predicate.speed()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.horizontal_speed", predicate.horizontalSpeed()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.vertical_speed", predicate.verticalSpeed()));
        components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.fall_distance", predicate.fallDistance()));

        return components;
    }

    @NotNull
    public static List<Component> getItemAttributeModifiersEntryTooltip(IClientUtils utils, int pad, String key, ItemAttributeModifiers.Entry entry) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getHolderTooltip(utils, pad + 1, "ali.property.value.attribute", entry.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        components.addAll(getAttributeModifierTooltip(utils, pad + 1, "ali.property.branch.attribute_modifier", entry.modifier()));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.slot", entry.slot()));

        return components;
    }

    @NotNull
    public static List<Component> getAttributeModifierTooltip(IClientUtils utils, int pad, String key, AttributeModifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.id", modifier.id()));
        components.addAll(getDoubleTooltip(utils, pad + 1, "ali.property.value.amount", modifier.amount()));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.operation", modifier.operation()));

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstanceTooltip(IClientUtils utils, int pad, String key, MobEffectInstance effect) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getMobEffectTooltip(utils, pad + 1, "ali.property.value.mob_effect", effect.getEffect().value()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.duration", effect.getDuration()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.amplifier", effect.getAmplifier()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.ambient", effect.isAmbient()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.is_visible", effect.isVisible()));
        components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.show_icon", effect.showIcon()));

        if (effect.hiddenEffect != null) {
            components.addAll(getMobEffectInstanceTooltip(utils, pad + 1, "ali.property.branch.hidden_effect", effect.hiddenEffect));
        }

        return components;
    }

    @NotNull
    public static List<Component> getRuleTooltip(IClientUtils utils, int pad, String key, Tool.Rule rule) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getHolderSetTooltip(utils, pad + 1, "ali.property.branch.blocks", "ali.property.value.null", rule.blocks(), RegistriesTooltipUtils::getBlockTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.correct_for_drops", rule.correctForDrops(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.speed", rule.speed(), GenericTooltipUtils::getFloatTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getMapDecorationEntryTooltip(IClientUtils utils, int pad, MapDecorations.Entry entry) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMapDecorationTypeTooltip(utils, pad, "ali.property.value.type", entry.type().value()));
        components.addAll(getDoubleTooltip(utils, pad + 1, "ali.property.value.x", entry.x()));
        components.addAll(getDoubleTooltip(utils, pad + 1, "ali.property.value.z", entry.z()));
        components.addAll(getFloatTooltip(utils, pad + 1, "ali.property.value.rotation", entry.rotation()));

        return components;
    }

    @NotNull
    public static List<Component> getItemStackTooltip(IClientUtils utils, int pad, String key, ItemStack item) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getItemTooltip(utils, pad + 1, "ali.property.value.item", item.getItem()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.count", item.getCount()));
        components.addAll(getDataComponentMapTooltip(utils, pad + 1, "ali.property.branch.components", item.getComponents()));

        return components;
    }

    @NotNull
    public static List<Component> getDataComponentMapTooltip(IClientUtils utils, int pad, String key, DataComponentMap map) {
        List<Component> components = new LinkedList<>();

        if (!map.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            map.forEach((action) -> {
                components.addAll(getDataComponentTypeTooltip(utils, pad + 1, "ali.property.value.type", action.type()));
                components.addAll(utils.getDataComponentTypeTooltip(utils, pad + 2, action.type(), action.value()));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getSuspiciousStewEffectEntryTooltip(IClientUtils utils, int pad, String key, SuspiciousStewEffects.Entry entry) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getMobEffectTooltip(utils, pad + 1, "ali.property.value.type", entry.effect().value()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.duration", entry.duration()));

        return components;
    }

    @NotNull
    public static List<Component> getGlobalPosTooltip(IClientUtils utils, int pad, String key, GlobalPos globalPos) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getResourceKeyTooltip(utils, pad + 1, "ali.property.value.dimension", globalPos.dimension()));
        components.addAll(getBlockPosTooltip(utils, pad + 1, "ali.property.branch.position", globalPos.pos()));

        return components;
    }

    @NotNull
    public static List<Component> getBeehiveBlockEntityOccupantTooltip(IClientUtils utils, int pad, String key, BeehiveBlockEntity.Occupant occupant) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.entity_data", occupant.entityData().copyTag().getAsString()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.ticks_in_hive", occupant.ticksInHive()));
        components.addAll(getIntegerTooltip(utils, pad + 1, "ali.property.value.min_ticks_in_hive", occupant.minTicksInHive()));

        return components;
    }

    @NotNull
    public static List<Component> getEffectEntryTooltip(IClientUtils utils, int pad, String key, SetStewEffectFunction.EffectEntry entry) {
        List<Component> components = new LinkedList<>();

        components.addAll(getHolderTooltip(utils, pad, key, entry.effect(), RegistriesTooltipUtils::getMobEffectTooltip));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.duration", entry.duration()));

        return components;
    }

    @NotNull
    public static List<Component> getPropertyTooltip(IClientUtils utils, int pad, com.mojang.authlib.properties.Property property) {
        List<Component> components = new LinkedList<>();

        components.addAll(getStringTooltip(utils, pad, "ali.property.value.name", property.name()));
        components.addAll(getStringTooltip(utils, pad, "ali.property.value.value", property.value()));

        if (property.signature() != null) {
            components.addAll(getStringTooltip(utils, pad, "ali.property.value.signature", property.signature()));
        }

        return components;
    }

    @NotNull
    public static List<Component> getInputPredicateTooltip(IClientUtils utils, int pad, String key, InputPredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.forward", predicate.forward(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.backward", predicate.backward(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.left", predicate.left(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.right", predicate.right(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.jump", predicate.jump(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.sneak", predicate.sneak(), GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.sprint", predicate.sprint(), GenericTooltipUtils::getBooleanTooltip));

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
    public static List<Component> getDoubleTooltip(IClientUtils ignoredUtils, int pad, String key, Double value) {
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
    public static <V, T extends Registry<V>> List<Component> getRegistryTooltip(IClientUtils utils, int pad, String key, ResourceKey<T> registry, V value) {
        HolderLookup.Provider provider = utils.lookupProvider();

        if (provider != null) {
            Optional<? extends HolderLookup.RegistryLookup<V>> lookup = provider.lookup(registry);

            if (lookup.isPresent()) {
                Optional<Holder.Reference<V>> first = lookup.get().listElements().filter((l) -> l.value() == value).findFirst();

                if (first.isPresent()) {
                    return getResourceKeyTooltip(utils, pad, key, Objects.requireNonNull(first.get().key()));
                }
            }
        }

        return List.of();
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

    @NotNull
    public static <T, P extends Collection<T>> List<Component> getCollectionTooltip(IClientUtils utils, int pad, String key, String value, Optional<P> values, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        List<Component> components = new LinkedList<>();

        values.ifPresent((v) -> {
            if (!v.isEmpty()) {
                components.add(pad(pad, translatable(key)));
                v.forEach((val) -> components.addAll(mapper.apply(utils, pad + 1, value, val)));
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
