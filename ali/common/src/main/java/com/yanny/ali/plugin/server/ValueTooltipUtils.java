package com.yanny.ali.plugin.server;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class ValueTooltipUtils {
    @NotNull
    public static TooltipBuilder getConditionTooltip(IServerUtils utils, LootItemCondition condition) {
        return utils.getConditionTooltip(utils, condition);
    }

    @NotNull
    public static TooltipBuilder getFunctionTooltip(IServerUtils utils, LootItemFunction function) {
        return utils.getFunctionTooltip(utils, function);
    }

    @NotNull
    public static TooltipBuilder getIngredientTooltip(IServerUtils utils, Ingredient ingredient) {
        return utils.getIngredientTooltip(utils, ingredient);
    }

    @NotNull
    public static TooltipBuilder getFormulaTooltip(IServerUtils utils, ApplyBonusCount.Formula formula) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, formula.getType());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            tooltip.add(utils.getValueTooltip(utils, binomialWithBonusCount.extraRounds).build("ali.property.value.extra_rounds"));
            tooltip.add(utils.getValueTooltip(utils, binomialWithBonusCount.probability).build("ali.property.value.probability"));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            tooltip.add(utils.getValueTooltip(utils, uniformBonusCount.bonusMultiplier).build("ali.property.value.bonus_multiplier"));
        }

        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getModifierTooltip(IServerUtils utils, SetAttributesFunction.Modifier modifier) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, modifier.name).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, modifier.attribute).build("ali.property.value.attribute"))
                .add(utils.getValueTooltip(utils, modifier.operation).build("ali.property.value.operation"))
                .add(utils.getValueTooltip(utils, modifier.amount).build("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, modifier.id).build("ali.property.value.uuid"))
                .add(utils.getValueTooltip(utils, modifier.slots).build(TooltipBuilder.multi("ali.property.value.equipment_slot", "ali.property.branch.equipment_slots")))
        ).key("ali.property.branch.modifier");
    }

    @NotNull
    public static TooltipBuilder getPairTooltip(IServerUtils utils, Pair<?, ?> pair) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, pair.getFirst());

        tooltip.add(utils.getValueTooltip(utils, pair.getSecond()).build("ali.property.value.color"));
        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getStatePropertiesPredicateTooltip(IServerUtils utils, StatePropertiesPredicate propertiesPredicate) {
        return utils.getValueTooltip(utils, propertiesPredicate.properties);
    }

    @NotNull
    public static TooltipBuilder getDamageSourcePredicateTooltip(IServerUtils utils, DamageSourcePredicate damagePredicate) {
        if (damagePredicate != DamageSourcePredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, damagePredicate.tags).build("ali.property.branch.tags"))
                    .add(utils.getValueTooltip(utils, damagePredicate.directEntity).build("ali.property.branch.direct_entity"))
                    .add(utils.getValueTooltip(utils, damagePredicate.sourceEntity).build("ali.property.branch.source_entity"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static <T> TooltipBuilder getTagPredicateTooltip(IServerUtils ignoredUtils, TagPredicate<T> tagPredicate) {
        return TooltipBuilder.keyValue(tagPredicate.tag.location().toString(), Boolean.toString(tagPredicate.expected));
    }

    @NotNull
    public static TooltipBuilder getEntityPredicateTooltip(IServerUtils utils, EntityPredicate entityPredicate) {
        if (entityPredicate != EntityPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, entityPredicate.entityType).build("ali.property.value.entity_type"))
                    .add(utils.getValueTooltip(utils, entityPredicate.distanceToPlayer).build("ali.property.branch.distance_to_player"))
                    .add(utils.getValueTooltip(utils, entityPredicate.location).build("ali.property.branch.location"))
                    .add(utils.getValueTooltip(utils, entityPredicate.steppingOnLocation).build("ali.property.branch.stepping_on_location"))
                    .add(utils.getValueTooltip(utils, entityPredicate.effects).build(TooltipBuilder.multi("ali.property.value.mob_effect", "ali.property.branch.mob_effects")))
                    .add(utils.getValueTooltip(utils, entityPredicate.nbt).build("ali.property.value.nbt"))
                    .add(utils.getValueTooltip(utils, entityPredicate.flags).build("ali.property.branch.entity_flags"))
                    .add(utils.getValueTooltip(utils, entityPredicate.equipment).build("ali.property.branch.entity_equipment"))
                    .add(utils.getValueTooltip(utils, entityPredicate.subPredicate).build("ali.property.branch.entity_sub_predicate"))
                    .add(utils.getValueTooltip(utils, entityPredicate.vehicle).build("ali.property.branch.vehicle"))
                    .add(utils.getValueTooltip(utils, entityPredicate.passenger).build("ali.property.branch.passenger"))
                    .add(utils.getValueTooltip(utils, entityPredicate.targetedEntity).build("ali.property.branch.targeted_entity"))
                    .add(utils.getValueTooltip(utils, entityPredicate.team).build("ali.property.value.team"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEntityTypePredicateTooltip(IServerUtils utils, EntityTypePredicate entityTypePredicate) {
        if (entityTypePredicate != EntityTypePredicate.ANY) {
            if (entityTypePredicate instanceof EntityTypePredicate.TypePredicate typePredicate) {
                return utils.getValueTooltip(utils, typePredicate.type);
            }
            if (entityTypePredicate instanceof EntityTypePredicate.TagPredicate tagPredicate) {
                return utils.getValueTooltip(utils, tagPredicate.tag);
            }
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getDistancePredicateTooltip(IServerUtils utils, DistancePredicate distancePredicate) {
        if (distancePredicate != DistancePredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, distancePredicate.x).build("ali.property.value.x"))
                    .add(utils.getValueTooltip(utils, distancePredicate.y).build("ali.property.value.y"))
                    .add(utils.getValueTooltip(utils, distancePredicate.z).build("ali.property.value.z"))
                    .add(utils.getValueTooltip(utils, distancePredicate.horizontal).build("ali.property.value.horizontal"))
                    .add(utils.getValueTooltip(utils, distancePredicate.absolute).build("ali.property.value.absolute"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getLocationPredicateTooltip(IServerUtils utils, LocationPredicate locationPredicate) {
        if (locationPredicate != LocationPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, locationPredicate.x).build("ali.property.value.x"))
                    .add(utils.getValueTooltip(utils, locationPredicate.y).build("ali.property.value.y"))
                    .add(utils.getValueTooltip(utils, locationPredicate.z).build("ali.property.value.z"))
                    .add(utils.getValueTooltip(utils, locationPredicate.biome).build("ali.property.value.biome"))
                    .add(utils.getValueTooltip(utils, locationPredicate.structure).build("ali.property.value.structure"))
                    .add(utils.getValueTooltip(utils, locationPredicate.dimension).build("ali.property.value.dimension"))
                    .add(utils.getValueTooltip(utils, locationPredicate.smokey).build("ali.property.value.smokey"))
                    .add(utils.getValueTooltip(utils, locationPredicate.light).build("ali.property.value.light"))
                    .add(utils.getValueTooltip(utils, locationPredicate.block).build("ali.property.branch.block_predicate"))
                    .add(utils.getValueTooltip(utils, locationPredicate.fluid).build("ali.property.branch.fluid_predicate"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getLightPredicateTooltip(IServerUtils utils, LightPredicate lightPredicate) {
        if (lightPredicate != LightPredicate.ANY) {
            return utils.getValueTooltip(utils, lightPredicate.composite);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicate blockPredicate) {
        if (blockPredicate != BlockPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, blockPredicate.tag).build("ali.property.value.tag"))
                    .add(utils.getValueTooltip(utils, blockPredicate.blocks).build(TooltipBuilder.multi("ali.property.value.block", "ali.property.branch.blocks")))
                    .add(utils.getValueTooltip(utils, blockPredicate.properties).build("ali.property.branch.properties"))
                    .add(utils.getValueTooltip(utils, blockPredicate.nbt).build("ali.property.value.nbt"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getNbtPredicateTooltip(IServerUtils utils, NbtPredicate nbtPredicate) {
        if (nbtPredicate != NbtPredicate.ANY) {
            return utils.getValueTooltip(utils, nbtPredicate.tag);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getFluidPredicateTooltip(IServerUtils utils, FluidPredicate fluidPredicate) {
        if (fluidPredicate != FluidPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, fluidPredicate.tag).build("ali.property.value.tag"))
                    .add(utils.getValueTooltip(utils, fluidPredicate.fluid).build("ali.property.value.fluid"))
                    .add(utils.getValueTooltip(utils, fluidPredicate.properties).build("ali.property.branch.properties"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateTooltip(IServerUtils utils, MobEffectsPredicate mobEffectsPredicate) {
        if (mobEffectsPredicate != MobEffectsPredicate.ANY) {
            return getMapTooltip(utils, mobEffectsPredicate.effects, GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEntityFlagsPredicateTooltip(IServerUtils utils, EntityFlagsPredicate predicate) {
        if (predicate != EntityFlagsPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, predicate.isOnFire).build("ali.property.value.is_on_fire"))
                    .add(utils.getValueTooltip(utils, predicate.isBaby).build("ali.property.value.is_baby"))
                    .add(utils.getValueTooltip(utils, predicate.isCrouching).build("ali.property.value.is_crouching"))
                    .add(utils.getValueTooltip(utils, predicate.isSprinting).build("ali.property.value.is_sprinting"))
                    .add(utils.getValueTooltip(utils, predicate.isSwimming).build("ali.property.value.is_swimming"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEntityEquipmentPredicateTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        if (predicate != EntityEquipmentPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, predicate.head).build("ali.property.branch.head"))
                    .add(utils.getValueTooltip(utils, predicate.chest).build("ali.property.branch.chest"))
                    .add(utils.getValueTooltip(utils, predicate.legs).build("ali.property.branch.legs"))
                    .add(utils.getValueTooltip(utils, predicate.feet).build("ali.property.branch.feet"))
                    .add(utils.getValueTooltip(utils, predicate.mainhand).build("ali.property.branch.mainhand"))
                    .add(utils.getValueTooltip(utils, predicate.offhand).build("ali.property.branch.offhand"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getItemPredicateTooltip(IServerUtils utils, ItemPredicate itemPredicate) {
        if (itemPredicate != ItemPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, itemPredicate.tag).build("ali.property.value.tag"))
                    .add(utils.getValueTooltip(utils, itemPredicate.items).build(TooltipBuilder.multi("ali.property.value.item", "ali.property.branch.items")))
                    .add(utils.getValueTooltip(utils, itemPredicate.count).build("ali.property.value.count"))
                    .add(utils.getValueTooltip(utils, itemPredicate.durability).build("ali.property.value.durability"))
                    .add(utils.getValueTooltip(utils, itemPredicate.enchantments).build(TooltipBuilder.multi("ali.property.value.enchantment", "ali.property.branch.enchantments")))
                    .add(utils.getValueTooltip(utils, itemPredicate.storedEnchantments).build("ali.property.branch.stored_enchantments"))
                    .add(utils.getValueTooltip(utils, itemPredicate.potion).build("ali.property.value.potion"))
                    .add(utils.getValueTooltip(utils, itemPredicate.nbt).build("ali.property.value.nbt"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEnchantmentPredicateTooltip(IServerUtils utils, EnchantmentPredicate enchantmentPredicate) {
        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            if (enchantmentPredicate.enchantment != null) {
                TooltipBuilder builder = utils.getValueTooltip(utils, enchantmentPredicate.enchantment);

                if (enchantmentPredicate.level != MinMaxBounds.Ints.ANY) {
                    builder.add(utils.getValueTooltip(utils, enchantmentPredicate.level).build("ali.property.value.level"));
                }

                return builder;
            } else {
                return TooltipBuilder.keyOnly("ali.property.branch.enchantments")
                        .add(utils.getValueTooltip(utils, enchantmentPredicate.level).build("ali.property.value.level"));
            }
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate entitySubPredicate) {
        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            return optional.map((entry) -> {
                return TooltipBuilder.array((b) -> {
                    if (entitySubPredicate instanceof LighthingBoltPredicate boltPredicate) {
                        b.add(utils.getValueTooltip(utils, boltPredicate.blocksSetOnFire).build("ali.property.value.blocks_on_fire"));
                        b.add(utils.getValueTooltip(utils, boltPredicate.entityStruck).build("ali.property.branch.stuck_entity"));
                    } else if (entitySubPredicate instanceof FishingHookPredicate fishingHookPredicate) {
                        b.add(utils.getValueTooltip(utils, fishingHookPredicate.inOpenWater).build("ali.property.value.in_open_water"));
                    } else if (entitySubPredicate instanceof PlayerPredicate playerPredicate) {
                        b.add(utils.getValueTooltip(utils, playerPredicate.level).build("ali.property.value.level"));
                        b.add(utils.getValueTooltip(utils, playerPredicate.gameType).build("ali.property.value.game_type"));
                        b.add(getMapTooltip(utils, playerPredicate.stats, GenericTooltipUtils::getStatsEntryTooltip).build("ali.property.branch.stats"));
                        b.add(getMapTooltip(utils, playerPredicate.recipes, GenericTooltipUtils::getRecipeEntryTooltip).build("ali.property.branch.recipes"));
                        b.add(getMapTooltip(utils, playerPredicate.advancements, GenericTooltipUtils::getAdvancementEntryTooltip).build("ali.property.branch.advancements"));
                        b.add(utils.getValueTooltip(utils, playerPredicate.lookingAt).build("ali.property.branch.looking_at"));
                    } else if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                        b.add(utils.getValueTooltip(utils, slimePredicate.size).build("ali.property.value.size"));
                    } else {
                        JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                        if (jsonObject.has("variant")) {
                            b.add(utils.getValueTooltip(utils, jsonObject.getAsJsonPrimitive("variant").getAsString()).build("ali.property.value.variant"));
                        } else {
                            b.add(utils.getValueTooltip(utils, jsonObject.getAsString()).build("ali.property.value.variant"));
                        }
                    }
                });
            }).orElse(TooltipBuilder.empty());
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getBlockPosTooltip(IServerUtils ignoredUtils, BlockPos pos) {
        return TooltipBuilder.value(pos.getX(), pos.getY(), pos.getZ());
    }

    @NotNull
    public static TooltipBuilder getCopyOperationTooltip(IServerUtils utils, CopyNbtFunction.CopyOperation copyOperation) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, copyOperation.sourcePathText).build("ali.property.value.source"))
                .add(utils.getValueTooltip(utils, copyOperation.targetPathText).build("ali.property.value.target"))
                .add(utils.getValueTooltip(utils, copyOperation.op).build("ali.property.value.merge_strategy"))
        ).key("ali.property.branch.operation");
    }

    @NotNull
    public static TooltipBuilder getAdvancementDonePredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementDonePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.state).key("ali.property.value.done");
    }

    @NotNull
    public static TooltipBuilder getAdvancementCriterionsPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementCriterionsPredicate predicate) {
        return getMapTooltip(utils, predicate.criterions, GenericTooltipUtils::getCriterionEntryTooltip).key("ali.property.branch.criterions");
    }

    @NotNull
    public static TooltipBuilder getItemStackTooltip(IServerUtils utils, ItemStack item) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, item.getItem()).build("ali.property.value.item"))
                .add(utils.getValueTooltip(utils, item.getCount()).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, item.getTag()).build("ali.property.value.tag"))
        );
    }

    @NotNull
    public static TooltipBuilder getNumberProviderTooltip(IServerUtils utils, NumberProvider value) {
        return TooltipBuilder.value(utils.convertNumber(utils, value));
    }

    @NotNull
    public static TooltipBuilder getIntRangeTooltip(IServerUtils utils, IntRange range) {
        return TooltipBuilder.value(RangeValue.rangeToString(utils.convertNumber(utils, range.min), utils.convertNumber(utils, range.max)));
    }

    @NotNull
    public static TooltipBuilder getMinMaxBoundsTooltip(IServerUtils ignoredUtils, MinMaxBounds.Ints ints) {
        if (ints != MinMaxBounds.Ints.ANY) {
            return TooltipBuilder.value(GenericTooltipUtils.toString(ints));
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getMinMaxBoundsTooltip(IServerUtils ignoredUtils, MinMaxBounds.Doubles doubles) {
        if (doubles != MinMaxBounds.Doubles.ANY) {
            return TooltipBuilder.value(GenericTooltipUtils.toString(doubles));
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getExactPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.ExactPropertyMatcher matcher) {
        return TooltipBuilder.keyValue(matcher.name, matcher.value);
    }

    @NotNull
    public static TooltipBuilder getRangedPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.RangedPropertyMatcher matcher) {
        String min = matcher.minValue;
        String max = matcher.maxValue;

        if (min != null) {
            if (max != null) {
                return TooltipBuilder.value(matcher.name, min, max).key("ali.property.value.ranged_property_both");
            } else {
                return TooltipBuilder.value(matcher.name, min).key("ali.property.value.ranged_property_gte");
            }
        } else {
            if (max != null) {
                return TooltipBuilder.value(matcher.name, max).key("ali.property.value.ranged_property_lte");
            } else {
                return TooltipBuilder.value(matcher.name).key("ali.property.value.ranged_property_any");
            }
        }
    }
}
