package com.yanny.ali.plugin.common;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.number.NumberEvaluator;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NodeUtils {
    @NotNull
    public static IDataNode getItemNode(IServerUtils utils, LootItem entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getItemNode(utils, entry, (f) -> Either.left(TooltipUtils.getItemStack(utils, entry.item.getDefaultInstance(), f)), rawChance, sumWeight, chanceConditions, functions, conditions);
    }

    @NotNull
    public static IDataNode getTagNode(IServerUtils utils, TagEntry entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getItemNode(utils, entry, (f) -> Either.right(entry.tag), rawChance, sumWeight, chanceConditions, functions, conditions);
    }

    @NotNull
    public static IDataNode getItemNode(IServerUtils utils, LootPoolSingletonContainer entry, Function<List<LootItemFunction>, Either<ItemStack, TagKey<? extends ItemLike>>> itemGetter,
                                        NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        LootCount chance = getChance(utils, entry, rawChance, sumWeight, chanceConditions);
        Either<ItemStack, TagKey<? extends ItemLike>> either = itemGetter.apply(allFunctions);
        LootCount count = getCount(utils, allFunctions);
        TooltipNode tooltip = TooltipUtils.getTooltip(utils, entry.quality, getChance(utils, allConditions, chance), count, getCountLimit(either), allFunctions, allConditions).build();

        if (either.left().isPresent() && either.left().get().isEmpty()) {
            return new EmptyNode(toFloat(chance.value()), tooltip);
        } else {
            return new ItemNode(toFloat(chance.value()), count.value(), either, tooltip, allFunctions, allConditions);
        }
    }

    @NotNull
    public static AlternativesNode getAlternativesNode(IServerUtils utils, AlternativesEntry entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, chanceConditions, functions, allConditions);
        TooltipNode tooltip = TooltipUtils.getAlternativesTooltip().build();

        return new AlternativesNode(children, tooltip);
    }

    @NotNull
    public static DynamicNode getDynamicNode(IServerUtils utils, DynamicLoot entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        LootCount chance = getChance(utils, entry, rawChance, sumWeight, chanceConditions);
        TooltipNode tooltip = TooltipUtils.getDynamicTooltip(utils, entry.quality, chance, allFunctions, allConditions).build();

        return new DynamicNode(toFloat(chance.value()), tooltip);
    }

    @NotNull
    public static EmptyNode getEmptyNode(IServerUtils utils, EmptyLootItem entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        LootCount chance = getChance(utils, entry, rawChance, sumWeight, chanceConditions);
        TooltipNode tooltip = TooltipUtils.getEmptyTooltip(utils, entry.quality, getChance(utils, allConditions, chance), allFunctions, allConditions).build();

        return new EmptyNode(toFloat(chance.value()), tooltip);
    }

    @NotNull
    public static GroupNode getGroupNode(IServerUtils utils, EntryGroup entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, chanceConditions, functions, allConditions);
        TooltipNode tooltip = TooltipUtils.getGroupTooltip().build();

        return new GroupNode(children, tooltip);
    }

    @NotNull
    public static SequenceNode getSequenceNode(IServerUtils utils, SequentialEntry entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, chanceConditions, functions, allConditions);
        TooltipNode tooltip = TooltipUtils.getSequentialTooltip().build();

        return new SequenceNode(children, tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, LootTableReference entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        LootCount chance = getChance(utils, entry, rawChance, sumWeight, chanceConditions);
        LootTable lootTable = utils.getLootTable(entry.name);
        TooltipNode tooltip = TooltipUtils.getReferenceTooltip(entry, chance).build();
        List<IDataNode> children;

        if (lootTable != null) {
            children = Collections.singletonList(getLootTableNode(Collections.emptyList(), utils, lootTable, chance.value(), chance.conditions(), allFunctions, allConditions));
        } else {
            children = Collections.singletonList(new MissingNode(utils.getValueTooltip(utils, entry.name).build(Lang.Value.LOOT_TABLE)));
        }

        return new ReferenceNode(children, toFloat(chance.value()), tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, ResourceLocation table, List<LootItemCondition> conditions, TooltipNode tooltip) {
        LootTable lootTable = utils.getLootTable(table);
        List<IDataNode> children;

        if (lootTable != null) {
            children = Collections.singletonList(getLootTableNode(Collections.emptyList(), utils, lootTable, 1, Collections.emptyList(), conditions));
        } else {
            children = Collections.singletonList(new MissingNode(utils.getValueTooltip(utils, table).build(Lang.Value.LOOT_TABLE)));
        }

        return new ReferenceNode(children, 1, tooltip);
    }

    @NotNull
    public static LootPoolNode getLootPoolNode(IServerUtils utils, LootPool entry, NumberExpr rawChance, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();
        List<TooltipNode> poolConditions = new ArrayList<>(chanceConditions);
        NumberExpr sumWeight = getTotalWeight(utils, Arrays.asList(entry.entries), poolConditions);
        List<TooltipNode> rollConditions = new ArrayList<>();
        NumberExpr rolls = TooltipUtils.rolls(utils, entry.rolls, entry.bonusRolls, rollConditions);
        TooltipNode tooltip = TooltipUtils.getLootPoolTooltip(new LootCount(rolls, rollConditions)).build();
        List<IDataNode> children = getChildren(utils, entry.entries, rawChance, sumWeight, poolConditions, allFunctions, allConditions);

        return new LootPoolNode(children, tooltip);
    }

    @NotNull
    public static LootTableNode getLootTableNode(List<IOperation> operations) {
        TooltipNode tooltip = TooltipUtils.getLootTableTooltip().build();
        List<IDataNode> children = new ArrayList<>();
        LootTableNode node = new LootTableNode(children, tooltip);

        processOperations(operations, node);
        return node;
    }

    @NotNull
    public static LootTableNode getLootTableNode(List<IOperation> operations, IServerUtils utils, LootTable entry, float rawChance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getLootTableNode(operations, utils, entry, NumberExpr.constant(rawChance), List.of(), functions, conditions);
    }

    @NotNull
    public static LootTableNode getLootTableNode(List<IOperation> operations, IServerUtils utils, LootTable entry, NumberExpr rawChance, List<TooltipNode> chanceConditions,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();
        TooltipNode tooltip = TooltipUtils.getLootTableTooltip().build();
        List<IDataNode> children = utils.getLootPools(entry).stream().map((lootPool) -> (IDataNode) getLootPoolNode(utils, lootPool, rawChance, chanceConditions, allFunctions, conditions)).toList();
        LootTableNode node = new LootTableNode(children, tooltip);

        processOperations(operations, node);
        return node;
    }

    @NotNull
    public static LootCount getChance(IServerUtils utils, LootPoolEntryContainer entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions) {
        List<TooltipNode> conditions = new ArrayList<>(chanceConditions);
        NumberExpr weight = utils.getEntryWeight(utils, entry, conditions);

        if (sumWeight instanceof NumberExpr.Const c && c.value() == 0) {
            return new LootCount(NumberExpr.constant(0), conditions);
        }

        return new LootCount(NumberExpr.mul(rawChance, NumberExpr.div(weight, sumWeight)), conditions);
    }

    public static float toFloat(NumberExpr chance) {
        NumberExpr value = chance.bind(TooltipUtils.luck(), 0);

        if (value instanceof NumberExpr.Const c) {
            return (float) c.value();
        }

        NumberInterval bounds = NumberEvaluator.bounds(value);

        return bounds.isBounded() ? (float) ((bounds.lo() + bounds.hi()) / 2) : 0;
    }

    @NotNull
    public static NumberExpr getSingletonWeight(IServerUtils ignoredUtils, LootPoolSingletonContainer entry, List<TooltipNode> ignoredConditions) {
        return TooltipUtils.luckBased(NumberExpr.constant(entry.weight), NumberExpr.constant(entry.quality));
    }

    @NotNull
    public static NumberExpr getCompositeWeight(IServerUtils utils, CompositeEntryBase entry, List<TooltipNode> conditions) {
        return getTotalWeight(utils, Arrays.asList(entry.children), conditions);
    }

    @NotNull
    public static NumberExpr getAlternativesWeight(IServerUtils ignoredUtils, AlternativesEntry ignoredEntry, List<TooltipNode> ignoredConditions) {
        return NumberExpr.constant(LootPoolSingletonContainer.DEFAULT_WEIGHT);
    }

    @NotNull
    public static List<LootPoolEntryContainer> getCompositeChildren(IServerUtils ignoredUtils, CompositeEntryBase entry) {
        return List.of(entry.children);
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemCondition> getAllConditions(LootPoolEntryContainer entry, List<LootItemCondition> conditions) {
        return Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemFunction> getAllFunctions(LootPoolSingletonContainer entry, List<LootItemFunction> functions) {
        return Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();
    }

    @NotNull
    public static List<IDataNode> getChildren(IServerUtils utils, LootPoolEntryContainer[] children, NumberExpr chance, NumberExpr sumWeight, List<TooltipNode> chanceConditions,
                                              List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<TooltipNode> shared = List.copyOf(chanceConditions);

        return Arrays.stream(children).map((c) -> utils.getEntryFactory(utils, c).create(utils, c, chance, sumWeight, shared, functions, conditions)).toList();
    }

    /** Condition types listed in {@code ignoredPredicateConditions} don't count as predicates. */
    public static boolean hasPredicates(IServerUtils utils, List<LootItemCondition> conditions) {
        List<ResourceLocation> ignored = utils.getConfiguration().ignoredPredicateConditions;

        return conditions.stream()
                .flatMap((c) -> utils.unwrapCondition(utils, c).stream())
                .anyMatch((c) -> !ignored.contains(BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(c.getType())));
    }

    @NotNull
    public static LootCount getChance(IServerUtils utils, List<LootItemCondition> conditions, float rawChance) {
        return getChance(utils, conditions, LootCount.of(NumberExpr.constant(rawChance)));
    }

    @NotNull
    public static LootCount getChance(IServerUtils utils, List<LootItemCondition> conditions, LootCount rawChance) {
        List<TooltipNode> chanceConditions = new ArrayList<>(rawChance.conditions());
        NumberExpr chance = NumberExpr.constant(1);

        for (LootItemCondition condition : conditions) {
            chance = utils.applyChanceModifier(utils, condition, chance, chanceConditions);
        }

        return new LootCount(NumberExpr.mul(rawChance.value(), chance), chanceConditions);
    }

    @NotNull
    public static LootCount getCount(IServerUtils utils, List<LootItemFunction> functions) {
        return getCount(utils, NumberExpr.constant(1), functions);
    }

    @NotNull
    public static LootCount getCount(IServerUtils utils, NumberExpr baseCount, List<LootItemFunction> functions) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr count = baseCount;

        for (LootItemFunction function : functions) {
            count = utils.applyCountModifier(utils, function, count, conditions);
        }

        return new LootCount(count, conditions);
    }

    @Nullable
    public static NumberInterval getCountLimit(ItemStack item) {
        return item.isEmpty() ? null : NumberInterval.closed(0, item.getMaxStackSize());
    }

    @Nullable
    public static NumberInterval getCountLimit(Either<ItemStack, TagKey<? extends ItemLike>> item) {
        int maxStackSize = resolveItems(item).stream().mapToInt(ItemStack::getMaxStackSize).max().orElse(0);

        return maxStackSize > 0 ? NumberInterval.closed(0, maxStackSize) : null;
    }

    @NotNull
    public static NumberExpr getTotalWeight(IServerUtils utils, List<LootPoolEntryContainer> entries, List<TooltipNode> conditions) {
        NumberExpr sum = NumberExpr.constant(0);

        for (LootPoolEntryContainer entry : entries) {
            sum = NumberExpr.add(sum, utils.getEntryWeight(utils, entry, conditions));
        }

        return sum;
    }

    public static void processOperations(List<IOperation> operations, LootTableNode node) {
        for (IOperation operation : operations) {
            if (operation instanceof IOperation.AddOperation addOperation) {
                node.addChildren(addOperation.node());
            } else if (operation instanceof IOperation.RemoveOperation removeOperation) {
                removeItem(node, removeOperation.factory(), removeOperation.predicate());
            } else if (operation instanceof IOperation.ReplaceOperation replaceOperation) {
                replaceItem(node, replaceOperation.factory(), replaceOperation.predicate());
            }
        }
    }

    private static void removeItem(IDataNode node, Function<IDataNode, IDataNode> factory, Predicate<ItemStack> predicate) {
        if (node instanceof ListNode listNode) {
            var iterator = listNode.nodes().listIterator();

            while (iterator.hasNext()) {
                IDataNode n = iterator.next();

                if (n instanceof IItemNode itemNode && predicateEither(itemNode, predicate)) {
                    IDataNode replacement = factory.apply(n);

                    if (replacement == null) {
                        iterator.remove();
                    } else {
                        iterator.set(replacement);
                    }
                } else if (n instanceof ListNode) {
                    removeItem(n, factory, predicate);
                }
            }

            removeEmptyNodes(node);
        }
    }

    private static void replaceItem(IDataNode node, Function<IDataNode, List<IDataNode>> factory, Predicate<ItemStack> predicate) {
        if (node instanceof ListNode listNode) {
            List<IDataNode> nodes = new ArrayList<>();

            listNode.nodes().replaceAll((n) -> {
                if (n instanceof IItemNode itemNode && predicateEither(itemNode, predicate)) {
                    List<IDataNode> result = factory.apply(n);

                    if (result.size() > 1) {
                        nodes.addAll(result.subList(1, result.size()));
                    }

                    return result.get(0);
                } else if (n instanceof ListNode) {
                    replaceItem(n, factory, predicate);
                }

                return n;
            });

            nodes.forEach(listNode::addChildren);
        }
    }

    private static boolean hasItems(IDataNode node) {
        if (node instanceof ListNode listNode) {
            return listNode.nodes().stream().anyMatch(NodeUtils::hasItems);
        } else {
            return node instanceof IItemNode;
        }
    }

    private static void removeEmptyNodes(IDataNode node) {
        if (node instanceof ListNode listNode) {
            listNode.nodes().removeIf((n) -> !hasItems(n));
        }
    }

    /**
     * The stacks an {@link IItemNode} stands for, resolved against the current registry - a tag becomes its members,
     * an empty stack becomes nothing. The returned list is mutable, so that {@link IItemNode#retainItems} can narrow
     * it in place.
     */
    @NotNull
    public static List<ItemStack> resolveItems(Either<ItemStack, TagKey<? extends ItemLike>> item) {
        return new ArrayList<>(item.map(
                (stack) -> stack.isEmpty() ? Collections.emptyList() : List.of(stack),
                NodeUtils::toItemStacks
        ));
    }

    @NotNull
    private static <T extends ItemLike> List<ItemStack> toItemStacks(TagKey<? extends ItemLike> tag) {
        //noinspection unchecked
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(tag.registry().location());

        if (registry != null) {
            //noinspection unchecked
            return registry
                    .getTag((TagKey<T>) tag)
                    .map((holders) -> holders.stream().map(Holder::value).map((i) -> i.asItem().getDefaultInstance()).toList())
                    .orElse(Collections.emptyList());
        } else {
            return Collections.emptyList();
        }
    }

    private static <T extends ItemLike> boolean predicateEither(IItemNode itemNode, Predicate<ItemStack> predicate) {
        return itemNode.getItem().map(
                predicate::test,
                (tagKey) -> {
                    //noinspection unchecked
                    Registry<T> registry = (Registry<T>)BuiltInRegistries.REGISTRY.get(tagKey.registry().location());

                    if (registry != null) {
                        //noinspection unchecked
                        List<ItemStack> stacks = registry
                                .getTag((TagKey<T>) tagKey)
                                .map((holders) -> holders.stream().map(Holder::value).map((i) -> i.asItem().getDefaultInstance()).toList())
                                .orElse(List.of());

                        return !stacks.isEmpty() && stacks.stream().allMatch(predicate);
                    } else {
                        return false;
                    }
                });
    }
}
