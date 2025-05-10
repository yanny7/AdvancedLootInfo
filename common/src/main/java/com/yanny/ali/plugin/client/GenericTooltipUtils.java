package com.yanny.ali.plugin.client;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.RangeValue;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.ali.plugin.client.RegistriesTooltipUtils.*;

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

            if (function instanceof LootItemConditionalFunction conditionalFunction && conditionalFunction.predicates.length > 0) {
                components.add(pad(pad + 1, translatable("ali.property.branch.conditions")));
                components.addAll(getConditionsTooltip(utils, pad + 2, Arrays.asList(conditionalFunction.predicates)));
            }

            return components;
        }).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Component> getFormulaTooltip(IClientUtils utils, int pad, String key, ApplyBonusCount.Formula formula) {
        List<Component> components = new LinkedList<>(getResourceLocationTooltip(utils, pad, key, formula.getType()));

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
    public static List<Component> getPropertyTooltip(IClientUtils utils, int pad, String key, Property<?> property) {
        return getStringTooltip(utils, pad, key, property.getName());
    }

    @NotNull
    public static List<Component> getModifierTooltip(IClientUtils utils, int pad, String key, SetAttributesFunction.Modifier modifier) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.name", modifier.name));
        components.addAll(getAttributeTooltip(utils, pad + 1, "ali.property.value.attribute", modifier.attribute));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.operation", modifier.operation));
        components.addAll(getNumberProviderTooltip(utils, pad + 1, "ali.property.value.amount", modifier.amount));
        components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.uuid", modifier.id, GenericTooltipUtils::getUUIDTooltip));
        components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.equipment_slots", List.of(modifier.slots), GenericTooltipUtils::getEnumTooltip));

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getUUIDTooltip(IClientUtils utils, int pad, String key, UUID uuid) {
        return List.of(pad(pad, translatable("ali.property.value.uuid", value(uuid))));
    }

    @NotNull
    public static List<Component> getBannerPatternsTooltip(IClientUtils utils, int pad, String key, Pair<Holder<BannerPattern>, DyeColor> pair) {
        List<Component> components = new LinkedList<>();

        components.addAll(getHolderTooltip(utils, pad, key, pair.getFirst(), RegistriesTooltipUtils::getBannerPatternTooltip));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.color", pair.getSecond()));

        return components;
    }

    @NotNull
    public static List<Component> getStatePropertiesPredicateTooltip(IClientUtils utils, int pad, String key, StatePropertiesPredicate propertiesPredicate) {
        return getCollectionTooltip(utils, pad, key, propertiesPredicate.properties, GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static List<Component> getPropertyMatcherTooltip(IClientUtils ignoredUtils, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
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
    public static List<Component> getDamageSourcePredicateTooltip(IClientUtils utils, int pad, String key, DamageSourcePredicate damagePredicate) {
        List<Component> components = new LinkedList<>();

        if (damagePredicate != DamageSourcePredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.tags", damagePredicate.tags, GenericTooltipUtils::getTagPredicateTooltip));
            components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.direct_entity", damagePredicate.directEntity));
            components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.source_entity", damagePredicate.sourceEntity));
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static <T> List<Component> getTagPredicateTooltip(IClientUtils utils, int pad, TagPredicate<T> tagPredicate) {
        return List.of(pad(pad, keyValue(tagPredicate.tag.location().toString(), tagPredicate.expected)));
    }

    @NotNull
    public static List<Component> getEntityPredicateTooltip(IClientUtils utils, int pad, String key, EntityPredicate entityPredicate) {
        List<Component> components = new LinkedList<>();

        if (entityPredicate != EntityPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getEntityTypePredicateTooltip(utils, pad + 1, "ali.property.value.entity_type", entityPredicate.entityType));
            components.addAll(getDistancePredicateTooltip(utils, pad + 1, "ali.property.branch.distance_to_player", entityPredicate.distanceToPlayer));
            components.addAll(getLocationPredicateTooltip(utils, pad + 1, "ali.property.branch.location", entityPredicate.location));
            components.addAll(getLocationPredicateTooltip(utils, pad + 1, "ali.property.branch.stepping_on_location", entityPredicate.steppingOnLocation));
            components.addAll(getMobEffectPredicateTooltip(utils, pad + 1, "ali.property.branch.mob_effects", entityPredicate.effects));
            components.addAll(getNbtPredicateTooltip(utils, pad + 1, "ali.property.value.nbt", entityPredicate.nbt));
            components.addAll(getEntityFlagsPredicateTooltip(utils, pad + 1, "ali.property.branch.entity_flags", entityPredicate.flags));
            components.addAll(getEntityEquipmentPredicateTooltip(utils, pad + 1, "ali.property.branch.entity_equipment", entityPredicate.equipment));
            components.addAll(getEntitySubPredicateTooltip(utils, pad + 1, "ali.property.branch.entity_sub_predicate", entityPredicate.subPredicate));
            components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.vehicle", entityPredicate.vehicle));
            components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.passenger", entityPredicate.passenger));
            components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.team", entityPredicate.team, GenericTooltipUtils::getStringTooltip));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityTypePredicateTooltip(IClientUtils utils, int pad, String key, EntityTypePredicate entityTypePredicate) {
        List<Component> components = new LinkedList<>();

        if (entityTypePredicate != EntityTypePredicate.ANY) {
            if (entityTypePredicate instanceof EntityTypePredicate.TypePredicate typePredicate) {
                components.addAll(getEntityTypeTooltip(utils, pad, key, typePredicate.type));
            }
            if (entityTypePredicate instanceof EntityTypePredicate.TagPredicate tagPredicate) {
                components.addAll(getTagKeyTooltip(utils, pad, key, tagPredicate.tag));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getDistancePredicateTooltip(IClientUtils utils, int pad, String key, DistancePredicate distancePredicate) {
        List<Component> components = new LinkedList<>();

        if (distancePredicate != DistancePredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", distancePredicate.x));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", distancePredicate.y));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", distancePredicate.z));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.horizontal", distancePredicate.horizontal));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.absolute", distancePredicate.absolute));
        }

        return components;
    }

    @NotNull
    public static List<Component> getLocationPredicateTooltip(IClientUtils utils, int pad, String key, LocationPredicate locationPredicate) {
        List<Component> components = new LinkedList<>();

        if (locationPredicate != LocationPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.x", locationPredicate.x));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.y", locationPredicate.y));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.z", locationPredicate.z));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.biome", locationPredicate.biome, GenericTooltipUtils::getResourceKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.structure", locationPredicate.structure, GenericTooltipUtils::getResourceKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.dimension", locationPredicate.dimension, GenericTooltipUtils::getResourceKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.smokey", locationPredicate.smokey, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getLightPredicateTooltip(utils, pad + 1, "ali.property.value.light", locationPredicate.light));
            components.addAll(getBlockPredicateTooltip(utils, pad + 1, "ali.property.branch.block_predicate", locationPredicate.block));
            components.addAll(getFluidPredicateTooltip(utils, pad + 1, "ali.property.branch.fluid_predicate", locationPredicate.fluid));
        }

        return components;
    }

    @NotNull
    public static List<Component> getLightPredicateTooltip(IClientUtils utils, int pad, String key, LightPredicate lightPredicate) {
        List<Component> components = new LinkedList<>();

        if (lightPredicate != LightPredicate.ANY) {
            components.addAll(getMinMaxBoundsTooltip(utils, pad, key, lightPredicate.composite));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBlockPredicateTooltip(IClientUtils utils, int pad, String key, BlockPredicate blockPredicate) {
        List<Component> components = new LinkedList<>();

        if (blockPredicate != BlockPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", blockPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));

            if (blockPredicate.blocks != null) {
                components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.blocks", "ali.property.value.null", blockPredicate.blocks, RegistriesTooltipUtils::getBlockTooltip));
            }

            components.addAll(getStatePropertiesPredicateTooltip(utils, pad + 1, "ali.property.branch.properties", blockPredicate.properties));
            components.addAll(getNbtPredicateTooltip(utils, pad + 1, "ali.property.value.nbt", blockPredicate.nbt));
        }

        return components;
    }

    @NotNull
    public static List<Component> getNbtPredicateTooltip(IClientUtils ignoredUtils, int pad, String key, NbtPredicate nbtPredicate) {
        List<Component> components = new LinkedList<>();

        if (nbtPredicate != NbtPredicate.ANY) {
            if (nbtPredicate.tag != null) {
                components.add(pad(pad, translatable(key, value(nbtPredicate.tag))));
            }
        }

        return components;
    }

    @NotNull
    public static List<Component> getFluidPredicateTooltip(IClientUtils utils, int pad, String key, FluidPredicate fluidPredicate) {
        List<Component> components = new LinkedList<>();

        if (fluidPredicate != FluidPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", fluidPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.fluid", fluidPredicate.fluid, RegistriesTooltipUtils::getFluidTooltip));
            components.addAll(getStatePropertiesPredicateTooltip(utils, pad + 1, "ali.property.branch.properties", fluidPredicate.properties));
        }

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectPredicateTooltip(IClientUtils utils, int pad, String key, MobEffectsPredicate mobEffectsPredicate) {
        List<Component> components = new LinkedList<>();

        if (mobEffectsPredicate != MobEffectsPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            mobEffectsPredicate.effects.forEach((effect, instancePredicate) -> {
                components.addAll(getMobEffectTooltip(utils, pad + 1, "ali.property.value.null", effect));
                components.addAll(getMobEffectInstancePredicateTooltip(utils, pad + 2, instancePredicate));
            });
        }

        return components;
    }

    @NotNull
    public static List<Component> getMobEffectInstancePredicateTooltip(IClientUtils utils, int pad, MobEffectsPredicate.MobEffectInstancePredicate predicate) {
        List<Component> components = new LinkedList<>();

        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.amplifier", predicate.amplifier));
        components.addAll(getMinMaxBoundsTooltip(utils, pad, "ali.property.value.duration", predicate.duration));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_ambient", predicate.ambient, GenericTooltipUtils::getBooleanTooltip));
        components.addAll(getOptionalTooltip(utils, pad, "ali.property.value.is_visible", predicate.visible, GenericTooltipUtils::getBooleanTooltip));

        return components;
    }

    @NotNull
    public static List<Component> getEntityFlagsPredicateTooltip(IClientUtils utils, int pad, String key, EntityFlagsPredicate predicate) {
        List<Component> components = new LinkedList<>();

        if (predicate != EntityFlagsPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_on_fire", predicate.isOnFire, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_baby", predicate.isBaby, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_crouching", predicate.isCrouching, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_sprinting", predicate.isSprinting, GenericTooltipUtils::getBooleanTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.is_swimming", predicate.isSwimming, GenericTooltipUtils::getBooleanTooltip));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntityEquipmentPredicateTooltip(IClientUtils utils, int pad, String key, EntityEquipmentPredicate predicate) {
        List<Component> components = new LinkedList<>();

        if (predicate != EntityEquipmentPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.head", predicate.head));
            components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.chest", predicate.chest));
            components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.legs", predicate.legs));
            components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.feet", predicate.feet));
            components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.mainhand", predicate.mainhand));
            components.addAll(getItemPredicateTooltip(utils, pad + 1, "ali.property.branch.offhand", predicate.offhand));
        }

        return components;
    }

    @NotNull
    public static List<Component> getItemPredicateTooltip(IClientUtils utils, int pad, String key, ItemPredicate itemPredicate) {
        List<Component> components = new LinkedList<>();

        if (itemPredicate != ItemPredicate.ANY) {
            components.add(pad(pad, translatable(key)));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.tag", itemPredicate.tag, GenericTooltipUtils::getTagKeyTooltip));

            if (itemPredicate.items != null) {
                components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.items", "ali.property.value.null", itemPredicate.items, RegistriesTooltipUtils::getItemTooltip));
            }

            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.count", itemPredicate.count));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.durability", itemPredicate.durability));
            components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.enchantments", "ali.property.value.null", List.of(itemPredicate.enchantments), GenericTooltipUtils::getEnchantmentPredicateTooltip));
            components.addAll(getCollectionTooltip(utils, pad + 1, "ali.property.branch.stored_enchantments", "ali.property.value.null", List.of(itemPredicate.storedEnchantments), GenericTooltipUtils::getEnchantmentPredicateTooltip));
            components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.potion", itemPredicate.potion, RegistriesTooltipUtils::getPotionTooltip));
            components.addAll(getNbtPredicateTooltip(utils, pad + 1, "ali.property.value.nbt", itemPredicate.nbt));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEnchantmentPredicateTooltip(IClientUtils utils, int pad, String key, EnchantmentPredicate enchantmentPredicate) {
        List<Component> components = new LinkedList<>();

        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            components.addAll(getOptionalTooltip(utils, pad, key, enchantmentPredicate.enchantment, RegistriesTooltipUtils::getEnchantmentTooltip));
            components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", enchantmentPredicate.level));
        }

        return components;
    }

    @NotNull
    public static List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, String key, EntitySubPredicate entitySubPredicate) {
        List<Component> components = new LinkedList<>();

        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable(key, entry.getKey())));

                if (entitySubPredicate instanceof LighthingBoltPredicate boltPredicate) {
                    components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.blocks_on_fire", boltPredicate.blocksSetOnFire));
                    components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.stuck_entity", boltPredicate.entityStruck));
                } else if (entitySubPredicate instanceof FishingHookPredicate fishingHookPredicate) {
                    components.addAll(getBooleanTooltip(utils, pad + 1, "ali.property.value.in_open_water", fishingHookPredicate.inOpenWater));
                } else if (entitySubPredicate instanceof PlayerPredicate playerPredicate) {
                    components.addAll(getMinMaxBoundsTooltip(utils, pad + 1, "ali.property.value.level", playerPredicate.level));
                    components.addAll(getOptionalTooltip(utils, pad + 1, "ali.property.value.game_type", playerPredicate.gameType, GenericTooltipUtils::getEnumTooltip));
                    components.addAll(getStatsTooltip(utils, pad + 1, "ali.property.branch.stats", playerPredicate.stats));
                    components.addAll(getRecipesTooltip(utils, pad + 1, "ali.property.branch.recipes", playerPredicate.recipes));
                    components.addAll(getAdvancementsTooltip(utils, pad + 1, "ali.property.branch.advancements", playerPredicate.advancements));
                    components.addAll(getEntityPredicateTooltip(utils, pad + 1, "ali.property.branch.looking_at", playerPredicate.lookingAt));
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

    @NotNull
    public static List<Component> getStatsTooltip(IClientUtils utils, int pad, String key, Map<Stat<?>, MinMaxBounds.Ints> statIntsMap) {
        List<Component> components = new LinkedList<>();

        if (!statIntsMap.isEmpty()) {
            components.add(pad(pad, translatable(key)));
            statIntsMap.forEach((stat, ints) -> {
                Object value = stat.getValue();

                if (value instanceof Item item) {
                    components.addAll(getItemTooltip(utils, pad + 1, "ali.property.value.item", item));
                    components.add(pad(pad + 2, keyValue(stat.getType().getDisplayName(), toString(ints))));
                } else if (value instanceof Block block) {
                    components.addAll(getBlockTooltip(utils, pad + 1, "ali.property.value.block", block));
                    components.add(pad(pad + 2, keyValue(stat.getType().getDisplayName(), toString(ints))));
                } else if (value instanceof EntityType<?> entityType) {
                    components.addAll(getEntityTypeTooltip(utils, pad + 1, "ali.property.value.entity_type", entityType));
                    components.add(pad(pad + 2, keyValue(stat.getType().getDisplayName(), toString(ints))));
                } else if (value instanceof ResourceLocation resourceLocation) {
                    components.addAll(getResourceLocationTooltip(utils, pad + 1, "ali.property.value.id", resourceLocation));
                    components.add(pad(pad + 2, keyValue(translatable(getTranslationKey(resourceLocation)), toString(ints))));
                }

            });
        }

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
                    components.add(pad(pad + 2, translatable("ali.property.value.done", donePredicate.state)));
                } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                    criterionsPredicate.criterions.forEach((criterion, state) -> components.add(pad(pad + 2, keyValue(criterion, state))));
                }
            });
        }

        return components;
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getBlockPosTooltip(IClientUtils ignoredUtils, int pad, String key, BlockPos pos) {
        return List.of(pad(pad, translatable(key, value(pos.getX()), value(pos.getY()), value(pos.getZ()))));
    }

    @NotNull
    public static List<Component> getCopyOperationTooltip(IClientUtils utils, int pad, String key, CopyNbtFunction.CopyOperation copyOperation) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable(key)));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.source", copyOperation.sourcePathText));
        components.addAll(getStringTooltip(utils, pad + 1, "ali.property.value.target", copyOperation.targetPathText));
        components.addAll(getEnumTooltip(utils, pad + 1, "ali.property.value.merge_strategy", copyOperation.op));

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
    public static <T> List<Component> getOptionalTooltip(IClientUtils utils, int pad, String key, @Nullable T optional, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        if (optional != null) {
            return mapper.apply(utils, pad, key, optional);
        } else {
            return List.of();
        }
    }

    @NotNull
    public static <T> List<Component> getHolderTooltip(IClientUtils utils, int pad, String key, Holder<T> holder, QuadFunction<IClientUtils, Integer, String, T, List<Component>> mapper) {
        return mapper.apply(utils, pad, key, holder.value());
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

    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }
}
