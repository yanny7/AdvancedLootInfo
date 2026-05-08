package com.yanny.ali.plugin.server;

import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.advancements.criterion.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.predicates.AttributeModifiersPredicate;
import net.minecraft.core.component.predicates.FireworkExplosionPredicate;
import net.minecraft.core.component.predicates.WritableBookPredicate;
import net.minecraft.core.component.predicates.WrittenBookPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContextArg;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getDataComponentExactPredicateTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;

public class ValueTooltipUtils {
    @NotNull
    public static TooltipBuilder getFormulaTooltip(IServerUtils utils, ApplyBonusCount.Formula formula) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, formula.getType().id());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount(int extraRounds, float probability)) {
            tooltip.add(utils.getValueTooltip(utils, extraRounds).build("ali.property.value.extra_rounds"));
            tooltip.add(utils.getValueTooltip(utils, probability).build("ali.property.value.probability"));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount(int bonusMultiplier)) {
            tooltip.add(utils.getValueTooltip(utils, bonusMultiplier).build("ali.property.value.bonus_multiplier"));
        }

        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getModifierTooltip(IServerUtils utils, SetAttributesFunction.Modifier modifier) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, modifier.attribute()).build("ali.property.value.attribute"))
                .add(utils.getValueTooltip(utils, modifier.operation()).build("ali.property.value.operation"))
                .add(utils.getValueTooltip(utils, modifier.amount()).build("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, modifier.id()).build("ali.property.value.id"))
                .add(utils.getValueTooltip(utils, modifier.slots()).build("ali.property.branch.equipment_slots"))
        );
    }

    @NotNull
    public static TooltipBuilder getPairTooltip(IServerUtils utils, Pair<?, ?> pair) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, pair.getFirst());

        tooltip.add(utils.getValueTooltip(utils, pair.getSecond()).build("ali.property.value.color"));
        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getStatePropertiesPredicateTooltip(IServerUtils utils, StatePropertiesPredicate propertiesPredicate) {
        return GenericTooltipUtils.getCollectionTooltip(utils, propertiesPredicate.properties(), GenericTooltipUtils::getPropertyMatcherTooltip);
    }

    @NotNull
    public static TooltipBuilder getDamageSourcePredicateTooltip(IServerUtils utils, DamageSourcePredicate damagePredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, damagePredicate.tags()).build("ali.property.branch.tags"))
                .add(utils.getValueTooltip(utils, damagePredicate.directEntity()).build("ali.property.branch.direct_entity"))
                .add(utils.getValueTooltip(utils, damagePredicate.sourceEntity()).build("ali.property.branch.source_entity"))
                .add(utils.getValueTooltip(utils, damagePredicate.isDirect()).build("ali.property.value.is_direct"))
        );
    }

    @NotNull
    public static <T> TooltipBuilder getTagPredicateTooltip(IServerUtils ignoredUtils, TagPredicate<T> tagPredicate) {
        return TooltipBuilder.keyValue(tagPredicate.tag().location().toString(), Boolean.toString(tagPredicate.expected()));
    }

    @NotNull
    public static TooltipBuilder getEntityPredicateTooltip(IServerUtils utils, EntityPredicate entityPredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, entityPredicate.entityType()).build("ali.property.branch.entity_types"))
                .add(utils.getValueTooltip(utils, entityPredicate.distanceToPlayer()).build("ali.property.branch.distance_to_player"))
                .add(utils.getValueTooltip(utils, entityPredicate.location()).build("ali.property.branch.location"))
                .add(utils.getValueTooltip(utils, entityPredicate.movement()).build("ali.property.branch.movement"))
                .add(utils.getValueTooltip(utils, entityPredicate.periodicTick()).build("ali.property.value.periodic_tick"))
                .add(utils.getValueTooltip(utils, entityPredicate.effects()).build("ali.property.branch.mob_effects"))
                .add(utils.getValueTooltip(utils, entityPredicate.nbt()).build("ali.property.value.nbt"))
                .add(utils.getValueTooltip(utils, entityPredicate.flags()).build("ali.property.branch.entity_flags"))
                .add(utils.getValueTooltip(utils, entityPredicate.equipment()).build("ali.property.branch.entity_equipment"))
                .add(utils.getValueTooltip(utils, entityPredicate.subPredicate()).build("ali.property.branch.entity_sub_predicate"))
                .add(utils.getValueTooltip(utils, entityPredicate.vehicle()).build("ali.property.branch.vehicle"))
                .add(utils.getValueTooltip(utils, entityPredicate.passenger()).build("ali.property.branch.passenger"))
                .add(utils.getValueTooltip(utils, entityPredicate.targetedEntity()).build("ali.property.branch.targeted_entity"))
                .add(utils.getValueTooltip(utils, entityPredicate.team()).build("ali.property.value.team"))
                .add(utils.getValueTooltip(utils, entityPredicate.slots()).build("ali.property.branch.slots"))
                .add(utils.getValueTooltip(utils, entityPredicate.components()).build("ali.property.branch.components"))
        );
    }

    @NotNull
    public static TooltipBuilder getEntityTypePredicateTooltip(IServerUtils utils, EntityTypePredicate entityTypePredicate) {
        return utils.getValueTooltip(utils, entityTypePredicate.types());
    }

    @NotNull
    public static TooltipBuilder getDistancePredicateTooltip(IServerUtils utils, DistancePredicate distancePredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, distancePredicate.x()).build("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, distancePredicate.y()).build("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, distancePredicate.z()).build("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, distancePredicate.horizontal()).build("ali.property.value.horizontal"))
                .add(utils.getValueTooltip(utils, distancePredicate.absolute()).build("ali.property.value.absolute"))
        );
    }

    @NotNull
    public static TooltipBuilder getLocationPredicateTooltip(IServerUtils utils, LocationPredicate locationPredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, locationPredicate.position()).build("ali.property.branch.position"))
                .add(utils.getValueTooltip(utils, locationPredicate.biomes()).build("ali.property.branch.biomes"))
                .add(utils.getValueTooltip(utils, locationPredicate.structures()).build("ali.property.branch.structures"))
                .add(utils.getValueTooltip(utils, locationPredicate.dimension()).build("ali.property.value.dimension"))
                .add(utils.getValueTooltip(utils, locationPredicate.smokey()).build("ali.property.value.smokey"))
                .add(utils.getValueTooltip(utils, locationPredicate.light()).build("ali.property.value.light"))
                .add(utils.getValueTooltip(utils, locationPredicate.block()).build("ali.property.branch.block_predicate"))
                .add(utils.getValueTooltip(utils, locationPredicate.fluid()).build("ali.property.branch.fluid_predicate"))
                .add(utils.getValueTooltip(utils, locationPredicate.canSeeSky()).build("ali.property.value.can_see_sky"))
        );
    }

    @NotNull
    public static TooltipBuilder getPositionPredicateTooltip(IServerUtils utils, LocationPredicate.PositionPredicate positionPredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, positionPredicate.x()).build("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, positionPredicate.y()).build("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, positionPredicate.z()).build("ali.property.value.z"))
        );
    }

    @NotNull
    public static TooltipBuilder getLightPredicateTooltip(IServerUtils utils, LightPredicate lightPredicate) {
        return utils.getValueTooltip(utils, lightPredicate.composite());
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicate blockPredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, blockPredicate.blocks()).build("ali.property.branch.blocks"))
                .add(utils.getValueTooltip(utils, blockPredicate.properties()).build("ali.property.branch.properties"))
                .add(utils.getValueTooltip(utils, blockPredicate.nbt()).build("ali.property.value.nbt"))
                .add(utils.getValueTooltip(utils, blockPredicate.components()).build("ali.property.branch.components"))
        );
    }

    @NotNull
    public static TooltipBuilder getNbtPredicateTooltip(IServerUtils utils, NbtPredicate nbtPredicate) {
        return utils.getValueTooltip(utils, nbtPredicate.tag());
    }

    @NotNull
    public static TooltipBuilder getFluidPredicateTooltip(IServerUtils utils, FluidPredicate fluidPredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, fluidPredicate.fluids()).build("ali.property.branch.fluids"))
                .add(utils.getValueTooltip(utils, fluidPredicate.properties()).build("ali.property.branch.properties"))
        );
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateTooltip(IServerUtils utils, MobEffectsPredicate mobEffectsPredicate) {
        return getMapTooltip(utils, mobEffectsPredicate.effectMap(), GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
    }

    @NotNull
    public static TooltipBuilder getEntityFlagsPredicateTooltip(IServerUtils utils, EntityFlagsPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.isOnGround()).build("ali.property.value.is_on_ground"))
                .add(utils.getValueTooltip(utils, predicate.isOnFire()).build("ali.property.value.is_on_fire"))
                .add(utils.getValueTooltip(utils, predicate.isBaby()).build("ali.property.value.is_baby"))
                .add(utils.getValueTooltip(utils, predicate.isCrouching()).build("ali.property.value.is_crouching"))
                .add(utils.getValueTooltip(utils, predicate.isSprinting()).build("ali.property.value.is_sprinting"))
                .add(utils.getValueTooltip(utils, predicate.isSwimming()).build("ali.property.value.is_swimming"))
                .add(utils.getValueTooltip(utils, predicate.isFlying()).build("ali.property.value.is_flying"))
        );
    }

    @NotNull
    public static TooltipBuilder getEntityEquipmentPredicateTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.head()).build("ali.property.branch.head"))
                .add(utils.getValueTooltip(utils, predicate.chest()).build("ali.property.branch.chest"))
                .add(utils.getValueTooltip(utils, predicate.legs()).build("ali.property.branch.legs"))
                .add(utils.getValueTooltip(utils, predicate.feet()).build("ali.property.branch.feet"))
                .add(utils.getValueTooltip(utils, predicate.body()).build("ali.property.branch.body"))
                .add(utils.getValueTooltip(utils, predicate.mainhand()).build("ali.property.branch.mainhand"))
                .add(utils.getValueTooltip(utils, predicate.offhand()).build("ali.property.branch.offhand"))
        );
    }

    @NotNull
    public static TooltipBuilder getItemPredicateTooltip(IServerUtils utils, ItemPredicate itemPredicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, itemPredicate.items()).build("ali.property.branch.items"))
                .add(utils.getValueTooltip(utils, itemPredicate.count()).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, itemPredicate.components()).build("ali.property.branch.components"))
        );
    }

    @NotNull
    public static TooltipBuilder getEnchantmentPredicateTooltip(IServerUtils utils, EnchantmentPredicate enchantmentPredicate) {
        return utils.getValueTooltip(utils, (enchantmentPredicate.enchantments().isPresent() ? enchantmentPredicate.enchantments() : Component.translatable("ali.util.advanced_loot_info.any")))
                .add(utils.getValueTooltip(utils, enchantmentPredicate.level()).build("ali.property.value.level"));
    }

    @NotNull
    public static TooltipBuilder getEntitySubPredicateTooltip(IServerUtils utils, EntitySubPredicate entitySubPredicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getEntitySubPredicateTooltip(utils, entitySubPredicate))); //TODO Fixme
    }

    @NotNull
    public static TooltipBuilder getBlockPosTooltip(IServerUtils ignoredUtils, BlockPos pos) {
        return TooltipBuilder.value(pos.getX(), pos.getY(), pos.getZ());
    }

    @NotNull
    public static TooltipBuilder getCopyOperationTooltip(IServerUtils utils, CopyCustomDataFunction.CopyOperation copyOperation) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, copyOperation.sourcePath()).build("ali.property.value.source_path"))
                .add(utils.getValueTooltip(utils, copyOperation.targetPath()).build("ali.property.value.target_path"))
                .add(utils.getValueTooltip(utils, copyOperation.op()).build("ali.property.value.merge_strategy"))
        );
    }

    @NotNull
    public static TooltipBuilder getEffectEntryTooltip(IServerUtils utils, SetStewEffectFunction.EffectEntry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils,entry.duration()).build("ali.property.value.duration"));
    }

    @NotNull
    public static TooltipBuilder getAdvancementDonePredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementDonePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.state()).key("ali.property.value.done");
    }

    @NotNull
    public static TooltipBuilder getAdvancementCriterionsPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementCriterionsPredicate predicate) {
        return getMapTooltip(utils, predicate.criterions(), GenericTooltipUtils::getCriterionEntryTooltip).key("ali.property.branch.criterions");
    }

    @NotNull
    public static TooltipBuilder getItemStackTooltip(IServerUtils utils, ItemStack item) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, item.getItem()).build("ali.property.value.item"))
                .add(utils.getValueTooltip(utils, item.getCount()).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, item.getComponents()).build("ali.property.branch.components"))
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
    public static TooltipBuilder getListOperationTooltip(IServerUtils utils, ListOperation operation) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, operation.mode());

        if (operation instanceof ListOperation.Insert(int offset)) {
            tooltip.add(utils.getValueTooltip(utils, offset).build("ali.property.value.offset"));
        } else if (operation instanceof ListOperation.ReplaceSection(int offset, Optional<Integer> size)) {
            tooltip.add(utils.getValueTooltip(utils, offset).build("ali.property.value.offset"));
            tooltip.add(utils.getValueTooltip(utils, size).build("ali.property.value.size"));
        }

        return tooltip;
    }

    @NotNull
    public static <T> TooltipBuilder getFilterableTooltip(IServerUtils utils, Filterable<T> data) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, data.raw()).build("ali.property.value.raw"))
                .add(utils.getValueTooltip(utils, data.filtered()).build("ali.property.value.filtered"))
        );
    }

    @NotNull
    public static TooltipBuilder getFireworkPredicateTooltip(IServerUtils utils, FireworkExplosionPredicate.FireworkPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.shape()).build("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, predicate.trail()).build("ali.property.value.trail"))
                .add(utils.getValueTooltip(utils, predicate.twinkle()).build("ali.property.value.twinkle"))
        );
    }

    @NotNull
    public static TooltipBuilder getContainerComponentManipulatorTooltip(IServerUtils utils, ContainerComponentManipulator<?> component) {
        return utils.getValueTooltip(utils, component.type());
    }

    @NotNull
    public static TooltipBuilder getNbtPathTooltip(IServerUtils utils, NbtPathArgument.NbtPath provider) {
        return utils.getValueTooltip(utils, provider.asString());
    }

    @NotNull
    public static TooltipBuilder getTypedDataComponentTooltip(IServerUtils utils, TypedDataComponent<?> typedDataComponent) {
        return utils.getValueTooltip(utils, typedDataComponent.type())
                .add(utils.getDataComponentTypeTooltip(utils, typedDataComponent.type(), typedDataComponent.value()));
    }

    @NotNull
    public static TooltipBuilder getPagePredicateTooltip(IServerUtils ignoredUtils, WritableBookPredicate.PagePredicate predicate) {
        return TooltipBuilder.value(predicate.contents());
    }

    @NotNull
    public static TooltipBuilder getPagePredicateTooltip(IServerUtils utils, WrittenBookPredicate.PagePredicate predicate) {
        return TooltipBuilder.component(Objects.requireNonNull(utils.lookupProvider()), predicate.contents());
    }

    @NotNull
    public static TooltipBuilder getEntryPredicateTooltip(IServerUtils utils, AttributeModifiersPredicate.EntryPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.attribute()).build("ali.property.branch.attributes"))
                .add(utils.getValueTooltip(utils, predicate.id()).build("ali.property.value.id"))
                .add(utils.getValueTooltip(utils, predicate.amount()).build("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, predicate.operation()).build("ali.property.value.operation"))
                .add(utils.getValueTooltip(utils, predicate.slot()).build("ali.property.value.slot"))
        );
    }

    @NotNull
    public static TooltipBuilder getDataComponentPatchTooltip(IServerUtils utils, DataComponentPatch data) {
        return getMapTooltip(utils, data.map, GenericTooltipUtils::getDataComponentPatchEntryTooltip);
    }

    @NotNull
    public static TooltipBuilder getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion data) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, data.shape()).build("ali.property.value.shape"))
                .add(utils.getValueTooltip(utils, data.colors()).build("ali.property.value.colors"))
                .add(utils.getValueTooltip(utils, data.fadeColors()).build("ali.property.value.fade_colors"))
                .add(utils.getValueTooltip(utils, data.hasTrail()).build("ali.property.value.has_trail"))
                .add(utils.getValueTooltip(utils, data.hasTwinkle()).build("ali.property.value.has_twinkle"))
        );
    }

    @NotNull
    public static TooltipBuilder getItemAttributeModifiersEntryTooltip(IServerUtils utils, ItemAttributeModifiers.Entry entry) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, entry.attribute()).build("ali.property.value.attribute"))
                .add(utils.getValueTooltip(utils, entry.modifier()).build("ali.property.branch.modifier"))
                .add(utils.getValueTooltip(utils, entry.slot()).build("ali.property.value.slot"))
        );
    }

    @NotNull
    public static TooltipBuilder getAttributeModifierTooltip(IServerUtils utils, AttributeModifier modifier) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, modifier.id()).build("ali.property.value.id"))
                .add(utils.getValueTooltip(utils, modifier.amount()).build("ali.property.value.amount"))
                .add(utils.getValueTooltip(utils, modifier.operation()).build("ali.property.value.operation"))
        );
    }

    @NotNull
    public static TooltipBuilder getMobEffectInstanceTooltip(IServerUtils utils, MobEffectInstance effect) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, effect.getEffect());

        tooltip.add(utils.getValueTooltip(utils, effect.getDuration()).build("ali.property.value.duration"));
        tooltip.add(utils.getValueTooltip(utils, effect.getAmplifier()).build("ali.property.value.amplifier"));
        tooltip.add(utils.getValueTooltip(utils, effect.isAmbient()).build("ali.property.value.ambient"));
        tooltip.add(utils.getValueTooltip(utils, effect.isVisible()).build("ali.property.value.is_visible"));
        tooltip.add(utils.getValueTooltip(utils, effect.showIcon()).build("ali.property.value.show_icon"));

        if (effect.hiddenEffect != null) {
            tooltip.add(utils.getValueTooltip(utils, effect.hiddenEffect).build("ali.property.value.hidden_effect"));
        }

        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getRuleTooltip(IServerUtils utils, Tool.Rule rule) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, rule.blocks()).build("ali.property.branch.blocks"))
                .add(utils.getValueTooltip(utils, rule.correctForDrops()).build("ali.property.value.correct_for_drops"))
                .add(utils.getValueTooltip(utils, rule.speed()).build("ali.property.value.speed"))
        );
    }

    @NotNull
    public static TooltipBuilder getMapDecorationEntryTooltip(IServerUtils utils, MapDecorations.Entry entry) {
        return utils.getValueTooltip(utils, entry.type())
                .add(utils.getValueTooltip(utils, entry.x()).build("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, entry.z()).build("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, entry.rotation()).build("ali.property.value.rotation"));
    }

    @NotNull
    public static TooltipBuilder getDataComponentMapTooltip(IServerUtils utils, DataComponentMap map) {
        if (!map.isEmpty()) {
            return TooltipBuilder.array((b) -> {
                map.forEach((action) -> {
                    TooltipBuilder t = utils.getValueTooltip(utils, action.type());

                    t.add(utils.getDataComponentTypeTooltip(utils, action.type(), action.value()));
                    b.add(t.build("aci.util.null"));
                });
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getSuspiciousStewEffectEntryTooltip(IServerUtils utils, SuspiciousStewEffects.Entry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils, entry.duration()).build("ali.property.value.duration"));
    }

    @NotNull
    public static TooltipBuilder getGlobalPosTooltip(IServerUtils utils, GlobalPos globalPos) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, globalPos.dimension()).build("ali.property.value.dimension"))
                .add(utils.getValueTooltip(utils, globalPos.pos()).build("ali.property.multi.position"))
        );
    }

    @NotNull
    public static TooltipBuilder getBeehiveBlockEntityOccupantTooltip(IServerUtils utils, BeehiveBlockEntity.Occupant occupant) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, occupant.entityData().copyTagWithoutId().toString()).build("ali.property.value.entity_data"))
                .add(utils.getValueTooltip(utils, occupant.ticksInHive()).build("ali.property.value.ticks_in_hive"))
                .add(utils.getValueTooltip(utils, occupant.minTicksInHive()).build("ali.property.value.min_ticks_in_hive"))
        );
    }

    @NotNull
    public static TooltipBuilder getAuthPropertyTooltip(IServerUtils utils, Property property) {
        return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, property.name()).build("ali.property.value.name"));
                b.add(utils.getValueTooltip(utils, property.value()).build("ali.property.value.value"));

                if (property.signature() != null) {
                    b.add(utils.getValueTooltip(utils, property.signature()).build("ali.property.value.signature"));
                }
        });
    }

    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionCountsPredicateEntryTooltip(IServerUtils utils, CollectionCountsPredicate.Entry<A, B> entry) {
        return utils.getValueTooltip(utils, entry.test())
                .add(utils.getValueTooltip(utils, entry.count()).build("ali.property.value.count"));
    }

    @NotNull
    public static TooltipBuilder getBannerPatternLayersTooltip(IServerUtils utils, BannerPatternLayers bannerPatternLayers) {
        return utils.getValueTooltip(utils, bannerPatternLayers.layers());
    }

    @NotNull
    public static TooltipBuilder getBannerPatternLayerTooltip(IServerUtils utils, BannerPatternLayers.Layer layer) {
        return utils.getValueTooltip(utils, layer.pattern())
                .add(utils.getValueTooltip(utils, layer.color()).build("ali.property.value.color"));
    }

    @NotNull
    public static TooltipBuilder getGameTypePredicateTooltip(IServerUtils utils, GameTypePredicate gameType) {
        return utils.getValueTooltip(utils, gameType.types());
    }

    @NotNull
    public static TooltipBuilder getLevelBasedValueTooltip(IServerUtils utils, LevelBasedValue levelBasedValue) {
        return TooltipBuilder.array((b) -> {
            switch (levelBasedValue) {
                case LevelBasedValue.Constant(float value) ->
                        b.add(utils.getValueTooltip(utils, value).build("ali.property.value.constant"));
                case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) -> {
                    b.add(TooltipBuilder.array((c) -> c
                            .add(utils.getValueTooltip(utils, value).build("ali.property.branch.value"))
                            .add(utils.getValueTooltip(utils, min).build("ali.property.value.min"))
                            .add(utils.getValueTooltip(utils, max).build("ali.property.value.max"))
                            .build("ali.property.branch.clamped")
                    ));
                }
                case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) -> {
                    b.add(TooltipBuilder.array((c) -> c
                            .add(utils.getValueTooltip(utils, numerator).build("ali.property.branch.numerator"))
                            .add(utils.getValueTooltip(utils, denominator).build("ali.property.branch.denominator"))
                            .build("ali.property.branch.fraction")
                    ));
                }
                case LevelBasedValue.Linear(float base, float perLevelAboveFirst) -> {
                    b.add(TooltipBuilder.array((c) -> c
                            .add(utils.getValueTooltip(utils, base).build("ali.property.value.base"))
                            .add(utils.getValueTooltip(utils, perLevelAboveFirst).build("ali.property.value.per_level"))
                            .build("ali.property.branch.linear")
                    ));
                }
                case LevelBasedValue.LevelsSquared(float added) -> {
                    b.add(TooltipBuilder.array((c) -> c
                            .add(utils.getValueTooltip(utils, added).build("ali.property.value.added"))
                            .build("ali.property.branch.level_squared")
                    ));
                }
                case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) -> {
                    b.add(TooltipBuilder.array((c) -> c
                            .add(utils.getValueTooltip(utils, values.toString()).build("ali.property.value.values"))
                            .add(utils.getValueTooltip(utils, fallback).build("ali.property.branch.fallback"))
                            .build("ali.property.branch.lookup")
                    ));
                }
                default -> {}
            }
        });
    }

    @NotNull
    public static TooltipBuilder getLocationWrapperTooltip(IServerUtils utils, EntityPredicate.LocationWrapper locationWrapper) {
        if (locationWrapper.located().isPresent() || locationWrapper.affectsMovement().isPresent() || locationWrapper.steppingOn().isPresent()) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, locationWrapper.located()).build("ali.property.branch.located"))
                    .add(utils.getValueTooltip(utils, locationWrapper.steppingOn()).build("ali.property.branch.stepping_on_location"))
                    .add(utils.getValueTooltip(utils, locationWrapper.affectsMovement()).build("ali.property.branch.affects_movement"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getMovementPredicateTooltip(IServerUtils utils, MovementPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.x()).build("ali.property.value.x"))
                .add(utils.getValueTooltip(utils, predicate.y()).build("ali.property.value.y"))
                .add(utils.getValueTooltip(utils, predicate.z()).build("ali.property.value.z"))
                .add(utils.getValueTooltip(utils, predicate.speed()).build("ali.property.value.speed"))
                .add(utils.getValueTooltip(utils, predicate.horizontalSpeed()).build("ali.property.value.horizontal_speed"))
                .add(utils.getValueTooltip(utils, predicate.verticalSpeed()).build("ali.property.value.vertical_speed"))
                .add(utils.getValueTooltip(utils, predicate.fallDistance()).build("ali.property.value.fall_distance"))
        );
    }

    @NotNull
    public static TooltipBuilder getSlotPredicateTooltip(IServerUtils utils, SlotsPredicate predicate) {
        return getMapTooltip(utils, predicate.slots(), GenericTooltipUtils::getSlotRangePredicateEntryTooltip);
    }

    @NotNull
    public static TooltipBuilder getInputPredicateTooltip(IServerUtils utils, InputPredicate predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.forward()).build("ali.property.value.forward"))
                .add(utils.getValueTooltip(utils, predicate.backward()).build("ali.property.value.backward"))
                .add(utils.getValueTooltip(utils, predicate.left()).build("ali.property.value.left"))
                .add(utils.getValueTooltip(utils, predicate.right()).build("ali.property.value.right"))
                .add(utils.getValueTooltip(utils, predicate.jump()).build("ali.property.value.jump"))
                .add(utils.getValueTooltip(utils, predicate.sneak()).build("ali.property.value.sneak"))
                .add(utils.getValueTooltip(utils, predicate.sprint()).build("ali.property.value.sprint"))
        );
    }

    @NotNull
    public static <T> TooltipBuilder getStandaloneTooltip(IServerUtils utils, ListOperation.StandAlone<T> predicate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, predicate.value()).build("ali.property.branch.values"))
                .add(utils.getValueTooltip(utils, predicate.operation()).build("ali.property.value.list_operation"))
        );
    }

    @NotNull
    public static TooltipBuilder getDamageReductionTooltip(IServerUtils utils, BlocksAttacks.DamageReduction value) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, value.horizontalBlockingAngle()).build("ali.property.value.horizontal_blocking_angle"))
                .add(utils.getValueTooltip(utils, value.type()).build("ali.property.branch.damage_types"))
                .add(utils.getValueTooltip(utils, value.base()).build("ali.property.value.base"))
                .add(utils.getValueTooltip(utils, value.factor()).build("ali.property.value.factor"))
        );
    }

    @NotNull
    public static TooltipBuilder getItemDamageTooltip(IServerUtils utils, BlocksAttacks.ItemDamageFunction value) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, value.threshold()).build("ali.property.value.threshold"))
                .add(utils.getValueTooltip(utils, value.base()).build("ali.property.value.base"))
                .add(utils.getValueTooltip(utils, value.factor()).build("ali.property.value.factor"))
        );
    }

    @NotNull
    public static TooltipBuilder getDataComponentMatchersTooltip(IServerUtils utils, DataComponentMatchers dataComponentMatchers) {
        if (!dataComponentMatchers.partial().isEmpty() || !dataComponentMatchers.exact().isEmpty()) {
            return TooltipBuilder.array((b) -> b
                    .add(getDataComponentExactPredicateTooltip(utils, dataComponentMatchers.exact()))
                    .add(getMapTooltip(utils, dataComponentMatchers.partial(), GenericTooltipUtils::getDataComponentPredicateEntryTooltip).build("ali.property.branch.partial_matchers"))
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static IKeyTooltipNode getLootContextArgTooltip(IServerUtils utils, LootContextArg<?> lootContextArg) {
        return utils.getValueTooltip(utils, lootContextArg.contextParam());
    }

    @NotNull
    public static IKeyTooltipNode getContextKeyTooltip(IServerUtils utils, ContextKey<?> contextKey) {
        return utils.getValueTooltip(utils, contextKey.name());
    }

    @NotNull
    public static IKeyTooltipNode getSlotRangeTooltip(IServerUtils utils, SlotRange slotRange) {
        return utils.getValueTooltip(utils, slotRange.toString());
    }

    @NotNull
    public static IKeyTooltipNode getKineticWeaponConditionTooltip(IServerUtils utils, KineticWeapon.Condition condition) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, condition.maxDurationTicks()).build("ali.property.value.max_duration_ticks"))
                .add(utils.getValueTooltip(utils, condition.minSpeed()).build("ali.property.value.min_speed"))
                .add(utils.getValueTooltip(utils, condition.minRelativeSpeed()).build("ali.property.value.min_relative_speed"));
    }
}
