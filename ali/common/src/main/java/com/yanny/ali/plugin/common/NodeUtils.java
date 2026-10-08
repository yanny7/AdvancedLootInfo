package com.yanny.ali.plugin.common;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.aci.number.NumberEvaluator;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NodeUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final Set<String> FAILED_OPERATIONS = new HashSet<>();
    private static final NumberInterval NON_NEGATIVE = new NumberInterval(0, Double.POSITIVE_INFINITY, true, false);

    @NotNull
    public static IDataNode getItemNode(IServerUtils utils, LootItem entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getItemNode(utils, entry, (f) -> Either.left(TooltipUtils.getItemStack(utils, entry.item.value().getDefaultInstance(), f)), rawChance, sumWeight, chanceConditions, functions, conditions);
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
        NumberExpr weight = utils.getEntryWeight(utils, entry, new ArrayList<>());
        List<IDataNode> children = entry.children.stream().map((c) -> {
            List<TooltipNode> childConditions = new ArrayList<>(chanceConditions);
            NumberExpr childSum = replaceWeight(sumWeight, weight, utils.getEntryWeight(utils, c, childConditions));

            return utils.getEntryFactory(utils, c).create(utils, c, rawChance, childSum, List.copyOf(childConditions), functions, allConditions);
        }).toList();
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
    public static ReferenceNode getReferenceNode(IServerUtils utils, NestedLootTable entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        LootCount chance = getChance(utils, entry, rawChance, sumWeight, chanceConditions);
        LootTable lootTable = utils.getLootTable(entry.contents.mapLeft(ResourceKey::location));
        TooltipNode tooltip = TooltipUtils.getReferenceTooltip(entry, chance).build();
        List<IDataNode> children;

        if (lootTable != null) {
            children = Collections.singletonList(getLootTableNode(Collections.emptyList(), utils, lootTable, chance.value(), chance.conditions(), allFunctions, allConditions));
        } else {
            children = Collections.singletonList(new MissingNode(utils.getValueTooltip(utils, entry.contents).build(Lang.Value.LOOT_TABLE)));
        }

        return new ReferenceNode(children, toFloat(chance.value()), tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, ResourceLocation table, List<LootItemCondition> conditions, TooltipNode tooltip) {
        LootTable lootTable = utils.getLootTable(Either.left(table));
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
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();
        List<TooltipNode> poolConditions = new ArrayList<>(chanceConditions);
        NumberExpr sumWeight = getTotalWeight(utils, entry.entries, poolConditions);
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
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
        TooltipNode tooltip = TooltipUtils.getLootTableTooltip().build();
        List<IDataNode> children = entry.pools.stream().map((lootPool) -> (IDataNode) getLootPoolNode(utils, lootPool, rawChance, chanceConditions, allFunctions, conditions)).toList();
        LootTableNode node = new LootTableNode(children, tooltip);

        processOperations(operations, node);
        return node;
    }

    @NotNull
    public static LootCount getChance(IServerUtils utils, LootPoolEntryContainer entry, NumberExpr rawChance, NumberExpr sumWeight, List<TooltipNode> chanceConditions) {
        List<TooltipNode> conditions = new ArrayList<>(chanceConditions);
        List<TooltipNode> weightConditions = new ArrayList<>();
        NumberExpr weight = utils.getEntryWeight(utils, entry, weightConditions);
        NumberExpr found = findWeight(sumWeight, weight);

        if (found != null) {
            weight = found;
        } else {
            weight = weight.shiftConditions(conditions.size());
            conditions.addAll(weightConditions);
        }

        if (sumWeight instanceof NumberExpr.Const c && c.value() == 0) {
            return new LootCount(NumberExpr.constant(0), List.of());
        }

        return withUsedConditions(liftCondition(NumberExpr.mul(rawChance, NumberExpr.div(weight, sumWeight))), conditions);
    }

    @NotNull
    private static NumberExpr liftCondition(NumberExpr expr) {
        if (expr instanceof NumberExpr.Cond) {
            return expr;
        }

        NumberExpr.Cond[] found = {null};

        expr.transform((e) -> {
            if (found[0] == null && e instanceof NumberExpr.Cond c) {
                found[0] = c;
            }
            return e;
        });

        if (found[0] == null) {
            return expr;
        }

        NumberExpr.Cond cond = found[0];
        List<NumberExpr.Branch> branches = cond.branches().stream().map((b) -> new NumberExpr.Branch(b.condition(), substitute(expr, cond, b.value()))).toList();

        return NumberExpr.cond(branches, substitute(expr, cond, cond.otherwise()));
    }

    @NotNull
    private static NumberExpr substitute(NumberExpr expr, NumberExpr target, NumberExpr replacement) {
        return expr.transform((e) -> e.equals(target) ? replacement : e);
    }

    @NotNull
    private static LootCount withUsedConditions(NumberExpr value, List<TooltipNode> conditions) {
        if (!(value instanceof NumberExpr.Cond cond)) {
            return new LootCount(value.transform(NodeUtils::dropConditions), List.of());
        }

        List<TooltipNode> used = new ArrayList<>();
        List<NumberExpr.Branch> branches = new ArrayList<>();

        for (NumberExpr.Branch branch : cond.branches()) {
            int index = -1;

            if (branch.condition() >= 0) {
                index = used.size();
                used.add(conditions.get(branch.condition()));
            }

            branches.add(new NumberExpr.Branch(index, branch.value().transform(NodeUtils::dropConditions)));
        }

        return new LootCount(NumberExpr.cond(branches, cond.otherwise().transform(NodeUtils::dropConditions)), used);
    }

    @NotNull
    private static NumberExpr dropConditions(NumberExpr expr) {
        if (expr instanceof NumberExpr.Cond c) {
            return NumberExpr.cond(c.branches().stream().map((b) -> new NumberExpr.Branch(-1, b.value())).toList(), c.otherwise());
        }

        return expr;
    }

    @NotNull
    private static NumberExpr replaceWeight(NumberExpr sumWeight, NumberExpr weight, NumberExpr replacement) {
        if (weight.equals(replacement)) {
            return sumWeight;
        }

        NumberExpr found = findWeight(sumWeight, weight);

        if (found == null) {
            return NumberExpr.add(NumberExpr.sub(sumWeight, weight), replacement);
        }

        boolean[] replaced = {false};

        return sumWeight.transform((e) -> {
            if (!replaced[0] && e.equals(found)) {
                replaced[0] = true;
                return replacement;
            }
            return e;
        });
    }

    @Nullable
    private static NumberExpr findWeight(NumberExpr sumWeight, NumberExpr weight) {
        if (weight instanceof NumberExpr.Const) {
            return null;
        }

        int base = firstCondition(weight);
        NumberExpr[] found = {null};

        sumWeight.transform((e) -> {
            if (found[0] == null && e.getClass() == weight.getClass() && weight.shiftConditions(firstCondition(e) - base).equals(e)) {
                found[0] = e;
            }
            return e;
        });
        return found[0];
    }

    private static int firstCondition(NumberExpr expr) {
        int[] first = {-1};

        expr.transform((e) -> {
            if (first[0] < 0 && e instanceof NumberExpr.Cond c) {
                c.branches().stream().mapToInt(NumberExpr.Branch::condition).filter((i) -> i >= 0).findFirst().ifPresent((i) -> first[0] = i);
            }
            return e;
        });
        return first[0];
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
        return getTotalWeight(utils, entry.children, conditions);
    }

    @NotNull
    public static NumberExpr getAlternativesWeight(IServerUtils utils, AlternativesEntry entry, List<TooltipNode> conditions) {
        int reachable = 0;

        while (reachable < entry.children.size() && !entry.children.get(reachable).conditions.isEmpty()) {
            reachable++;
        }

        NumberExpr weight = NumberExpr.constant(0);

        if (reachable < entry.children.size()) {
            weight = utils.getEntryWeight(utils, entry.children.get(reachable), conditions);
        }

        for (int i = reachable - 1; i >= 0; i--) {
            LootPoolEntryContainer child = entry.children.get(i);
            NumberExpr childWeight = utils.getEntryWeight(utils, child, conditions);

            if (!childWeight.equals(weight)) {
                weight = TooltipUtils.conditional(utils, weight, childWeight, child.conditions, conditions);
            }
        }

        return weight;
    }

    @NotNull
    public static List<LootPoolEntryContainer> getCompositeChildren(IServerUtils ignoredUtils, CompositeEntryBase entry) {
        return entry.children;
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemCondition> getAllConditions(LootPoolEntryContainer entry, List<LootItemCondition> conditions) {
        return Stream.concat(conditions.stream(), entry.conditions.stream()).toList();
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemFunction> getAllFunctions(LootPoolSingletonContainer entry, List<LootItemFunction> functions) {
        return Stream.concat(functions.stream(), entry.functions.stream()).toList();
    }

    @Unmodifiable
    @NotNull
    public static List<IDataNode> getChildren(IServerUtils utils, List<LootPoolEntryContainer> children, NumberExpr chance, NumberExpr sumWeight, List<TooltipNode> chanceConditions,
                                              List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<TooltipNode> shared = List.copyOf(chanceConditions);

        return children.stream().map((c) -> utils.getEntryFactory(utils, c).create(utils, c, chance, sumWeight, shared, functions, conditions)).toList();
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
        return item.isEmpty() ? null : NON_NEGATIVE;
    }

    @Nullable
    public static NumberInterval getCountLimit(Either<ItemStack, TagKey<? extends ItemLike>> item) {
        return resolveItems(item).isEmpty() ? null : NON_NEGATIVE;
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
            } else if (operation instanceof IOperation.RemoveOperation(Predicate<ItemStack> predicate, Function<IDataNode, IDataNode> factory)) {
                removeItem(node, guarded(factory, factory, (n) -> n), guarded(factory, predicate));
            } else if (operation instanceof IOperation.ReplaceOperation(Predicate<ItemStack> predicate, Function<IDataNode, List<IDataNode>> factory)) {
                replaceItem(node, guarded(factory, factory, List::of), guarded(factory, predicate));
            }
        }
    }

    public static void clearFailedOperations() {
        FAILED_OPERATIONS.clear();
    }

    @NotNull
    private static Predicate<ItemStack> guarded(Object operation, Predicate<ItemStack> predicate) {
        return (stack) -> {
            try {
                return predicate.test(stack);
            } catch (Throwable e) {
                logFailedOperation(operation, e);
                return false;
            }
        };
    }

    @NotNull
    private static <R> Function<IDataNode, R> guarded(Object operation, Function<IDataNode, R> factory, Function<IDataNode, R> unchanged) {
        return (node) -> {
            try {
                return factory.apply(node);
            } catch (Throwable e) {
                logFailedOperation(operation, e);
                return unchanged.apply(node);
            }
        };
    }

    private static void logFailedOperation(Object operation, Throwable e) {
        String name = ManagedRegistry.classKeyName(operation.getClass());

        if (FAILED_OPERATIONS.add(name)) {
            LOGGER.warn("Failed to apply loot modifier operation {}, skipping it: {}", name, e.getMessage(), e);
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

                    return result.getFirst();
                } else if (n instanceof ListNode) {
                    replaceItem(n, factory, predicate);
                }

                return n;
            });

            nodes.forEach(listNode::addChildren);
        }
    }

    private static boolean hasContent(IDataNode node) {
        if (node instanceof ListNode listNode) {
            return listNode.nodes().stream().anyMatch(NodeUtils::hasContent);
        } else {
            return !(node instanceof EmptyNode);
        }
    }

    private static void removeEmptyNodes(IDataNode node) {
        if (node instanceof ListNode listNode) {
            listNode.nodes().removeIf((n) -> n instanceof ListNode && !hasContent(n));
        }
    }

    public static void replaceEmptyItems(IDataNode node) {
        if (node instanceof ListNode listNode) {
            listNode.nodes().replaceAll((n) -> {
                if (n instanceof IItemNode itemNode && itemNode.getItem().left().filter(ItemStack::isEmpty).isPresent()) {
                    return new EmptyNode(n.getChance(), n.getTooltip());
                }

                replaceEmptyItems(n);
                return n;
            });
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
