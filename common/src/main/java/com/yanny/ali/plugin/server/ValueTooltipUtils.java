package com.yanny.ali.plugin.server;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ComponentTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class ValueTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getFormulaTooltip(IServerUtils utils, ApplyBonusCount.Formula formula) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, formula.getType().id());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount(int extraRounds, float probability)) {
            tooltip.add(utils.getValueTooltip(utils, extraRounds).key("ali.property.value.extra_rounds"));
            tooltip.add(utils.getValueTooltip(utils, probability).key("ali.property.value.probability"));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount(int bonusMultiplier)) {
            tooltip.add(utils.getValueTooltip(utils, bonusMultiplier).key("ali.property.value.bonus_multiplier"));
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
                .add(utils.getValueTooltip(utils, modifier.attribute()).key("ali.property.value.attribute"))
                .add(utils.getValueTooltip(utils, modifier.operation()).key("ali.property.value.operation"))
                .add(utils.getValueTooltip(utils, modifier.amount()).key("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, modifier.id()).key("ali.property.value.id"))
                .add(utils.getValueTooltip(utils, modifier.slots()).key("ali.property.branch.equipment_slots"));
    }

    @NotNull
    public static IKeyTooltipNode getUUIDTooltip(IServerUtils ignoredUtils, UUID uuid) {
        return ValueTooltipNode.value(uuid);
    }

    @NotNull
    public static IKeyTooltipNode getPairTooltip(IServerUtils utils, Pair<?, ?> pair) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, pair.getFirst());

        tooltip.add(utils.getValueTooltip(utils, pair.getSecond()).key("ali.property.value.color"));
        return tooltip;
    }

    @NotNull
    public static IKeyTooltipNode getStatePropertiesPredicateTooltip(IServerUtils utils, StatePropertiesPredicate propertiesPredicate) {
        return GenericTooltipUtils.getCollectionTooltip(utils, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static IKeyTooltipNode getDamageSourcePredicateTooltip(IServerUtils utils, DamageSourcePredicate damagePredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, damagePredicate.tags()).key("ali.property.branch.tags"))
                .add(utils.getValueTooltip(utils, damagePredicate.directEntity()).key("ali.property.branch.direct_entity"))
                .add(utils.getValueTooltip(utils, damagePredicate.sourceEntity()).key("ali.property.branch.source_entity"))
                .add(utils.getValueTooltip(utils, damagePredicate.isDirect()).key("ali.property.value.is_direct"));
    }

    @NotNull
    public static <T> IKeyTooltipNode getTagPredicateTooltip(IServerUtils ignoredUtils, TagPredicate<T> tagPredicate) {
        return ValueTooltipNode.keyValue(tagPredicate.tag().location().toString(), Boolean.toString(tagPredicate.expected()));
    }

    @NotNull
    public static IKeyTooltipNode getEntityPredicateTooltip(IServerUtils utils, EntityPredicate entityPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, entityPredicate.entityType()).key("ali.property.branch.entity_types"))
                .add(utils.getValueTooltip(utils, entityPredicate.distanceToPlayer()).key("ali.property.branch.distance_to_player"))
                .add(utils.getValueTooltip(utils, entityPredicate.location()).key("ali.property.branch.location"))
                .add(utils.getValueTooltip(utils, entityPredicate.movement()).key("ali.property.branch.movement"))
                .add(utils.getValueTooltip(utils, entityPredicate.periodicTick()).key("ali.property.value.periodic_tick"))
                .add(utils.getValueTooltip(utils, entityPredicate.effects()).key("ali.property.branch.mob_effects"))
                .add(utils.getValueTooltip(utils, entityPredicate.nbt()).key("ali.property.value.nbt"))
                .add(utils.getValueTooltip(utils, entityPredicate.flags()).key("ali.property.branch.entity_flags"))
                .add(utils.getValueTooltip(utils, entityPredicate.equipment()).key("ali.property.branch.entity_equipment"))
                .add(utils.getValueTooltip(utils, entityPredicate.subPredicate()).key("ali.property.branch.entity_sub_predicate"))
                .add(utils.getValueTooltip(utils, entityPredicate.vehicle()).key("ali.property.branch.vehicle"))
                .add(utils.getValueTooltip(utils, entityPredicate.passenger()).key("ali.property.branch.passenger"))
                .add(utils.getValueTooltip(utils, entityPredicate.targetedEntity()).key("ali.property.branch.targeted_entity"))
                .add(utils.getValueTooltip(utils, entityPredicate.team()).key("ali.property.value.team"))
                .add(utils.getValueTooltip(utils, entityPredicate.slots()).key("ali.property.branch.slots"));
    }

    @NotNull
    public static IKeyTooltipNode getEntityTypePredicateTooltip(IServerUtils utils, EntityTypePredicate entityTypePredicate) {
        return utils.getValueTooltip(utils, entityTypePredicate.types());
    }

    @NotNull
    public static IKeyTooltipNode getDistancePredicateTooltip(IServerUtils utils, DistancePredicate distancePredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, distancePredicate.x()).key("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, distancePredicate.y()).key("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, distancePredicate.z()).key("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, distancePredicate.horizontal()).key("ali.property.value.horizontal"))
                .add(utils.getValueTooltip(utils, distancePredicate.absolute()).key("ali.property.value.absolute"));
    }

    @NotNull
    public static IKeyTooltipNode getLocationPredicateTooltip(IServerUtils utils, LocationPredicate locationPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, locationPredicate.position()).key("ali.property.branch.position"))
                .add(utils.getValueTooltip(utils, locationPredicate.biomes()).key("ali.property.branch.biomes"))
                .add(utils.getValueTooltip(utils, locationPredicate.structures()).key("ali.property.branch.structures"))
                .add(utils.getValueTooltip(utils, locationPredicate.dimension()).key("ali.property.value.dimension"))
                .add(utils.getValueTooltip(utils, locationPredicate.smokey()).key("ali.property.value.smokey"))
                .add(utils.getValueTooltip(utils, locationPredicate.light()).key("ali.property.value.light"))
                .add(utils.getValueTooltip(utils, locationPredicate.block()).key("ali.property.branch.block_predicate"))
                .add(utils.getValueTooltip(utils, locationPredicate.fluid()).key("ali.property.branch.fluid_predicate"))
                .add(utils.getValueTooltip(utils, locationPredicate.canSeeSky()).key("ali.property.value.can_see_sky"));
    }

    @NotNull
    public static IKeyTooltipNode getPositionPredicateTooltip(IServerUtils utils, LocationPredicate.PositionPredicate positionPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, positionPredicate.x()).key("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, positionPredicate.y()).key("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, positionPredicate.z()).key("ali.property.value.z"));
    }

    @NotNull
    public static IKeyTooltipNode getLightPredicateTooltip(IServerUtils utils, LightPredicate lightPredicate) {
        return utils.getValueTooltip(utils, lightPredicate.composite());
    }

    @NotNull
    public static IKeyTooltipNode getBlockPredicateTooltip(IServerUtils utils, BlockPredicate blockPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, blockPredicate.blocks()).key("ali.property.branch.blocks"))
                .add(utils.getValueTooltip(utils, blockPredicate.properties()).key("ali.property.branch.properties"))
                .add(utils.getValueTooltip(utils, blockPredicate.nbt()).key("ali.property.value.nbt"));
    }

    @NotNull
    public static IKeyTooltipNode getNbtPredicateTooltip(IServerUtils utils, NbtPredicate nbtPredicate) {
        return utils.getValueTooltip(utils, nbtPredicate.tag());
    }

    @NotNull
    public static IKeyTooltipNode getFluidPredicateTooltip(IServerUtils utils, FluidPredicate fluidPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, fluidPredicate.fluids()).key("ali.property.branch.fluids"))
                .add(utils.getValueTooltip(utils, fluidPredicate.properties()).key("ali.property.branch.properties"));
    }

    @NotNull
    public static IKeyTooltipNode getMobEffectPredicateTooltip(IServerUtils utils, MobEffectsPredicate mobEffectsPredicate) {
        return getMapTooltip(utils, mobEffectsPredicate.effectMap(), GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
    }

    @NotNull
    public static IKeyTooltipNode getEntityFlagsPredicateTooltip(IServerUtils utils, EntityFlagsPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.isOnGround()).key("ali.property.value.is_on_ground"))
                .add(utils.getValueTooltip(utils, predicate.isOnFire()).key("ali.property.value.is_on_fire"))
                .add(utils.getValueTooltip(utils, predicate.isBaby()).key("ali.property.value.is_baby"))
                .add(utils.getValueTooltip(utils, predicate.isCrouching()).key("ali.property.value.is_crouching"))
                .add(utils.getValueTooltip(utils, predicate.isSprinting()).key("ali.property.value.is_sprinting"))
                .add(utils.getValueTooltip(utils, predicate.isSwimming()).key("ali.property.value.is_swimming"))
                .add(utils.getValueTooltip(utils, predicate.isFlying()).key("ali.property.value.is_flying"));
    }

    @NotNull
    public static IKeyTooltipNode getEntityEquipmentPredicateTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.head()).key("ali.property.branch.head"))
                .add(utils.getValueTooltip(utils, predicate.chest()).key("ali.property.branch.chest"))
                .add(utils.getValueTooltip(utils, predicate.legs()).key("ali.property.branch.legs"))
                .add(utils.getValueTooltip(utils, predicate.feet()).key("ali.property.branch.feet"))
                .add(utils.getValueTooltip(utils, predicate.body()).key("ali.property.branch.body"))
                .add(utils.getValueTooltip(utils, predicate.mainhand()).key("ali.property.branch.mainhand"))
                .add(utils.getValueTooltip(utils, predicate.offhand()).key("ali.property.branch.offhand"));
    }

    @NotNull
    public static IKeyTooltipNode getItemPredicateTooltip(IServerUtils utils, ItemPredicate itemPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, itemPredicate.items()).key("ali.property.branch.items"))
                .add(utils.getValueTooltip(utils, itemPredicate.count()).key("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, itemPredicate.components()).key("ali.property.branch.components"))
                .add(getMapTooltip(utils, itemPredicate.subPredicates(), GenericTooltipUtils::getItemSubPredicateEntryTooltip).key("ali.property.branch.item_predicates"));
    }

    @NotNull
    public static IKeyTooltipNode getEnchantmentPredicateTooltip(IServerUtils utils, EnchantmentPredicate enchantmentPredicate) {
        return utils.getValueTooltip(utils, (enchantmentPredicate.enchantments().isPresent() ? enchantmentPredicate.enchantments() : Component.translatable("ali.util.advanced_loot_info.any")))
                .add(utils.getValueTooltip(utils, enchantmentPredicate.level()).key("ali.property.value.level"));
    }

    @NotNull
    public static IKeyTooltipNode getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate entitySubPredicate) {
        return BranchTooltipNode.branch()
                .add(utils.getEntitySubPredicateTooltip(utils, entitySubPredicate));
    }

    @NotNull
    public static IKeyTooltipNode getBlockPosTooltip(IServerUtils ignoredUtils, BlockPos pos) {
        return ValueTooltipNode.value(pos.getX(), pos.getY(), pos.getZ());
    }

    @NotNull
    public static IKeyTooltipNode getCopyOperationTooltip(IServerUtils utils, CopyCustomDataFunction.CopyOperation copyOperation) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, copyOperation.sourcePath()).key("ali.property.value.source_path"))
                .add(utils.getValueTooltip(utils, copyOperation.targetPath()).key("ali.property.value.target_path"))
                .add(utils.getValueTooltip(utils, copyOperation.op()).key("ali.property.value.merge_strategy"));
    }

    @NotNull
    public static IKeyTooltipNode getEffectEntryTooltip(IServerUtils utils, SetStewEffectFunction.EffectEntry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils,entry.duration()).key("ali.property.value.duration"));
    }

    @NotNull
    public static IKeyTooltipNode getCompoundTagTooltip(IServerUtils utils, CompoundTag tag) {
        return utils.getValueTooltip(utils, tag.toString());
    }

    @NotNull
    public static IKeyTooltipNode getItemStackTooltip(IServerUtils utils, ItemStack item) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, item.getItem()).key("ali.property.value.item"))
                .add(utils.getValueTooltip(utils, item.getCount()).key("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, item.getComponents()).key("ali.property.branch.components"));
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
        return ComponentTooltipNode.value(component.copy());
    }

    @NotNull
    public static IKeyTooltipNode getMinMaxBoundsTooltip(IServerUtils ignoredUtils, MinMaxBounds.Ints ints) {
        if (ints != MinMaxBounds.Ints.ANY) {
            return ValueTooltipNode.value(GenericTooltipUtils.toString(ints));
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getMinMaxBoundsTooltip(IServerUtils ignoredUtils, MinMaxBounds.Doubles doubles) {
        if (doubles != MinMaxBounds.Doubles.ANY) {
            return ValueTooltipNode.value(GenericTooltipUtils.toString(doubles));
        }

        return EmptyTooltipNode.EMPTY;
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
            return EmptyTooltipNode.EMPTY;
        }

        left.ifPresent((tagKey) -> tooltip.add(utils.getValueTooltip(utils, tagKey).key("ali.property.value.tag")));
        right.ifPresent((list) -> {
            if (!list.isEmpty()) {
                list.forEach((holder) -> tooltip.add(utils.getValueTooltip(utils, holder).key("ali.property.value.null")));
            }
        });

        return tooltip;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static <T> IKeyTooltipNode getOptionalTooltip(IServerUtils utils, Optional<T> optional) {
        return optional.map((v) -> utils.getValueTooltip(utils, v)).orElse(EmptyTooltipNode.EMPTY);
    }

    @NotNull
    public static IKeyTooltipNode getCollectionTooltip(IServerUtils utils, @Nullable Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return EmptyTooltipNode.EMPTY;
        }

        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        for (Object o : collection) {
            tooltip.add(utils.getValueTooltip(utils, o).key("ali.property.value.null"));
        }

        return tooltip;
    }

    @NotNull
    public static IKeyTooltipNode getListOperationTooltip(IServerUtils utils, ListOperation operation) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, operation.mode());

        if (operation instanceof ListOperation.Insert(int offset)) {
            tooltip.add(utils.getValueTooltip(utils, offset).key("ali.property.value.offset"));
        } else if (operation instanceof ListOperation.ReplaceSection(int offset, Optional<Integer> size)) {
            tooltip.add(utils.getValueTooltip(utils, offset).key("ali.property.value.offset"));
            tooltip.add(utils.getValueTooltip(utils, size).key("ali.property.value.size"));
        }

        return tooltip;
    }

    @NotNull
    public static <T> IKeyTooltipNode getFilterableTooltip(IServerUtils utils, Filterable<T> data) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, data.raw()).key("ali.property.value.raw"))
                .add(utils.getValueTooltip(utils, data.filtered()).key("ali.property.value.filtered"));
    }

    @NotNull
    public static IKeyTooltipNode getFireworkPredicateTooltip(IServerUtils utils, ItemFireworkExplosionPredicate.FireworkPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.shape()).key("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, predicate.trail()).key("ali.property.value.trail"))
                .add(utils.getValueTooltip(utils, predicate.twinkle()).key("ali.property.value.twinkle"));
    }

    @NotNull
    public static IKeyTooltipNode getContainerComponentManipulatorTooltip(IServerUtils utils, ContainerComponentManipulator<?> component) {
        return utils.getValueTooltip(utils, component.type());
    }

    @NotNull
    public static IKeyTooltipNode getNbtPathTooltip(IServerUtils utils, NbtPathArgument.NbtPath provider) {
        return utils.getValueTooltip(utils, provider.asString());
    }

    @NotNull
    public static IKeyTooltipNode getDataComponentPredicateTooltip(IServerUtils utils, DataComponentPredicate dataComponentPredicate) {
        if (dataComponentPredicate != DataComponentPredicate.EMPTY) {
            return utils.getValueTooltip(utils, dataComponentPredicate.expectedComponents);
        } else {
            return EmptyTooltipNode.EMPTY;
        }
    }

    @NotNull
    public static IKeyTooltipNode getTypedDataComponentTooltip(IServerUtils utils, TypedDataComponent<?> typedDataComponent) {
        return utils.getValueTooltip(utils, typedDataComponent.type())
                .add(utils.getDataComponentTypeTooltip(utils, typedDataComponent.type(), typedDataComponent.value()));
    }

    @NotNull
    public static IKeyTooltipNode getPagePredicateTooltip(IServerUtils ignoredUtils, ItemWritableBookPredicate.PagePredicate predicate) {
        return ValueTooltipNode.value(predicate.contents());
    }

    @NotNull
    public static IKeyTooltipNode getPagePredicateTooltip(IServerUtils ignoredUtils, ItemWrittenBookPredicate.PagePredicate predicate) {
        return ComponentTooltipNode.value(predicate.contents());
    }

    @NotNull
    public static IKeyTooltipNode getEntryPredicateTooltip(IServerUtils utils, ItemAttributeModifiersPredicate.EntryPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.attribute()).key("ali.property.branch.attributes"))
                .add(utils.getValueTooltip(utils, predicate.id()).key("ali.property.value.id"))
                .add(utils.getValueTooltip(utils, predicate.amount()).key("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, predicate.operation()).key("ali.property.value.operation"))
                .add(utils.getValueTooltip(utils, predicate.slot()).key("ali.property.value.slot"));
    }

    @NotNull
    public static IKeyTooltipNode getDataComponentPatchTooltip(IServerUtils utils, DataComponentPatch data) {
        return getMapTooltip(utils, data.map, GenericTooltipUtils::getDataComponentPatchEntryTooltip);
    }

    @NotNull
    public static IKeyTooltipNode getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion data) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, data.shape()).key("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, data.colors()).key("ali.property.value.colors"))
                .add(utils.getValueTooltip(utils, data.fadeColors()).key("ali.property.value.fade_colors"))
                .add(utils.getValueTooltip(utils, data.hasTrail()).key("ali.property.value.has_trail"))
                .add(utils.getValueTooltip(utils, data.hasTwinkle()).key("ali.property.value.has_twinkle"));
    }

    @NotNull
    public static IKeyTooltipNode getIntListTooltip(IServerUtils utils, IntList data) {
        return utils.getValueTooltip(utils, data.toString());
    }
    
    @NotNull
    public static IKeyTooltipNode getItemAttributeModifiersEntryTooltip(IServerUtils utils, ItemAttributeModifiers.Entry entry) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, entry.attribute()).key("ali.property.value.attribute"))
                .add(utils.getValueTooltip(utils, entry.modifier()).key("ali.property.branch.modifier"))
                .add(utils.getValueTooltip(utils, entry.slot()).key("ali.property.value.slot"));
    }

    @NotNull
    public static IKeyTooltipNode getAttributeModifierTooltip(IServerUtils utils, AttributeModifier modifier) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, modifier.id()).key("ali.property.value.id"))
                .add(utils.getValueTooltip(utils, modifier.amount()).key("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, modifier.operation()).key("ali.property.value.operation"));
    }

    @NotNull
    public static IKeyTooltipNode getPossibleEffectTooltip(IServerUtils utils, FoodProperties.PossibleEffect effect) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, effect.effect()).key("ali.property.value.effect"))
                .add(utils.getValueTooltip(utils, effect.probability()).key("ali.property.value.probability"));
    }

    @NotNull
    public static IKeyTooltipNode getMobEffectInstanceTooltip(IServerUtils utils, MobEffectInstance effect) {
        IKeyTooltipNode tooltip = utils.getValueTooltip(utils, effect.getEffect());

        tooltip.add(utils.getValueTooltip(utils, effect.getDuration()).key("ali.property.value.duration"));
        tooltip.add(utils.getValueTooltip(utils, effect.getAmplifier()).key("ali.property.value.amplifier"));
        tooltip.add(utils.getValueTooltip(utils, effect.isAmbient()).key("ali.property.value.ambient"));
        tooltip.add(utils.getValueTooltip(utils, effect.isVisible()).key("ali.property.value.is_visible"));
        tooltip.add(utils.getValueTooltip(utils, effect.showIcon()).key("ali.property.value.show_icon"));

        if (effect.hiddenEffect != null) {
            tooltip.add(utils.getValueTooltip(utils, effect.hiddenEffect).key("ali.property.value.hidden_effect"));
        }

        return tooltip;
    }

    @NotNull
    public static IKeyTooltipNode getRuleTooltip(IServerUtils utils, Tool.Rule rule) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, rule.blocks()).key("ali.property.branch.blocks"))
                .add(utils.getValueTooltip(utils, rule.correctForDrops()).key("ali.property.value.correct_for_drops"))
                .add(utils.getValueTooltip(utils, rule.speed()).key("ali.property.value.speed"));
    }

    @NotNull
    public static IKeyTooltipNode getMapDecorationEntryTooltip(IServerUtils utils, MapDecorations.Entry entry) {
        return utils.getValueTooltip(utils, entry.type())
                .add(utils.getValueTooltip(utils, entry.x()).key("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, entry.z()).key("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, entry.rotation()).key("ali.property.value.rotation"));
    }

    @NotNull
    public static IKeyTooltipNode getDataComponentMapTooltip(IServerUtils utils, DataComponentMap map) {
        if (!map.isEmpty()) {
            IKeyTooltipNode tooltip = BranchTooltipNode.branch();

            map.forEach((action) -> {
                IKeyTooltipNode t = utils.getValueTooltip(utils, action.type()).key("ali.property.value.null");

                t.add(utils.getDataComponentTypeTooltip(utils, action.type(), action.value()));
                tooltip.add(t);
            });

            return tooltip;
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getSuspiciousStewEffectEntryTooltip(IServerUtils utils, SuspiciousStewEffects.Entry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils, entry.duration()).key("ali.property.value.duration"));
    }

    @NotNull
    public static IKeyTooltipNode getGlobalPosTooltip(IServerUtils utils, GlobalPos globalPos) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, globalPos.dimension()).key("ali.property.value.dimension"))
                .add(utils.getValueTooltip(utils, globalPos.pos()).key("ali.property.multi.position"));
    }

    @NotNull
    public static IKeyTooltipNode getBeehiveBlockEntityOccupantTooltip(IServerUtils utils, BeehiveBlockEntity.Occupant occupant) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, occupant.entityData().copyTag().getAsString()).key("ali.property.value.entity_data"))
                .add(utils.getValueTooltip(utils, occupant.ticksInHive()).key("ali.property.value.ticks_in_hive"))
                .add(utils.getValueTooltip(utils, occupant.minTicksInHive()).key("ali.property.value.min_ticks_in_hive"));
    }

    @NotNull
    public static IKeyTooltipNode getAuthPropertyTooltip(IServerUtils utils, com.mojang.authlib.properties.Property property) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, property.name()).key("ali.property.value.name"))
                .add(utils.getValueTooltip(utils, property.value()).key("ali.property.value.value"));

        if (property.signature() != null) {
            tooltip.add(utils.getValueTooltip(utils, property.signature()).key("ali.property.value.signature"));
        }

        return tooltip;
    }

    @NotNull
    public static <A, B extends Predicate<A>> IKeyTooltipNode getCollectionCountsPredicateEntryTooltip(IServerUtils utils, CollectionCountsPredicate.Entry<A, B> entry) {
        return utils.getValueTooltip(utils, entry.test())
                .add(utils.getValueTooltip(utils, entry.count()).key("ali.property.value.count"));
    }

    @NotNull
    public static IKeyTooltipNode getBannerPatternLayersTooltip(IServerUtils utils, BannerPatternLayers bannerPatternLayers) {
        return utils.getValueTooltip(utils, bannerPatternLayers.layers());
    }

    @NotNull
    public static IKeyTooltipNode getBannerPatternLayerTooltip(IServerUtils utils, BannerPatternLayers.Layer layer) {
        return utils.getValueTooltip(utils, layer.pattern())
                .add(utils.getValueTooltip(utils, layer.color()).key("ali.property.value.color"));
    }

    @NotNull
    public static IKeyTooltipNode getGameTypePredicateTooltip(IServerUtils utils, GameTypePredicate gameType) {
        return utils.getValueTooltip(utils, gameType.types());
    }

    @NotNull
    public static IKeyTooltipNode getLevelBasedValueTooltip(IServerUtils utils, LevelBasedValue levelBasedValue) {
        IKeyTooltipNode tooltip = BranchTooltipNode.branch();

        switch (levelBasedValue) {
            case LevelBasedValue.Constant(float value) ->
                    tooltip.add(utils.getValueTooltip(utils, value).key("ali.property.value.constant"));
            case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                ITooltipNode t = BranchTooltipNode.branch("ali.property.branch.clamped")
                        .add(utils.getValueTooltip(utils, value).key("ali.property.branch.value"))
                        .add(utils.getValueTooltip(utils, min).key("ali.property.value.min"))
                        .add(utils.getValueTooltip(utils, max).key("ali.property.value.max"));
                tooltip.add(t);
            }
            case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                ITooltipNode t = BranchTooltipNode.branch("ali.property.branch.fraction")
                        .add(utils.getValueTooltip(utils, numerator).key("ali.property.branch.numerator"))
                        .add(utils.getValueTooltip(utils, denominator).key("ali.property.branch.denominator"));
                tooltip.add(t);
            }
            case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> {
                ITooltipNode t = BranchTooltipNode.branch("ali.property.branch.linear")
                        .add(utils.getValueTooltip(utils, base).key("ali.property.value.base"))
                        .add(utils.getValueTooltip(utils, perLevelAboveFirst).key("ali.property.value.per_level"));
                tooltip.add(t);
            }
            case LevelBasedValue.LevelsSquared(float added) -> {
                ITooltipNode t = BranchTooltipNode.branch("ali.property.branch.level_squared")
                        .add(utils.getValueTooltip(utils, added).key("ali.property.value.added"));
                tooltip.add(t);
            }
            case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                ITooltipNode t = BranchTooltipNode.branch("ali.property.branch.lookup")
                        .add(utils.getValueTooltip(utils, values.toString()).key("ali.property.value.values"))
                        .add(utils.getValueTooltip(utils, fallback).key("ali.property.branch.fallback"));
                tooltip.add(t);
            }
            default -> {
            }
        }

        return tooltip;
    }

    @NotNull
    public static IKeyTooltipNode getLocationWrapperTooltip(IServerUtils utils, EntityPredicate.LocationWrapper locationWrapper) {
        if (locationWrapper.located().isPresent() || locationWrapper.affectsMovement().isPresent() || locationWrapper.steppingOn().isPresent()) {
            return BranchTooltipNode.branch()
                    .add(utils.getValueTooltip(utils, locationWrapper.located()).key("ali.property.branch.located"))
                    .add(utils.getValueTooltip(utils, locationWrapper.steppingOn()).key("ali.property.branch.stepping_on_location"))
                    .add(utils.getValueTooltip(utils, locationWrapper.affectsMovement()).key("ali.property.branch.affects_movement"));
        }

        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static IKeyTooltipNode getMovementPredicateTooltip(IServerUtils utils, MovementPredicate predicate) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, predicate.x()).key("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, predicate.y()).key("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, predicate.z()).key("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, predicate.speed()).key("ali.property.value.speed"))
                .add(utils.getValueTooltip(utils, predicate.horizontalSpeed()).key("ali.property.value.horizontal_speed"))
                .add(utils.getValueTooltip(utils, predicate.verticalSpeed()).key("ali.property.value.vertical_speed"))
                .add(utils.getValueTooltip(utils, predicate.fallDistance()).key("ali.property.value.fall_distance"));
    }

    @NotNull
    public static IKeyTooltipNode getSlotPredicateTooltip(IServerUtils utils, SlotsPredicate predicate) {
        return getMapTooltip(utils, predicate.slots(), GenericTooltipUtils::getSlotRangePredicateEntryTooltip);
    }

    @NotNull
    public static <T> IKeyTooltipNode getEitherHolderTooltip(IServerUtils utils, EitherHolder<T> holder) {
        return holder.asEither().map((v) -> utils.getValueTooltip(utils, v.value()), (k) -> utils.getValueTooltip(utils, k));
    }
}
