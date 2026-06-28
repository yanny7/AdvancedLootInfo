package com.yanny.ali.plugin.server;

import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.advancements.predicates.*;
import net.minecraft.advancements.predicates.entity.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.*;
import net.minecraft.core.component.predicates.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContextArg;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getMapTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getTranslationKey;

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
    public static TooltipBuilder getDataComponentPredicateTooltip(IServerUtils utils, DataComponentPredicate itemSubPredicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getDataComponentPredicateTooltip(utils, itemSubPredicate)));
    }

    @NotNull
    public static TooltipBuilder getConsumeEffectTooltip(IServerUtils utils, ConsumeEffect consumeEffect) {
        return TooltipBuilder.array((b) -> b.add(utils.getConsumeEffectTooltip(utils, consumeEffect)));
    }

    @NotNull
    public static TooltipBuilder getFormulaTooltip(IServerUtils utils, ApplyBonusCount.Formula formula) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, formula.getType().id());

        if (formula instanceof ApplyBonusCount.BinomialWithBonusCount(int extraRounds, float probability)) {
            tooltip.add(utils.getValueTooltip(utils, extraRounds).build(Lang.Value.EXTRA_ROUNDS));
            tooltip.add(utils.getValueTooltip(utils, probability).build(Lang.Value.PROBABILITY));
        } else if (formula instanceof ApplyBonusCount.UniformBonusCount(int bonusMultiplier)) {
            tooltip.add(utils.getValueTooltip(utils, bonusMultiplier).build(Lang.Value.BONUS_MULTIPLIER));
        }

        return tooltip;
    }

    @NotNull
    public static TooltipBuilder getModifierTooltip(IServerUtils utils, SetAttributesFunction.Modifier modifier) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, modifier.attribute()).build(Lang.Value.ATTRIBUTE));
            b.add(utils.getValueTooltip(utils, modifier.operation()).build(Lang.Value.OPERATION));
            b.add(utils.getValueTooltip(utils, modifier.amount()).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, modifier.id()).build(Lang.Value.ID));
            b.add(utils.getValueTooltip(utils, modifier.slots()).build(Lang.Branch.EQUIPMENT_SLOTS));
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
        return utils.getValueTooltip(utils, propertiesPredicate.properties());
    }

    @NotNull
    public static TooltipBuilder getDamageSourcePredicateTooltip(IServerUtils utils, DamageSourcePredicate damagePredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, damagePredicate.tags()).build(Lang.Branch.TAGS));
            b.add(utils.getValueTooltip(utils, damagePredicate.directEntity()).build(Lang.Branch.DIRECT_ENTITY));
            b.add(utils.getValueTooltip(utils, damagePredicate.sourceEntity()).build(Lang.Branch.SOURCE_ENTITY));
            b.add(utils.getValueTooltip(utils, damagePredicate.isDirect()).build(Lang.Value.IS_DIRECT));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static <T> TooltipBuilder getTagPredicateTooltip(IServerUtils ignoredUtils, TagPredicate<T> tagPredicate) {
        return TooltipBuilder.keyValue(tagPredicate.tag().location().toString(), Boolean.toString(tagPredicate.expected()));
    }

    @NotNull
    public static TooltipBuilder getEntityPredicateTooltip(IServerUtils utils, EntityPredicate entityPredicate) {
        return TooltipBuilder.array((b) -> {
            for (Map.Entry<Codec<? extends EntitySubPredicate>, EntitySubPredicate> entry : entityPredicate.parts.entrySet()) {
                //noinspection unchecked
                b.add(utils.getEntitySubPredicateTooltip(utils, (Codec<EntitySubPredicate>) entry.getKey(), entry.getValue()));
            }
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getEntityTypePredicateTooltip(IServerUtils utils, EntityTypePredicate entityTypePredicate) {
        return utils.getValueTooltip(utils, entityTypePredicate.types());
    }

    @NotNull
    public static TooltipBuilder getDistancePredicateTooltip(IServerUtils utils, DistancePredicate distancePredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, distancePredicate.x()).build(Lang.Value.X));
            b.add(utils.getValueTooltip(utils, distancePredicate.y()).build(Lang.Value.Y));
            b.add(utils.getValueTooltip(utils, distancePredicate.z()).build(Lang.Value.Z));
            b.add(utils.getValueTooltip(utils, distancePredicate.horizontal()).build(Lang.Value.HORIZONTAL));
            b.add(utils.getValueTooltip(utils, distancePredicate.absolute()).build(Lang.Value.ABSOLUTE));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getLocationPredicateTooltip(IServerUtils utils, LocationPredicate locationPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, locationPredicate.position()).build(Lang.Branch.POSITION));
            b.add(utils.getValueTooltip(utils, locationPredicate.biomes()).build(Lang.Branch.BIOMES));
            b.add(utils.getValueTooltip(utils, locationPredicate.structures()).build(Lang.Branch.STRUCTURES));
            b.add(utils.getValueTooltip(utils, locationPredicate.dimension()).build(Lang.Value.DIMENSION));
            b.add(utils.getValueTooltip(utils, locationPredicate.smokey()).build(Lang.Value.SMOKEY));
            b.add(utils.getValueTooltip(utils, locationPredicate.light()).build(Lang.Value.LIGHT));
            b.add(utils.getValueTooltip(utils, locationPredicate.block()).build(Lang.Branch.BLOCK_PREDICATE));
            b.add(utils.getValueTooltip(utils, locationPredicate.fluid()).build(Lang.Branch.FLUID_PREDICATE));
            b.add(utils.getValueTooltip(utils, locationPredicate.canSeeSky()).build(Lang.Value.CAN_SEE_SKY));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getPositionPredicateTooltip(IServerUtils utils, LocationPredicate.PositionPredicate positionPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, positionPredicate.x()).build(Lang.Value.X));
            b.add(utils.getValueTooltip(utils, positionPredicate.y()).build(Lang.Value.Y));
            b.add(utils.getValueTooltip(utils, positionPredicate.z()).build(Lang.Value.Z));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getLightPredicateTooltip(IServerUtils utils, LightPredicate lightPredicate) {
        return utils.getValueTooltip(utils, lightPredicate.composite());
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicate blockPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, blockPredicate.blocks()).build(Lang.Branch.BLOCKS));
            b.add(utils.getValueTooltip(utils, blockPredicate.properties()).build(Lang.Branch.PROPERTIES));
            b.add(utils.getValueTooltip(utils, blockPredicate.nbt()).build(Lang.Value.NBT));
            b.add(utils.getValueTooltip(utils, blockPredicate.components()).build(Lang.Branch.COMPONENTS));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getNbtPredicateTooltip(IServerUtils utils, NbtPredicate nbtPredicate) {
        return utils.getValueTooltip(utils, nbtPredicate.tag());
    }

    @NotNull
    public static TooltipBuilder getFluidPredicateTooltip(IServerUtils utils, FluidPredicate fluidPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fluidPredicate.fluids()).build(Lang.Branch.FLUIDS));
            b.add(utils.getValueTooltip(utils, fluidPredicate.properties()).build(Lang.Branch.PROPERTIES));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getMobEffectPredicateTooltip(IServerUtils utils, MobEffectsPredicate mobEffectsPredicate) {
        return getMapTooltip(utils, mobEffectsPredicate.effectMap(), GenericTooltipUtils::getMobEffectPredicateEntryTooltip);
    }

    @NotNull
    public static TooltipBuilder getEntityFlagsPredicateTooltip(IServerUtils utils, EntityFlagsPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.isOnGround()).build(Lang.Value.IS_ON_GROUND));
            b.add(utils.getValueTooltip(utils, predicate.isOnFire()).build(Lang.Value.IS_ON_FIRE));
            b.add(utils.getValueTooltip(utils, predicate.isBaby()).build(Lang.Value.IS_BABY));
            b.add(utils.getValueTooltip(utils, predicate.isCrouching()).build(Lang.Value.IS_CROUCHING));
            b.add(utils.getValueTooltip(utils, predicate.isSprinting()).build(Lang.Value.IS_SPRINTING));
            b.add(utils.getValueTooltip(utils, predicate.isSwimming()).build(Lang.Value.IS_SWIMMING));
            b.add(utils.getValueTooltip(utils, predicate.isFlying()).build(Lang.Value.IS_FLYING));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getEntityEquipmentPredicateTooltip(IServerUtils utils, EntityEquipmentPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.head()).build(Lang.Branch.HEAD));
            b.add(utils.getValueTooltip(utils, predicate.chest()).build(Lang.Branch.CHEST));
            b.add(utils.getValueTooltip(utils, predicate.legs()).build(Lang.Branch.LEGS));
            b.add(utils.getValueTooltip(utils, predicate.feet()).build(Lang.Branch.FEET));
            b.add(utils.getValueTooltip(utils, predicate.body()).build(Lang.Branch.BODY));
            b.add(utils.getValueTooltip(utils, predicate.mainhand()).build(Lang.Branch.MAINHAND));
            b.add(utils.getValueTooltip(utils, predicate.offhand()).build(Lang.Branch.OFFHAND));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getItemPredicateTooltip(IServerUtils utils, ItemPredicate itemPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, itemPredicate.items()).build(Lang.Branch.ITEMS));
            b.add(utils.getValueTooltip(utils, itemPredicate.count()).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, itemPredicate.components()).build(Lang.Branch.COMPONENTS));
        }).key(Lang.Branch.PREDICATE);
    }

    @NotNull
    public static TooltipBuilder getEnchantmentPredicateTooltip(IServerUtils utils, EnchantmentPredicate enchantmentPredicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, enchantmentPredicate.enchantments()).build(Lang.Branch.ENCHANTMENTS));
            b.add(utils.getValueTooltip(utils, enchantmentPredicate.level()).build(Lang.Value.LEVEL));
        }).key(Lang.Branch.ENCHANTMENT);
    }

    @NotNull
    public static TooltipBuilder getBlockPosTooltip(IServerUtils ignoredUtils, BlockPos pos) {
        return TooltipBuilder.value(pos.getX(), pos.getY(), pos.getZ());
    }

    @NotNull
    public static TooltipBuilder getCopyOperationTooltip(IServerUtils utils, CopyCustomDataFunction.CopyOperation copyOperation) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, copyOperation.sourcePath()).build(Lang.Value.SOURCE_PATH));
            b.add(utils.getValueTooltip(utils, copyOperation.targetPath()).build(Lang.Value.TARGET_PATH));
            b.add(utils.getValueTooltip(utils, copyOperation.op()).build(Lang.Value.MERGE_STRATEGY));
        }).key(Lang.Branch.OPERATION);
    }

    @NotNull
    public static TooltipBuilder getEffectEntryTooltip(IServerUtils utils, SetStewEffectFunction.EffectEntry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils,entry.duration()).build(Lang.Value.DURATION));
    }

    @NotNull
    public static TooltipBuilder getAdvancementDonePredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementDonePredicate predicate) {
        return utils.getValueTooltip(utils, predicate.state()).key(Lang.Value.DONE);
    }

    @NotNull
    public static TooltipBuilder getAdvancementCriterionsPredicateTooltip(IServerUtils utils, PlayerPredicate.AdvancementCriterionsPredicate predicate) {
        return getMapTooltip(utils, predicate.criterions(), GenericTooltipUtils::getCriterionEntryTooltip).key(Lang.Branch.CRITERIONS);
    }

    @NotNull
    public static TooltipBuilder getItemStackTooltip(IServerUtils utils, ItemStack item) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, item.getItem()).build(Lang.Value.ITEM));
            b.add(utils.getValueTooltip(utils, item.getCount()).build(Lang.Value.COUNT));
            b.add(utils.getValueTooltip(utils, item.getComponents()).build(Lang.Branch.COMPONENTS));
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
    public static TooltipBuilder getPropertyMatcherTooltip(IServerUtils ignoredUtils, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        String name = propertyMatcher.name();

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher(String value)) {
            return TooltipBuilder.keyValue(name, value);
        }
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher(Optional<String> minValue, Optional<String> maxValue)) {
            if (minValue.isPresent()) {
                if (maxValue.isPresent()) {
                    return TooltipBuilder.value(name, minValue.get(), maxValue.get()).key(Lang.Value.RANGED_BOTH);
                } else {
                    return TooltipBuilder.value(name, minValue.get()).key(Lang.Value.RANGED_GTE);
                }
            } else {
                if (maxValue.isPresent()) {
                    return TooltipBuilder.value(name, maxValue.get()).key(Lang.Value.RANGED_LTE);
                } else {
                    return TooltipBuilder.value(name).key(Lang.Value.RANGED_ANY);
                }
            }
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getStatMatcherTooltip(IServerUtils utils, PlayerPredicate.StatMatcher<?> stat) {
        String key = TooltipBuilder.translate(((TranslatableContents) stat.type().getDisplayName().getContents()).getKey());
        Holder<?> value = stat.value();

        return TooltipBuilder.array((b) -> {
            if (value.value() instanceof Item item) {
                b.add(utils.getValueTooltip(utils, item)
                        .add(TooltipBuilder.keyValue(key, GenericTooltipUtils.toString(stat.range())).build())
                        .build(Lang.Value.ITEM));
            } else if (value.value() instanceof Block block) {
                b.add(utils.getValueTooltip(utils, block)
                        .add(TooltipBuilder.keyValue(key, GenericTooltipUtils.toString(stat.range())).build())
                        .build(Lang.Value.BLOCK));
            } else if (value.value() instanceof EntityType<?> entityType) {
                b.add(utils.getValueTooltip(utils, entityType)
                        .add(TooltipBuilder.keyValue(key, GenericTooltipUtils.toString(stat.range())).build())
                        .build(Lang.Value.ENTITY_TYPE));
            } else if (value.value() instanceof Identifier identifier) {
                b.add(utils.getValueTooltip(utils, identifier)
                        .add(TooltipBuilder.keyValue(TooltipBuilder.translate(getTranslationKey(identifier)), GenericTooltipUtils.toString(stat.range())).build())
                        .build(Lang.Value.ID));
            }
        });
    }

    @NotNull
    public static TooltipBuilder getListOperationTooltip(IServerUtils utils, ListOperation operation) {
        TooltipBuilder tooltip = utils.getValueTooltip(utils, operation.mode());

        if (operation instanceof ListOperation.Insert(int offset)) {
            tooltip.add(utils.getValueTooltip(utils, offset).build(Lang.Value.OFFSET));
        } else if (operation instanceof ListOperation.ReplaceSection(int offset, Optional<Integer> size)) {
            tooltip.add(utils.getValueTooltip(utils, offset).build(Lang.Value.OFFSET));
            tooltip.add(utils.getValueTooltip(utils, size).build(Lang.Value.SIZE));
        }

        return tooltip;
    }

    @NotNull
    public static <T> TooltipBuilder getFilterableTooltip(IServerUtils utils, Filterable<T> data) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, data.raw()).build(Lang.Value.RAW));
            b.add(utils.getValueTooltip(utils, data.filtered()).build(Lang.Value.FILTERED));
        });
    }

    @NotNull
    public static TooltipBuilder getFireworkPredicateTooltip(IServerUtils utils, FireworkExplosionPredicate.FireworkPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.shape()).build(Lang.Value.SHAPE));
            b.add(utils.getValueTooltip(utils, predicate.trail()).build(Lang.Value.TRAIL));
            b.add(utils.getValueTooltip(utils, predicate.twinkle()).build(Lang.Value.TWINKLE));
            b.key(Lang.Branch.FIREWORK);
        });
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
        return TooltipBuilder.value(predicate.contents()).key(Lang.Value.PAGE);
    }

    @NotNull
    public static TooltipBuilder getPagePredicateTooltip(IServerUtils utils, WrittenBookPredicate.PagePredicate predicate) {
        return TooltipBuilder.component(utils.lookupProvider(), predicate.contents()).key(Lang.Value.PAGE);
    }

    @NotNull
    public static TooltipBuilder getEntryPredicateTooltip(IServerUtils utils, AttributeModifiersPredicate.EntryPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.attribute()).build(Lang.Branch.ATTRIBUTES));
            b.add(utils.getValueTooltip(utils, predicate.id()).build(Lang.Value.ID));
            b.add(utils.getValueTooltip(utils, predicate.amount()).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, predicate.operation()).build(Lang.Value.OPERATION));
            b.add(utils.getValueTooltip(utils, predicate.slot()).build(Lang.Value.SLOT));
        }).key(Lang.Branch.MODIFIER);
    }

    @NotNull
    public static TooltipBuilder getDataComponentPatchTooltip(IServerUtils utils, DataComponentPatch data) {
        return getMapTooltip(utils, data.map, GenericTooltipUtils::getDataComponentPatchEntryTooltip);
    }

    @NotNull
    public static TooltipBuilder getFireworkExplosionTooltip(IServerUtils utils, FireworkExplosion data) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, data.shape()).build(Lang.Value.SHAPE));
            b.add(utils.getValueTooltip(utils, data.colors()).build(Lang.Value.COLORS));
            b.add(utils.getValueTooltip(utils, data.fadeColors()).build(Lang.Value.FADE_COLORS));
            b.add(utils.getValueTooltip(utils, data.hasTrail()).build(Lang.Value.HAS_TRAIL));
            b.add(utils.getValueTooltip(utils, data.hasTwinkle()).build(Lang.Value.HAS_TWINKLE));
        }).key(Lang.Branch.EXPLOSION);
    }

    @NotNull
    public static TooltipBuilder getItemAttributeModifiersEntryTooltip(IServerUtils utils, ItemAttributeModifiers.Entry entry) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, entry.attribute()).build(Lang.Value.ATTRIBUTE));
            b.add(utils.getValueTooltip(utils, entry.modifier()).build(Lang.Branch.MODIFIER));
            b.add(utils.getValueTooltip(utils, entry.slot()).build(Lang.Value.SLOT));
        }).key(Lang.Branch.MODIFIER);
    }

    @NotNull
    public static TooltipBuilder getAttributeModifierTooltip(IServerUtils utils, AttributeModifier modifier) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, modifier.id()).build(Lang.Value.ID));
            b.add(utils.getValueTooltip(utils, modifier.amount()).build(Lang.Value.AMOUNT));
            b.add(utils.getValueTooltip(utils, modifier.operation()).build(Lang.Value.OPERATION));
        });
    }

    @NotNull
    public static TooltipBuilder getMobEffectInstanceTooltip(IServerUtils utils, MobEffectInstance effect) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, effect.getEffect()).build(Lang.Value.EFFECT));
            b.add(utils.getValueTooltip(utils, effect.getDuration()).build(Lang.Value.DURATION));
            b.add(utils.getValueTooltip(utils, effect.getAmplifier()).build(Lang.Value.AMPLIFIER));
            b.add(utils.getValueTooltip(utils, effect.isAmbient()).build(Lang.Value.AMBIENT));
            b.add(utils.getValueTooltip(utils, effect.isVisible()).build(Lang.Value.IS_VISIBLE));
            b.add(utils.getValueTooltip(utils, effect.showIcon()).build(Lang.Value.SHOW_ICON));

            if (effect.hiddenEffect != null) {
                b.add(utils.getValueTooltip(utils, effect.hiddenEffect).build(Lang.Branch.HIDDEN_EFFECT));
            }
        }).key(Lang.Branch.EFFECT);
    }

    @NotNull
    public static TooltipBuilder getRuleTooltip(IServerUtils utils, Tool.Rule rule) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rule.blocks()).build(Lang.Branch.BLOCKS));
            b.add(utils.getValueTooltip(utils, rule.correctForDrops()).build(Lang.Value.CORRECT_FOR_DROPS));
            b.add(utils.getValueTooltip(utils, rule.speed()).build(Lang.Value.SPEED));
        }).key(Lang.Branch.RULE);
    }

    @NotNull
    public static TooltipBuilder getMapDecorationEntryTooltip(IServerUtils utils, MapDecorations.Entry entry) {
        return utils.getValueTooltip(utils, entry.type())
                .add(utils.getValueTooltip(utils, entry.x()).build(Lang.Value.X))
                .add(utils.getValueTooltip(utils, entry.z()).build(Lang.Value.Z))
                .add(utils.getValueTooltip(utils, entry.rotation()).build(Lang.Value.ROTATION));
    }

    @NotNull
    public static TooltipBuilder getDataComponentMapTooltip(IServerUtils utils, DataComponentMap map) {
        if (!map.isEmpty()) {
            return TooltipBuilder.array((b) -> map.stream()
                    .filter((typedDataComponent) -> !Objects.equals(DataComponents.COMMON_ITEM_COMPONENTS.get(typedDataComponent.type()), typedDataComponent.value()))
                    .sorted(Comparator.comparing((v) -> Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(v.type())).toString()))
                    .forEach((action) -> {
                        TooltipBuilder t = utils.getValueTooltip(utils, action.type());

                        t.add(utils.getDataComponentTypeTooltip(utils, action.type(), action.value()));
                        b.add(t);
                    })
            );
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getSuspiciousStewEffectEntryTooltip(IServerUtils utils, SuspiciousStewEffects.Entry entry) {
        return utils.getValueTooltip(utils, entry.effect())
                .add(utils.getValueTooltip(utils, entry.duration()).build(Lang.Value.DURATION));
    }

    @NotNull
    public static TooltipBuilder getGlobalPosTooltip(IServerUtils utils, GlobalPos globalPos) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, globalPos.dimension()).build(Lang.Value.DIMENSION));
            b.add(utils.getValueTooltip(utils, globalPos.pos()).build(Lang.Multi.POSITON));
        });
    }

    @NotNull
    public static TooltipBuilder getBeehiveBlockEntityOccupantTooltip(IServerUtils utils, BeehiveBlockEntity.Occupant occupant) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, occupant.entityData().copyTagWithoutId().toString()).build(Lang.Value.ENTITY_DATA));
            b.add(utils.getValueTooltip(utils, occupant.ticksInHive()).build(Lang.Value.TICKS_IN_HIVE));
            b.add(utils.getValueTooltip(utils, occupant.minTicksInHive()).build(Lang.Value.MIN_TICKS_IN_HIVE));
        }).key(Lang.Branch.OCCUPANT);
    }

    @NotNull
    public static TooltipBuilder getAuthPropertyTooltip(IServerUtils utils, Property property) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, property.name()).build(Lang.Value.NAME));
            b.add(utils.getValueTooltip(utils, property.value()).build(Lang.Value.VALUE));

            if (property.signature() != null) {
                b.add(utils.getValueTooltip(utils, property.signature()).build(Lang.Value.SIGNATURE));
            }
        }).key(Lang.Branch.PROPERTY);
    }

    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionCountsPredicateEntryTooltip(IServerUtils utils, CollectionCountsPredicate.Entry<A, B> entry) {
        return utils.getValueTooltip(utils, entry.test())
                .add(utils.getValueTooltip(utils, entry.count()).build(Lang.Value.COUNT));
    }

    @NotNull
    public static TooltipBuilder getBannerPatternLayersTooltip(IServerUtils utils, BannerPatternLayers bannerPatternLayers) {
        return utils.getValueTooltip(utils, bannerPatternLayers.layers());
    }

    @NotNull
    public static TooltipBuilder getBannerPatternLayerTooltip(IServerUtils utils, BannerPatternLayers.Layer layer) {
        return utils.getValueTooltip(utils, layer.pattern())
                .add(utils.getValueTooltip(utils, layer.color()).build(Lang.Value.COLOR));
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
                        b.add(utils.getValueTooltip(utils, value).build(Lang.Value.CONSTANT));
                case LevelBasedValue.Clamped(LevelBasedValue value, float min, float max) ->
                        b.add(TooltipBuilder.array((c) -> c
                                .add(utils.getValueTooltip(utils, value).build(Lang.Branch.VALUE))
                                .add(utils.getValueTooltip(utils, min).build(Lang.Value.MIN))
                                .add(utils.getValueTooltip(utils, max).build(Lang.Value.MAX))
                                .build(Lang.Branch.CLAMPED)
                        ));
                case LevelBasedValue.Fraction(LevelBasedValue numerator, LevelBasedValue denominator) ->
                        b.add(TooltipBuilder.array((c) -> c
                                .add(utils.getValueTooltip(utils, numerator).build(Lang.Branch.NUMERATOR))
                                .add(utils.getValueTooltip(utils, denominator).build(Lang.Branch.DENOMINATOR))
                                .build(Lang.Branch.FRACTION)
                        ));
                case LevelBasedValue.Linear(float base, float perLevelAboveFirst) ->
                        b.add(TooltipBuilder.array((c) -> c
                                .add(utils.getValueTooltip(utils, base).build(Lang.Value.BASE))
                                .add(utils.getValueTooltip(utils, perLevelAboveFirst).build(Lang.Value.PER_LEVEL))
                                .build(Lang.Branch.LINEAR)
                ));
                case LevelBasedValue.LevelsSquared(float added) ->
                        b.add(TooltipBuilder.array((c) -> c
                                .add(utils.getValueTooltip(utils, added).build(Lang.Value.ADDED))
                                .build(Lang.Branch.LEVEL_SQUARED)
                ));
                case LevelBasedValue.Lookup(List<Float> values, LevelBasedValue fallback) ->
                        b.add(TooltipBuilder.array((c) -> c
                                .add(utils.getValueTooltip(utils, values.toString()).build(Lang.Value.VALUES))
                                .add(utils.getValueTooltip(utils, fallback).build(Lang.Branch.FALLBACK))
                                .build(Lang.Branch.LOOKUP)
                        ));
                default -> {}
            }
        });
    }

    @NotNull
    public static TooltipBuilder getMovementPredicateTooltip(IServerUtils utils, MovementPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.x()).build(Lang.Value.X));
            b.add(utils.getValueTooltip(utils, predicate.y()).build(Lang.Value.Y));
            b.add(utils.getValueTooltip(utils, predicate.z()).build(Lang.Value.Z));
            b.add(utils.getValueTooltip(utils, predicate.speed()).build(Lang.Value.SPEED));
            b.add(utils.getValueTooltip(utils, predicate.horizontalSpeed()).build(Lang.Value.HORIZONTAL_SPEED));
            b.add(utils.getValueTooltip(utils, predicate.verticalSpeed()).build(Lang.Value.VERTICAL_SPEED));
            b.add(utils.getValueTooltip(utils, predicate.fallDistance()).build(Lang.Value.FALL_DISTANCE));
        });
    }

    @NotNull
    public static TooltipBuilder getSlotPredicateTooltip(IServerUtils utils, SlotsPredicate predicate) {
        return getMapTooltip(utils, predicate.slots(), GenericTooltipUtils::getSlotRangePredicateEntryTooltip);
    }

    @NotNull
    public static TooltipBuilder getHolderSetTooltip(IServerUtils utils, HolderSet<?> holderSet) {
        //noinspection unchecked
        Either<TagKey<?>, List<Holder<?>>> either = (Either<TagKey<?>, List<Holder<?>>>)(Object) holderSet.unwrap();
        Optional<TagKey<?>> left = either.left();
        Optional<List<Holder<?>>> right = either.right();

        if (left.isEmpty() && right.orElse(List.of()).isEmpty()) {
            return TooltipBuilder.empty();
        }

        return left
                .map(tagKey -> TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, tagKey).key(Lang.Value.TAG))))
                .orElseGet(() -> TooltipBuilder.branch((b) -> {
                    List<Holder<?>> list = right.get();

                    if (!list.isEmpty()) {
                        list.forEach((holder) -> b.add(utils.getValueTooltip(utils, holder)));
                    }
                }));

    }

    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionPredicateTooltip(IServerUtils utils, CollectionPredicate<A, B> predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.contains()).build(Lang.Branch.CONTAINS));
            b.add(utils.getValueTooltip(utils, predicate.counts()).build(Lang.Branch.COUNTS));
            b.add(utils.getValueTooltip(utils, predicate.size()).build(Lang.Value.SIZE));
        });
    }

    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionContentsPredicateTooltip(IServerUtils utils, CollectionContentsPredicate<A, B> predicate) {
        return utils.getValueTooltip(utils, predicate.unpack());
    }

    @NotNull
    public static <A, B extends Predicate<A>> TooltipBuilder getCollectionCountsPredicateTooltip(IServerUtils utils, CollectionCountsPredicate<A, B> predicate) {
        return utils.getValueTooltip(utils, predicate.unpack());
    }

    @NotNull
    public static TooltipBuilder getInputPredicateTooltip(IServerUtils utils, InputPredicate predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.forward()).build(Lang.Value.FORWARD));
            b.add(utils.getValueTooltip(utils, predicate.backward()).build(Lang.Value.BACKWARD));
            b.add(utils.getValueTooltip(utils, predicate.left()).build(Lang.Value.LEFT));
            b.add(utils.getValueTooltip(utils, predicate.right()).build(Lang.Value.RIGHT));
            b.add(utils.getValueTooltip(utils, predicate.jump()).build(Lang.Value.JUMP));
            b.add(utils.getValueTooltip(utils, predicate.sneak()).build(Lang.Value.SNEAK));
            b.add(utils.getValueTooltip(utils, predicate.sprint()).build(Lang.Value.SPRINT));
        });
    }

    @NotNull
    public static <T> TooltipBuilder getStandaloneTooltip(IServerUtils utils, ListOperation.StandAlone<T> predicate) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.value()).build(Lang.Branch.VALUES));
            b.add(utils.getValueTooltip(utils, predicate.operation()).build(Lang.Value.LIST_OPERATION));
        });
    }

    @NotNull
    public static TooltipBuilder getDamageReductionTooltip(IServerUtils utils, BlocksAttacks.DamageReduction value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.horizontalBlockingAngle()).build(Lang.Value.HORIZONTAL_BLOCKING_ANGLE));
            b.add(utils.getValueTooltip(utils, value.type()).build(Lang.Branch.DAMAGE_TYPES));
            b.add(utils.getValueTooltip(utils, value.base()).build(Lang.Value.BASE));
            b.add(utils.getValueTooltip(utils, value.factor()).build(Lang.Value.FACTOR));
        }).key(Lang.Branch.DAMAGE_REDUCTION);
    }

    @NotNull
    public static TooltipBuilder getItemDamageTooltip(IServerUtils utils, BlocksAttacks.ItemDamageFunction value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.threshold()).build(Lang.Value.THRESHOLD));
            b.add(utils.getValueTooltip(utils, value.base()).build(Lang.Value.BASE));
            b.add(utils.getValueTooltip(utils, value.factor()).build(Lang.Value.FACTOR));
        });
    }

    @NotNull
    public static TooltipBuilder getDataComponentMatchersTooltip(IServerUtils utils, DataComponentMatchers dataComponentMatchers) {
        if (!dataComponentMatchers.partial().isEmpty() || !dataComponentMatchers.exact().isEmpty()) {
            return TooltipBuilder.array((b) -> {
                b.add(utils.getValueTooltip(utils, dataComponentMatchers.exact()).build(Lang.Branch.EXPECTED_COMPONENTS));
                b.add(getMapTooltip(utils, dataComponentMatchers.partial(), GenericTooltipUtils::getDataComponentPredicateEntryTooltip).build(Lang.Branch.PARTIAL_MATCHERS));
            });
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getDataComponentExactPredicateTooltip(IServerUtils utils, DataComponentExactPredicate dataComponentMatchers) {
        return utils.getValueTooltip(utils, dataComponentMatchers.expectedComponents).key(Lang.Branch.EXPECTED_COMPONENTS);
    }

    @NotNull
    public static TooltipBuilder getLootContextArgTooltip(IServerUtils utils, LootContextArg<?> lootContextArg) {
        return utils.getValueTooltip(utils, lootContextArg.contextParam());
    }

    @NotNull
    public static TooltipBuilder getContextKeyTooltip(IServerUtils utils, ContextKey<?> contextKey) {
        return utils.getValueTooltip(utils, contextKey.name());
    }

    @NotNull
    public static TooltipBuilder getSlotRangeTooltip(IServerUtils utils, SlotRange slotRange) {
        return utils.getValueTooltip(utils, slotRange.toString());
    }

    @NotNull
    public static TooltipBuilder getKineticWeaponConditionTooltip(IServerUtils utils, KineticWeapon.Condition condition) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, condition.maxDurationTicks()).build(Lang.Value.MAX_DURATION_TICKS))
                .add(utils.getValueTooltip(utils, condition.minSpeed()).build(Lang.Value.MIN_SPEED))
                .add(utils.getValueTooltip(utils, condition.minRelativeSpeed()).build(Lang.Value.MIN_RELATIVE_SPEED))
        );
    }

    @NotNull
    public static TooltipBuilder getItemStackTemplateTooltip(IServerUtils utils, ItemStackTemplate itemStackTemplate) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, itemStackTemplate.item()).build("ali.property.value.item"))
                .add(utils.getValueTooltip(utils, itemStackTemplate.count()).build("ali.property.value.count"))
                .add(utils.getValueTooltip(utils, itemStackTemplate.components()).build("ali.property.branch.components"))
        ).key(Lang.Branch.ITEM);
    }

    @NotNull
    public static TooltipBuilder getFoodPredicateTooltip(IServerUtils utils, FoodPredicate predicate) {
        if (predicate != FoodPredicate.ANY) {
            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, predicate.level()).build("ali.property.value.level"))
                    .add(utils.getValueTooltip(utils, predicate.saturation()).build("ali.property.value.saturation"))
            );
        }

        return TooltipBuilder.empty();
    }
}
