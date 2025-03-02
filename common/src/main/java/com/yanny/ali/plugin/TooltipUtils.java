package com.yanny.ali.plugin;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.ILootCondition;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.condition.RandomChanceCondition;
import com.yanny.ali.plugin.condition.RandomChanceWithLootingCondition;
import com.yanny.ali.plugin.condition.TableBonusCondition;
import com.yanny.ali.plugin.entry.SingletonEntry;
import com.yanny.ali.plugin.function.*;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class TooltipUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

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
            components.add(pad(pad, translatable("ali.property.function.conditions")));
            components.addAll(TooltipUtils.getConditions(function.conditions, pad + 1));
        }

        return components;
    }

    public static void addItemPredicate(List<Component> components, int pad, Component component, ItemPredicate predicate) {
        components.add(pad(pad, component));

        predicate.tag().ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.condition.item.tag", t.location().toString()))));
        predicate.items().ifPresent((i) -> {
            components.add(pad(pad + 1, translatable("ali.property.condition.item.items")));
            i.forEach((item) -> components.add(pad(pad + 2, value(translatable(item.value().getDescriptionId())))));
        });
        addMinMaxBounds(components, pad + 1, "ali.property.condition.item.count", predicate.count());
        addMinMaxBounds(components, pad + 1, "ali.property.condition.item.durability", predicate.durability());

        for (EnchantmentPredicate enchantment : predicate.enchantments()) {
            addEnchantmentPredicate(components, pad + 1, "ali.property.condition.item.enchantment", enchantment);
        }

        for (EnchantmentPredicate enchantment : predicate.storedEnchantments()) {
            addEnchantmentPredicate(components, pad + 1, "ali.property.condition.item.stored_enchantment", enchantment);
        }

        predicate.potion().ifPresent((p) -> {
            components.add(pad(pad + 1, translatable("ali.property.condition.item.potion")));

            p.value().getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
        });

        predicate.nbt().ifPresent((n) -> addNbtPredicate(components, pad + 1, "ali.property.condition.item.nbt", n));
    }

    public static void addLocationPredicate(List<Component> components, int pad, Component component, LocationPredicate predicate) {
        predicate.position().ifPresent((p) -> {
            addMinMaxBounds(components, pad, "ali.property.condition.location.x", p.x());
            addMinMaxBounds(components, pad, "ali.property.condition.location.y", p.y());
            addMinMaxBounds(components, pad, "ali.property.condition.location.z", p.z());
        });
        predicate.biome().ifPresent((b) -> addResourceKey(components, pad, "ali.property.condition.location.biome", b));
        predicate.structure().ifPresent((s) -> addResourceKey(components, pad, "ali.property.condition.location.structure", s));
        predicate.dimension().ifPresent((d) -> addResourceKey(components, pad, "ali.property.condition.location.dimension", d));
        predicate.smokey().ifPresent((s) -> addBoolean(components, pad, "ali.property.condition.location.smokey", s));
        predicate.light().ifPresent((l) -> addLight(components, pad, "ali.property.condition.location.light", l));
        predicate.block().ifPresent((b) -> addBlock(components, pad, translatable("ali.property.condition.location.block"), b));
        predicate.fluid().ifPresent((f) -> addFluid(components, pad, translatable("ali.property.condition.location.fluid"), f));
    }

    public static void addEntityPredicate(List<Component> components, int pad, Component component, EntityPredicate predicate) {
        components.add(pad(pad, component));

        predicate.entityType().ifPresent((e) -> addEntityTypePredicate(components, pad + 1, translatable("ali.property.condition.predicate.entity_type"), e));
        predicate.distanceToPlayer().ifPresent((d) -> addDistancePredicate(components, pad + 1, translatable("ali.property.condition.predicate.dist_to_player"), d));
        predicate.location().ifPresent((l) -> addLocationPredicate(components, pad + 1, translatable("ali.property.condition.predicate.location"), l));
        predicate.steppingOnLocation().ifPresent((l) -> addLocationPredicate(components, pad + 1, translatable("ali.property.condition.predicate.stepping_on_location"), l));
        predicate.effects().ifPresent((e) -> addMobEffectsPredicate(components, pad + 1, translatable("ali.property.condition.predicate.effect"), e));
        predicate.nbt().ifPresent((n) -> addNbtPredicate(components, pad + 1, "ali.property.condition.predicate.nbt", n));
        predicate.flags().ifPresent((f) -> addEntityFlagsPredicate(components, pad + 1, translatable("ali.property.condition.predicate.flags"), f));
        predicate.equipment().ifPresent((e) -> addEntityEquipmentPredicate(components, pad + 1, translatable("ali.property.condition.predicate.equipment"), e));
        predicate.subPredicate().ifPresent((s) -> addEntitySubPredicate(components, pad + 1, "ali.property.condition.predicate.entity_sub_type", s));

        predicate.vehicle().ifPresent((v) -> addEntityPredicate(components, pad + 1, translatable("ali.property.condition.predicate.vehicle"), v));
        predicate.passenger().ifPresent((p) -> addEntityPredicate(components, pad + 1, translatable("ali.property.condition.predicate.passenger"), p));
        predicate.targetedEntity().ifPresent((t) -> addEntityPredicate(components, pad + 1, translatable("ali.property.condition.predicate.targeted_entity"), t));
        predicate.team().ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.condition.predicate.team", t))));
    }

    private static void addMobEffectsPredicate(List<Component> components, int pad, Component component, MobEffectsPredicate predicate) {
        components.add(pad(pad, component));

        predicate.effectMap().forEach((effect, instancePredicate) -> {
            components.add(pad(pad + 1, value(translatable(effect.value().getDescriptionId()))));

            addMinMaxBounds(components, pad + 2, "ali.property.condition.effect.amplifier", instancePredicate.amplifier());
            addMinMaxBounds(components, pad + 2, "ali.property.condition.effect.duration", instancePredicate.duration());
            instancePredicate.ambient().ifPresent((a) -> addBoolean(components, pad + 2, "ali.property.condition.effect.ambient", a));
            instancePredicate.visible().ifPresent((v) -> addBoolean(components, pad + 2, "ali.property.condition.effect.visible", v));
        });
    }

    private static void addEntityEquipmentPredicate(List<Component> components, int pad, Component component, EntityEquipmentPredicate predicate) {
        components.add(pad(pad, component));

        predicate.head().ifPresent((h) -> addItemPredicate(components, pad + 1, translatable("ali.property.condition.equipment.head"), h));
        predicate.chest().ifPresent((c) -> addItemPredicate(components, pad + 1, translatable("ali.property.condition.equipment.chest"), c));
        predicate.legs().ifPresent((l) -> addItemPredicate(components, pad + 1, translatable("ali.property.condition.equipment.legs"), l));
        predicate.feet().ifPresent((f) -> addItemPredicate(components, pad + 1, translatable("ali.property.condition.equipment.feet"), f));
        predicate.mainhand().ifPresent((m) -> addItemPredicate(components, pad + 1, translatable("ali.property.condition.equipment.mainhand"), m));
        predicate.offhand().ifPresent((o) -> addItemPredicate(components, pad + 1, translatable("ali.property.condition.equipment.offhand"), o));
    }

    private static void addEntityFlagsPredicate(List<Component> components, int pad, Component component, EntityFlagsPredicate predicate) {
        components.add(pad(pad, component));

        predicate.isOnFire().ifPresent((i) -> addBoolean(components, pad + 1, "ali.property.condition.flags.on_fire", i));
        predicate.isBaby().ifPresent((i) -> addBoolean(components, pad + 1, "ali.property.condition.flags.is_baby", i));
        predicate.isCrouching().ifPresent((i) -> addBoolean(components, pad + 1, "ali.property.condition.flags.is_crouching", i));
        predicate.isSprinting().ifPresent((i) -> addBoolean(components, pad + 1, "ali.property.condition.flags.is_sprinting", i));
        predicate.isSwimming().ifPresent((i) -> addBoolean(components, pad + 1, "ali.property.condition.flags.is_swimming", i));
    }

    private static void addEntitySubPredicate(List<Component> components, int pad, String key, EntitySubPredicate entitySubPredicate) {
        Optional<Map.Entry<String, EntitySubPredicate.Type>> optional = EntitySubPredicate.Types.TYPES.entrySet().stream().filter((p) -> p.getValue() == entitySubPredicate.type()).findFirst();

        optional.ifPresent((entry) -> {
            components.add(pad(pad, translatable(key, entry.getKey())));

            if (entitySubPredicate instanceof LightningBoltPredicate predicate) {
                addMinMaxBounds(components, pad + 1, "ali.property.condition.sub_entity.blocks_on_fire", predicate.blocksSetOnFire());
                predicate.entityStruck().ifPresent((e) -> addEntityPredicate(components, pad + 2, translatable("ali.property.condition.sub_entity.stuck_entity"), e));
            } else if (entitySubPredicate instanceof FishingHookPredicate predicate) {
                predicate.inOpenWater().ifPresent((o) -> components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.in_open_water", o))));
            } else if (entitySubPredicate instanceof PlayerPredicate predicate) {
                addMinMaxBounds(components, pad + 1, "ali.property.condition.sub_entity.level", predicate.level());

                predicate.gameType().ifPresent((g) -> components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.game_type", value(g.getShortDisplayName())))));

                if (!predicate.stats().isEmpty()) {
                    components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.stats")));
                    predicate.stats().forEach((stat) -> {
                        Object value = stat.value();
                        components.add(pad(pad + 2, value instanceof Item ? translatable(((Item) value).getDescriptionId()) : value));
                        components.add(pad(pad + 3, keyValue(stat.type().getDisplayName(), toString(stat.range()))));
                    });
                }

                if (!predicate.recipes().isEmpty()) {
                    components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.recipes")));
                    predicate.recipes().forEach((recipe, required) -> components.add(pad(pad + 2, keyValue(recipe.toString(), required))));
                }

                if (!predicate.advancements().isEmpty()) {
                    components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.advancements")));
                    predicate.advancements().forEach((advancement, advancementPredicate) -> {
                        components.add(pad(pad + 2, advancement.toString()));

                        if (advancementPredicate instanceof PlayerPredicate.AdvancementDonePredicate donePredicate) {
                            components.add(pad(pad + 3, translatable("ali.property.condition.sub_entity.advancement.done", donePredicate.state())));
                        } else if (advancementPredicate instanceof PlayerPredicate.AdvancementCriterionsPredicate criterionsPredicate) {
                            criterionsPredicate.criterions().forEach((criterion, state) -> components.add(pad(pad + 3, keyValue(criterion, state))));
                        }
                    });
                }
            } if (entitySubPredicate instanceof SlimePredicate slimePredicate) {
                addMinMaxBounds(components, pad + 1, "ali.property.condition.sub_entity.size", slimePredicate.size());
            } else {
                JsonObject jsonObject = EntitySubPredicate.CODEC.encodeStart(JsonOps.INSTANCE, entitySubPredicate).result().get().getAsJsonObject();

                if (jsonObject.has("variant")) {
                    components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.variant", jsonObject.getAsJsonPrimitive("variant").getAsString())));
                } else {
                    components.add(pad(pad + 1, translatable("ali.property.condition.sub_entity.variant", jsonObject.toString())));
                }
            }
        });
    }

    private static void addDistancePredicate(List<Component> components, int pad, Component component, DistancePredicate predicate) {
        components.add(pad(pad, component));

        addMinMaxBounds(components, pad + 1, "ali.property.condition.dist_predicate.x", predicate.x());
        addMinMaxBounds(components, pad + 1, "ali.property.condition.dist_predicate.y", predicate.y());
        addMinMaxBounds(components, pad + 1, "ali.property.condition.dist_predicate.z", predicate.z());
        addMinMaxBounds(components, pad + 1, "ali.property.condition.dist_predicate.horizontal", predicate.horizontal());
        addMinMaxBounds(components, pad + 1, "ali.property.condition.dist_predicate.absolute", predicate.absolute());
    }

    private static void addEntityTypePredicate(List<Component> components, int pad, Component component, EntityTypePredicate predicate) {
        components.add(pad(pad, component));

        for (Holder<EntityType<?>> holder : predicate.types()) {
            components.add(pad(pad + 1, value(holder.value().getDescription())));
        }
    }

    private static void addEnchantmentPredicate(List<Component> components, int pad, String key, EnchantmentPredicate predicate) {
        components.add(pad(pad, translatable(key)));

        predicate.enchantment().ifPresent((e) -> components.add(pad(pad + 1, value(translatable(e.value().getDescriptionId())))));
        addMinMaxBounds(components, pad + 1, "ali.property.condition.enchantment.level", predicate.level());
    }

    private static void addResourceKey(List<Component> components, int pad, String key, ResourceKey<?> resourceKey) {
        components.add(pad(pad, translatable(key, value(resourceKey.location().toString()))));
    }

    private static void addBoolean(List<Component> components, int pad, String key, boolean aBoolean) {
        components.add(pad(pad, translatable(key, value(aBoolean))));
    }

    private static void addLight(List<Component> components, int pad, String key, LightPredicate lightPredicate) {
        addMinMaxBounds(components, pad, key, lightPredicate.composite());
    }

    private static void addBlock(List<Component> components, int pad, Component component, BlockPredicate predicate) {
        components.add(pad(pad, component));

        predicate.tag().ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.condition.block.tag", t.location().toString()))));
        predicate.blocks().ifPresent((b) -> {
            components.add(pad(pad + 1, translatable("ali.property.condition.block.blocks")));
            b.forEach((block) -> components.add(pad(pad + 2, value(translatable(block.value().getDescriptionId())))));
        });
        predicate.properties().ifPresent((p) -> addStateProperties(components, pad + 1, translatable("ali.property.condition.block.state"), p));
        predicate.nbt().ifPresent((n) -> addNbtPredicate(components, pad + 1, "ali.property.condition.block.nbt", n));
    }

    private static void addFluid(List<Component> components, int pad, Component component, FluidPredicate predicate) {
        components.add(pad(pad, component));

        predicate.tag().ifPresent((t) -> components.add(pad(pad + 1, translatable("ali.property.condition.fluid.tag", t.location().toString()))));
        predicate.fluid().ifPresent((f) -> components.add(pad(pad + 1, translatable("ali.property.condition.fluid.fluid", value(translatable(BuiltInRegistries.FLUID.getKey(f.value()).toString()))))));
        predicate.properties().ifPresent((p) -> addStateProperties(components, pad + 1, translatable("ali.property.condition.fluid.state"), p));
    }

    public static void addStateProperties(List<Component> components, int pad, Component component, StatePropertiesPredicate predicate) {
        components.add(pad(pad, component));

        predicate.properties().forEach((propertyMatcher) -> addPropertyMatcher(components, pad + 1, propertyMatcher));
    }

    public static void addDamageSourcePredicate(List<Component> components, int pad, Component component, DamageSourcePredicate predicate) {
        components.add(pad(pad, component));

        if (!predicate.tags().isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.condition.damage_source.tags")));
            predicate.tags().forEach((t) -> components.add(pad(pad + 2, keyValue(t.tag().location().toString(), t.expected()))));
        }

        predicate.directEntity().ifPresent((d) -> addEntityPredicate(components, pad + 1, translatable("ali.property.condition.damage_source.direct_entity"), d));
        predicate.sourceEntity().ifPresent((s) -> addEntityPredicate(components, pad + 1, translatable("ali.property.condition.damage_source.source_entity"), s));
    }

    private static void addPropertyMatcher(List<Component> components, int pad, StatePropertiesPredicate.PropertyMatcher propertyMatcher) {
        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher matcher) {
            components.add(pad(pad, keyValue(propertyMatcher.name(), matcher.value())));
        }

        if (propertyMatcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher matcher) {
            components.add(pad(pad, value(propertyMatcher.name() + "=[" + matcher.minValue() + "-" + matcher.maxValue() + "]")));
        }
    }

    private static void addNbtPredicate(List<Component> components, int pad, String key, NbtPredicate predicate) {
        components.add(pad(pad, translatable(key, value(predicate.tag().toString()))));
    }

    private static void addMinMaxBounds(List<Component> components, int pad, String key, MinMaxBounds.Doubles doubles) {
        if (doubles.min().isPresent() || doubles.max().isPresent()) {
            components.add(pad(pad, translatable(key, value(toString(doubles)))));
        }
    }

    private static void addMinMaxBounds(List<Component> components, int pad, String key, MinMaxBounds.Ints ints) {
        if (ints.min().isPresent() || ints.max().isPresent()) {
            components.add(pad(pad, translatable(key, value(toString(ints)))));
        }
    }

    @NotNull
    private static String toString(MinMaxBounds.Doubles doubles) {
        if (doubles.min().isPresent()) {
            if (doubles.max().isPresent()) {
                if (!Objects.equals(doubles.min(), doubles.max())) {
                    return String.format("%.1f-%.1f", doubles.min().get(), doubles.max().get());
                } else {
                    return String.format("=%.1f", doubles.min().get());
                }
            } else {
                return String.format("≥%.1f", doubles.min().get());
            }
        } else {
            if (doubles.max().isPresent()) {
                return String.format("<%.1f", doubles.max().get());
            }

            return "???";
        }
    }

    @NotNull
    private static String toString(MinMaxBounds.Ints ints) {
        if (ints.min().isPresent()) {
            if (ints.max().isPresent()) {
                if (!Objects.equals(ints.min(), ints.max())) {
                    return String.format("%d-%d", ints.min().get(), ints.max().get());
                } else {
                    return String.format("=%d", ints.min().get());
                }
            } else {
                return String.format("≥%d", ints.min().get());
            }
        } else {
            if (ints.max().isPresent()) {
                return String.format("<%d", ints.max().get());
            }

            return "???";
        }
    }

    @NotNull
    @Unmodifiable
    public static List<Component> getQuality(SingletonEntry entry) {
        if (entry.quality != 0) {
            return List.of(translatable("ali.description.quality", entry.quality));
        }

        return List.of();
    }

    @NotNull
    public static Component getCount(RangeValue count) {
        return translatable("ali.description.count", count);
    }

    @NotNull
    public static Component getChance(RangeValue chance) {
        return translatable("ali.description.chance", value(chance, "%"));
    }

    @NotNull
    public static List<Component> getBonusChance(@Nullable Pair<Enchantment, Map<Integer, RangeValue>> bonusChance) {
        List<Component> components = new LinkedList<>();

        if (bonusChance != null) {
            bonusChance.getSecond().forEach((level, value) -> components.add(pad(1, translatable(
                    "ali.description.chance_bonus",
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
                    "ali.description.count_bonus",
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
            value.set(new RangeValue(condition.values.get(0)));
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
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(condition.location);

            if (enchantment != null) {
                for (int level = 1; level < condition.values.size(); level++) {
                    RangeValue value = new RangeValue(condition.values.get(level));
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

        functions.stream().filter((f) -> f instanceof ApplyBonusFunction).forEach((f) ->
                ((ApplyBonusFunction) f).formula.calculateCount(value, 0));

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
            Enchantment enchantment = function.enchantment.value();

            for (int level = 1; level < enchantment.getMaxLevel() + 1; level++) {
                RangeValue value = new RangeValue(count);
                function.formula.calculateCount(value, level);
                bonusCount.put(level, value);
            }

            return new Pair<>(enchantment, bonusCount);
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

    @NotNull
    public static MutableComponent translatableType(String prefix, Enum<?> type, Object... args) {
        return translatable(prefix + "." + type.name().toLowerCase(), args);
    }

    @NotNull
    public static MutableComponent translatableType(String prefix, String type, Object... args) {
        return translatable(prefix + "." + type.toLowerCase(), args);
    }

    @NotNull
    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(key, Arrays.stream(args).map((arg) -> {
            if (arg instanceof MutableComponent) {
                return arg;
            } else {
                return Component.literal(arg.toString()).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
            }
        }).toArray()).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static MutableComponent value(Object value) {
        if (value instanceof MutableComponent) {
            return ((MutableComponent) value).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
        } else {
            return Component.literal(value.toString()).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
        }
    }

    @NotNull
    public static MutableComponent value(Object value, String unit) {
        return Component.translatable("ali.util.advanced_loot_info.two_values", value, unit).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
    }

    @NotNull
    public static MutableComponent pair(Object value1, Object value2) {
        return Component.translatable("ali.util.advanced_loot_info.two_values_with_space", value1, value2).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static MutableComponent pad(int count, Object arg) {
        if (count > 0) {
            return pair(Component.translatable("ali.util.advanced_loot_info.pad." + count), arg);
        } else {
            if (arg instanceof MutableComponent) {
                return ((MutableComponent) arg).withStyle(TEXT_STYLE);
            } else {
                return Component.literal(arg.toString()).withStyle(TEXT_STYLE);
            }
        }
    }

    @NotNull
    public static MutableComponent keyValue(Object key, Object value) {
        return translatable("ali.util.advanced_loot_info.key_value", key instanceof MutableComponent ? key : Component.literal(key.toString()), value(value));
    }
}
