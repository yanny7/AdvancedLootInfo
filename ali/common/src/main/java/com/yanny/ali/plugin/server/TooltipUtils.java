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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;


import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;


import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class TooltipUtils {
    public static final ResourceLocation LUCK = Utils.modLoc("luck");
    public static final ResourceLocation STORAGE = Utils.modLoc("storage");
    public static final ResourceLocation ENCHANTMENT_LEVEL = Utils.modLoc("enchantment_level");

    public static ItemStack getItemStack(IServerUtils utils, ItemStack itemStack, List<LootItemFunction> functions) {
        for (LootItemFunction function : functions) {
            itemStack = utils.applyItemStackModifier(utils, function, itemStack);
        }

        return itemStack;
    }

    @NotNull
    public static NumberExpr applyRandomChance(IServerUtils utils, LootItemRandomChanceCondition condition, NumberExpr chance, List<TooltipNode> conditions) {
        return NumberExpr.mul(chance, utils.convertNumber(utils, condition.chance(), conditions));
    }

    @NotNull
    public static NumberExpr applyRandomChanceWithLooting(IServerUtils utils, LootItemRandomChanceWithEnchantedBonusCondition condition, NumberExpr chance, List<TooltipNode> ignoredConditions) {
        return NumberExpr.mul(chance, getEnchantedBonusChance(utils, condition));
    }

    @NotNull
    public static NumberExpr getEnchantedBonusChance(IServerUtils utils, LootItemRandomChanceWithEnchantedBonusCondition condition) {
        NumberExpr level = level(condition.enchantment());
        NumberExpr enchanted = utils.convertLevelBasedValue(utils, condition.enchantedChance(), level);

        return NumberExpr.lookup(level, List.of(NumberExpr.constant(condition.unenchantedChance())), enchanted);
    }

    @NotNull
    public static NumberExpr applyTableBonus(IServerUtils ignoredUtils, BonusLevelTableCondition condition, NumberExpr chance, List<TooltipNode> ignoredConditions) {
        if (condition.values().isEmpty()) {
            return chance;
        }

        List<NumberExpr> values = new ArrayList<>();

        for (float value : condition.values()) {
            values.add(NumberExpr.constant(value));
        }

        return NumberExpr.mul(chance, NumberExpr.lookup(level(condition.enchantment()), values, null));
    }

    @NotNull
    public static NumberExpr applySetCount(IServerUtils utils, SetItemCountFunction function, NumberExpr count, List<TooltipNode> conditions) {
        NumberExpr value = utils.convertIntNumber(utils, function.value, conditions);

        return NumberExpr.max(NumberExpr.constant(0), function.add ? NumberExpr.add(count, value) : value);
    }

    @NotNull
    public static NumberExpr applyBonus(IServerUtils ignoredUtils, ApplyBonusCount function, NumberExpr count, List<TooltipNode> ignoredConditions) {
        NumberExpr level = level(function.enchantment);

        if (function.formula instanceof ApplyBonusCount.OreDrops) {
            return NumberExpr.mul(count, NumberExpr.max(NumberExpr.constant(1), NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.add(level, NumberExpr.constant(1)))));
        } else if (function.formula instanceof ApplyBonusCount.BinomialWithBonusCount formula) {
            return NumberExpr.add(count, NumberExpr.binomial(NumberExpr.add(level, NumberExpr.constant(formula.extraRounds())), NumberExpr.constant(formula.probability())));
        } else if (function.formula instanceof ApplyBonusCount.UniformBonusCount formula) {
            return NumberExpr.add(count, NumberExpr.uniformInt(NumberExpr.constant(0), NumberExpr.mul(NumberExpr.constant(formula.bonusMultiplier()), level)));
        }

        return count;
    }

    @NotNull
    public static NumberExpr applyLimitCount(IServerUtils utils, LimitCount function, NumberExpr count, List<TooltipNode> conditions) {
        return limit(utils, count, function.limiter, conditions);
    }

    @NotNull
    public static NumberExpr applyLootingEnchant(IServerUtils utils, EnchantedCountIncreaseFunction function, NumberExpr count, List<TooltipNode> conditions) {
        NumberExpr bonus = NumberExpr.fn(NumberFunctions.ROUND, NumberExpr.mul(level(function.enchantment), utils.convertNumber(utils, function.value, conditions)));
        NumberExpr result = NumberExpr.add(count, bonus);

        return function.limit > 0 ? NumberExpr.min(result, NumberExpr.constant(function.limit)) : result;
    }

    @NotNull
    public static NumberExpr limit(IServerUtils utils, NumberExpr value, IntRange range, List<TooltipNode> conditions) {
        NumberExpr min = range.min != null ? utils.convertIntNumber(utils, range.min, conditions) : null;
        NumberExpr max = range.max != null ? utils.convertIntNumber(utils, range.max, conditions) : null;

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
    public static NumberExpr anyEnchantmentLevel(IServerUtils utils) {
        int maxLevel = utils.getServerLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .mapToInt((holder) -> holder.value().getMaxLevel())
                .max()
                .orElse(1);

        return new NumberExpr.Var(ENCHANTMENT_LEVEL, List.of(), 1, maxLevel);
    }

    @NotNull
    public static NumberExpr level(Holder<Enchantment> enchantment) {
        String id = enchantment.unwrapKey().map((key) -> key.location().toString()).orElse("?");

        return NumberExpr.level(id, enchantment.value().getMaxLevel());
    }

    @NotNull
    public static NumberExpr conditional(IServerUtils utils, NumberExpr original, NumberExpr modified, List<LootItemCondition> predicates, List<TooltipNode> conditions) {
        List<LootItemCondition> leaves = predicates.stream().flatMap((c) -> utils.unwrapCondition(utils, c).stream()).toList();

        if (leaves.stream().allMatch((c) -> c instanceof LootItemRandomChanceCondition(ConstantValue ignored))) {
            double probability = 1;

            for (LootItemCondition leaf : leaves) {
                probability *= ((ConstantValue) ((LootItemRandomChanceCondition) leaf).chance()).value();
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
        return !function.predicates.isEmpty();
    }

    @NotNull
    public static NumberExpr rolls(IServerUtils utils, NumberProvider rolls, NumberProvider bonusRolls, List<TooltipNode> conditions) {
        return luckBased(utils.convertIntNumber(utils, rolls, conditions), utils.convertNumber(utils, bonusRolls, conditions));
    }

    @NotNull
    public static TooltipBuilder getNumberTooltip(IServerUtils utils, NumberProvider provider) {
        List<TooltipNode> conditions = new ArrayList<>();

        return TooltipBuilder.number(utils.convertNumber(utils, provider, conditions), conditions);
    }

    @NotNull
    public static TooltipBuilder getIntNumberTooltip(IServerUtils utils, NumberProvider provider) {
        List<TooltipNode> conditions = new ArrayList<>();

        return TooltipBuilder.number(utils.convertIntNumber(utils, provider, conditions), conditions);
    }

    @NotNull
    public static NumberExpr luckBased(NumberExpr base, NumberExpr bonus) {
        if (bonus instanceof NumberExpr.Const c && c.value() == 0) {
            return base;
        }

        return NumberExpr.max(NumberExpr.constant(0), NumberExpr.add(base, NumberExpr.fn(NumberFunctions.FLOOR, NumberExpr.mul(bonus, luck()))));
    }

    @NotNull
    public static NumberExpr.Var luck() {
        return new NumberExpr.Var(LUCK, List.of(), -1, 4);
    }

    @NotNull
    public static List<LootItemCondition> unwrapAllOf(IServerUtils ignoredUtils, AllOfCondition condition) {
        return condition.terms;
    }

    @Unmodifiable
    @Nullable
    public static List<LootItemCondition> unwrapConditionReference(IServerUtils utils, ConditionReference condition) {
        return utils.getServerLevel().getServer().reloadableRegistries().lookup().get(Registries.PREDICATE, condition.name())
                .map((holder) -> Collections.singletonList(holder.value()))
                .orElse(null);
    }

    @NotNull
    public static List<LootItemFunction> unwrapFunctionSequence(IServerUtils ignoredUtils, SequenceFunction function) {
        return function.functions;
    }

    @Unmodifiable
    @Nullable
    public static List<LootItemFunction> unwrapFunctionReference(IServerUtils utils, FunctionReference function) {
        if (isConditional(function)) {
            return null;
        }

        return utils.getServerLevel().getServer().reloadableRegistries().lookup().get(Registries.ITEM_MODIFIER, function.name)
                .map((holder) -> Collections.singletonList(holder.value()))
                .orElse(null);
    }

    @NotNull
    public static ItemStack applyEnchantRandomlyItemStackModifier(IServerUtils utils, EnchantRandomlyFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            boolean isBook = itemStack.is(Items.BOOK);
            boolean compatible = !isBook && function.onlyCompatible;
            ItemStack finalItemStack = itemStack;
            Optional<HolderSet<Enchantment>> enchantments = function.options;

            if (enchantments.isEmpty()) {
                List<Holder<Enchantment>> list = function.options.map(HolderSet::stream).orElseGet(() -> utils.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT)
                        .listElements().map(Function.identity())).filter((ref) -> compatible || ref.value().canEnchant(finalItemStack)).toList();

                if (list.size() == 1) {
                    enchantments = Optional.of(HolderSet.direct(list.getFirst()));
                }
            }

            if (enchantments.isPresent() && enchantments.get().size() == 1 && enchantments.get().get(0).value().getMinLevel() == enchantments.get().get(0).value().getMaxLevel()) {
                itemStack.enchant(enchantments.get().get(0), enchantments.get().get(0).value().getMaxLevel());
            } else if (isBook) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applyEnchantWithLevelsItemStackModifier(IServerUtils ignoredUtils, EnchantWithLevelsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    public static ItemStack applySetAttributesItemStackModifier(IServerUtils ignoredUtils, SetAttributesFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty()) {
            if (function.replace) {
                itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, updateModifiers(function.modifiers, ItemAttributeModifiers.EMPTY));
            } else {
                itemStack.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, (modifiers) -> {
                    if (modifiers.modifiers().isEmpty()) {
                        return updateModifiers(function.modifiers, itemStack.getItem().getDefaultAttributeModifiers());
                    } else {
                        return updateModifiers(function.modifiers, modifiers);
                    }
                });
            }
        }

        return itemStack;
    }

    public static ItemStack applySetNameItemStackModifier(IServerUtils ignoredUtils, SetNameFunction function, ItemStack itemStack) {
        if (function.predicates.isEmpty() && function.name.isPresent()) {
            itemStack.set(function.target.component(), function.name.get());
        }

        return itemStack;
    }

    @NotNull
    public static ItemStack applySetEnchantmentsItemStackModifier(IServerUtils ignoredUtils, SetEnchantmentsFunction function, ItemStack itemStack) {
        if (itemStack.isEnchantable() && function.predicates.isEmpty()) {
            if (itemStack.is(Items.BOOK)) {
                itemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            } else {
                itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        return itemStack;
    }

    public static ItemStack applyItemStackModifier(IServerUtils ignoredUtils, LootItemFunction function, ItemStack itemStack) {
        if (function instanceof LootItemConditionalFunction conditional && !conditional.predicates.isEmpty()) {
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
    public static <T, U extends Comparable<? super U>> Comparator<Holder<T>> comparingHolder(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<Holder<T>> & Serializable) (h1, h2) -> keyExtractor.apply(h1.value()).compareTo(keyExtractor.apply(h2.value()));
    }

    @NotNull
    public static TooltipBuilder getLootTableTooltip() {
        return TooltipBuilder.keyOnly(Lang.Group.ALL);
    }

    @NotNull
    public static TooltipBuilder getReferenceTooltip(NestedLootTable entry, LootCount chance) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(getQualityTooltip(entry.quality));
            b.add(getChanceTooltip(chance));
        });
    }

    @NotNull
    public static TooltipBuilder getLootPoolTooltip(LootCount rolls) {
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
    public static TooltipBuilder getDynamicTooltip(IServerUtils utils, int quality, LootCount chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.DYNAMIC));
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(chance));
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
    public static TooltipBuilder getEmptyTooltip(IServerUtils utils, int quality, LootCount chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.EMPTY));
            b.add(getQualityTooltip(quality));
            b.add(getChanceTooltip(chance));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
            b.add(GenericTooltipUtils.getFunctionsSectionTooltip(utils, functions));
        });
    }

    @NotNull
    public static TooltipBuilder getTooltip(IServerUtils utils, int quality, LootCount chance, LootCount count, @Nullable NumberInterval countLimit,
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
    public static TooltipBuilder getChanceTooltip(LootCount chance) {
        if (chance.value() instanceof NumberExpr.Const c && c.value() > 0.9999999) {
            return TooltipBuilder.empty();
        }

        TooltipBuilder builder = TooltipBuilder.percent(chance.value());

        chance.conditions().forEach(builder::add);
        return builder.key(Lang.Description.CHANCE);
    }

    @NotNull
    public static TooltipBuilder getCountTooltip(LootCount count, @Nullable NumberInterval limit) {
        TooltipBuilder builder = TooltipBuilder.number(count.value(), false, limit);

        count.conditions().forEach(builder::add);
        return builder.key(Lang.Description.COUNT);
    }

    @NotNull
    public static TooltipBuilder getRolls(LootCount rolls) {
        return TooltipBuilder.number(rolls.value(), rolls.conditions()).key(Lang.Description.ROLLS);
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

    private static ItemAttributeModifiers updateModifiers(List<SetAttributesFunction.Modifier> modifiers, ItemAttributeModifiers itemAttributeModifiers) {
        for (SetAttributesFunction.Modifier modifier : modifiers) {
            ResourceLocation id = modifier.id();

            if (modifier.slots().size() == 1 && modifier.amount().getType() == NumberProviders.CONSTANT) {
                EquipmentSlotGroup equipmentSlot = Util.getRandom(modifier.slots(), RandomSource.create());
                ConstantValue value = (ConstantValue) modifier.amount();

                itemAttributeModifiers = itemAttributeModifiers.withModifierAdded(modifier.attribute(), new AttributeModifier(id, value.getFloat(null), modifier.operation()), equipmentSlot);
            }
        }

        return itemAttributeModifiers;
    }
}
