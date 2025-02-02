package com.yanny.advanced_loot_info.plugin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.*;
import com.yanny.advanced_loot_info.plugin.condition.RandomChanceCondition;
import com.yanny.advanced_loot_info.plugin.condition.RandomChanceWithLootingCondition;
import com.yanny.advanced_loot_info.plugin.condition.TableBonusCondition;
import com.yanny.advanced_loot_info.plugin.entry.SingletonEntry;
import com.yanny.advanced_loot_info.plugin.function.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class TooltipUtils {
    private TooltipUtils() {}

    @NotNull
    public static List<Component> getConditions(List<ILootCondition> conditions, int pad) {
        List<Component> components = new LinkedList<>();

        conditions.forEach((condition) -> components.addAll(condition.getTooltip(pad)));

        return components;
    }

    @NotNull
    public static List<Component> getFunctions(List<ILootFunction> functions, int pad) {
        List<Component> components = new LinkedList<>();

        functions.forEach((function) -> {
            components.addAll(function.getTooltip(pad));

            if (function instanceof LootConditionalFunction conditionalFunction) {
                components.addAll(getConditionalFunction(conditionalFunction, pad + 1));
            }
        });

        return components;
    }

    @NotNull
    private static List<Component> getConditionalFunction(LootConditionalFunction function, int pad) {
        List<Component> components = new LinkedList<>();

        if (!function.conditions.isEmpty()) {
            components.add(pad(pad, translatable("emi.property.function.conditions")));
            components.addAll(TooltipUtils.getConditions(function.conditions, pad + 1));
        }

        return components;
    }

    public static void addItemPredicate(List<Component> components, int pad, Component component, ItemPredicate itemPredicate) {
        if (itemPredicate != ItemPredicate.ANY) {
            MixinItemPredicate predicate = (MixinItemPredicate) itemPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.item.tag", predicate.getTag().location().toString())));
            }

            if (predicate.getItems() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.item.items")));

                predicate.getItems().forEach((item) -> components.add(pad(pad + 2, value(translatable(item.getDescriptionId())))));
            }

            addMinMaxBounds(components, pad + 1, "emi.property.condition.item.count", predicate.getCount());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.item.durability", predicate.getDurability());

            for (EnchantmentPredicate enchantment : predicate.getEnchantments()) {
                addEnchantmentPredicate(components, pad + 1, "emi.property.condition.item.enchantment", enchantment);
            }

            for (EnchantmentPredicate enchantment : predicate.getStoredEnchantments()) {
                addEnchantmentPredicate(components, pad + 1, "emi.property.condition.item.stored_enchantment", enchantment);
            }

            if (predicate.getPotion() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.item.potion")));

                predicate.getPotion().getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
            }

            addNbtPredicate(components, pad + 1, "emi.property.condition.item.nbt", predicate.getNbt());
        }
    }

    public static void addLocationPredicate(List<Component> components, int pad, Component component, LocationPredicate predicate) {
        if (predicate != LocationPredicate.ANY) {
            MixinLocationPredicate locationPredicate = (MixinLocationPredicate) predicate;

            addMinMaxBounds(components, pad, "emi.property.condition.location.x", locationPredicate.getX());
            addMinMaxBounds(components, pad, "emi.property.condition.location.y", locationPredicate.getY());
            addMinMaxBounds(components, pad, "emi.property.condition.location.z", locationPredicate.getZ());
            addResourceKey(components, pad, "emi.property.condition.location.biome", locationPredicate.getBiome());
            addResourceKey(components, pad, "emi.property.condition.location.structure", locationPredicate.getStructure());
            addResourceKey(components, pad, "emi.property.condition.location.dimension", locationPredicate.getDimension());
            addBoolean(components, pad, "emi.property.condition.location.smokey", locationPredicate.getSmokey());
            addLight(components, pad, "emi.property.condition.location.light", locationPredicate.getLight());
            addBlock(components, pad, translatable("emi.property.condition.location.block"), locationPredicate.getBlock());
            addFluid(components, pad, translatable("emi.property.condition.location.fluid"), locationPredicate.getFluid());
        }
    }

    public static void addEntityPredicate(List<Component> components, int pad, Component component, EntityPredicate entityPredicate) {
        if (entityPredicate != EntityPredicate.ANY) {
            MixinEntityPredicate predicate = (MixinEntityPredicate) entityPredicate;

            components.add(pad(pad, component));

            addEntityTypePredicate(components, pad + 1, translatable("emi.property.condition.predicate.entity_type"), predicate.getEntityType());
            addDistancePredicate(components, pad + 1, translatable("emi.property.condition.predicate.dist_to_player"), predicate.getDistanceToPlayer());
            addLocationPredicate(components, pad + 1, translatable("emi.property.condition.predicate.location"), predicate.getLocation());
            addLocationPredicate(components, pad + 1, translatable("emi.property.condition.predicate.stepping_on_location"), predicate.getSteppingOnLocation());
            addMobEffectsPredicate(components, pad + 1, translatable("emi.property.condition.predicate.effect"), predicate.getEffects());
            addNbtPredicate(components, pad + 1, "emi.property.condition.predicate.nbt", predicate.getNbt());
            addEntityFlagsPredicate(components, pad + 1, translatable("emi.property.condition.predicate.flags"), predicate.getFlags());
            addEntityEquipmentPredicate(components, pad + 1, translatable("emi.property.condition.predicate.equipment"), predicate.getEquipment());
            addEntitySubPredicate(components, pad + 1, "emi.property.condition.predicate.entity_sub_type", predicate.getSubPredicate());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.vehicle"), predicate.getVehicle());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.passenger"), predicate.getPassenger());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.predicate.targeted_entity"), predicate.getTargetedEntity());

            if (predicate.getTeam() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.predicate.team", predicate.getTeam())));
            }
        }
    }

    private static void addMobEffectsPredicate(List<Component> components, int pad, Component component, MobEffectsPredicate predicate) {
        if (predicate != MobEffectsPredicate.ANY) {
            MixinMobEffectPredicate locationPredicate = (MixinMobEffectPredicate) predicate;

            components.add(pad(pad, component));

            locationPredicate.getEffects().forEach((effect, instancePredicate) -> {
                MixinMobEffectPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) instancePredicate;

                components.add(pad(pad + 1, value(translatable(effect.getDescriptionId()))));
                addMinMaxBounds(components, pad + 2, "emi.property.condition.effect.amplifier", mobEffectInstancePredicate.getAmplifier());
                addMinMaxBounds(components, pad + 2, "emi.property.condition.effect.duration", mobEffectInstancePredicate.getDuration());
                addBoolean(components, pad + 2, "emi.property.condition.effect.ambient", mobEffectInstancePredicate.getAmbient());
                addBoolean(components, pad + 2, "emi.property.condition.effect.visible", mobEffectInstancePredicate.getVisible());
            });
        }
    }

    private static void addEntityEquipmentPredicate(List<Component> components, int pad, Component component, EntityEquipmentPredicate entityEquipmentPredicate) {
        if (entityEquipmentPredicate != EntityEquipmentPredicate.ANY) {
            MixinEntityEquipmentPredicate predicate = (MixinEntityEquipmentPredicate) entityEquipmentPredicate;

            components.add(pad(pad, component));

            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.head"), predicate.getHead());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.chest"), predicate.getChest());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.legs"), predicate.getLegs());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.feet"), predicate.getFeet());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.mainhand"), predicate.getMainhand());
            addItemPredicate(components, pad + 1, translatable("emi.property.condition.equipment.offhand"), predicate.getOffhand());
        }
    }

    private static void addEntityFlagsPredicate(List<Component> components, int pad, Component component, EntityFlagsPredicate entityFlagsPredicate) {
        if (entityFlagsPredicate != EntityFlagsPredicate.ANY) {
            MixinEntityFlagsPredicate predicate = (MixinEntityFlagsPredicate) entityFlagsPredicate;

            components.add(pad(pad, component));

            addBoolean(components, pad + 1, "emi.property.condition.flags.on_fire", predicate.getIsOnFire());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_baby", predicate.getIsBaby());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_crouching", predicate.getIsCrouching());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_sprinting", predicate.getIsSprinting());
            addBoolean(components, pad + 1, "emi.property.condition.flags.is_swimming", predicate.getIsSwimming());
        }
    }

    private static void addEntitySubPredicate(List<Component> components, int pad, String key, EntitySubPredicate entitySubPredicate) {
        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable(key, entry.getKey())));

                if (entitySubPredicate instanceof LighthingBoltPredicate predicate) {
                    MixinLighthingBoltPredicate boltPredicate = (MixinLighthingBoltPredicate) predicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.condition.sub_entity.blocks_on_fire", boltPredicate.getBlocksSetOnFire());
                    addEntityPredicate(components, pad + 2, translatable("emi.property.condition.sub_entity.stuck_entity"), boltPredicate.getEntityStruck());
                } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                    MixinFishingHookPredicate fishingHookPredicate = (MixinFishingHookPredicate) predicate;

                    components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.in_open_water", fishingHookPredicate.isInOpenWater())));
                } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                    MixinPlayerPredicate playerPredicate = (MixinPlayerPredicate) predicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.condition.sub_entity.level", playerPredicate.getLevel());

                    if (playerPredicate.getGameType() != null) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.game_type", value(playerPredicate.getGameType().getShortDisplayName()))));
                    }

                    if (!playerPredicate.getStats().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.stats")));
                        playerPredicate.getStats().forEach((stat, ints) -> {
                            Object value = stat.getValue();
                            components.add(pad(pad + 2, value instanceof Item ? translatable(((Item) value).getDescriptionId()) : value));
                            components.add(pad(pad + 3, keyValue(stat.getType().getDisplayName(), toString((MixinMinMaxBounds.Ints) ints))));
                        });
                    }

                    if (!playerPredicate.getRecipes().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.recipes")));
                        playerPredicate.getRecipes().forEach((recipe, required) -> components.add(pad(pad + 2, keyValue(recipe.toString(), required))));
                    }

                    if (!playerPredicate.getAdvancements().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.advancements")));
                        playerPredicate.getAdvancements().forEach((advancement, advancementPredicate) -> {
                            components.add(pad(pad + 2, advancement.toString()));

                            if (advancementPredicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                                components.add(pad(pad + 3, translatable("emi.property.condition.sub_entity.advancement.done", ((MixinPlayerPredicate.AdvancementDonePredicate) donePredicate).getState())));
                            } else if (advancementPredicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                                MixinPlayerPredicate.AdvancementCriterionsPredicate mixPredicate = (MixinPlayerPredicate.AdvancementCriterionsPredicate) criterionsPredicate;

                                mixPredicate.getCriterions().forEach((criterion, state) -> components.add(pad(pad + 3, keyValue(criterion, state))));
                            }
                        });
                    }
                } if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                    MixinSlimePredicate predicate = (MixinSlimePredicate) slimePredicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.condition.sub_entity.size", predicate.getSize());
                } else {
                    JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                    if (jsonObject.has("variant")) {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                    } else {
                        components.add(pad(pad + 1, translatable("emi.property.condition.sub_entity.variant", jsonObject.toString())));
                    }
                }
            });
        }
    }

    private static void addDistancePredicate(List<Component> components, int pad, Component component, DistancePredicate distancePredicate) {
        if (distancePredicate != DistancePredicate.ANY) {
            MixinDistancePredicate predicate = (MixinDistancePredicate) distancePredicate;

            components.add(pad(pad, component));
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.x", predicate.getX());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.y", predicate.getY());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.z", predicate.getZ());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.horizontal", predicate.getHorizontal());
            addMinMaxBounds(components, pad + 1, "emi.property.condition.dist_predicate.absolute", predicate.getAbsolute());
        }
    }

    private static void addEntityTypePredicate(List<Component> components, int pad, Component component, EntityTypePredicate entityTypePredicate) {
        if (entityTypePredicate != EntityTypePredicate.ANY) {
            MixinEntityTypePredicate predicate = (MixinEntityTypePredicate) entityTypePredicate;

            components.add(pad(pad, component));

            if (predicate instanceof MixinEntityTypePredicate.TypePredicate typePredicate) {
                components.add(pad(pad + 1, value(typePredicate.getType().getDescription())));
            }
            if (predicate instanceof MixinEntityTypePredicate.TagPredicate tagPredicate) {
                components.add(pad(pad + 1, value(tagPredicate.getTag().location()))); //TODO to list
            }
        }
    }

    private static void addEnchantmentPredicate(List<Component> components, int pad, String key, EnchantmentPredicate enchantmentPredicate) {
        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            MixinEnchantmentPredicate predicate = (MixinEnchantmentPredicate) enchantmentPredicate;

            components.add(pad(pad, translatable(key)));

            if (predicate.getEnchantment() != null) {
                components.add(pad(pad + 1, value(translatable(predicate.getEnchantment().getDescriptionId()))));
            }
            addMinMaxBounds(components, pad + 1, "emi.property.condition.enchantment.level", predicate.getLevel());
        }
    }

    private static void addResourceKey(List<Component> components, int pad, String key, @Nullable ResourceKey<?> resourceKey) {
        if (resourceKey != null) {
            components.add(pad(pad, translatable(key, value(resourceKey.location().toString()))));
        }
    }

    private static void addBoolean(List<Component> components, int pad, String key, @Nullable Boolean aBoolean) {
        if (aBoolean != null) {
            components.add(pad(pad, translatable(key, value(aBoolean.toString()))));
        }
    }

    private static void addLight(List<Component> components, int pad, String key, LightPredicate lightPredicate) {
        if (lightPredicate != LightPredicate.ANY) {
            addMinMaxBounds(components, pad, key, ((MixinLightPredicate) lightPredicate).getComposite());
        }
    }

    private static void addBlock(List<Component> components, int pad, Component component, BlockPredicate blockPredicate) {
        if (blockPredicate != BlockPredicate.ANY) {
            MixinBlockPredicate predicate = (MixinBlockPredicate) blockPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.block.tag", predicate.getTag().location().toString())));
            }
            if (predicate.getBlocks() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.block.blocks")));

                predicate.getBlocks().forEach((block) -> components.add(pad(pad + 2, value(translatable(block.getDescriptionId())))));
            }
            addStateProperties(components, pad + 1, translatable("emi.property.condition.block.state"), predicate.getProperties());
            addNbtPredicate(components, pad + 1, "emi.property.condition.block.nbt", predicate.getNbt());
        }
    }

    private static void addFluid(List<Component> components, int pad, Component component, FluidPredicate fluidPredicate) {
        if (fluidPredicate != FluidPredicate.ANY) {
            MixinFluidPredicate predicate = (MixinFluidPredicate) fluidPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.fluid.tag", predicate.getTag().location().toString())));
            }
            if (predicate.getFluid() != null) {
                components.add(pad(pad + 1, translatable("emi.property.condition.fluid.fluid", value(translatable(predicate.getFluid().getFluidType().getDescriptionId())))));
            }
            addStateProperties(components, pad + 1, translatable("emi.property.condition.fluid.state"), predicate.getProperties());
        }
    }

    public static void addStateProperties(List<Component> components, int pad, Component component, StatePropertiesPredicate propertiesPredicate) {
        if (propertiesPredicate != StatePropertiesPredicate.ANY) {
            MixinStatePropertiesPredicate predicate = (MixinStatePropertiesPredicate) propertiesPredicate;

            components.add(pad(pad, component));

            predicate.getProperties().forEach((propertyMatcher) -> addPropertyMatcher(components, pad + 1, propertyMatcher));
        }
    }

    public static void addDamageSourcePredicate(List<Component> components, int pad, Component component, DamageSourcePredicate damageSourcePredicate) {
        if (damageSourcePredicate != DamageSourcePredicate.ANY) {
            MixinDamageSourcePredicate predicate = (MixinDamageSourcePredicate) damageSourcePredicate;

            components.add(pad(pad, component));

            if (!predicate.getTags().isEmpty()) {
                components.add(pad(pad + 1, translatable("emi.property.condition.damage_source.tags")));
                predicate.getTags().forEach((tagPredicate) -> {
                    MixinTagPredicate<?> p = (MixinTagPredicate<?>) tagPredicate;

                    components.add(pad(pad + 2, keyValue(p.getTag().location().toString(), p.getExpected())));
                });
            }

            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.damage_source.direct_entity"), predicate.getDirectEntity());
            addEntityPredicate(components, pad + 1, translatable("emi.property.condition.damage_source.source_entity"), predicate.getSourceEntity());
        }
    }

    private static void addPropertyMatcher(List<Component> components, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        if (propertyMatcher instanceof MixinStatePropertiesPredicate.ExactPropertyMatcher matcher) {
            components.add(pad(pad, keyValue(matcher.getName(), matcher.getValue())));
        }
        if (propertyMatcher instanceof MixinStatePropertiesPredicate.RangedPropertyMatcher matcher) {
            components.add(pad(pad, value(matcher.getName() + "=[" + matcher.getMinValue() + "-" + matcher.getMaxValue() + "]")));
        }
    }

    private static void addNbtPredicate(List<Component> components, int pad, String key, NbtPredicate nbtPredicate) {
        if (nbtPredicate != NbtPredicate.ANY) {
            components.add(pad(pad, translatable(key, value(((MixinNbtPredicate) nbtPredicate).getTag().toString()))));
        }
    }

    private static void addMinMaxBounds(List<Component> components, int pad, String key, MinMaxBounds.Doubles doubles) {
        if (doubles != MinMaxBounds.Doubles.ANY) {
            MixinMinMaxBounds.Doubles absolute = (MixinMinMaxBounds.Doubles) doubles;
            components.add(pad(pad, translatable(key, value(toString(absolute)))));
        }
    }

    private static void addMinMaxBounds(List<Component> components, int pad, String key, MinMaxBounds.Ints ints) {
        if (ints != MinMaxBounds.Ints.ANY) {
            MixinMinMaxBounds.Ints absolute = (MixinMinMaxBounds.Ints) ints;
            components.add(pad(pad, translatable(key, value(toString(absolute)))));
        }
    }

    @NotNull
    private static String toString(MixinMinMaxBounds.Doubles doubles) {
        if (doubles.getMin() != null) {
            if (doubles.getMax() != null) {
                if (!Objects.equals(doubles.getMin(), doubles.getMax())) {
                    return String.format("%.1f-%.1f", doubles.getMin(), doubles.getMax());
                } else {
                    return String.format("=%.1f", doubles.getMin());
                }
            } else {
                return String.format("≥%.1f", doubles.getMin());
            }
        } else {
            if (doubles.getMax() != null) {
                return String.format("<%.1f", doubles.getMax());
            }

            return "???";
        }
    }

    @NotNull
    private static String toString(MixinMinMaxBounds.Ints ints) {
        if (ints.getMin() != null) {
            if (ints.getMax() != null) {
                if (!Objects.equals(ints.getMin(), ints.getMax())) {
                    return String.format("%d-%d", ints.getMin(), ints.getMax());
                } else {
                    return String.format("=%d", ints.getMin());
                }
            } else {
                return String.format("≥%d", ints.getMin());
            }
        } else {
            if (ints.getMax() != null) {
                return String.format("<%d", ints.getMax());
            }

            return "???";
        }
    }

    @NotNull
    @Unmodifiable
    public static List<Component> getQuality(SingletonEntry entry) {
        if (entry.quality != 0) {
            return List.of(translatable("emi.description.advanced_loot_info.quality", entry.quality));
        }

        return List.of();
    }

    @NotNull
    public static Component getCount(RangeValue count) {
        return translatable("emi.description.advanced_loot_info.count", count);
    }

    @NotNull
    public static Component getChance(RangeValue chance) {
        return translatable("emi.description.advanced_loot_info.chance", value(chance, "%"));
    }

    @NotNull
    public static List<Component> getBonusChance(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance) {
        List<Component> components = new LinkedList<>();

        if (bonusChance != null) {
            bonusChance.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.chance_bonus",
                    value(value, "%"),
                    Component.translatable(bonusChance.getFirst().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    public static List<Component> getBonusCount(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusCount) {
        List<Component> components = new LinkedList<>();

        if (bonusCount != null) {
            bonusCount.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.advanced_loot_info.count_bonus",
                    value,
                    Component.translatable(bonusCount.getFirst().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    public static RangeValue getChance(List<ILootCondition> conditions, float chance) {
        RangeValue value = new RangeValue(chance);
        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;
            value.multiply(condition.percent);
        }

        list = conditions.stream().filter((f) -> f instanceof RandomChanceCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceCondition condition = (RandomChanceCondition) c;
            value.multiply(condition.probability);
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusCondition).toList();

        for (ILootCondition c : list) {
            TableBonusCondition condition = (TableBonusCondition) c;
            value.set(new RangeValue(condition.values[0]));
        }

        return value.multiply(100);
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusChance(List<ILootCondition> conditions, float chance) {
        Map<Integer, RangeValue> bonusChance = new HashMap<>();

        List<ILootCondition> list = conditions.stream().filter((f) -> f instanceof RandomChanceWithLootingCondition).toList();

        for (ILootCondition c : list) {
            RandomChanceWithLootingCondition condition = (RandomChanceWithLootingCondition) c;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(chance * (condition.percent + level * condition.multiplier));
                bonusChance.put(level, value.multiply(100));
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusChance);
        }

        list = conditions.stream().filter((f) -> f instanceof TableBonusCondition).toList();

        for (ILootCondition c : list) {
            TableBonusCondition condition = (TableBonusCondition) c;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(condition.location);

            if (enchantment != null) {
                for (int level = 1; level < condition.values.length; level++) {
                    RangeValue value = new RangeValue(condition.values[level]);
                    bonusChance.put(level, value.multiply(100));
                }

                return new Pair<>(enchantment, bonusChance);
            }
        }

        return null;
    }

    @NotNull
    public static RangeValue getCount(List<ILootFunction> functions) {
        RangeValue value = new RangeValue();

        functions.stream().filter((f) -> f instanceof SetCountFunction).forEach((f) -> {
            SetCountFunction function = (SetCountFunction) f;

            if (function.add) {
                value.add(function.count);
            } else {
                value.set(function.count);
            }

        });

        functions.stream().filter((f) -> f instanceof ApplyBonusFunction).forEach((f) -> {
            ((ApplyBonusFunction) f).formula.calculateCount(value, 0);
        });

        functions.stream().filter((f) -> f instanceof LimitCountFunction).forEach((f) -> {
            LimitCountFunction function = (LimitCountFunction) f;

            value.clamp(function.min, function.max);
        });

        return value;
    }

    @Nullable
    @Unmodifiable
    public static Pair<Enchantment, Map<Integer, RangeValue>> getBonusCount(List<ILootFunction> functions, RangeValue count) {
        Map<Integer, RangeValue> bonusCount = new HashMap<>();

        List<ILootFunction> list = functions.stream().filter((f) -> f instanceof ApplyBonusFunction).toList();

        for (ILootFunction f : list) {
            ApplyBonusFunction function = (ApplyBonusFunction) f;
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(function.enchantment);

            if (enchantment != null) {
                for (int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                    RangeValue value = new RangeValue(count);
                    function.formula.calculateCount(value, level);
                    bonusCount.put(level, value);
                }

                return new Pair<>(enchantment, bonusCount);
            }
        }

        list = functions.stream().filter((f) -> f instanceof LootingEnchantFunction).toList();

        for (ILootFunction f : list) {
            LootingEnchantFunction function = (LootingEnchantFunction) f;

            for (int level = 1; level < Enchantments.MOB_LOOTING.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                value.addMax(new RangeValue(function.value).multiply(level));
                bonusCount.put(level, value);
            }

            return new Pair<>(Enchantments.MOB_LOOTING, bonusCount);
        }

        return null;
    }
}
