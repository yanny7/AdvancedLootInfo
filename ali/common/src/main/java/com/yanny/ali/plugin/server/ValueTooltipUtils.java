package com.yanny.ali.plugin.server;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
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
            tooltip.add(utils.getValueTooltip(utils, binomialWithBonusCount.extraRounds).build(Lang.Value.EXTRA_ROUNDS));
            tooltip.add(utils.getValueTooltip(utils, binomialWithBonusCount.probability).build(Lang.Value.PROBABILITY));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            tooltip.add(utils.getValueTooltip(utils, uniformBonusCount.bonusMultiplier).build(Lang.Value.BONUS_MULTIPLIER));
        }

        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getModifierTooltip(IServerUtils utils, SetAttributesFunction.Modifier modifier) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, modifier.name).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, modifier.attribute).build(Lang.Value.ATTRIBUTE));
            b.add(utils.getValueTooltip(utils, modifier.operation).build(Lang.Value.OPERATION));
            b.add(utils.getValueTooltip(utils, modifier.amount).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, modifier.id).build(Lang.Value.UUID));
            b.add(utils.getValueTooltip(utils, modifier.slots).build(Lang.Branch.EQUIPMENT_SLOTS));
        }).key(Lang.Branch.MODIFIER);
    }

    @NotNull
    public static TooltipBuilder getPairTooltip(IServerUtils utils, Pair<?, ?> pair) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, pair.getFirst());

        tooltip.add(utils.getValueTooltip(utils, pair.getSecond()).build(Lang.Value.COLOR));
        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getStatePropertiesPredicateTooltip(IServerUtils utils, StatePropertiesPredicate propertiesPredicate) {
        return utils.getValueTooltip(utils, propertiesPredicate.properties);
    }

    @NotNull
    public static TooltipBuilder getDamageSourcePredicateTooltip(IServerUtils utils, DamageSourcePredicate damagePredicate) {
        if (damagePredicate != DamageSourcePredicate.ANY) {
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, damagePredicate.tags).build(Lang.Branch.TAGS));
                b.add(utils.getValueTooltip(utils, damagePredicate.directEntity).build(Lang.Branch.DIRECT_ENTITY));
                b.add(utils.getValueTooltip(utils, damagePredicate.sourceEntity).build(Lang.Branch.SOURCE_ENTITY));
            });
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
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, entityPredicate.entityType).build(Lang.Branch.ENTITY_TYPES));
                b.add(utils.getValueTooltip(utils, entityPredicate.distanceToPlayer).build(Lang.Branch.DISTANCE_TO_PLAYER));
                b.add(utils.getValueTooltip(utils, entityPredicate.location).build(Lang.Branch.LOCATION));
                b.add(utils.getValueTooltip(utils, entityPredicate.steppingOnLocation).build(Lang.Branch.STEPPING_ON_LOCATION));
                b.add(utils.getValueTooltip(utils, entityPredicate.effects).build(Lang.Branch.MOB_EFFECTS));
                b.add(utils.getValueTooltip(utils, entityPredicate.nbt).build(Lang.Value.NBT));
                b.add(utils.getValueTooltip(utils, entityPredicate.flags).build(Lang.Branch.ENTITY_FLAGS));
                b.add(utils.getValueTooltip(utils, entityPredicate.equipment).build(Lang.Branch.ENTITY_EQUIPMENT));
                b.add(utils.getValueTooltip(utils, entityPredicate.subPredicate).build(Lang.Branch.ENTITY_SUB_PREDICATE));
                b.add(utils.getValueTooltip(utils, entityPredicate.vehicle).build(Lang.Branch.VEHICLE));
                b.add(utils.getValueTooltip(utils, entityPredicate.passenger).build(Lang.Branch.PASSENGER));
                b.add(utils.getValueTooltip(utils, entityPredicate.targetedEntity).build(Lang.Branch.TARGETED_ENTITY));
                b.add(utils.getValueTooltip(utils, entityPredicate.team).build(Lang.Value.TEAM));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEntityTypePredicateTooltip(IServerUtils utils, EntityTypePredicate entityTypePredicate) {
        if (entityTypePredicate != EntityTypePredicate.ANY) {
            return TooltipBuilder.branch((b) -> {
                if (entityTypePredicate instanceof EntityTypePredicate.TypePredicate typePredicate) {
                    b.add(utils.getValueTooltip(utils, typePredicate.type));
                }
                if (entityTypePredicate instanceof EntityTypePredicate.TagPredicate tagPredicate) {
                    b.add(utils.getValueTooltip(utils, tagPredicate.tag).key(Lang.Value.TAG));
                }
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getDistancePredicateTooltip(IServerUtils utils, DistancePredicate distancePredicate) {
        if (distancePredicate != DistancePredicate.ANY) {
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, distancePredicate.x).build(Lang.Value.X));
                b.add(utils.getValueTooltip(utils, distancePredicate.y).build(Lang.Value.Y));
                b.add(utils.getValueTooltip(utils, distancePredicate.z).build(Lang.Value.Z));
                b.add(utils.getValueTooltip(utils, distancePredicate.horizontal).build(Lang.Value.HORIZONTAL));
                b.add(utils.getValueTooltip(utils, distancePredicate.absolute).build(Lang.Value.ABSOLUTE));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getLocationPredicateTooltip(IServerUtils utils, LocationPredicate locationPredicate) {
        if (locationPredicate != LocationPredicate.ANY) {
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, locationPredicate.x).build(Lang.Value.X));
                b.add(utils.getValueTooltip(utils, locationPredicate.y).build(Lang.Value.Y));
                b.add(utils.getValueTooltip(utils, locationPredicate.z).build(Lang.Value.Z));
                b.add(utils.getValueTooltip(utils, locationPredicate.biome).build(Lang.Value.BIOME));
                b.add(utils.getValueTooltip(utils, locationPredicate.structure).build(Lang.Value.STRUCTURE));
                b.add(utils.getValueTooltip(utils, locationPredicate.dimension).build(Lang.Value.DIMENSION));
                b.add(utils.getValueTooltip(utils, locationPredicate.smokey).build(Lang.Value.SMOKEY));
                b.add(utils.getValueTooltip(utils, locationPredicate.light).build(Lang.Value.LIGHT));
                b.add(utils.getValueTooltip(utils, locationPredicate.block).build(Lang.Branch.BLOCK_PREDICATE));
                b.add(utils.getValueTooltip(utils, locationPredicate.fluid).build(Lang.Branch.FLUID_PREDICATE));
            });
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
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, blockPredicate.tag).build(Lang.Value.TAG));
                b.add(utils.getValueTooltip(utils, blockPredicate.blocks).build(Lang.Branch.BLOCKS));
                b.add(utils.getValueTooltip(utils, blockPredicate.properties).build(Lang.Branch.PROPERTIES));
                b.add(utils.getValueTooltip(utils, blockPredicate.nbt).build(Lang.Value.NBT));
            });
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
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, fluidPredicate.tag).build(Lang.Value.TAG));
                b.add(utils.getValueTooltip(utils, fluidPredicate.fluid).build(Lang.Value.FLUID));
                b.add(utils.getValueTooltip(utils, fluidPredicate.properties).build(Lang.Branch.PROPERTIES));
            });
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
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, predicate.isOnFire).build(Lang.Value.IS_ON_FIRE));
                b.add(utils.getValueTooltip(utils, predicate.isBaby).build(Lang.Value.IS_BABY));
                b.add(utils.getValueTooltip(utils, predicate.isCrouching).build(Lang.Value.IS_CROUCHING));
                b.add(utils.getValueTooltip(utils, predicate.isSprinting).build(Lang.Value.IS_SPRINTING));
                b.add(utils.getValueTooltip(utils, predicate.isSwimming).build(Lang.Value.IS_SWIMMING));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEntityEquipmentPredicateTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        if (predicate != EntityEquipmentPredicate.ANY) {
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, predicate.head).build(Lang.Branch.HEAD));
                b.add(utils.getValueTooltip(utils, predicate.chest).build(Lang.Branch.CHEST));
                b.add(utils.getValueTooltip(utils, predicate.legs).build(Lang.Branch.LEGS));
                b.add(utils.getValueTooltip(utils, predicate.feet).build(Lang.Branch.FEET));
                b.add(utils.getValueTooltip(utils, predicate.mainhand).build(Lang.Branch.MAINHAND));
                b.add(utils.getValueTooltip(utils, predicate.offhand).build(Lang.Branch.OFFHAND));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getItemPredicateTooltip(IServerUtils utils, ItemPredicate itemPredicate) {
        if (itemPredicate != ItemPredicate.ANY) {
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, itemPredicate.tag).build(Lang.Value.TAG));
                b.add(utils.getValueTooltip(utils, itemPredicate.items).build(Lang.Branch.ITEMS));
                b.add(utils.getValueTooltip(utils, itemPredicate.count).build(Lang.Value.COUNT));
                b.add(utils.getValueTooltip(utils, itemPredicate.durability).build(Lang.Value.DURABILITY));
                b.add(utils.getValueTooltip(utils, itemPredicate.enchantments).build(Lang.Branch.ENCHANTMENTS));
                b.add(utils.getValueTooltip(utils, itemPredicate.storedEnchantments).build(Lang.Branch.STORED_ENCHANTMENTS));
                b.add(utils.getValueTooltip(utils, itemPredicate.potion).build(Lang.Value.POTION));
                b.add(utils.getValueTooltip(utils, itemPredicate.nbt).build(Lang.Value.NBT));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getEnchantmentPredicateTooltip(IServerUtils utils, EnchantmentPredicate enchantmentPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, enchantmentPredicate.enchantment).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, enchantmentPredicate.level).build(Lang.Value.LEVEL));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate entitySubPredicate) {
        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            return optional.map((entry) -> {
                return TooltipBuilder.array((b) -> {
                    if (entitySubPredicate instanceof LighthingBoltPredicate boltPredicate) {
                        b.add(utils.getValueTooltip(utils, boltPredicate.blocksSetOnFire).build(Lang.Value.BLOCKS_ON_FIRE));
                        b.add(utils.getValueTooltip(utils, boltPredicate.entityStruck).build(Lang.Branch.STUCK_ENTITY));
                    } else if (entitySubPredicate instanceof FishingHookPredicate fishingHookPredicate) {
                        b.add(utils.getValueTooltip(utils, fishingHookPredicate.inOpenWater).build(Lang.Value.IN_OPEN_WATER));
                    } else if (entitySubPredicate instanceof PlayerPredicate playerPredicate) {
                        b.add(utils.getValueTooltip(utils, playerPredicate.level).build(Lang.Value.LEVEL));
                        b.add(utils.getValueTooltip(utils, playerPredicate.gameType).build(Lang.Value.GAME_TYPE));
                        b.add(getMapTooltip(utils, playerPredicate.stats, GenericTooltipUtils::getStatsEntryTooltip).build(Lang.Branch.STATS));
                        b.add(getMapTooltip(utils, playerPredicate.recipes, GenericTooltipUtils::getRecipeEntryTooltip).build(Lang.Branch.RECIPES));
                        b.add(getMapTooltip(utils, playerPredicate.advancements, GenericTooltipUtils::getAdvancementEntryTooltip).build(Lang.Branch.ADVANCEMENTS));
                        b.add(utils.getValueTooltip(utils, playerPredicate.lookingAt).build(Lang.Branch.LOOKING_AT));
                    } else if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                        b.add(utils.getValueTooltip(utils, slimePredicate.size).build(Lang.Value.SIZE));
                    } else {
                        JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                        if (jsonObject.has("variant")) {
                            b.add(utils.getValueTooltip(utils, jsonObject.getAsJsonPrimitive("variant").getAsString()).build(Lang.Value.VARIANT));
                        } else {
                            b.add(utils.getValueTooltip(utils, jsonObject.getAsString()).build(Lang.Value.VARIANT));
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
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, copyOperation.sourcePathText).build(Lang.Value.SOURCE));
            b.add(utils.getValueTooltip(utils, copyOperation.targetPathText).build(Lang.Value.TARGET));
            b.add(utils.getValueTooltip(utils, copyOperation.op).build(Lang.Value.MERGE_STRATEGY));
        }).key(Lang.Branch.OPERATION);
    }

    @NotNull
    public static TooltipBuilder getAdvancementDonePredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementDonePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.state).key(Lang.Value.DONE);
    }

    @NotNull
    public static TooltipBuilder getAdvancementCriterionsPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementCriterionsPredicate predicate) {
        return getMapTooltip(utils, predicate.criterions, GenericTooltipUtils::getCriterionEntryTooltip).key(Lang.Branch.CRITERIONS);
    }

    @NotNull
    public static TooltipBuilder getItemStackTooltip(IServerUtils utils, ItemStack item) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, item.getItem()).build(Lang.Value.ITEM));
            b.add(utils.getValueTooltip(utils, item.getCount()).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, item.getTag()).build(Lang.Value.TAG));
        }).key(Lang.Branch.ITEM);
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
                return TooltipBuilder.value(matcher.name, min, max).key(Lang.Value.RANGED_BOTH);
            } else {
                return TooltipBuilder.value(matcher.name, min).key(Lang.Value.RANGED_GTE);
            }
        } else {
            if (max != null) {
                return TooltipBuilder.value(matcher.name, max).key(Lang.Value.RANGED_LTE);
            } else {
                return TooltipBuilder.value(matcher.name).key(Lang.Value.RANGED_ANY);
            }
        }
    }
}
