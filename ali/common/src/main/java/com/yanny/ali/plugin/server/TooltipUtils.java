package com.yanny.ali.plugin.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TooltipUtils {
    public static final ResourceLocation LUCK = Utils.modLoc("luck");

    public static ItemStack getItemStack(IServerUtils utils, ItemStack itemStack, List<LootItemFunction> functions) {
        for (LootItemFunction function : functions) {
            itemStack = utils.applyItemStackModifier(utils, function, itemStack);
        }

        return itemStack;
    }

    @NotNull
    public static NumberExpr applyRandomChance(IServerUtils ignoredUtils, LootItemRandomChanceCondition condition, NumberExpr chance) {
        return NumberExpr.mul(chance, NumberExpr.constant(condition.probability));
    }

    @NotNull
    public static NumberExpr applyRandomChanceWithLooting(IServerUtils ignoredUtils, LootItemRandomChanceWithLootingCondition condition, NumberExpr chance) {
        NumberExpr level = level(Enchantments.MOB_LOOTING);

        return NumberExpr.mul(chance, NumberExpr.add(NumberExpr.constant(condition.percent), NumberExpr.mul(level, NumberExpr.constant(condition.lootingMultiplier))));
    }

    @NotNull
    public static NumberExpr applyTableBonus(IServerUtils ignoredUtils, BonusLevelTableCondition condition, NumberExpr chance) {
        if (condition.values.length == 0) {
            return chance;
        }

        List<NumberExpr> values = new ArrayList<>();

        for (float value : condition.values) {
            values.add(NumberExpr.constant(value));
        }

        return NumberExpr.mul(chance, NumberExpr.lookup(level(condition.enchantment), values, null));
    }

    @NotNull
    public static NumberExpr applySetCount(IServerUtils utils, SetItemCountFunction function, NumberExpr count) {
        NumberExpr value = utils.convertIntNumber(utils, function.value);

        return NumberExpr.max(NumberExpr.constant(0), function.add ? NumberExpr.add(count, value) : value);
    }

    @NotNull
    public static NumberExpr applyBonus(IServerUtils ignoredUtils, ApplyBonusCount function, NumberExpr count) {
        NumberExpr level = level(function.enchantment);

        if (function.formula instanceof ApplyBonusCount.OreDrops) {
            return NumberExpr.mul(count, NumberExpr.max(NumberExpr.constant(1), NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.add(level, NumberExpr.constant(1)))));
        } else if (function.formula instanceof ApplyBonusCount.BinomialWithBonusCount formula) {
            return NumberExpr.add(count, NumberExpr.binomial(NumberExpr.add(level, NumberExpr.constant(formula.extraRounds)), NumberExpr.constant(formula.probability)));
        } else if (function.formula instanceof ApplyBonusCount.UniformBonusCount formula) {
            return NumberExpr.add(count, NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.mul(NumberExpr.constant(formula.bonusMultiplier), level)));
        }

        return count;
    }

    @NotNull
    public static NumberExpr applyLimitCount(IServerUtils utils, LimitCount function, NumberExpr count) {
        return limit(utils, count, function.limiter);
    }

    @NotNull
    public static NumberExpr applyLootingEnchant(IServerUtils utils, LootingEnchantFunction function, NumberExpr count) {
        NumberExpr bonus = NumberExpr.fn(NumberFunctions.ROUND, NumberExpr.mul(level(Enchantments.MOB_LOOTING), utils.convertNumber(utils, function.value)));
        NumberExpr result = NumberExpr.add(count, bonus);

        return function.limit > 0 ? NumberExpr.min(result, NumberExpr.constant(function.limit)) : result;
    }

    @NotNull
    public static NumberExpr limit(IServerUtils utils, NumberExpr value, IntRange range) {
        NumberExpr min = range.min != null ? utils.convertIntNumber(utils, range.min) : null;
        NumberExpr max = range.max != null ? utils.convertIntNumber(utils, range.max) : null;

        if (min != null && max != null) {
            return NumberExpr.clamp(value, min, max);
        } else if (min != null) {
            return NumberExpr.max(value, min);
        } else if (max != null) {
            return NumberExpr.min(value, max);
        }

        return value;
    }

    @NotNull
    public static NumberExpr level(Enchantment enchantment) {
        return NumberExpr.level(String.valueOf(BuiltInRegistries.ENCHANTMENT.getKey(enchantment)), enchantment.getMaxLevel());
    }

    @NotNull
    public static NumberExpr conditional(IServerUtils utils, NumberExpr original, NumberExpr modified, List<LootItemCondition> predicates, List<TooltipNode> conditions) {
        List<LootItemCondition> leaves = predicates.stream().flatMap((c) -> utils.unwrapCondition(utils, c).stream()).toList();

        if (leaves.stream().allMatch(LootItemRandomChanceCondition.class::isInstance)) {
            double probability = 1;

            for (LootItemCondition leaf : leaves) {
                probability *= ((LootItemRandomChanceCondition) leaf).probability;
            }

            if (probability >= 1) {
                return modified;
            } else if (probability <= 0) {
                return original;
            }

            return NumberExpr.weighted(List.of(new NumberExpr.WeightedEntry(probability, modified), new NumberExpr.WeightedEntry(1 - probability, original)));
        }

        TooltipNode condition = TooltipBuilder.array((b) -> predicates.forEach((p) -> b.add(utils.getConditionTooltip(utils, p)))).build();
        int index = -1;

        if (condition != TooltipNode.empty()) {
            index = conditions.size();
            conditions.add(condition);
        }

        return NumberExpr.cond(List.of(new NumberExpr.Branch(index, modified)), original);
    }

    public static boolean isConditional(LootItemConditionalFunction function) {
        return function.predicates.length != 0;
    }

    @NotNull
    public static NumberExpr rolls(IServerUtils utils, NumberProvider rolls, NumberProvider bonusRolls) {
        NumberExpr base = utils.convertIntNumber(utils, rolls);
        NumberExpr bonus = utils.convertNumber(utils, bonusRolls);

        if (bonus instanceof NumberExpr.Const c && c.value() == 0) {
            return base;
        }

        NumberExpr luck = new NumberExpr.Var(LUCK, List.of(), -1, 4);

        return NumberExpr.max(NumberExpr.constant(0), NumberExpr.add(base, NumberExpr.fn(NumberFunctions.FLOOR, NumberExpr.mul(bonus, luck))));
    }

    @NotNull
    public static List<LootItemCondition> unwrapAllOf(IServerUtils ignoredUtils, AllOfCondition condition) {
        return Arrays.asList(condition.terms);
    }

    @NotNull
    public static List<LootItemCondition> unwrapCompositePredicate(IServerUtils ignoredUtils, LootDataManager.CompositePredicate condition) {
        return Arrays.asList(condition.terms);
    }

    @Unmodifiable
    @Nullable
    public static List<LootItemCondition> unwrapConditionReference(IServerUtils utils, ConditionReference condition) {
        LootItemCondition referenced = utils.getServerLevel().getServer().getLootData().getElement(new LootDataId<>(LootDataType.PREDICATE, condition.name));
        return referenced != null ? Collections.singletonList(referenced) : null;
    }

    @NotNull
    public static List<LootItemFunction> unwrapFunctionSequence(IServerUtils ignoredUtils, LootDataManager.FunctionSequence function) {
        return Arrays.asList(function.functions);
    }

    @Unmodifiable
    @Nullable
    public static List<LootItemFunction> unwrapFunctionReference(IServerUtils utils, FunctionReference function) {
        if (isConditional(function)) {
            return null;
        }

        LootItemFunction referenced = utils.getServerLevel().getServer().getLootData().getElement(new LootDataId<>(LootDataType.MODIFIER, function.name));
        return referenced != null ? Collections.singletonList(referenced) : null;
    }

    @NotNull
    public static ItemStack applyEnchantRandomlyItemStackModifier(IServerUtils ignoredUtils, EnchantRandomlyFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.length == 0) {
            boolean isBook = itemStack.is(Items.BOOK);
            ItemStack finalItemStack = itemStack;
            List<Enchantment> enchantments = function.enchantments;

            if (enchantments.isEmpty()) {
                enchantments = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).filter((enchantment) -> isBook || enchantment.canEnchant(finalItemStack)).toList();
            }

            if (enchantments.size() == 1 && enchantments.get(0).getMinLevel() == enchantments.get(0).getMaxLevel()) {
                itemStack.enchant(enchantments.get(0), enchantments.get(0).getMaxLevel());
            } else if (isBook) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IServerUtils ignoredUtils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.length == 0) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IServerUtils ignoredUtils, SetAttributesFunction function, ItemStack itemStack) {
        if (function.predicates.length == 0) {
            for (SetAttributesFunction.Modifier modifier : function.modifiers) {
                UUID id = modifier.id;

                if (id == null) {
                    id = UUID.randomUUID();
                }

                if (modifier.slots.length == 1 && modifier.amount.getType() == NumberProviders.CONSTANT) {
                    EquipmentSlot equipmentSlot = Util.getRandom(modifier.slots, RandomSource.create());
                    ConstantValue value = (ConstantValue) modifier.amount;

                    itemStack.addAttributeModifier(modifier.attribute, new AttributeModifier(id, modifier.name, value.getFloat(null), modifier.operation), equipmentSlot);
                }
            }
        }

        return itemStack;
    }

    public static ItemStack applySetNameItemStackModifier(IServerUtils ignoredUtils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.length == 0) {
            itemStack.setHoverName(function.name);
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IServerUtils ignoredUtils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && conditional.predicates.length > 0) {
            return itemStack;
        }

        itemStack = function.apply(itemStack, null);
        return itemStack;
    }

    public static void addObjectFields(IServerUtils utils, TooltipBuilder tooltip, Object object, Class<?> baseClass) {
        if (object.getClass().isEnum()) {
            return;
        }

        List<Field> fields = TooltipUtils.getAllFields(object.getClass(), baseClass);
        List<Field> names = fields.stream().filter((f) -> !Modifier.isStatic(f.getModifiers())).toList();

        names.forEach((f) -> {
            if (f.isSynthetic()) {
                return;
            }

            try {
                f.setAccessible(true);

                Object obj = f.get(object);
                String key = f.getName();
                TooltipBuilder builder = utils.getValueTooltip(utils, obj);

                if (builder.hasKey()) {
                    tooltip.add(TooltipBuilder.array((b) -> b.add(builder).rawKey(key)));
                } else {
                    tooltip.add(builder.rawKey(key));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static TooltipBuilder getJsonTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e).rawKey(key))));
        }

        return getElementTooltip(utils, element);
    }

    @NotNull
    public static TooltipBuilder getLootTableTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALL);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(LootTableReference entry, float chance, int sumWeight) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(getQualityTooltip(entry.quality));
            b.add(getChanceTooltip(NumberExpr.constant(chance * entry.weight / sumWeight)));
        });
    }

    @NotNull
    public static TooltipBuilder getLootPoolTooltip(NumberExpr rolls) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.RANDOM));
            b.add(getRolls(rolls));
        });
    }

    @NotNull
    public static TooltipBuilder getAlternativesTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALTERNATIVES);
    }

    @NotNull
    public static TooltipBuilder getDynamicTooltip(IServerUtils utils, int quality, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.DYNAMIC));
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(NumberExpr.constant(chance)));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getGroupTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALL);
    }

    @NotNull
    public static TooltipBuilder getSequentialTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.SEQUENCE);
    }

    @NotNull
    public static TooltipBuilder getEmptyTooltip(IServerUtils utils, int quality, NumberExpr chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.EMPTY));
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(chance));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getTooltip(IServerUtils utils, int quality, NumberExpr chance, LootCount count, @Nullable NumberInterval countLimit,
                                            List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(chance));
            b.add(getCountTooltip(count, countLimit));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getWeightTooltip(int weight) {
        if (weight != LootPoolSingletonContainer.DEFAULT_WEIGHT) {
            return TooltipBuilder.value(weight).key(Lang.Value.WEIGHT);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getQualityTooltip(int quality) {
        if (quality != LootPoolSingletonContainer.DEFAULT_QUALITY) {
            return TooltipBuilder.value(quality).key(Lang.Description.QUALITY);
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    public static TooltipBuilder getChanceTooltip(NumberExpr chance) {
        if (chance instanceof NumberExpr.Const c && c.value() > 0.9999999) {
            return TooltipBuilder.empty();
        }

        return TooltipBuilder.percent(chance).key(Lang.Description.CHANCE);
    }

    @NotNull
    public static TooltipBuilder getCountTooltip(LootCount count, @Nullable NumberInterval limit) {
        TooltipBuilder builder = TooltipBuilder.number(count.value(), false, limit);

        count.conditions().forEach(builder::add);
        return builder.key(Lang.Description.COUNT);
    }

    @NotNull
    public static TooltipBuilder getRolls(NumberExpr rolls) {
        return TooltipBuilder.number(rolls).key(Lang.Description.ROLLS);
    }

    private static TooltipBuilder getElementTooltip(IServerUtils utils, JsonElement element) {
        if (element.isJsonObject()) {
            return TooltipBuilder.array((b) -> element.getAsJsonObject().asMap().forEach((key, e) -> b.add(getElementTooltip(utils, e)).rawKey(key)));
        } else if (element.isJsonArray()) {
            return TooltipBuilder.array((b) -> element.getAsJsonArray().forEach((e) -> b.add(getElementTooltip(utils, e))));
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();

            if (jsonPrimitive.isBoolean()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsBoolean());
            } else if (jsonPrimitive.isString()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsString());
            } else if (jsonPrimitive.isNumber()) {
                return utils.getValueTooltip(utils, jsonPrimitive.getAsNumber());
            }
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    private static List<Field> getAllFields(Class<?> clazz, Class<?> baseClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != baseClass && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();

            Collections.addAll(fields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }
}
