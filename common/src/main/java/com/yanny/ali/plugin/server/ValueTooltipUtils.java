package com.yanny.ali.plugin.server;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ComponentTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class ValueTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getFormulaTooltip(IServerUtils utils, ApplyBonusCount.Formula formula) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, formula.getType().id());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount) {
            tooltip.add(utils.getValueTooltip(utils, binomialWithBonusCount.extraRounds()).build("ali.property.value.extra_rounds"));
            tooltip.add(utils.getValueTooltip(utils, binomialWithBonusCount.probability()).build("ali.property.value.probability"));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount uniformBonusCount) {
            tooltip.add(utils.getValueTooltip(utils, uniformBonusCount.bonusMultiplier()).build("ali.property.value.bonus_multiplier"));
        }

        return tooltip;
    }

    @NotNull
    public static IKeyTooltipNode getPropertyTooltip(IServerUtils utils, Property<?> property) {
        return utils.getValueTooltip(utils, property.getName());
    }

    @NotNull
    public static IKeyTooltipNode getModifierTooltip(IServerUtils utils, SetAttributesFunction.Modifier modifier) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, modifier.name()).build("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, modifier.attribute()).build("ali.property.value.attribute"))
                .add(utils.getValueTooltip(utils, modifier.operation()).build("ali.property.value.operation"))
                .add(utils.getValueTooltip(utils, modifier.amount()).build("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, modifier.id()).build("ali.property.value.uuid"))
                .add(utils.getValueTooltip(utils, modifier.slots()).build("ali.property.branch.equipment_slots"));
    }

    @NotNull
    public static IKeyTooltipNode getUUIDTooltip(IServerUtils ignoredUtils, UUID uuid) {
        return ValueTooltipNode.value(uuid);
    }

    @NotNull
    public static IKeyTooltipNode getPairTooltip(IServerUtils utils, Pair<?, ?> pair) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, pair.getFirst());

        tooltip.add(utils.getValueTooltip(utils, pair.getSecond()).build("ali.property.value.color"));
        return tooltip;
    }

    @NotNull
    public static IKeyTooltipNode getStatePropertiesPredicateTooltip(IServerUtils utils, StatePropertiesPredicate propertiesPredicate) {
        return GenericTooltipUtils.getCollectionTooltip(utils, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static IKeyTooltipNode getDamageSourcePredicateTooltip(IServerUtils utils, DamageSourcePredicate damagePredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, damagePredicate.tags()).build("ali.property.branch.tags"))
                .add(utils.getValueTooltip(utils, damagePredicate.directEntity()).build("ali.property.branch.direct_entity"))
                .add(utils.getValueTooltip(utils, damagePredicate.sourceEntity()).build("ali.property.branch.source_entity"));
    }

    @NotNull
    public static <T> IKeyTooltipNode getTagPredicateTooltip(IServerUtils ignoredUtils, TagPredicate<T> tagPredicate) {
        return ValueTooltipNode.keyValue(tagPredicate.tag().location().toString(), Boolean.toString(tagPredicate.expected()));
    }

    @NotNull
    public static IKeyTooltipNode getEntityPredicateTooltip(IServerUtils utils, EntityPredicate entityPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, entityPredicate.entityType()).build("ali.property.branch.entity_types"))
                .add(utils.getValueTooltip(utils, entityPredicate.distanceToPlayer()).build("ali.property.branch.distance_to_player"))
                .add(utils.getValueTooltip(utils, entityPredicate.location()).build("ali.property.branch.location"))
                .add(utils.getValueTooltip(utils, entityPredicate.steppingOnLocation()).build("ali.property.branch.stepping_on_location"))
                .add(utils.getValueTooltip(utils, entityPredicate.effects()).build("ali.property.branch.mob_effects"))
                .add(utils.getValueTooltip(utils, entityPredicate.nbt()).build("ali.property.value.nbt"))
                .add(utils.getValueTooltip(utils, entityPredicate.flags()).build("ali.property.branch.entity_flags"))
                .add(utils.getValueTooltip(utils, entityPredicate.equipment()).build("ali.property.branch.entity_equipment"))
                .add(utils.getValueTooltip(utils, entityPredicate.subPredicate()).build("ali.property.branch.entity_sub_predicate"))
                .add(utils.getValueTooltip(utils, entityPredicate.vehicle()).build("ali.property.branch.vehicle"))
                .add(utils.getValueTooltip(utils, entityPredicate.passenger()).build("ali.property.branch.passenger"))
                .add(utils.getValueTooltip(utils, entityPredicate.targetedEntity()).build("ali.property.branch.targeted_entity"))
                .add(utils.getValueTooltip(utils, entityPredicate.team()).build("ali.property.value.team"));
    }

    @NotNull
    public static IKeyTooltipNode getEntityTypePredicateTooltip(IServerUtils utils, EntityTypePredicate entityTypePredicate) {
        return utils.getValueTooltip(utils, entityTypePredicate.types());
    }

    @NotNull
    public static IKeyTooltipNode getDistancePredicateTooltip(IServerUtils utils, DistancePredicate distancePredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, distancePredicate.x()).build("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, distancePredicate.y()).build("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, distancePredicate.z()).build("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, distancePredicate.horizontal()).build("ali.property.value.horizontal"))
                .add(utils.getValueTooltip(utils, distancePredicate.absolute()).build("ali.property.value.absolute"));
    }

    @NotNull
    public static IKeyTooltipNode getLocationPredicateTooltip(IServerUtils utils, LocationPredicate locationPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, locationPredicate.position()).build("ali.property.branch.position"))
                .add(utils.getValueTooltip(utils, locationPredicate.biome()).build("ali.property.value.biome"))
                .add(utils.getValueTooltip(utils, locationPredicate.structure()).build("ali.property.value.structure"))
                .add(utils.getValueTooltip(utils, locationPredicate.dimension()).build("ali.property.value.dimension"))
                .add(utils.getValueTooltip(utils, locationPredicate.smokey()).build("ali.property.value.smokey"))
                .add(utils.getValueTooltip(utils, locationPredicate.light()).build("ali.property.value.light"))
                .add(utils.getValueTooltip(utils, locationPredicate.block()).build("ali.property.branch.block_predicate"))
                .add(utils.getValueTooltip(utils, locationPredicate.fluid()).build("ali.property.branch.fluid_predicate"));
    }

    @NotNull
    public static IKeyTooltipNode getPositionPredicateTooltip(IServerUtils utils, LocationPredicate.PositionPredicate positionPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, positionPredicate.x()).build("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, positionPredicate.y()).build("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, positionPredicate.z()).build("ali.property.value.z"));
    }

    @NotNull
    public static IKeyTooltipNode getLightPredicateTooltip(IServerUtils utils, LightPredicate lightPredicate) {
        return utils.getValueTooltip(utils, lightPredicate.composite());
    }

    @NotNull
    public static IKeyTooltipNode getBlockPredicateTooltip(IServerUtils utils, BlockPredicate blockPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, blockPredicate.tag()).build("ali.property.value.tag"))
                .add(utils.getValueTooltip(utils, blockPredicate.blocks()).build("ali.property.branch.blocks"))
                .add(utils.getValueTooltip(utils, blockPredicate.properties()).build("ali.property.branch.properties"))
                .add(utils.getValueTooltip(utils, blockPredicate.nbt()).build("ali.property.value.nbt"));
    }

    @NotNull
    public static IKeyTooltipNode getNbtPredicateTooltip(IServerUtils utils, NbtPredicate nbtPredicate) {
        return utils.getValueTooltip(utils, nbtPredicate.tag());
    }

    @NotNull
    public static IKeyTooltipNode getFluidPredicateTooltip(IServerUtils utils, FluidPredicate fluidPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fluidPredicate.tag()).build("ali.property.value.tag"))
                .add(utils.getValueTooltip(utils, fluidPredicate.fluid()).build("ali.property.value.fluid"))
                .add(utils.getValueTooltip(utils, fluidPredicate.properties()).build("ali.property.branch.properties"));
    }

    @NotNull
    public static IKeyTooltipNode getMobEffectPredicateTooltip(IServerUtils utils, MobEffectsPredicate mobEffectsPredicate) {
        return getMapTooltip(utils, mobEffectsPredicate.effectMap(), GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
    }

    @NotNull
    public static IKeyTooltipNode getEntityFlagsPredicateTooltip(IServerUtils utils, EntityFlagsPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.isOnFire()).build("ali.property.value.is_on_fire"))
                .add(utils.getValueTooltip(utils, predicate.isBaby()).build("ali.property.value.is_baby"))
                .add(utils.getValueTooltip(utils, predicate.isCrouching()).build("ali.property.value.is_crouching"))
                .add(utils.getValueTooltip(utils, predicate.isSprinting()).build("ali.property.value.is_sprinting"))
                .add(utils.getValueTooltip(utils, predicate.isSwimming()).build("ali.property.value.is_swimming"));
    }

    @NotNull
    public static IKeyTooltipNode getEntityEquipmentPredicateTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.head()).build("ali.property.branch.head"))
                .add(utils.getValueTooltip(utils, predicate.chest()).build("ali.property.branch.chest"))
                .add(utils.getValueTooltip(utils, predicate.legs()).build("ali.property.branch.legs"))
                .add(utils.getValueTooltip(utils, predicate.feet()).build("ali.property.branch.feet"))
                .add(utils.getValueTooltip(utils, predicate.mainhand()).build("ali.property.branch.mainhand"))
                .add(utils.getValueTooltip(utils, predicate.offhand()).build("ali.property.branch.offhand"));
    }

    @NotNull
    public static IKeyTooltipNode getItemPredicateTooltip(IServerUtils utils, ItemPredicate itemPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, itemPredicate.tag()).build("ali.property.value.tag"))
                .add(utils.getValueTooltip(utils, itemPredicate.items()).build("ali.property.branch.items"))
                .add(utils.getValueTooltip(utils, itemPredicate.count()).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, itemPredicate.durability()).build("ali.property.value.durability"))
                .add(utils.getValueTooltip(utils, itemPredicate.enchantments()).build("ali.property.branch.enchantments"))
                .add(utils.getValueTooltip(utils, itemPredicate.storedEnchantments()).build("ali.property.branch.stored_enchantments"))
                .add(utils.getValueTooltip(utils, itemPredicate.potion()).build("ali.property.value.potion"))
                .add(utils.getValueTooltip(utils, itemPredicate.nbt()).build("ali.property.value.nbt"));
    }

    @NotNull
    public static IKeyTooltipNode getEnchantmentPredicateTooltip(IServerUtils utils, EnchantmentPredicate enchantmentPredicate) {
        return utils.getValueTooltip(utils, (enchantmentPredicate.enchantment().isPresent() ? enchantmentPredicate.enchantment() : Component.translatable("ali.util.advanced_loot_info.any")))
                .add(utils.getValueTooltip(utils, enchantmentPredicate.level()).build("ali.property.value.level"));
    }

    @NotNull
    public static IKeyTooltipNode getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate entitySubPredicate) {
        Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

        return optional.map((entry) -> {
            IKeyTooltipNode tooltip = BranchTooltipNode.branch();

            if (entitySubPredicate instanceof LightningBoltPredicate boltPredicate) {
                tooltip.add(utils.getValueTooltip(utils, boltPredicate.blocksSetOnFire()).build("ali.property.value.blocks_on_fire"));
                tooltip.add(utils.getValueTooltip(utils, boltPredicate.entityStruck()).build("ali.property.branch.stuck_entity"));
            } else if (entitySubPredicate instanceof FishingHookPredicate fishingHookPredicate) {
                tooltip.add(utils.getValueTooltip(utils, fishingHookPredicate.inOpenWater()).build("ali.property.value.in_open_water"));
            } else if (entitySubPredicate instanceof PlayerPredicate playerPredicate) {
                tooltip.add(utils.getValueTooltip(utils, playerPredicate.level()).build("ali.property.value.level"));
                tooltip.add(utils.getValueTooltip(utils, playerPredicate.gameType()).build("ali.property.value.game_type"));
                tooltip.add(GenericTooltipUtils.getCollectionTooltip(utils, playerPredicate.stats(), GenericTooltipUtils::getStatMatcherTooltip).build("ali.property.branch.stats"));
                tooltip.add(getMapTooltip(utils, playerPredicate.recipes(), GenericTooltipUtils::getRecipeEntryTooltip).build("ali.property.branch.recipes"));
                tooltip.add(getMapTooltip(utils, playerPredicate.advancements(), GenericTooltipUtils::getAdvancementEntryTooltip).build("ali.property.branch.advancements"));
                tooltip.add(utils.getValueTooltip(utils, playerPredicate.lookingAt()).build("ali.property.branch.looking_at"));
            } else if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                tooltip.add(utils.getValueTooltip(utils, slimePredicate.size()).build("ali.property.value.size"));
            } else {
                EntitySubPredicate.CODEC.encodeStart(JsonOps.INSTANCE, entitySubPredicate).result().ifPresent((element) -> {
                    JsonObject jsonObject = element.getAsJsonObject();

                    if (jsonObject.has("variant")) {
                        tooltip.add(utils.getValueTooltip(utils, jsonObject.getAsJsonPrimitive("variant").getAsString()).build("ali.property.value.variant"));
                    } else {
                        tooltip.add(utils.getValueTooltip(utils, jsonObject.toString()).build("ali.property.value.variant"));
                    }
                });
            }

            return tooltip;
        }).orElse(EmptyTooltipNode.empty());
    }

    @NotNull
    public static IKeyTooltipNode getBlockPosTooltip(IServerUtils ignoredUtils, BlockPos pos) {
        return ValueTooltipNode.value(pos.getX(), pos.getY(), pos.getZ());
    }

    @NotNull
    public static IKeyTooltipNode getCopyOperationTooltip(IServerUtils utils, CopyNbtFunction.CopyOperation copyOperation) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, copyOperation.sourcePath().string()).build("ali.property.value.source"))
                .add(utils.getValueTooltip(utils, copyOperation.targetPath().string()).build("ali.property.value.target"))
                .add(utils.getValueTooltip(utils, copyOperation.op()).build("ali.property.value.merge_strategy"));
    }

    @NotNull
    public static IKeyTooltipNode getEffectEntryTooltip(IServerUtils utils, SetStewEffectFunction.EffectEntry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils,entry.duration()).build("ali.property.value.duration"));
    }

    @NotNull
    public static IKeyTooltipNode getCompoundTagTooltip(IServerUtils utils, CompoundTag tag) {
        return utils.getValueTooltip(utils, tag.toString());
    }

    @NotNull
    public static IKeyTooltipNode getAdvancementPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementPredicate predicate) {
        if (predicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
            return utils.getValueTooltip(utils, donePredicate.state());
        } else if (predicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
            return getMapTooltip(utils, criterionsPredicate.criterions(), GenericTooltipUtils::getCriterionEntryTooltip);
        }

        return EmptyTooltipNode.empty();
    }

    @NotNull
    public static IKeyTooltipNode getItemStackTooltip(IServerUtils utils, ItemStack item) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, item.getItem()).build("ali.property.value.item"))
                .add(utils.getValueTooltip(utils, item.getCount()).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, item.getTag()).build("ali.property.value.tag"));
    }

    @NotNull
    public static IKeyTooltipNode getNumberProviderTooltip(IServerUtils utils, NumberProvider value) {
        return ValueTooltipNode.value(utils.convertNumber(utils, value));
    }

    @NotNull
    public static IKeyTooltipNode getIntRangeTooltip(IServerUtils utils, IntRange range) {
        return ValueTooltipNode.value(RangeValue.rangeToString(utils.convertNumber(utils, range.min), utils.convertNumber(utils, range.max)));
    }

    @NotNull
    public static IKeyTooltipNode getBooleanTooltip(IServerUtils ignoredUtils, Boolean value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getIntegerTooltip(IServerUtils ignoredUtils, int value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getLongTooltip(IServerUtils ignoredUtils, Long value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getByteTooltip(IServerUtils ignoredUtils, Byte value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getStringTooltip(IServerUtils ignoredUtils, String value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getFloatTooltip(IServerUtils ignoredUtils, Float value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getDoubleTooltip(IServerUtils ignoredUtils, Double value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static IKeyTooltipNode getEnumTooltip(IServerUtils ignoredUtils, Enum<?> value) {
        return ValueTooltipNode.value(value.name());
    }

    @NotNull
    public static IKeyTooltipNode getResourceLocationTooltip(IServerUtils ignoredUtils, ResourceLocation value) {
        return ValueTooltipNode.value(value);
    }

    @NotNull
    public static <T> IKeyTooltipNode getBuiltInRegistryTooltip(IServerUtils utils, Registry<T> registry, T value) {
        return utils.getValueTooltip(utils, registry.getKey(value));
    }

    @NotNull
    public static <T> IKeyTooltipNode getResourceKeyTooltip(IServerUtils utils, ResourceKey<T> value) {
        return utils.getValueTooltip(utils, value.location());
    }

    @NotNull
    public static IKeyTooltipNode getTagKeyTooltip(IServerUtils utils, TagKey<?> value) {
        return utils.getValueTooltip(utils, value.location());
    }

    @NotNull
    public static IKeyTooltipNode getComponentTooltip(IServerUtils ignoredUtils, Component component) {
        return ComponentTooltipNode.values(component.copy());
    }

    @NotNull
    public static IKeyTooltipNode getMinMaxBoundsTooltip(IServerUtils ignoredUtils, MinMaxBounds.Ints ints) {
        if (ints != MinMaxBounds.Ints.ANY) {
            return ValueTooltipNode.value(GenericTooltipUtils.toString(ints));
        }

        return EmptyTooltipNode.empty();
    }

    @NotNull
    public static IKeyTooltipNode getMinMaxBoundsTooltip(IServerUtils ignoredUtils, MinMaxBounds.Doubles doubles) {
        if (doubles != MinMaxBounds.Doubles.ANY) {
            return ValueTooltipNode.value(GenericTooltipUtils.toString(doubles));
        }

        return EmptyTooltipNode.empty();
    }

    @NotNull
    public static <T> IKeyTooltipNode getHolderTooltip(IServerUtils utils, Holder<T> holder) {
        return utils.getValueTooltip(utils, holder.value());
    }

    public static <T> IKeyTooltipNode getHolderSetTooltip(IServerUtils utils, HolderSet<T> holderSet) {
        IKeyTooltipNode tooltip;
        Either<TagKey<T>, List<Holder<T>>> either = holderSet.unwrap();
        Optional<TagKey<T>> left = either.left();
        Optional<List<Holder<T>>> right = either.right();

        if (left.isPresent() || !right.orElse(List.of()).isEmpty()) {
            tooltip = BranchTooltipNode.branch();
        } else {
            return EmptyTooltipNode.empty();
        }

        left.ifPresent((tagKey) -> tooltip.add(utils.getValueTooltip(utils, tagKey).build("ali.property.value.tag")));
        right.ifPresent((list) -> {
            if (!list.isEmpty()) {
                list.forEach((holder) -> tooltip.add(utils.getValueTooltip(utils, holder).build("ali.property.value.null")));
            }
        });

        return tooltip;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <T> IKeyTooltipNode getOptionalTooltip(IServerUtils utils, Optional<T> optional) {
        return optional.map((v) -> utils.getValueTooltip(utils, v)).orElse(EmptyTooltipNode.empty());
    }

    @NotNull
    public static IKeyTooltipNode getCollectionTooltip(IServerUtils utils, @Nullable Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return EmptyTooltipNode.empty();
        }

        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        for (Object o : collection) {
            tooltip.add(utils.getValueTooltip(utils, o).build("ali.property.value.null"));
        }

        return tooltip;
    }
}
