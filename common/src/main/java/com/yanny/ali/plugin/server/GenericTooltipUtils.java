package com.yanny.ali.plugin.server;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiFunction;

import static com.yanny.ali.plugin.server.RegistriesTooltipUtils.*;

public class GenericTooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMissingFunction(IServerUtils utils, LootItemFunction function) {
        return getFunctionTypeTooltip(utils, "ali.type.function.missing", function.getType());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getMissingCondition(IServerUtils utils, LootItemCondition condition) {
        return getConditionTypeTooltip(utils, "ali.type.condition.missing", condition.getType());
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

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFormulaTooltip(IServerUtils utils, String key, ApplyBonusCount.Formula formula) {
        ITooltipNode tooltip = getResourceLocationTooltip(utils, key, formula.getType());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.extra_rounds", binomialWithBonusCount.extraRounds));
            tooltip.add(getFloatTooltip(utils, "ali.property.value.probability", binomialWithBonusCount.probability));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.bonus_multiplier", uniformBonusCount.bonusMultiplier));
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

        tooltip.add(getStringTooltip(utils, "ali.property.value.name", modifier.name));
        tooltip.add(getAttributeTooltip(utils, "ali.property.value.attribute", modifier.attribute));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.operation", modifier.operation));
        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.amount", modifier.amount));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.uuid", modifier.id, GenericTooltipUtils::getUUIDTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.equipment_slots", "ali.property.value.null", List.of(modifier.slots), GenericTooltipUtils::getEnumTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getUUIDTooltip(IServerUtils utils, String key, UUID uuid) {
        return new TooltipNode(translatable("ali.property.value.uuid", value(uuid)));
    }

    @NotNull
    public static ITooltipNode getBannerPatternsTooltip(IServerUtils utils, String key, Pair<Holder<BannerPattern>, DyeColor> pair) {
        ITooltipNode tooltip = getHolderTooltip(utils, key, pair.getFirst(), RegistriesTooltipUtils::getBannerPatternTooltip);

        tooltip.add(getEnumTooltip(utils, "ali.property.value.color", pair.getSecond()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getStatePropertiesPredicateTooltip(IServerUtils utils, String key, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(utils, key, propertiesPredicate.properties, GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static ITooltipNode getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        if (propertyMatcher instanceof StatePropertiesPredicate.ExactPropertyMatcher matcher) {
            return new TooltipNode(keyValue(matcher.name, matcher.value));
        }
        if (propertyMatcher instanceof StatePropertiesPredicate.RangedPropertyMatcher matcher) {
            String min = matcher.minValue;
            String max = matcher.maxValue;

            if (min != null) {
                if (max != null) {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_both", matcher.name, min, max)));
                } else {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_gte", matcher.name, min)));
                }
            } else {
                if (max != null) {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_lte", matcher.name, max)));
                } else {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_any", matcher.name)));
                }
            }
        }
        
        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getDamageSourcePredicateTooltip(IServerUtils utils, String key, DamageSourcePredicate damagePredicate) {
        if (damagePredicate != DamageSourcePredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));
            
            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.tags", "ali.property.value.null", damagePredicate.tags, GenericTooltipUtils::getTagPredicateTooltip));
            tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.direct_entity", damagePredicate.directEntity));
            tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.source_entity", damagePredicate.sourceEntity));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static <T> ITooltipNode getTagPredicateTooltip(IServerUtils utils, String key, TagPredicate<T> tagPredicate) {
        return new TooltipNode(translatable(key, keyValue(tagPredicate.tag.location().toString(), tagPredicate.expected)));
    }

    @NotNull
    public static ITooltipNode getEntityPredicateTooltip(IServerUtils utils, String key, EntityPredicate entityPredicate) {
        if (entityPredicate != EntityPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getEntityTypePredicateTooltip(utils, "ali.property.value.entity_type", entityPredicate.entityType));
            tooltip.add(getDistancePredicateTooltip(utils, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer));
            tooltip.add(getLocationPredicateTooltip(utils, "ali.property.branch.location", entityPredicate.location));
            tooltip.add(getLocationPredicateTooltip(utils, "ali.property.branch.stepping_on_location", entityPredicate.steppingOnLocation));
            tooltip.add(getMobEffectPredicateTooltip(utils, "ali.property.branch.mob_effects", entityPredicate.effects));
            tooltip.add(getNbtPredicateTooltip(utils, "ali.property.value.nbt", entityPredicate.nbt));
            tooltip.add(getEntityFlagsPredicateTooltip(utils, "ali.property.branch.entity_flags", entityPredicate.flags));
            tooltip.add(getEntityEquipmentPredicateTooltip(utils, "ali.property.branch.entity_equipment", entityPredicate.equipment));
            tooltip.add(getEntitySubPredicateTooltip(utils, "ali.property.branch.entity_sub_predicate", entityPredicate.subPredicate));
            tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.vehicle", entityPredicate.vehicle));
            tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.passenger", entityPredicate.passenger));
            tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.team", entityPredicate.team, GenericTooltipUtils::getStringTooltip));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getEntityTypePredicateTooltip(IServerUtils utils, String key, EntityTypePredicate entityTypePredicate) {
        if (entityTypePredicate != EntityTypePredicate.ANY) {
            if (entityTypePredicate instanceof EntityTypePredicate.TypePredicate typePredicate) {
                return getEntityTypeTooltip(utils, key, typePredicate.type);
            }
            if (entityTypePredicate instanceof EntityTypePredicate.TagPredicate tagPredicate) {
                return getTagKeyTooltip(utils, key, tagPredicate.tag);
            }
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getDistancePredicateTooltip(IServerUtils utils, String key, DistancePredicate distancePredicate) {
        if (distancePredicate != DistancePredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.x", distancePredicate.x));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.y", distancePredicate.y));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.z", distancePredicate.z));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.horizontal", distancePredicate.horizontal));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.absolute", distancePredicate.absolute));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getLocationPredicateTooltip(IServerUtils utils, String key, LocationPredicate locationPredicate) {
        if (locationPredicate != LocationPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.x", locationPredicate.x));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.y", locationPredicate.y));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.z", locationPredicate.z));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.biome", locationPredicate.biome, GenericTooltipUtils::getResourceKeyTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.structure", locationPredicate.structure, GenericTooltipUtils::getResourceKeyTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.dimension", locationPredicate.dimension, GenericTooltipUtils::getResourceKeyTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.smokey", locationPredicate.smokey, GenericTooltipUtils::getBooleanTooltip));
            tooltip.add(getLightPredicateTooltip(utils, "ali.property.value.light", locationPredicate.light));
            tooltip.add(getBlockPredicateTooltip(utils, "ali.property.branch.block_predicate", locationPredicate.block));
            tooltip.add(getFluidPredicateTooltip(utils, "ali.property.branch.fluid_predicate", locationPredicate.fluid));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getLightPredicateTooltip(IServerUtils utils, String key, LightPredicate lightPredicate) {
        if (lightPredicate != LightPredicate.ANY) {
            return getMinMaxBoundsTooltip(utils, key, lightPredicate.composite);
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getBlockPredicateTooltip(IServerUtils utils, String key, BlockPredicate blockPredicate) {
        if (blockPredicate != BlockPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", blockPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));
            tooltip.add(getOptionalCollectionTooltip(utils, "ali.property.branch.blocks", "ali.property.value.null", blockPredicate.blocks, RegistriesTooltipUtils::getBlockTooltip));
            tooltip.add(getStatePropertiesPredicateTooltip(utils, "ali.property.branch.properties", blockPredicate.properties));
            tooltip.add(getNbtPredicateTooltip(utils, "ali.property.value.nbt", blockPredicate.nbt));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getNbtPredicateTooltip(IServerUtils utils, String key, NbtPredicate nbtPredicate) {
        if (nbtPredicate != NbtPredicate.ANY) {
            return getOptionalTooltip(utils, key, nbtPredicate.tag, GenericTooltipUtils::getCompoundTagTooltip);
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getFluidPredicateTooltip(IServerUtils utils, String key, FluidPredicate fluidPredicate) {
        if (fluidPredicate != FluidPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", fluidPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.fluid", fluidPredicate.fluid, RegistriesTooltipUtils::getFluidTooltip));
            tooltip.add(getStatePropertiesPredicateTooltip(utils, "ali.property.branch.properties", fluidPredicate.properties));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getMobEffectPredicateTooltip(IServerUtils utils, String key, MobEffectsPredicate mobEffectsPredicate) {
        if (mobEffectsPredicate != MobEffectsPredicate.ANY) {
            return getMapTooltip(utils, key, mobEffectsPredicate.effects, GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getEntityFlagsPredicateTooltip(IServerUtils utils, String key, EntityFlagsPredicate predicate) {
        if (predicate != EntityFlagsPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_on_fire", predicate.isOnFire, GenericTooltipUtils::getBooleanTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_baby", predicate.isBaby, GenericTooltipUtils::getBooleanTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_crouching", predicate.isCrouching, GenericTooltipUtils::getBooleanTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_sprinting", predicate.isSprinting, GenericTooltipUtils::getBooleanTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_swimming", predicate.isSwimming, GenericTooltipUtils::getBooleanTooltip));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getEntityEquipmentPredicateTooltip(IServerUtils utils, String key, EntityEquipmentPredicate predicate) {
        if (predicate != EntityEquipmentPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.head", predicate.head));
            tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.chest", predicate.chest));
            tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.legs", predicate.legs));
            tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.feet", predicate.feet));
            tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.mainhand", predicate.mainhand));
            tooltip.add(getItemPredicateTooltip(utils, "ali.property.branch.offhand", predicate.offhand));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getItemPredicateTooltip(IServerUtils utils, String key, ItemPredicate itemPredicate) {
        if (itemPredicate != ItemPredicate.ANY) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", itemPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));
            tooltip.add(getOptionalCollectionTooltip(utils, "ali.property.branch.items", "ali.property.value.null", itemPredicate.items, RegistriesTooltipUtils::getItemTooltip));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.count", itemPredicate.count));
            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.durability", itemPredicate.durability));
            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.enchantments", "ali.property.value.null", List.of(itemPredicate.enchantments), GenericTooltipUtils::getEnchantmentPredicateTooltip));
            tooltip.add(getCollectionTooltip(utils, "ali.property.branch.stored_enchantments", "ali.property.value.null", List.of(itemPredicate.storedEnchantments), GenericTooltipUtils::getEnchantmentPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.value.potion", itemPredicate.potion, RegistriesTooltipUtils::getPotionTooltip));
            tooltip.add(getNbtPredicateTooltip(utils, "ali.property.value.nbt", itemPredicate.nbt));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getEnchantmentPredicateTooltip(IServerUtils utils, String key, EnchantmentPredicate enchantmentPredicate) {
        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            ITooltipNode tooltip = getOptionalTooltip(utils, key, enchantmentPredicate.enchantment, RegistriesTooltipUtils::getEnchantmentTooltip);

            tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", enchantmentPredicate.level));

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, String key, EntitySubPredicate entitySubPredicate) {
        if (entitySubPredicate != EntitySubPredicate.ANY) {

            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            return optional.map((entry) -> {
                ITooltipNode tooltip = new TooltipNode(translatable(key, entry.getKey()));

                if (entitySubPredicate instanceof LighthingBoltPredicate boltPredicate) {
                    tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.blocks_on_fire", boltPredicate.blocksSetOnFire));
                    tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.stuck_entity", boltPredicate.entityStruck));
                } else if (entitySubPredicate instanceof FishingHookPredicate fishingHookPredicate) {
                    tooltip.add(getBooleanTooltip(utils, "ali.property.value.in_open_water", fishingHookPredicate.inOpenWater));
                } else if (entitySubPredicate instanceof PlayerPredicate playerPredicate) {
                    tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", playerPredicate.level));
                    tooltip.add(getOptionalTooltip(utils, "ali.property.value.game_type", playerPredicate.gameType, GenericTooltipUtils::getEnumTooltip));
                    tooltip.add(getStatsTooltip(utils, "ali.property.branch.stats", playerPredicate.stats));
                    tooltip.add(getMapTooltip(utils, "ali.property.branch.recipes", playerPredicate.recipes, GenericTooltipUtils::getRecipeEntryTooltip));
                    tooltip.add(getMapTooltip(utils, "ali.property.branch.advancements", playerPredicate.advancements, GenericTooltipUtils::getAdvancementEntryTooltip));
                    tooltip.add(getEntityPredicateTooltip(utils, "ali.property.branch.looking_at", playerPredicate.lookingAt));
                } else if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                    tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.size", slimePredicate.size));
                } else {
                    JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                    if (jsonObject.has("variant")) {
                        tooltip.add(getStringTooltip(utils, "ali.property.value.variant", jsonObject.getAsJsonPrimitive("variant").getAsString()));
                    } else {
                        tooltip.add(getStringTooltip(utils, "ali.property.value.variant", jsonObject.getAsString()));
                    }
                }

                return tooltip;
            }).orElse(TooltipNode.EMPTY);
        }

        return TooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getStatsTooltip(IServerUtils utils, String key, Map<Stat<?>, MinMaxBounds.Ints> statIntsMap) {
        if (!statIntsMap.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            statIntsMap.forEach((stat, ints) -> {
                Object value = stat.getValue();

                if (value instanceof Item item) {
                    ITooltipNode itemTooltip = getItemTooltip(utils, "ali.property.value.item", item);

                    itemTooltip.add(new TooltipNode(keyValue(stat.getType().getDisplayName(), toString(ints))));
                    tooltip.add(itemTooltip);
                } else if (value instanceof Block block) {
                    ITooltipNode blockTooltip = getBlockTooltip(utils, "ali.property.value.block", block);

                    blockTooltip.add(new TooltipNode(keyValue(stat.getType().getDisplayName(), toString(ints))));
                    tooltip.add(blockTooltip);
                } else if (value instanceof EntityType<?> entityType) {
                    ITooltipNode entityTooltip = getEntityTypeTooltip(utils, "ali.property.value.entity_type", entityType);

                    entityTooltip.add(new TooltipNode(keyValue(stat.getType().getDisplayName(), toString(ints))));
                    tooltip.add(entityTooltip);
                } else if (value instanceof ResourceLocation resourceLocation) {
                    ITooltipNode locationTooltip = getResourceLocationTooltip(utils, "ali.property.value.id", resourceLocation);

                    locationTooltip.add(new TooltipNode(keyValue(translatable(getTranslationKey(resourceLocation)), toString(ints))));
                    tooltip.add(locationTooltip);
                }
            });

            return tooltip;
        }

        return TooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getBlockPosTooltip(IServerUtils ignoredUtils, String key, BlockPos pos) {
        return new TooltipNode(translatable(key, value(pos.getX()), value(pos.getY()), value(pos.getZ())));
    }

    @NotNull
    public static ITooltipNode getCopyOperationTooltip(IServerUtils utils, String key, CopyNbtFunction.CopyOperation copyOperation) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getStringTooltip(utils, "ali.property.value.source", copyOperation.sourcePathText));
        tooltip.add(getStringTooltip(utils, "ali.property.value.target", copyOperation.targetPathText));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.merge_strategy", copyOperation.op));

        return tooltip;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCompoundTagTooltip(IServerUtils ignoredUtils, String key, CompoundTag tag) {
        return new TooltipNode(translatable(key, value(tag.toString())));
    }

    @NotNull
    public static ITooltipNode getAdvancementPredicateTooltip(IServerUtils utils, String key, PlayerPredicate.AdvancementPredicate predicate) {
        if (predicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
            return new TooltipNode(translatable(key, donePredicate.state));
        } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
            return getMapTooltip(utils, criterionsPredicate.criterions, GenericTooltipUtils::getCriterionEntryTooltip);
        }

        return TooltipNode.EMPTY;
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
    public static <T> ITooltipNode getOptionalTooltip(IServerUtils utils, String key, @Nullable T optional, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        if (optional != null) {
            return mapper.apply(utils, key, optional);
        } else {
            return TooltipNode.EMPTY;
        }
    }

    @NotNull
    public static <T> ITooltipNode getHolderTooltip(IServerUtils utils, String key, Holder<T> holder, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        return mapper.apply(utils, key, holder.value());
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
    public static <T> ITooltipNode getOptionalCollectionTooltip(IServerUtils utils, String key, String value, @Nullable Collection<T> values, TriFunction<IServerUtils, String, T, ITooltipNode> mapper) {
        if (values != null && !values.isEmpty()) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            values.forEach((v) -> tooltip.add(mapper.apply(utils, value, v)));

            return tooltip;
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

    // MAP ENTRY

    @Unmodifiable
    @NotNull
    public static ITooltipNode getRecipeEntryTooltip(IServerUtils ignoredUtils, Map.Entry<ResourceLocation, Boolean> entry) {
        return new TooltipNode(keyValue(entry.getKey(), entry.getValue()));
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCriterionEntryTooltip(IServerUtils ignoredUtils, Map.Entry<String, Boolean> entry) {
        return new TooltipNode(keyValue(entry.getKey(), entry.getValue()));
    }

    @NotNull
    public static ITooltipNode getIntRangeEntryTooltip(IServerUtils utils, Map.Entry<String, IntRange> entry) {
        ITooltipNode tooltip = new TooltipNode(value(entry.getKey()));

        tooltip.add(getIntRangeTooltip(utils, "ali.property.value.limit", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMobEffectPredicateEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry) {
        ITooltipNode tooltip = getMobEffectTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.amplifier", entry.getValue().amplifier));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.duration", entry.getValue().duration));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_ambient", entry.getValue().ambient, GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_visible", entry.getValue().visible, GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantmentLevelsEntryTooltip(IServerUtils utils, Map.Entry<Enchantment, NumberProvider> entry) {
        ITooltipNode tooltip = getEnchantmentTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.levels", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getMobEffectDurationEntryTooltip(IServerUtils utils, Map.Entry<MobEffect, NumberProvider> entry) {
        ITooltipNode tooltip = getMobEffectTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.duration", entry.getValue()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getAdvancementEntryTooltip(IServerUtils utils, Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry) {
        ITooltipNode tooltip = getResourceLocationTooltip(utils, "ali.property.value.null", entry.getKey());

        tooltip.add(getAdvancementPredicateTooltip(utils, "ali.property.value.done", entry.getValue()));

        return tooltip;
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
