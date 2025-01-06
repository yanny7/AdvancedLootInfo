package com.yanny.emi_loot_addon.compatibility;

import com.google.gson.JsonObject;
import com.yanny.emi_loot_addon.mixin.*;
import com.yanny.emi_loot_addon.network.LootCondition;
import com.yanny.emi_loot_addon.network.LootGroup;
import com.yanny.emi_loot_addon.network.RangeValue;
import com.yanny.emi_loot_addon.network.condition.*;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public abstract class EmiBaseLoot extends BasicEmiRecipe {
    protected final LootGroup message;
    protected final List<ItemData> itemDataList;

    public EmiBaseLoot(EmiRecipeCategory category, ResourceLocation id, LootGroup message) {
        super(category, id, 9 * 18, 1024);
        this.message = message;
        itemDataList = ItemData.parse(message);
        outputs = itemDataList.stream()
                .map((d) -> d.item)
                .filter(Objects::nonNull)
                .map(EmiStack::of)
                .toList();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addWidgets(widgets, new int[]{0, 0});
    }

    @Override
    public Recipe<?> getBackingRecipe() {
        return null;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    public void addWidgets(WidgetHolder widgetHolder, int[] pos) {
        for (ItemData itemData : itemDataList) {
            SlotWidget widget = new LootSlotWidget(EmiStack.of(itemData.item), pos[0], pos[1])
                    .setCount(itemData.count);

            widget.appendTooltip(getChance(itemData));
            getBonusChance(itemData).forEach(widget::appendTooltip);

            widget.appendTooltip(getCount(itemData));
            getBonusCount(itemData).forEach(widget::appendTooltip);

            getConditions(itemData.conditions, 0).forEach(widget::appendTooltip);

            widget.appendTooltip(Component.literal(itemData.conditions.stream().filter((c) -> !c.HANDLED).map((c) -> c.type).toList().toString()))
                    .appendTooltip(Component.literal(itemData.functions.stream().filter((f) -> !f.HANDLED).map((f) -> f.type).toList().toString()));

            widget.recipeContext(this);
            widgetHolder.add(widget);

            if ((pos[0] + 18) / (9*18) > 0) {
                pos[1] += 18;
            }

            pos[0] = (pos[0] + 18) % (9*18);
        }
    }

    @NotNull
    private static List<Component> getConditions(List<LootCondition> conditions, int pad) {
        List<Component> components = new LinkedList<>();

        for (LootCondition condition : conditions) {
            switch (condition.type) {
                case ALL_OF -> components.addAll(getAllOfCondition((AllOfCondition) condition, pad));
                case ANY_OF -> components.addAll(getAnyOfCondition((AnyOfCondition) condition, pad));
                case INVERTED -> components.addAll(getInvertedCondition((InvertedCondition) condition, pad));
                case SURVIVES_EXPLOSION,
                     KILLED_BY_PLAYER -> components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
                case ENTITY_PROPERTIES -> components.addAll(getEntityPropertiesCondition((EntityPropertiesCondition) condition, pad));
                case ENTITY_SCORES -> components.addAll(getEntityScoresCondition((EntityScoresCondition) condition, pad));
                case BLOCK_STATE_PROPERTY -> components.addAll(getBlockStatePropertyCondition((BlockStatePropertyCondition) condition, pad));
                case MATCH_TOOL -> components.addAll(getMatchToolCondition((MatchToolCondition) condition, pad));
                case DAMAGE_SOURCE_PROPERTIES -> components.addAll(getDamageSourceCondition((DamageSourcePropertiesCondition) condition, pad));
                case LOCATION_CHECK -> components.addAll(getLocationCheckCondition((LocationCheckCondition) condition, pad));
                case WEATHER_CHECK -> components.addAll(getWeatherCheckCondition((WeatherCheckCondition) condition, pad));
                case REFERENCE -> components.addAll(getReferenceCondition((ReferenceCondition) condition, pad));
                case TIME_CHECK -> components.addAll(getTimeCheckCondition((TimeCheckCondition) condition, pad));
                case VALUE_CHECK -> components.addAll(getValueCheckCondition((ValueCheckCondition) condition, pad));
                case LOOT_CONDITION_TYPE -> components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type, value(((CanToolPerformActionCondition) condition).action))));
                case RANDOM_CHANCE,
                     TABLE_BONUS,
                     RANDOM_CHANCE_WITH_LOOTING -> {
                    continue;
                }
                default -> {
                    //TODO logger
                }
            }

            condition.HANDLED = true;
        }

        return components;
    }

    @NotNull
    private static List<Component> getAllOfCondition(AllOfCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        components.addAll(getConditions(condition.terms, pad + 1));

        return components;
    }

    @NotNull
    private static List<Component> getAnyOfCondition(AnyOfCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        components.addAll(getConditions(condition.terms, pad + 1));

        return components;
    }

    @NotNull
    private static List<Component> getInvertedCondition(InvertedCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        components.addAll(getConditions(List.of(condition.term), pad + 1));

        return components;
    }

    @NotNull
    private static List<Component> getEntityPropertiesCondition(EntityPropertiesCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        addEntityPredicate(components, pad + 1, translatable("emi.property.predicate.target", value(translatableType("emi.enum.target", condition.target))), condition.predicate);

        return components;
    }

    @NotNull
    private static List<Component> getEntityScoresCondition(EntityScoresCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        components.add(pad(pad + 1, translatable("emi.property.predicate.target", value(translatableType("emi.enum.target", condition.target)))));
        components.add(pad(pad + 1, translatable("emi.property.scores.score")));
        condition.scores.forEach((score, tuple) -> components.add(pad(pad + 2, keyValue(score, RangeValue.rangeToString(tuple.getA(), tuple.getB())))));

        return components;
    }

    @NotNull
    private static List<Component> getBlockStatePropertyCondition(BlockStatePropertyCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        addStateProperties(components, pad, translatableType("emi.type.emi_loot_addon", condition.type), condition.properties);

        return components;
    }

    @NotNull
    private static List<Component> getMatchToolCondition(MatchToolCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        addItemPredicate(components, pad, translatableType("emi.type.emi_loot_addon", condition.type), condition.predicate);

        return components;
    }

    @NotNull
    private static List<Component> getDamageSourceCondition(DamageSourcePropertiesCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        addDamageSourcePredicate(components, pad, translatableType("emi.type.emi_loot_addon", condition.type), condition.predicate);

        return components;
    }

    @NotNull
    private static List<Component> getLocationCheckCondition(LocationCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        addLocationPredicate(components, pad + 1, "emi.property.location_check.location", condition.predicate);
        components.add(pad(pad + 1, translatable("emi.property.location_check.offset")));
        components.add(pad(pad + 2, translatable("emi.property.location_check.x", condition.offset.getX())));
        components.add(pad(pad + 2, translatable("emi.property.location_check.y", condition.offset.getY())));
        components.add(pad(pad + 2, translatable("emi.property.location_check.z", condition.offset.getZ())));

        return components;
    }

    @NotNull
    private static List<Component> getWeatherCheckCondition(WeatherCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));

        if (condition.isRaining != null) {
            components.add(pad(pad + 1, translatable("emi.property.weather_check.is_raining", condition.isRaining)));
        }

        if (condition.isThundering != null) {
            components.add(pad(pad + 1, translatable("emi.property.weather_check.is_thundering", condition.isThundering)));
        }

        return components;
    }

    @NotNull
    private static List<Component> getReferenceCondition(ReferenceCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type, condition.name)));

        return components;
    }

    @NotNull
    private static List<Component> getTimeCheckCondition(TimeCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        components.add(pad(pad + 1, translatable("emi.property.time_check.period", condition.period)));
        components.add(pad(pad + 1, translatable("emi.property.time_check.value", RangeValue.rangeToString(condition.min, condition.max))));

        return components;
    }

    @NotNull
    private static List<Component> getValueCheckCondition(ValueCheckCondition condition, int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatableType("emi.type.emi_loot_addon", condition.type)));
        components.add(pad(pad + 1, translatable("emi.property.value_check.provider", condition.provider)));
        components.add(pad(pad + 1, translatable("emi.property.value_check.range", RangeValue.rangeToString(condition.min, condition.max))));

        return components;
    }

    @NotNull
    private static Component getCount(ItemData data) {
        return translatable("emi.description.emi_loot_addon.count", data.count);
    }

    @NotNull
    private static Component getChance(ItemData data) {
        if ((!data.rolls.isRange() && data.rolls.min() > 1) || (data.rolls.isRange() && data.rolls.max() > 1)) {
            return translatable("emi.description.emi_loot_addon.chance_rolls", value(data.rolls.toIntString(), "x"), value(data.chance, "%"));
        }

        return translatable("emi.description.emi_loot_addon.chance", value(data.chance, "%"));
    }

    @NotNull
    private static List<Component> getBonusChance(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusChance != null) {
            data.bonusChance.getValue().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.emi_loot_addon.chance_bonus",
                    value(value, "%"),
                    Component.translatable(data.bonusChance.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    @NotNull
    private static List<Component> getBonusCount(ItemData data) {
        List<Component> components = new LinkedList<>();

        if (data.bonusCount != null) {
            data.bonusCount.getValue().forEach((level, value) -> components.add(pad(1, translatable(
                    "emi.description.emi_loot_addon.count_bonus",
                    value,
                    Component.translatable(data.bonusCount.getKey().getDescriptionId()),
                    Component.translatable("enchantment.level." + level)
            ))));
        }

        return components;
    }

    private static void addEntityPredicate(List<Component> components, int pad, Component component, EntityPredicate entityPredicate) {
        if (entityPredicate != EntityPredicate.ANY) {
            MixinEntityPredicate predicate = (MixinEntityPredicate) entityPredicate;

            components.add(pad(pad, component));

            addEntityTypePredicate(components, pad + 1, "emi.property.predicate.entity_type", predicate.getEntityType());
            addDistancePredicate(components, pad + 1, "emi.property.predicate.dist_to_player", predicate.getDistanceToPlayer());
            addLocationPredicate(components, pad + 1, "emi.property.predicate.location", predicate.getLocation());
            addLocationPredicate(components, pad + 1, "emi.property.predicate.stepping_on_location", predicate.getSteppingOnLocation());
            addMobEffectsPredicate(components, pad + 1, "emi.property.predicate.effect", predicate.getEffects());
            addNbtPredicate(components, pad + 1, "emi.property.predicate.nbt", predicate.getNbt());
            addEntityFlagsPredicate(components, pad + 1, "emi.property.predicate.flags", predicate.getFlags());
            addEntityEquipmentPredicate(components, pad + 1, "emi.property.predicate.equipment", predicate.getEquipment());
            addEntitySubPredicate(components, pad + 1, "emi.property.predicate.entity_sub_type", predicate.getSubPredicate());
            addEntityPredicate(components, pad + 1, translatable("emi.property.predicate.vehicle"), predicate.getVehicle());
            addEntityPredicate(components, pad + 1, translatable("emi.property.predicate.passenger"), predicate.getPassenger());
            addEntityPredicate(components, pad + 1, translatable("emi.property.predicate.targeted_entity"), predicate.getTargetedEntity());

            if (predicate.getTeam() != null) {
                components.add(pad(pad + 1, translatable("emi.property.predicate.team", predicate.getTeam())));
            }
        }
    }

    private static void addEntityTypePredicate(List<Component> components, int pad, String key, EntityTypePredicate entityTypePredicate) {
        if (entityTypePredicate != EntityTypePredicate.ANY) {
            MixinEntityTypePredicate predicate = (MixinEntityTypePredicate) entityTypePredicate;

            components.add(pad(pad, translatable(key)));

            if (predicate instanceof MixinEntityTypePredicate.TypePredicate typePredicate) {
                components.add(pad(pad + 1, value(typePredicate.getType().getDescription())));
            }
            if (predicate instanceof MixinEntityTypePredicate.TagPredicate tagPredicate) {
                components.add(pad(pad + 1, value(tagPredicate.getTag().location()))); //TODO to list
            }
        }
    }

    private static void addDistancePredicate(List<Component> components, int pad, String key, DistancePredicate distancePredicate) {
        if (distancePredicate != DistancePredicate.ANY) {
            MixinDistancePredicate predicate = (MixinDistancePredicate) distancePredicate;

            components.add(pad(pad, translatable(key)));
            addMinMaxBounds(components, pad + 1, "emi.property.dist_predicate.x", predicate.getX());
            addMinMaxBounds(components, pad + 1, "emi.property.dist_predicate.y", predicate.getY());
            addMinMaxBounds(components, pad + 1, "emi.property.dist_predicate.z", predicate.getZ());
            addMinMaxBounds(components, pad + 1, "emi.property.dist_predicate.horizontal", predicate.getHorizontal());
            addMinMaxBounds(components, pad + 1, "emi.property.dist_predicate.absolute", predicate.getAbsolute());
        }
    }

    private static void addDamageSourcePredicate(List<Component> components, int pad, Component component, DamageSourcePredicate damageSourcePredicate) {
        if (damageSourcePredicate != DamageSourcePredicate.ANY) {
            MixinDamageSourcePredicate predicate = (MixinDamageSourcePredicate) damageSourcePredicate;

            components.add(pad(pad, component));

            if (!predicate.getTags().isEmpty()) {
                components.add(pad(pad + 1, translatable("emi.property.damage_source.tags")));
                predicate.getTags().forEach((tagPredicate) -> {
                    MixinTagPredicate<?> p = (MixinTagPredicate<?>) tagPredicate;

                    components.add(pad(pad + 2, keyValue(p.getTag().location().toString(), p.getExpected())));
                });
            }

            addEntityPredicate(components, pad + 1, translatable("emi.property.damage_source.direct_entity"), predicate.getDirectEntity());
            addEntityPredicate(components, pad + 1, translatable("emi.property.damage_source.source_entity"), predicate.getSourceEntity());
        }
    }

    private static void addLocationPredicate(List<Component> components, int pad, String key, LocationPredicate predicate) {
        if (predicate != LocationPredicate.ANY) {
            MixinLocationPredicate locationPredicate = (MixinLocationPredicate) predicate;

            components.add(pad(pad, translatable(key)));
            addMinMaxBounds(components, pad + 1, "emi.property.location.x", locationPredicate.getX());
            addMinMaxBounds(components, pad + 1, "emi.property.location.y", locationPredicate.getY());
            addMinMaxBounds(components, pad + 1, "emi.property.location.z", locationPredicate.getZ());
            addResourceKey(components, pad + 1, "emi.property.location.biome", locationPredicate.getBiome());
            addResourceKey(components, pad + 1, "emi.property.location.structure", locationPredicate.getStructure());
            addResourceKey(components, pad + 1, "emi.property.location.dimension", locationPredicate.getDimension());
            addBoolean(components, pad + 1, "emi.property.location.smokey", locationPredicate.getSmokey());
            addLight(components, pad + 1, "emi.property.location.light", locationPredicate.getLight());
            addBlock(components, pad + 1, "emi.property.location.block", locationPredicate.getBlock());
            addFluid(components, pad + 1, "emi.property.location.fluid", locationPredicate.getFluid());
        }
    }

    private static void addMobEffectsPredicate(List<Component> components, int pad, String key, MobEffectsPredicate predicate) {
        if (predicate != MobEffectsPredicate.ANY) {
            MixinMobEffectPredicate locationPredicate = (MixinMobEffectPredicate) predicate;

            components.add(pad(pad, translatable(key)));

            locationPredicate.getEffects().forEach((effect, instancePredicate) -> {
                MixinMobEffectPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) instancePredicate;

                components.add(pad(pad + 1, value(translatable(effect.getDescriptionId()))));
                addMinMaxBounds(components, pad + 2, "emi.property.effect.amplifier", mobEffectInstancePredicate.getAmplifier());
                addMinMaxBounds(components, pad + 2, "emi.property.effect.duration", mobEffectInstancePredicate.getDuration());
                addBoolean(components, pad + 2, "emi.property.effect.ambient", mobEffectInstancePredicate.getAmbient());
                addBoolean(components, pad + 2, "emi.property.effect.visible", mobEffectInstancePredicate.getVisible());
            });
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

    private static void addBlock(List<Component> components, int pad, String key, BlockPredicate blockPredicate) {
        if (blockPredicate != BlockPredicate.ANY) {
            MixinBlockPredicate predicate = (MixinBlockPredicate) blockPredicate;

            components.add(pad(pad, translatable(key)));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.block.tag", predicate.getTag().location().toString())));
            }
            if (predicate.getBlocks() != null) {
                components.add(pad(pad + 1, translatable("emi.property.block.blocks")));

                predicate.getBlocks().forEach((block) -> components.add(pad(pad + 2, value(translatable(block.getDescriptionId())))));
            }
            addStateProperties(components, pad + 1, translatable("emi.property.block.state"), predicate.getProperties());
            addNbtPredicate(components, pad + 1, "emi.property.block.nbt", predicate.getNbt());
        }
    }

    private static void addFluid(List<Component> components, int pad, String key, FluidPredicate fluidPredicate) {
        if (fluidPredicate != FluidPredicate.ANY) {
            MixinFluidPredicate predicate = (MixinFluidPredicate) fluidPredicate;

            components.add(pad(pad, translatable(key)));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.fluid.tag", predicate.getTag().location().toString())));
            }
            if (predicate.getFluid() != null) {
                components.add(pad(pad + 1, translatable("emi.property.fluid.fluid", value(translatable(predicate.getFluid().getFluidType().getDescriptionId())))));
            }
            addStateProperties(components, pad + 1, translatable("emi.property.fluid.state"), predicate.getProperties());
        }
    }

    private static void addStateProperties(List<Component> components, int pad, Component component, StatePropertiesPredicate propertiesPredicate) {
        if (propertiesPredicate != StatePropertiesPredicate.ANY) {
            MixinStatePropertiesPredicate predicate = (MixinStatePropertiesPredicate) propertiesPredicate;

            components.add(pad(pad, component));

            predicate.getProperties().forEach((propertyMatcher) -> addPropertyMatcher(components, pad + 1, propertyMatcher));
        }
    }

    private static void addEntityFlagsPredicate(List<Component> components, int pad, String key, EntityFlagsPredicate entityFlagsPredicate) {
        if (entityFlagsPredicate != EntityFlagsPredicate.ANY) {
            MixinEntityFlagsPredicate predicate = (MixinEntityFlagsPredicate) entityFlagsPredicate;

            components.add(pad(pad, translatable(key)));

            addBoolean(components, pad + 1, "emi.property.flags.on_fire", predicate.getIsOnFire());
            addBoolean(components, pad + 1, "emi.property.flags.is_baby", predicate.getIsBaby());
            addBoolean(components, pad + 1, "emi.property.flags.is_crouching", predicate.getIsCrouching());
            addBoolean(components, pad + 1, "emi.property.flags.is_sprinting", predicate.getIsSprinting());
            addBoolean(components, pad + 1, "emi.property.flags.is_swimming", predicate.getIsSwimming());
        }
    }

    private static void addEntityEquipmentPredicate(List<Component> components, int pad, String key, EntityEquipmentPredicate entityEquipmentPredicate) {
        if (entityEquipmentPredicate != EntityEquipmentPredicate.ANY) {
            MixinEntityEquipmentPredicate predicate = (MixinEntityEquipmentPredicate) entityEquipmentPredicate;

            components.add(pad(pad, translatable(key)));

            addItemPredicate(components, pad + 1, translatable("emi.property.equipment.head"), predicate.getHead());
            addItemPredicate(components, pad + 1, translatable("emi.property.equipment.chest"), predicate.getChest());
            addItemPredicate(components, pad + 1, translatable("emi.property.equipment.legs"), predicate.getLegs());
            addItemPredicate(components, pad + 1, translatable("emi.property.equipment.feet"), predicate.getFeet());
            addItemPredicate(components, pad + 1, translatable("emi.property.equipment.mainhand"), predicate.getMainhand());
            addItemPredicate(components, pad + 1, translatable("emi.property.equipment.offhand"), predicate.getOffhand());
        }
    }

    private static void addEntitySubPredicate(List<Component> components, int pad, String key, EntitySubPredicate entitySubPredicate) {
        if (entitySubPredicate != EntitySubPredicate.ANY) {
            Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

            optional.ifPresent((entry) -> {
                components.add(pad(pad, translatable(key, entry.getKey())));

                if (entitySubPredicate instanceof LighthingBoltPredicate predicate) {
                    MixinLighthingBoltPredicate boltPredicate = (MixinLighthingBoltPredicate) predicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.sub_entity.blocks_on_fire", boltPredicate.getBlocksSetOnFire());
                    addEntityPredicate(components, pad + 2, translatable("emi.property.sub_entity.stuck_entity"), boltPredicate.getEntityStruck());
                } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                    MixinFishingHookPredicate fishingHookPredicate = (MixinFishingHookPredicate) predicate;

                    components.add(pad(pad + 1, translatable("emi.property.sub_entity.in_open_water", fishingHookPredicate.isInOpenWater())));
                } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                    MixinPlayerPredicate playerPredicate = (MixinPlayerPredicate) predicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.sub_entity.level", playerPredicate.getLevel());

                    if (playerPredicate.getGameType() != null) {
                        components.add(pad(pad + 1, translatable("emi.property.sub_entity.game_type", value(playerPredicate.getGameType().getShortDisplayName()))));
                    }

                    if (!playerPredicate.getStats().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.sub_entity.stats")));
                        playerPredicate.getStats().forEach((stat, ints) -> {
                            Object value = stat.getValue();
                            components.add(pad(pad + 2, value instanceof Item ? translatable(((Item) value).getDescriptionId()) : value));
                            components.add(pad(pad + 3, keyValue(stat.getType().getDisplayName(), EmiBaseLoot.toString((MixinMinMaxBounds.Ints) ints))));
                        });
                    }

                    if (!playerPredicate.getRecipes().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.sub_entity.recipes")));
                        playerPredicate.getRecipes().forEach((recipe, required) -> components.add(pad(pad + 2, keyValue(recipe.toString(), required))));
                    }

                    if (!playerPredicate.getAdvancements().isEmpty()) {
                        components.add(pad(pad + 1, translatable("emi.property.sub_entity.advancements")));
                        playerPredicate.getAdvancements().forEach((advancement, advancementPredicate) -> {
                            components.add(pad(pad + 2, advancement.toString()));

                            if (advancementPredicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                                components.add(pad(pad + 3, translatable("emi.property.sub_entity.advancement.done", ((MixinPlayerPredicate.AdvancementDonePredicate) donePredicate).getState())));
                            } else if (advancementPredicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                                MixinPlayerPredicate.AdvancementCriterionsPredicate mixPredicate = (MixinPlayerPredicate.AdvancementCriterionsPredicate) criterionsPredicate;

                                mixPredicate.getCriterions().forEach((criterion, state) -> components.add(pad(pad + 3, keyValue(criterion, state))));
                            }
                        });
                    }
                } if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                    MixinSlimePredicate predicate = (MixinSlimePredicate) slimePredicate;

                    addMinMaxBounds(components, pad + 1, "emi.property.sub_entity.size", predicate.getSize());
                } else {
                    JsonObject jsonObject = entitySubPredicate.serializeCustomData();

                    if (jsonObject.has("variant")) {
                        components.add(pad(pad + 1, translatable("emi.property.sub_entity.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                    } else {
                        components.add(pad(pad + 1, translatable("emi.property.sub_entity.variant", jsonObject.toString())));
                    }
                }
            });
        }
    }

    private static void addItemPredicate(List<Component> components, int pad, Component component, ItemPredicate itemPredicate) {
        if (itemPredicate != ItemPredicate.ANY) {
            MixinItemPredicate predicate = (MixinItemPredicate) itemPredicate;

            components.add(pad(pad, component));

            if (predicate.getTag() != null) {
                components.add(pad(pad + 1, translatable("emi.property.item.tag", predicate.getTag().location().toString())));
            }

            if (predicate.getItems() != null) {
                components.add(pad(pad + 1, translatable("emi.property.item.items")));

                predicate.getItems().forEach((item) -> components.add(pad(pad + 2, value(translatable(item.getDescriptionId())))));
            }

            addMinMaxBounds(components, pad + 1, "emi.property.item.count", predicate.getCount());
            addMinMaxBounds(components, pad + 1, "emi.property.item.durability", predicate.getDurability());

            for (EnchantmentPredicate enchantment : predicate.getEnchantments()) {
                addEnchantmentPredicate(components, pad + 1, "emi.property.item.enchantment", enchantment);
            }

            for (EnchantmentPredicate enchantment : predicate.getStoredEnchantments()) {
                addEnchantmentPredicate(components, pad + 1, "emi.property.item.stored_enchantment", enchantment);
            }

            if (predicate.getPotion() != null) {
                components.add(pad(pad + 1, translatable("emi.property.item.potion")));

                predicate.getPotion().getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
            }

            addNbtPredicate(components, pad + 1, "emi.property.item.nbt", predicate.getNbt());
        }
    }

    private static void addEnchantmentPredicate(List<Component> components, int pad, String key, EnchantmentPredicate enchantmentPredicate) {
        if (enchantmentPredicate != EnchantmentPredicate.ANY) {
            MixinEnchantmentPredicate predicate = (MixinEnchantmentPredicate) enchantmentPredicate;

            components.add(pad(pad, translatable(key)));

            if (predicate.getEnchantment() != null) {
                components.add(pad(pad + 1, value(translatable(predicate.getEnchantment().getDescriptionId()))));
            }
            addMinMaxBounds(components, pad + 1, "emi.property.enchantment.level", predicate.getLevel());
        }
    }

    private static void addNbtPredicate(List<Component> components, int pad, String key, NbtPredicate nbtPredicate) {
        if (nbtPredicate != NbtPredicate.ANY) {
            components.add(pad(pad, translatable(key, value(((MixinNbtPredicate) nbtPredicate).getTag().toString()))));
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
}
