package com.yanny.ali.plugin.server;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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
import java.util.function.BiFunction;

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

    @Unmodifiable
    @NotNull
    public static ITooltipNode getFormulaTooltip(IServerUtils utils, String key, ApplyBonusCount.Formula formula) {
        ITooltipNode tooltip = getResourceLocationTooltip(utils, key, formula.getType().id());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.extra_rounds", binomialWithBonusCount.extraRounds()));
            tooltip.add(getFloatTooltip(utils, "ali.property.value.probability", binomialWithBonusCount.probability()));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            tooltip.add(getIntegerTooltip(utils, "ali.property.value.bonus_multiplier", uniformBonusCount.bonusMultiplier()));
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

        tooltip.add(getStringTooltip(utils, "ali.property.value.name", modifier.name()));
        tooltip.add(getHolderTooltip(utils, "ali.property.value.attribute", modifier.attribute(), RegistriesTooltipUtils::getAttributeTooltip));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.operation", modifier.operation()));
        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.amount", modifier.amount()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.uuid", modifier.id(), GenericTooltipUtils::getUUIDTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.equipment_slots", "ali.property.value.null", modifier.slots(), GenericTooltipUtils::getEnumTooltip));

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
        return getCollectionTooltip(utils, key, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static ITooltipNode getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher matcher) {
            return new TooltipNode(keyValue(name, matcher.value()));
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher matcher) {
            Optional<String> min = matcher.minValue();
            Optional<String> max = matcher.maxValue();

            if (min.isPresent()) {
                if (max.isPresent()) {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_both", name, min.get(), max.get())));
                } else {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_gte", name, min.get())));
                }
            } else {
                if (max.isPresent()) {
                    return new TooltipNode(value(translatable("ali.property.value.ranged_property_lte", name, max.get())));
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
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.location", entityPredicate.location(), GenericTooltipUtils::getLocationPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.stepping_on_location", entityPredicate.steppingOnLocation(), GenericTooltipUtils::getLocationPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.mob_effects", entityPredicate.effects(), GenericTooltipUtils::getMobEffectPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.nbt", entityPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_flags", entityPredicate.flags(), GenericTooltipUtils::getEntityFlagsPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_equipment", entityPredicate.equipment(), GenericTooltipUtils::getEntityEquipmentPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.entity_sub_predicate", entityPredicate.subPredicate(), GenericTooltipUtils::getEntitySubPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.vehicle", entityPredicate.vehicle(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.passenger", entityPredicate.passenger(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.targeted_entity", entityPredicate.targetedEntity(), GenericTooltipUtils::getEntityPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.team", entityPredicate.team(), GenericTooltipUtils::getStringTooltip));

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
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.biome", locationPredicate.biome(), GenericTooltipUtils::getResourceKeyTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.structure", locationPredicate.structure(), GenericTooltipUtils::getResourceKeyTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.dimension", locationPredicate.dimension(), GenericTooltipUtils::getResourceKeyTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.smokey", locationPredicate.smokey(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.light", locationPredicate.light(), GenericTooltipUtils::getLightPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.block_predicate", locationPredicate.block(), GenericTooltipUtils::getBlockPredicateTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.branch.fluid_predicate", locationPredicate.fluid(), GenericTooltipUtils::getFluidPredicateTooltip));

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

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", blockPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
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

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", fluidPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        tooltip.add(getOptionalHolderTooltip(utils, "ali.property.value.fluid", fluidPredicate.fluid(), RegistriesTooltipUtils::getFluidTooltip));
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

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_on_fire", predicate.isOnFire(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_baby", predicate.isBaby(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_crouching", predicate.isCrouching(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_sprinting", predicate.isSprinting(), GenericTooltipUtils::getBooleanTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.is_swimming", predicate.isSwimming(), GenericTooltipUtils::getBooleanTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEntityEquipmentPredicateTooltip(IServerUtils utils, String key, EntityEquipmentPredicate predicate) {
            ITooltipNode tooltip = new TooltipNode(translatable(key));

            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.head", predicate.head(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.chest", predicate.chest(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.legs", predicate.legs(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.feet", predicate.feet(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.mainhand", predicate.mainhand(), GenericTooltipUtils::getItemPredicateTooltip));
            tooltip.add(getOptionalTooltip(utils, "ali.property.branch.offhand", predicate.offhand(), GenericTooltipUtils::getItemPredicateTooltip));

            return tooltip;
    }

    @NotNull
    public static ITooltipNode getItemPredicateTooltip(IServerUtils utils, String key, ItemPredicate itemPredicate) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", itemPredicate.tag(), GenericTooltipUtils::getTagKeyTooltip));
        tooltip.add(getOptionalHolderSetTooltip(utils, "ali.property.branch.items", "ali.property.value.null", itemPredicate.items(), RegistriesTooltipUtils::getItemTooltip));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.count", itemPredicate.count()));
        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.durability", itemPredicate.durability()));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.enchantments", "ali.property.value.null", itemPredicate.enchantments(), GenericTooltipUtils::getEnchantmentPredicateTooltip));
        tooltip.add(getCollectionTooltip(utils, "ali.property.branch.stored_enchantments", "ali.property.value.null", itemPredicate.storedEnchantments(), GenericTooltipUtils::getEnchantmentPredicateTooltip));
        tooltip.add(getOptionalHolderTooltip(utils, "ali.property.value.potion", itemPredicate.potion(), RegistriesTooltipUtils::getPotionTooltip));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.nbt", itemPredicate.nbt(), GenericTooltipUtils::getNbtPredicateTooltip));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEnchantmentPredicateTooltip(IServerUtils utils, String key, EnchantmentPredicate enchantmentPredicate) {
        ITooltipNode tooltip = getOptionalHolderTooltip(utils, key, enchantmentPredicate.enchantment(), RegistriesTooltipUtils::getEnchantmentTooltip);

        tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", enchantmentPredicate.level()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, String key, EntitySubPredicate entitySubPredicate) {
        Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

        return optional.map((entry) -> {
            ITooltipNode tooltip = new TooltipNode(translatable(key, entry.getKey()));

            if (entitySubPredicate instanceof LightningBoltPredicate predicate) {
                tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.blocks_on_fire", predicate.blocksSetOnFire()));
                tooltip.add(getOptionalTooltip(utils, "ali.property.branch.stuck_entity", predicate.entityStruck(), GenericTooltipUtils::getEntityPredicateTooltip));
            } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                tooltip.add(getOptionalTooltip(utils, "ali.property.value.in_open_water", predicate.inOpenWater(), GenericTooltipUtils::getBooleanTooltip));
            } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.level", predicate.level()));
                tooltip.add(getOptionalTooltip(utils, "ali.property.value.game_type", predicate.gameType(), GenericTooltipUtils::getEnumTooltip));
                tooltip.add(getCollectionTooltip(utils, "ali.property.branch.stats", predicate.stats(), GenericTooltipUtils::getStatMatcherTooltip));
                tooltip.add(getMapTooltip(utils, "ali.property.branch.recipes", predicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip));
                tooltip.add(getMapTooltip(utils, "ali.property.branch.advancements", predicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip));
                tooltip.add(getOptionalTooltip(utils, "ali.property.branch.looking_at", predicate.lookingAt(), GenericTooltipUtils::getEntityPredicateTooltip));
            } else if (entitySubPredicate instanceof SlimePredicate predicate) {
                tooltip.add(getMinMaxBoundsTooltip(utils, "ali.property.value.size", predicate.size()));
            } else {
                EntitySubPredicate.CODEC.encodeStart(JsonOps.INSTANCE, entitySubPredicate).result().ifPresent((element) -> {
                    JsonObject jsonObject = element.getAsJsonObject();

                    if (jsonObject.has("variant")) {
                        tooltip.add(new TooltipNode(translatable("ali.property.value.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                    } else {
                        tooltip.add(new TooltipNode(translatable("ali.property.value.variant", jsonObject.toString())));
                    }
                });
            }

            return tooltip;
        }).orElse(TooltipNode.EMPTY);
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
    public static ITooltipNode getCopyOperationTooltip(IServerUtils utils, String key, CopyNbtFunction.CopyOperation copyOperation) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getStringTooltip(utils, "ali.property.value.source", copyOperation.sourcePath().string()));
        tooltip.add(getStringTooltip(utils, "ali.property.value.target", copyOperation.targetPath().string()));
        tooltip.add(getEnumTooltip(utils, "ali.property.value.merge_strategy", copyOperation.op()));

        return tooltip;
    }

    @NotNull
    public static ITooltipNode getEffectEntryTooltip(IServerUtils utils, String key, SetStewEffectFunction.EffectEntry entry) {
        ITooltipNode tooltip = getHolderTooltip(utils, key, entry.effect(), RegistriesTooltipUtils::getMobEffectTooltip);

        tooltip.add(getNumberProviderTooltip(utils,"ali.property.value.duration", entry.duration()));

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
            return new TooltipNode(translatable(key, donePredicate.state()));
        } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
            return getMapTooltip(utils, criterionsPredicate.criterions(), GenericTooltipUtils::getCriterionEntryTooltip);
        }

        return TooltipNode.EMPTY;
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getItemStackTooltip(IServerUtils utils, String key, ItemStack item) {
        ITooltipNode tooltip = new TooltipNode(translatable(key));

        tooltip.add(getItemTooltip(utils, "ali.property.value.item", item.getItem()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.count", item.getCount()));
        tooltip.add(getOptionalTooltip(utils, "ali.property.value.tag", Optional.ofNullable(item.getTag()), GenericTooltipUtils::getCompoundTagTooltip));

        return tooltip;
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
