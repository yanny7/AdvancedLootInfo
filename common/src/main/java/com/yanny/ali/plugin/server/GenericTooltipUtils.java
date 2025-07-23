package com.yanny.ali.plugin.server;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
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
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GenericTooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMissingFunction(IServerUtils utils, LootItemFunction function) {
        return getFunctionTypeTooltip(utils, "ali.util.advanced_loot_info.missing", function.getType());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMissingCondition(IServerUtils utils, LootItemCondition condition) {
        return getConditionTypeTooltip(utils, "ali.util.advanced_loot_info.missing", condition.getType());
    }

    @NotNull
    public static List<ITooltipNode> getConditionListTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        return conditions.stream().map((condition) -> utils.getConditionTooltip(utils, condition)).toList();
    }

    @NotNull
    public static List<ITooltipNode> getConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            List<ITooltipNode> tooltip = new ArrayList<>();

            tooltip.add(new TooltipNode(translatable("ali.util.advanced_loot_info.delimiter.conditions")));
            tooltip.addAll(getConditionListTooltip(utils, conditions));

            return tooltip;
        }

        return Collections.emptyList();
    }

    @NotNull
    public static ITooltipNode getSubConditionsTooltip(IServerUtils utils, List<LootItemCondition> conditions) {
        if (!conditions.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.property.branch.conditions"));

            for (ITooltipNode node : getConditionListTooltip(utils, conditions)) {
                tooltip.add(node);
            }

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static List<ITooltipNode> getFunctionListTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        return functions.stream().map((function) -> utils.getFunctionTooltip(utils, function)).toList();
    }

    @NotNull
    public static List<ITooltipNode> getFunctionsTooltip(IServerUtils utils, List<LootItemFunction> functions) {
        if (!functions.isEmpty()) {
            List<ITooltipNode> tooltip = new ArrayList<>();

            tooltip.add(new TooltipNode(translatable("ali.util.advanced_loot_info.delimiter.functions")));
            tooltip.addAll(getFunctionListTooltip(utils, functions));

            return tooltip;
        }

        return Collections.emptyList();
    }

    @NotNull
    public static ITooltipNode getFormulaTooltip(IServerUtils utils, String key, ApplyBonusCount.Formula formula) {
        ITooltipNode tooltip = getResourceLocationTooltip(utils, key, formula.getType().id());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount(int extraRounds, float probability)) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.extra_rounds", extraRounds));
            tooltip.add(getFloatTooltip(utils, "ali.property.value.probability", probability));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount(int bonusMultiplier)) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.bonus_multiplier", bonusMultiplier));
        }

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPropertyTooltip(IServerUtils utils, String key, Property<?> property) {
        return getStringTooltip(utils, key, property.getName());
    }

    @NotNull
    public static ITooltipNode getModifierTooltip(IServerUtils utils, String key, SetAttributesFunction.Modifier modifier) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.attribute", modifier.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.operation", modifier.operation()));
        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.amount", modifier.amount()));
        tooltip.add(getResourceLocationTooltip(utils, "ali.property.value.id", modifier.id()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.equipment_slots", "ali.property.value.null", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getUUIDTooltip(IServerUtils utils, String key, UUID uuid) {
        return new TooltipNode(translatable(key, value(uuid)));
    }

    @NotNull
    public static ITooltipNode getBannerPatternLayersTooltip(IServerUtils utils, String key, BannerPatternLayers bannerPatternLayers) {
        return getCollectionTooltip(utils, key, "ali.property.value.null", bannerPatternLayers.layers(), GenericTooltipUtils::getBannerPatternLayerTooltip);
    }

    @NotNull
    public static ITooltipNode getBannerPatternLayerTooltip(IServerUtils utils, String key, BannerPatternLayers.Layer layer) {
        ITooltipNode tooltip = getHolderTooltip(utils, key, layer.pattern(), RegistriesTooltipUtils::getBannerPatternTooltip);

        tooltip.add(getEnumTooltip(utils, "ali.property.value.color", layer.color()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getStatePropertiesPredicateTooltip(IServerUtils utils, String key, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(utils, key, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static ITooltipNode getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
            return new TooltipNode(keyValue(name, value));
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> minValue, Optional<String> maxValue)) {
            if (minValue.isPresent()) {
                if (maxValue.isPresent()) {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_both", name, minValue.get(), maxValue.get())));
                } else {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_gte", name, minValue.get())));
                }
            } else {
                if (maxValue.isPresent()) {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_lte", name, maxValue.get())));
                } else {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_any", name)));
                }
            }
        }
        
        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getDamageSourcePredicateTooltip(IServerUtils utils, String key, DamageSourcePredicate damagePredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.tags", "ali.property.value.null", damagePredicate.tags(), GenericTooltipUtils::getTagPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.direct_entity", damagePredicate.directEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.source_entity", damagePredicate.sourceEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_direct", damagePredicate.isDirect(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static <T> ITooltipNode getTagPredicateTooltip(IServerUtils utils, String key, TagPredicate<T> tagPredicate) {
        return new TooltipNode(translatable(key, keyValue(tagPredicate.tag().location().toString(), tagPredicate.expected())));
    }

    @NotNull
    public static ITooltipNode getEntityPredicateTooltip(IServerUtils utils, String key, EntityPredicate entityPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_types", entityPredicate.entityType(), GenericTooltipUtils::getEntityTypePredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer(), GenericTooltipUtils::getDistancePredicateTooltip));
        tooltip.add(getLocationWrapperTooltip(utils, "ali.property.branch.location", entityPredicate.location()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.movement", entityPredicate.movement(), GenericTooltipUtils::getMovementPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.periodic_tick", entityPredicate.periodicTick(), GenericTooltipUtils::getIntegerTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.mob_effects", entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.nbt", entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_flags", entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_equipment", entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_sub_predicate", entityPredicate.subPredicate(), GenericTooltipUtils::getEntitySubPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.slots", entityPredicate.slots(), GenericTooltipUtils::getSlotPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEntityTypePredicateTooltip(IServerUtils utils, String key, EntityTypePredicate entityTypePredicate) {
        return getHolderSetTooltip(utils, key, "ali.property.value.null", entityTypePredicate.types(), RegistriesTooltipUtils::getEntityTypeTooltip);
    }

    @NotNull
    public static ITooltipNode getDistancePredicateTooltip(IServerUtils utils, String key, DistancePredicate distancePredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.x", distancePredicate.x()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.y", distancePredicate.y()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.z", distancePredicate.z()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.horizontal", distancePredicate.horizontal()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.absolute", distancePredicate.absolute()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getLocationPredicateTooltip(IServerUtils utils, String key, LocationPredicate locationPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.position", locationPredicate.position(), GenericTooltipUtils::getPositionPredicateTooltip));
        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.biomes", "ali.property.value.null", locationPredicate.biomes(), RegistriesTooltipUtils::getBiomeTooltip));
        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.structures", "ali.property.value.null", locationPredicate.structures(), RegistriesTooltipUtils::getStructureTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.light", locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.block_predicate", locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.fluid_predicate", locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.can_see_sky", locationPredicate.canSeeSky(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getPositionPredicateTooltip(IServerUtils utils, String key, LocationPredicate.PositionPredicate positionPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.x", positionPredicate.x()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.y", positionPredicate.y()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.z", positionPredicate.z()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getLightPredicateTooltip(IServerUtils utils, String key, LightPredicate lightPredicate) {
            return getMinMaxBoundsTooltip(utils, key, lightPredicate.composite());
    }

    @NotNull
    public static ITooltipNode getBlockPredicateTooltip(IServerUtils utils, String key, BlockPredicate blockPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.blocks", "ali.property.value.null", blockPredicate.blocks(), RegistriesTooltipUtils::getBlockTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.properties", blockPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.nbt", blockPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getNbtPredicateTooltip(IServerUtils utils, String key, NbtPredicate nbtPredicate) {
        return getCompoundTagTooltip(utils, key, nbtPredicate.tag());
    }

    @NotNull
    public static ITooltipNode getFluidPredicateTooltip(IServerUtils utils, String key, FluidPredicate fluidPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.fluids", "ali.property.value.null", fluidPredicate.fluids(), RegistriesTooltipUtils::getFluidTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.properties", fluidPredicate.properties(), GenericTooltipUtils::getStatePropertiesPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMobEffectPredicateTooltip(IServerUtils utils, String key, MobEffectsPredicate mobEffectsPredicate) {
        return getMapTooltip(utils, key, mobEffectsPredicate.effectMap(), GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
    }

    @NotNull
    public static ITooltipNode getEntityFlagsPredicateTooltip(IServerUtils utils, String key, EntityFlagsPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_on_ground", predicate.isOnGround(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_on_fire", predicate.isOnFire(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_baby", predicate.isBaby(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_crouching", predicate.isCrouching(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_sprinting", predicate.isSprinting(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_swimming", predicate.isSwimming(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_flying", predicate.isFlying(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEntityEquipmentPredicateTooltip(IServerUtils utils, String key, EntityEquipmentPredicate predicate) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.head", predicate.head(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.chest", predicate.chest(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.legs", predicate.legs(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.feet", predicate.feet(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.body", predicate.body(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.mainhand", predicate.mainhand(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.offhand", predicate.offhand(), GenericTooltipUtils::getItemPredicateTooltip));

            return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemPredicateTooltip(IServerUtils utils, String key, ItemPredicate itemPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.items", "ali.property.value.null", itemPredicate.items(), RegistriesTooltipUtils::getItemTooltip));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.count", itemPredicate.count()));
        tooltip.add(getDataComponentPredicateTooltip(utils, "ali.property.branch.components", itemPredicate.components()));
        tooltip.add(getMapTooltip(utils, "ali.property.branch.item_predicates", itemPredicate.subPredicates(), GenericTooltipUtils::getItemSubPredicateEntryTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantmentPredicateTooltip(IServerUtils utils, String key, EnchantmentPredicate enchantmentPredicate) {
        ITooltipNode tooltip = getOptionalHolderSetTooltip(utils, key, "ali.property.value.null", enchantmentPredicate.enchantments(), RegistriesTooltipUtils::getEnchantmentTooltip);

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", enchantmentPredicate.level()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getGameTypePredicateTooltip(IServerUtils utils, String key, GameTypePredicate gameType) {
        return getCollectionTooltip(utils, key, "ali.property.value.null", gameType.types(), GenericTooltipUtils::getEnumTooltip);
    }

    @NotNull
    public static ITooltipNode getStatMatcherTooltip(IServerUtils utils, PlayerPredicate.StatMatcher<?> stat) {
        ITooltipNode tooltip;
        Holder<?> value = stat.value();

        if (value.value() instanceof Item item) {
            tooltip = getItemTooltip(utils, "ali.property.value.item", item);
            tooltip.add(new TooltipNode(keyValue(stat.type().getDisplayName(), toString(stat.range()))));
        } else if (value.value() instanceof Block block) {
            tooltip = getBlockTooltip(utils, "ali.property.value.block", block);
            tooltip.add(new TooltipNode(keyValue(stat.type().getDisplayName(), toString(stat.range()))));
        } else if (value.value() instanceof EntityType<?> entityType) {
            tooltip = getEntityTypeTooltip(utils, "ali.property.value.entity_type", entityType);
            tooltip.add(new TooltipNode(keyValue(stat.type().getDisplayName(), toString(stat.range()))));
        } else if (value.value() instanceof ResourceLocation resourceLocation) {
            tooltip = getResourceLocationTooltip(utils, "ali.property.value.id", resourceLocation);
            tooltip.add(new TooltipNode(keyValue(translatable(getTranslationKey(resourceLocation)), toString(stat.range()))));
        } else {
            tooltip = TooltipNode.EMPTY;
        }

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBlockPosTooltip(IServerUtils ignoredUtils, String key, BlockPos pos) {
        return new TooltipNode(translatable(key, value(pos.getX()), value(pos.getY()), value(pos.getZ())));
    }

    @NotNull
    public static ITooltipNode getListOperationTooltip(IServerUtils utils, String key, ListOperation operation) {
        ITooltipNode tooltip = getEnumTooltip(utils, key, operation.mode());

        if (operation instanceof ListOperation.Insert(int offset)) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.offset", offset));
        } else if (operation instanceof ListOperation.ReplaceSection(int offset, Optional<Integer> size)) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.offset", offset));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.size", size, GenericTooltipUtils::getIntegerTooltip));
        }

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getContainerComponentManipulatorTooltip(IServerUtils utils, String key, ContainerComponentManipulator<?> component) {
        return getDataComponentTypeTooltip(utils, key, component.type());
    }

    @NotNull
    public static ITooltipNode getCopyOperationTooltip(IServerUtils utils, String key, CopyCustomDataFunction.CopyOperation copyOperation) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getNbtPathTooltip(utils, "ali.property.value.source_path", copyOperation.sourcePath()));
        tooltip.add(getNbtPathTooltip(utils, "ali.property.value.target_path", copyOperation.targetPath()));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.merge_strategy", copyOperation.op()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getNbtPathTooltip(IServerUtils utils, String key, NbtPathArgument.NbtPath provider) {
        return getStringTooltip(utils, key, provider.asString());
    }

    @NotNull
    public static ITooltipNode getDataComponentPredicateTooltip(IServerUtils utils, String key, DataComponentPredicate dataComponentPredicate) {
        if (dataComponentPredicate != DataComponentPredicate.EMPTY) {
            return getCollectionTooltip(utils, key, "ali.property.value.null", dataComponentPredicate.expectedComponents, GenericTooltipUtils::getTypedDataComponentTooltip);
        } else {
            return TooltipNode.EMPTY;
        }
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTypedDataComponentTooltip(IServerUtils utils, String key, TypedDataComponent<?> typedDataComponent) {
        ITooltipNode tooltip = getDataComponentTypeTooltip(utils, key, typedDataComponent.type());

        tooltip.add(utils.getDataComponentTypeTooltip(utils, typedDataComponent.type(), typedDataComponent.value()));

        return tooltip;
    }

    @NotNull
    public static <A, B extends Predicate<A>> ITooltipNode getCollectionPredicateTooltip(IServerUtils utils, String key, String value, Optional<CollectionPredicate<A, B>> optional,
                                                                                            TriFunction<IServerUtils, String, B, ITooltipNode> subPredicate) {
        if (optional.isPresent()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            CollectionPredicate<A, B> predicate = optional.get();

            tooltip.add(getCollectionContentsPredicateTooltip(utils, "ali.property.branch.contains", value, predicate.contains(), subPredicate));
            tooltip.add(getCollectionCountsPredicateTooltip(utils, "ali.property.branch.counts", value, predicate.counts(), (u1, v1, s1) -> {
                ITooltipNode comps = subPredicate.apply(u1, v1, s1.test());

                comps.add(getMinMaxBoundsTooltip(u1, "ali.property.value.count", s1.count()));

                return comps;
            }));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.size", predicate.size(), GenericTooltipUtils::getMinMaxBoundsTooltip));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <A, B extends Predicate<A>> ITooltipNode getCollectionContentsPredicateTooltip(IServerUtils utils, String key, String value, Optional<CollectionContentsPredicate<A, B>> predicate,
                                                                                                    TriFunction<IServerUtils, String, B, ITooltipNode> mapper) {
        return predicate.map((p) -> getCollectionTooltip(utils, key, value, p.unpack(), mapper)).orElse(TooltipNode.EMPTY);
    }

    @NotNull
    public static <A, B extends Predicate<A>> ITooltipNode getCollectionCountsPredicateTooltip(IServerUtils utils, String key, String value, Optional<CollectionCountsPredicate<A, B>> predicate,
                                                                                                  TriFunction<IServerUtils, String, CollectionCountsPredicate.Entry<A, B>, ITooltipNode> mapper) {
        return predicate.map((p) -> getCollectionTooltip(utils, key, value, p.unpack(), mapper)).orElse(TooltipNode.EMPTY);
    }

    @NotNull
    public static ITooltipNode getFireworkPredicateTooltip(IServerUtils utils, String key, ItemFireworkExplosionPredicate.FireworkPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.shape", predicate.shape(), GenericTooltipUtils::getEnumTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.trail", predicate.trail(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.twinkle", predicate.twinkle(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPagePredicateTooltip(IServerUtils ignoredUtils, String key, ItemWritableBookPredicate.PagePredicate predicate) {
        return new TooltipNode(translatable(key, value(predicate.contents())));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getPagePredicateTooltip(IServerUtils ignoredUtils, String key, ItemWrittenBookPredicate.PagePredicate predicate) {
        return new TooltipNode(translatable(key, value(predicate.contents())));
    }

    @NotNull
    public static ITooltipNode getEntryPredicateTooltip(IServerUtils utils, String key, ItemAttributeModifiersPredicate.EntryPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.attributes", "ali.property.value.null", predicate.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.id", predicate.id(), GenericTooltipUtils::getResourceLocationTooltip));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.amount", predicate.amount()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.operation", predicate.operation(), GenericTooltipUtils::getEnumTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.slot", predicate.slot(), GenericTooltipUtils::getEnumTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDataComponentPatchTooltip(IServerUtils utils, String key, DataComponentPatch data) {
        return getMapTooltip(utils, key, data.map, GenericTooltipUtils::getDataComponentPatchEntryTooltip);
    }

    @NotNull
    public static ITooltipNode getFireworkExplosionTooltip(IServerUtils utils, String key, FireworkExplosion data) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getEnumTooltip(utils, "ali.property.value.shape", data.shape()));
        tooltip.add(getIntListTooltip(utils, "ali.property.value.colors", data.colors()));
        tooltip.add(getIntListTooltip(utils, "ali.property.value.fade_colors", data.fadeColors()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.has_trail", data.hasTrail()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.has_twinkle", data.hasTwinkle()));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntListTooltip(IServerUtils utils, String key, IntList data) {
        return getStringTooltip(utils, key, data.toString());
    }

    @NotNull
    public static <T> ITooltipNode getFilterableTooltip(IServerUtils utils, String key, Filterable<T> data,
                                                           TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(mapper.apply(utils, "ali.property.value.raw", data.raw()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.filtered", data.filtered(), mapper));

        return tooltip;
    }

    @NotNull
    public static <T> ITooltipNode getFilterableTooltip(IServerUtils utils, String key, Optional<Filterable<T>> data,
                                                           TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        if (data.isPresent()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            Filterable<T> d = data.get();

            tooltip.add(mapper.apply(utils, "ali.property.value.raw", d.raw()));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.filtered", d.filtered(), mapper));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <T> ITooltipNode getFilterableTooltip(IServerUtils utils, String key, String value, Collection<Filterable<T>> data,
                                                           TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        ITooltipNode tooltips = new TooltipNode(translatable(key));

        for (Filterable<T> d : data) {
            ITooltipNode tooltip = new TooltipNode(translatable(value));

            tooltip.add(mapper.apply(utils, "ali.property.value.raw", d.raw()));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.filtered", d.filtered(), mapper));
            tooltips.add(tooltip);
        }

        return tooltips;
    }

    @NotNull
    public static ITooltipNode getLevelBasedValueTooltip(IServerUtils utils, String key, LevelBasedValue levelBasedValue) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        switch (levelBasedValue) {
            case LevelBasedValue.Constant(float value) -> 
                    tooltip.add(getFloatTooltip(utils, "ali.property.value.constant", value));
            case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.clamped"));
                
                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.value", value));
                t.add(getFloatTooltip(utils, "ali.property.value.min", min));
                t.add(getFloatTooltip(utils, "ali.property.value.max", max));
                tooltip.add(t);
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.fraction"));
                
                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.numerator", numerator));
                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.denominator", denominator));
                tooltip.add(t);
            }
            case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.linear"));
                
                t.add(getFloatTooltip(utils, "ali.property.value.base", base));
                t.add(getFloatTooltip(utils, "ali.property.value.per_level", perLevelAboveFirst));
                tooltip.add(t);
            }
            case LevelBasedValue.LevelsSquared(float added) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.level_squared"));
                
                t.add(getFloatTooltip(utils, "ali.property.value.added", added));
                tooltip.add(t);
            }
            case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                ITooltipNode t = new TooltipNode(translatable("ali.property.branch.lookup"));
                
                t.add(getStringTooltip(utils, "ali.property.value.values", values.toString()));
                t.add(getLevelBasedValueTooltip(utils, "ali.property.branch.fallback", fallback));
                tooltip.add(t);
            }
            default -> {
            }
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getLocationWrapperTooltip(IServerUtils utils, String key, EntityPredicate.LocationWrapper locationWrapper) {
        if (locationWrapper.located().isPresent() || locationWrapper.affectsMovement().isPresent() || locationWrapper.steppingOn().isPresent()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.located", locationWrapper.located(), GenericTooltipUtils::getLocationPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.stepping_on_location", locationWrapper.steppingOn(), GenericTooltipUtils::getLocationPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.affects_movement", locationWrapper.affectsMovement(), GenericTooltipUtils::getLocationPredicateTooltip));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getMovementPredicateTooltip(IServerUtils utils, String key, MovementPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));
        
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.x", predicate.x()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.y", predicate.y()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.z", predicate.z()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.speed", predicate.speed()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.horizontal_speed", predicate.horizontalSpeed()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.vertical_speed", predicate.verticalSpeed()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.fall_distance", predicate.fallDistance()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemAttributeModifiersEntryTooltip(IServerUtils utils, String key, ItemAttributeModifiers.Entry entry) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getHolderTooltip(utils, "ali.property.value.attribute", entry.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        tooltip.add(getAttributeModifierTooltip(utils, "ali.property.branch.modifier", entry.modifier()));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.slot", entry.slot()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getAttributeModifierTooltip(IServerUtils utils, String key, AttributeModifier modifier) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getResourceLocationTooltip(utils, "ali.property.value.id", modifier.id()));
        tooltip.add(getDoubleTooltip(utils, "ali.property.value.amount", modifier.amount()));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.operation", modifier.operation()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMobEffectInstanceTooltip(IServerUtils utils, String key, MobEffectInstance effect) {
        ITooltipNode tooltip = getHolderTooltip(utils, key, effect.getEffect(), RegistriesTooltipUtils::getMobEffectTooltip);

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.duration", effect.getDuration()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.amplifier", effect.getAmplifier()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.ambient", effect.isAmbient()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.is_visible", effect.isVisible()));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.show_icon", effect.showIcon()));

        if (effect.hiddenEffect != null) {
            tooltip.add(getMobEffectInstanceTooltip(utils, "ali.property.value.hidden_effect", effect.hiddenEffect));
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getRuleTooltip(IServerUtils utils, String key, Tool.Rule rule) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getHolderSetTooltip(utils, "ali.property.branch.blocks", "ali.property.value.null", rule.blocks(), RegistriesTooltipUtils::getBlockTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.correct_for_drops", rule.correctForDrops(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.speed", rule.speed(), GenericTooltipUtils::getFloatTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMapDecorationEntryTooltip(IServerUtils utils, String key, MapDecorations.Entry entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, key, entry.type(), RegistriesTooltipUtils::getMapDecorationTypeTooltip);

        tooltip.add(getDoubleTooltip(utils, "ali.property.value.x", entry.x()));
        tooltip.add(getDoubleTooltip(utils, "ali.property.value.z", entry.z()));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.rotation", entry.rotation()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemStackTooltip(IServerUtils utils, String key, ItemStack item) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getItemTooltip(utils, "ali.property.value.item", item.getItem()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.count", item.getCount()));
        tooltip.add(getDataComponentMapTooltip(utils, "ali.property.branch.components", item.getComponents()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDataComponentMapTooltip(IServerUtils utils, String key, DataComponentMap map) {
        if (!map.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            map.forEach((action) -> {
                ITooltipNode t = getDataComponentTypeTooltip(utils, "ali.property.value.null", action.type());

                t.add(utils.getDataComponentTypeTooltip(utils, action.type(), action.value()));
                tooltip.add(t);
            });

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getSuspiciousStewEffectEntryTooltip(IServerUtils utils, SuspiciousStewEffects.Entry entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, "ali.property.value.null", entry.effect(), RegistriesTooltipUtils::getMobEffectTooltip);

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.duration", entry.duration()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getGlobalPosTooltip(IServerUtils utils, String key, GlobalPos globalPos) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getResourceKeyTooltip(utils, "ali.property.value.dimension", globalPos.dimension()));
        tooltip.add(getBlockPosTooltip(utils, "ali.property.multi.position", globalPos.pos()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getBeehiveBlockEntityOccupantTooltip(IServerUtils utils, String key, BeehiveBlockEntity.Occupant occupant) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getStringTooltip(utils, "ali.property.value.entity_data", occupant.entityData().copyTag().getAsString()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.ticks_in_hive", occupant.ticksInHive()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.min_ticks_in_hive", occupant.minTicksInHive()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEffectEntryTooltip(IServerUtils utils, String key, SetStewEffectFunction.EffectEntry entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, key, entry.effect(), RegistriesTooltipUtils::getMobEffectTooltip);

        tooltip.add(getNumberProviderTooltip(utils,"ali.property.value.duration", entry.duration()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getAuthPropertyTooltip(IServerUtils utils, com.mojang.authlib.properties.Property property) {
        ITooltipNode tooltip = new TooltipNode();

        tooltip.add(getStringTooltip(utils, "ali.property.value.name", property.name()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.value", property.value()));

        if (property.signature() != null) {
            tooltip.add(getStringTooltip(utils, "ali.property.value.signature", property.signature()));
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getInputPredicateTooltip(IServerUtils utils, String key, InputPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.forward", predicate.forward(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.backward", predicate.backward(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.left", predicate.left(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.right", predicate.right(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.jump", predicate.jump(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.sneak", predicate.sneak(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.sprint", predicate.sprint(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCompoundTagTooltip(IServerUtils ignoredUtils, String key, CompoundTag tag) {
        return new TooltipNode(translatable(key, value(tag.toString())));
    }

    @NotNull
    public static ITooltipNode getAdvancementPredicateTooltip(IServerUtils utils, String key, PlayerPredicate.AdvancementPredicate predicate) {
        if (predicate instanceof PlayerPredicate.AdvancementDonePredicate(boolean state)) {
            return new TooltipNode(translatable(key, state));
        } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions)) {
            return getMapTooltip(utils, criterions, GenericTooltipUtils::getCriterionEntryTooltip);
        }

        return TooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static <T> ITooltipNode getStandaloneTooltip(IServerUtils utils, String key, String value, Optional<ListOperation.StandAlone<T>> standalone,
                                                        TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        if (standalone.isPresent()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            ListOperation.StandAlone<T> s = standalone.get();

            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.values", value, s.value(), mapper));
            tooltip.add(getListOperationTooltip(utils, "ali.property.value.list_operation", s.operation()));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, String key, EntitySubPredicate predicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(utils.getEntitySubPredicateTooltip(utils, predicate));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getSlotPredicateTooltip(IServerUtils utils, String key, SlotsPredicate predicate) {
        return getMapTooltip(utils, key, predicate.slots(), GenericTooltipUtils::getSlotRangePredicateEntryTooltip);
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
    public static ITooltipNode getNumberProviderTooltip(IServerUtils utils, String key, NumberProvider value) {
        return new TooltipNode(translatable(key, value(utils.convertNumber(utils, value))));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntRangeTooltip(IServerUtils utils, String key, IntRange range) {
        return new TooltipNode(translatable(key, value(RangeValue.rangeToString(utils.convertNumber(utils, range.min), utils.convertNumber(utils, range.max)))));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBooleanTooltip(IServerUtils utils, String key, Boolean value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getIntegerTooltip(IServerUtils ignoredUtils, String key, int value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLongTooltip(IServerUtils ignoredUtils, String key, Long value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getStringTooltip(IServerUtils utils, String key, String value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFloatTooltip(IServerUtils ignoredUtils, String key, Float value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getDoubleTooltip(IServerUtils ignoredUtils, String key, Double value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getEnumTooltip(IServerUtils ignoredUtils, String key, Enum<?> value) {
        return new TooltipNode(translatable(key, value(value.name())));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getResourceLocationTooltip(IServerUtils ignoredUtils, String key, ResourceLocation value) {
        return new TooltipNode(translatable(key, value(value)));
    }

    @Unmodifiable
    @NotNull
    public static <T> ITooltipNode getBuiltInRegistryTooltip(IServerUtils utils, String key, Registry<T> registry, T value) {
        return getResourceLocationTooltip(utils, key, Objects.requireNonNull(registry.getKey(value)));
    }

    @Unmodifiable
    @NotNull
    public static <V, T extends Registry<V>> ITooltipNode getRegistryTooltip(IServerUtils utils, String key, ResourceKey<T> registry, V value) {
        HolderLookup.Provider provider = utils.lookupProvider();

        if (provider != null) {
            Optional<? extends HolderLookup.RegistryLookup<V>> lookup = provider.lookup(registry);

            if (lookup.isPresent()) {
                Optional<Holder.Reference<V>> first = lookup.get().listElements().filter((l) -> l.value() == value).findFirst();

                if (first.isPresent()) {
                    return getResourceKeyTooltip(utils, key, Objects.requireNonNull(first.get().key()));
                }
            }
        }

        return TooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static <T> ITooltipNode getResourceKeyTooltip(IServerUtils utils, String key, ResourceKey<T> value) {
        return new TooltipNode(translatable(key, value(value.location())));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getTagKeyTooltip(IServerUtils utils, String key, TagKey<?> value) {
        return getResourceLocationTooltip(utils, key, value.location());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getComponentTooltip(IServerUtils ignoredUtils, String key, Component component) {
        return new TooltipNode(translatable(key, value(component)));
    }

    @NotNull
    public static ITooltipNode getMinMaxBoundsTooltip(IServerUtils ignoredUtils, String key, MinMaxBounds.Ints ints) {
        if (ints != MinMaxBounds.Ints.ANY) {
            return new TooltipNode(translatable(key, value(toString(ints))));
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getMinMaxBoundsTooltip(IServerUtils ignoredUtils, String key, MinMaxBounds.Doubles doubles) {
        if (doubles != MinMaxBounds.Doubles.ANY) {
            return new TooltipNode(translatable(key, value(toString(doubles))));
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <T> ITooltipNode getOptionalTooltip(IServerUtils utils, Optional<T> optional, BiFunction<IServerUtils, T, ITooltipNode> mapper) {
        return optional.map((value) -> mapper.apply(utils, value)).orElse(TooltipNode.EMPTY);
    }

    @NotNull
    public static <T> ITooltipNode getOptionalTooltip(IServerUtils utils, String key, Optional<T> optional, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        return optional.map((value) -> mapper.apply(utils, key, value)).orElse(TooltipNode.EMPTY);
    }

    @NotNull
    public static <T> ITooltipNode getOptionalHolderTooltip(IServerUtils utils, String key, Optional<Holder<T>> optional, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        return optional.map((holder) -> getHolderTooltip(utils, key, holder, mapper)).orElse(TooltipNode.EMPTY);
    }

    @NotNull
    public static <T> ITooltipNode getOptionalHolderSetTooltip(IServerUtils utils, String key, String value, Optional<HolderSet<T>> optional, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        return optional.map((holderSet) -> getHolderSetTooltip(utils, key, value, holderSet, mapper)).orElse(TooltipNode.EMPTY);
    }

    @NotNull
    public static <T> ITooltipNode getHolderTooltip(IServerUtils utils, String key, Holder<T> holder, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        return mapper.apply(utils, key, holder.value());
    }

    @NotNull
    public static <T> ITooltipNode getHolderSetTooltip(IServerUtils utils, String key, String value, HolderSet<T> holderSet, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        ITooltipNode tooltip;
        Either<TagKey<T>, List<Holder<T>>> either = holderSet.unwrap();
        Optional<TagKey<T>> left = either.left();
        Optional<List<Holder<T>>> right = either.right();

        if (left.isPresent() || !right.orElse(List.of()).isEmpty()) {
            tooltip = new TooltipNode(translatable(key));
        } else {
            tooltip = new TooltipNode();
        }

        left.ifPresent((tagKey) -> tooltip.add(getTagKeyTooltip(utils, "ali.property.value.tag", tagKey)));
        right.ifPresent((list) -> {
            if (!list.isEmpty()) {
                holderSet.forEach((holder) -> tooltip.add(getHolderTooltip(utils, value, holder, mapper)));
            }
        });

        return tooltip;
    }

    @NotNull
    public static <T> ITooltipNode getCollectionTooltip(IServerUtils utils, Collection<T> values, BiFunction<IServerUtils, T, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode();

            values.forEach((value) -> tooltip.add(mapper.apply(utils, value)));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <T> ITooltipNode getCollectionTooltip(IServerUtils utils, String key, Collection<T> values, BiFunction<IServerUtils, T, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            
            values.forEach((value) -> tooltip.add(mapper.apply(utils, value)));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <T> ITooltipNode getCollectionTooltip(IServerUtils utils, String key, String value, Collection<T> values, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            
            values.forEach((v) -> tooltip.add(mapper.apply(utils, value, v)));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <T, P extends Collection<T>> ITooltipNode getCollectionTooltip(IServerUtils utils, String key, String value, Optional<P> values, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        if (values.isPresent()) {
            P v = values.get();

            if (!v.isEmpty()) {
                ITooltipNode tooltip = new TooltipNode(translatable(key));

                v.forEach((val) -> tooltip.add(mapper.apply(utils, value, val)));

                return tooltip;
            }
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <K, V> ITooltipNode getMapTooltip(IServerUtils utils, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode();

            values.entrySet().forEach((e) -> tooltip.add(mapper.apply(utils, e)));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <K, V> ITooltipNode getMapTooltip(IServerUtils utils, String key, Map<K, V> values, BiFunction<IServerUtils, Map.Entry<K, V>, ITooltipNode> mapper) {
        if (!values.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            values.entrySet().forEach((e) -> tooltip.add(mapper.apply(utils, e)));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static <T> ITooltipNode getEitherHolderTooltip(IServerUtils utils, String key, EitherHolder<T> holder, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        return holder.asEither().map((v) -> mapper.apply(utils, key, v.value()), (k) -> getResourceKeyTooltip(utils, key, k));
    }

    // MAP ENTRY

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceKey<Recipe<?>>, Boolean> entry) {
        return new TooltipNode(keyValue(entry.getKey().location(), entry.getValue()));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return new TooltipNode(keyValue(entry.getKey(), entry.getValue()));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getStringEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, String> entry) {
        return new TooltipNode(keyValue(entry.getKey(), entry.getValue()));
    }

    @NotNull
    public static ITooltipNode getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        ITooltipNode tooltip = new TooltipNode(value(entry.getKey()));

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.limit", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, "ali.property.value.null", entry.getKey(), RegistriesTooltipUtils::getMobEffectTooltip);

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.amplifier", entry.getValue().amplifier()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.duration", entry.getValue().duration()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_ambient", entry.getValue().ambient(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_visible", entry.getValue().visible(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, NumberProvider> entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, "ali.property.value.null", entry.getKey(), RegistriesTooltipUtils::getEnchantmentTooltip);

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.levels", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        ITooltipNode tooltip = getResourceLocationTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getAdvancementPredicateTooltip(utils, "ali.property.value.done", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMapDecorationEntryTooltip(IServerUtils utils, Map.Entry<String, MapDecorations.Entry> entry) {
        ITooltipNode tooltip = getStringTooltip(utils, "ali.property.value.decoration", entry.getKey());

        tooltip.add(getMapDecorationEntryTooltip(utils, "ali.property.value.null", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getBlockPropertyEntryTooltip(IServerUtils utils, Map.Entry<Holder<Block>, Property<?>> entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, "ali.property.value.block", entry.getKey(), RegistriesTooltipUtils::getBlockTooltip);

        tooltip.add(getPropertyTooltip(utils, "ali.property.value.property", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getPropertiesEntryTooltip(IServerUtils utils, Map.Entry<String, Collection<com.mojang.authlib.properties.Property>> entry) {
        ITooltipNode tooltip = getStringTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getCollectionTooltip(utils, entry.getValue(), GenericTooltipUtils::getAuthPropertyTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantmentLevelEntryTooltip(IServerUtils utils, Map.Entry<Holder<Enchantment>, Integer> entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, "ali.property.value.null", entry.getKey(), RegistriesTooltipUtils::getEnchantmentTooltip);

        tooltip.add(getIntegerTooltip(utils, "ali.property.value.level", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getToggleEntryTooltip(IServerUtils utils, Map.Entry<ToggleTooltips.ComponentToggle<?>, Boolean> entry) {
        ITooltipNode tooltip = getDataComponentTypeTooltip(utils, "ali.property.value.null", entry.getKey().type());

        tooltip.add(getBooleanTooltip(utils, "ali.property.value.value", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getDataComponentPatchEntryTooltip(IServerUtils utils, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        ITooltipNode tooltip = getDataComponentTypeTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getOptionalTooltip(utils, entry.getValue(), (u, v) -> u.getDataComponentTypeTooltip(u, entry.getKey(), v)));

        if (entry.getValue().isEmpty()) {
            tooltip.add(new TooltipNode(translatable("ali.util.advanced_loot_info.removed")));
        }

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemSubPredicateEntryTooltip(IServerUtils utils, Map.Entry<ItemSubPredicate.Type<?>, ItemSubPredicate> entry) {
        return utils.getItemSubPredicateTooltip(utils, entry.getValue());
    }

    @NotNull
    public static ITooltipNode getSlotRangePredicateEntryTooltip(IServerUtils utils, Map.Entry<SlotRange, ItemPredicate> entry) {
        ITooltipNode tooltip = getIntListTooltip(utils, "ali.property.value.null", entry.getKey().slots());

        tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.predicate", entry.getValue()));

        return tooltip;
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

    @NotNull
    private static String getTranslationKey(ResourceLocation location) {
        return "stat." + location.toString().replace(':', '.');
    }
}
