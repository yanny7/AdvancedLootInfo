package com.yanny.ali.plugin.common;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NodeUtils {
    @NotNull
    public static IDataNode getItemNode(IServerUtils utils, LootItem entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getItemNode(utils, entry, (f) -> Either.left(TooltipUtils.getItemStack(utils, entry.item.value().getDefaultInstance(), f)), rawChance, sumWeight, functions, conditions);
    }

    @NotNull
    public static IDataNode getTagNode(IServerUtils utils, TagEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        Optional<TagKey<Item>> tag = entry.tag.unwrapKey();

        if (tag.isPresent() || entry.tag.size() == 1) {
            return getItemNode(utils, entry, (f) -> getTagEntryItem(utils, entry.tag, f), rawChance, sumWeight, functions, conditions);
        }

        List<IDataNode> children = entry.tag.stream()
                .map((item) -> getItemNode(utils, entry, (f) -> Either.left(TooltipUtils.getItemStack(utils, item.value().getDefaultInstance(), f)), rawChance, sumWeight, functions, conditions))
                .toList();

        return new GroupNode(children, TooltipUtils.getGroupTooltip().build());
    }

    @NotNull
    private static Either<ItemStack, TagKey<? extends ItemLike>> getTagEntryItem(IServerUtils utils, HolderSet<Item> items, List<LootItemFunction> functions) {
        return items.unwrapKey()
                .<Either<ItemStack, TagKey<? extends ItemLike>>>map(Either::right)
                .orElseGet(() -> Either.left(TooltipUtils.getItemStack(utils, items.get(0).value().getDefaultInstance(), functions)));
    }

    @NotNull
    public static IDataNode getItemNode(IServerUtils utils, UniformContainerBase entry, Function<List<LootItemFunction>, Either<ItemStack, TagKey<? extends ItemLike>>> itemGetter, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        float chance = getChance(entry, rawChance, sumWeight);
        EnchantedRanges enchantedChance = getEnchantedChance(utils, allConditions, chance);
        EnchantedRanges enchantedCount = getEnchantedCount(utils, allFunctions);
        TooltipNode tooltip = TooltipUtils.getTooltip(utils, entry.quality, enchantedChance, enchantedCount, allFunctions, allConditions).build();
        Either<ItemStack, TagKey<? extends ItemLike>> either = itemGetter.apply(allFunctions);

        if (either.left().isPresent() && either.left().get().isEmpty()) {
            return new EmptyNode(chance, tooltip);
        } else {
            return new ItemNode(chance, enchantedCount.getUnenchantedValue(), either, tooltip, allFunctions, allConditions);
        }
    }

    @NotNull
    public static AlternativesNode getAlternativesNode(IServerUtils utils, AlternativesEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, allFunctions, allConditions);
        TooltipNode tooltip = TooltipUtils.getAlternativesTooltip().build();

        return new AlternativesNode(children, tooltip);
    }

    @NotNull
    public static DynamicNode getDynamicNode(IServerUtils utils, DynamicLoot entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        TooltipNode tooltip = TooltipUtils.getDynamicTooltip(utils, entry.quality, chance, allFunctions, allConditions).build();

        return new DynamicNode(chance, tooltip);
    }

    @NotNull
    public static EmptyNode getEmptyNode(IServerUtils utils, EmptyLootItem entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        EnchantedRanges enchantedChance = getEnchantedChance(utils, allConditions, chance);
        TooltipNode tooltip = TooltipUtils.getEmptyTooltip(utils, entry.quality, enchantedChance, allFunctions, allConditions).build();

        return new EmptyNode(chance, tooltip);
    }

    @NotNull
    public static GroupNode getGroupNode(IServerUtils utils, EntryGroup entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, allFunctions, allConditions);
        TooltipNode tooltip = TooltipUtils.getGroupTooltip().build();

        return new GroupNode(children, tooltip);
    }

    @NotNull
    public static SequenceNode getSequenceNode(IServerUtils utils, SequentialEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, allFunctions, allConditions);
        TooltipNode tooltip = TooltipUtils.getSequentialTooltip().build();

        return new SequenceNode(children, tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, NestedLootTable entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        TooltipNode tooltip = TooltipUtils.getReferenceTooltip(entry, rawChance, sumWeight).build();
        List<IDataNode> children = entry.value.stream().map((holder) -> {
            LootTable lootTable = utils.getLootTable(getLootTableReference(holder));

            if (lootTable != null) {
                return getLootTableNode(Collections.emptyList(), utils, lootTable, chance, allFunctions, allConditions);
            } else {
                return new MissingNode(utils.getValueTooltip(utils, holder).build(Lang.Value.LOOT_TABLE));
            }
        }).toList();

        return new ReferenceNode(children, chance, tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, Identifier table, List<LootItemCondition> conditions, TooltipNode tooltip) {
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
    public static SlotNode getSlotNode(IServerUtils utils, SlotLoot entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        EnchantedRanges enchantedChance = getEnchantedChance(utils, allConditions, chance);
        TooltipNode tooltip = EntryTooltipUtils.getSlotTooltip(utils, entry.slotSource.value(), entry.quality, enchantedChance, allFunctions, allConditions).build();

        return new SlotNode(chance, tooltip);
    }

    @NotNull
    public static LootPoolNode getLootPoolNode(IServerUtils utils, LootPool entry, float rawChance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), unwrapFunctions(entry.modifier).stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), unwrapConditions(entry.condition).stream()).toList();
        int sumWeight = getTotalWeight(entry.entries);
        TooltipNode tooltip = TooltipUtils.getLootPoolTooltip(utils.convertInt(utils, entry.rolls), utils.convertFloat(utils, entry.bonusRolls)).build();
        List<IDataNode> children = getChildren(utils, entry.entries, rawChance, sumWeight, allFunctions, allConditions);

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
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), unwrapFunctions(entry.modifier).stream()).toList();
        TooltipNode tooltip = TooltipUtils.getLootTableTooltip().build();
        List<IDataNode> children = entry.pools.stream().map((lootPool) -> (IDataNode) getLootPoolNode(utils, lootPool, rawChance, allFunctions, conditions)).toList();
        LootTableNode node = new LootTableNode(children, tooltip);

        processOperations(operations, node);
        return node;
    }

    public static float getChance(UniformContainerBase entry, float rawChance, int sumWeight) {
        return rawChance * entry.weight / sumWeight;
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemCondition> getAllConditions(LootPoolEntryContainer entry, List<LootItemCondition> conditions) {
        return Stream.concat(conditions.stream(), unwrapConditions(entry.condition).stream()).toList();
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemFunction> getAllFunctions(LootPoolEntryContainer entry, List<LootItemFunction> functions) {
        return Stream.concat(functions.stream(), unwrapFunctions(entry.modifier).stream()).toList();
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemCondition> unwrapConditions(Optional<Holder<LootItemCondition>> condition) {
        List<LootItemCondition> result = new ArrayList<>();

        condition.ifPresent((c) -> unwrapCondition(c, result));
        return Collections.unmodifiableList(result);
    }

    @Unmodifiable
    @NotNull
    public static List<LootItemFunction> unwrapFunctions(Optional<Holder<LootItemFunction>> modifier) {
        List<LootItemFunction> result = new ArrayList<>();

        modifier.ifPresent((f) -> unwrapFunction(f, result));
        return Collections.unmodifiableList(result);
    }

    private static void unwrapCondition(Holder<LootItemCondition> holder, List<LootItemCondition> result) {
        if (holder.isBound()) {
            if (holder.value() instanceof AllOfCondition allOf) {
                allOf.terms.forEach((term) -> unwrapCondition(term, result));
            } else {
                result.add(holder.value());
            }
        }
    }

    private static void unwrapFunction(Holder<LootItemFunction> holder, List<LootItemFunction> result) {
        if (holder.isBound()) {
            if (holder.value() instanceof SequenceFunction sequence && sequence.condition.isEmpty()) {
                sequence.functions.forEach((function) -> unwrapFunction(function, result));
            } else {
                result.add(holder.value());
            }
        }
    }

    @NotNull
    private static Either<Identifier, LootTable> getLootTableReference(Holder<LootTable> holder) {
        return holder.unwrapKey().<Either<Identifier, LootTable>>map((key) -> Either.left(key.identifier())).orElseGet(() -> Either.right(holder.value()));
    }

    @Unmodifiable
    @NotNull
    public static List<IDataNode> getChildren(IServerUtils utils, List<LootPoolEntryContainer> children, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return children.stream().map((c) -> utils.getEntryFactory(utils, c).create(utils, c, chance, sumWeight, functions, conditions)).toList();
    }

    /** Condition types listed in {@code ignoredPredicateConditions} don't count as predicates. */
    public static boolean hasPredicates(IServerUtils utils, List<LootItemCondition> conditions) {
        List<Identifier> ignored = utils.getConfiguration().ignoredPredicateConditions;

        return conditions.stream()
                .flatMap((c) -> utils.unwrapCondition(utils, c).stream())
                .anyMatch((c) -> !ignored.contains(BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(c.codec())));
    }

    @NotNull
    public static EnchantedRanges getEnchantedChance(IServerUtils utils, List<LootItemCondition> conditions, float rawChance) {
        EnchantedRanges chance = new EnchantedRanges(1);

        for (LootItemCondition condition : conditions) {
            utils.applyChanceModifier(utils, condition, chance);
        }

        chance.modifyAllEntries((value) -> value.multiply(rawChance * 100));
        return chance;
    }

    @NotNull
    public static EnchantedRanges getEnchantedCount(IServerUtils utils, List<LootItemFunction> functions) {
        EnchantedRanges count = new EnchantedRanges(new RangeValue(1));

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        count.modifyAllEntries((value) -> value.clamp(0, 9999));
        return count;
    }

    public static int getTotalWeight(List<LootPoolEntryContainer> entries) {
        int sum = 0;

        for (LootPoolEntryContainer entry : entries) {
            if (entry instanceof UniformContainerBase uniformContainer) {
                sum += uniformContainer.weight;
            } else if (entry instanceof CompositeEntryBase compositeEntryBase) {
                if (entry instanceof AlternativesEntry) {
                    sum += UniformContainerBase.DEFAULT_WEIGHT;
                } else {
                    sum += getTotalWeight(compositeEntryBase.children);
                }
            }
        }

        return sum;
    }

    public static void processOperations(List<IOperation> operations, LootTableNode node) {
        for (IOperation operation : operations) {
            if (operation instanceof IOperation.AddOperation addOperation) {
                node.addChildren(addOperation.node());
            } else if (operation instanceof IOperation.RemoveOperation(Predicate<ItemStack> predicate, Function<IDataNode, IDataNode> factory)) {
                removeItem(node, factory, predicate);
            } else if (operation instanceof IOperation.ReplaceOperation(Predicate<ItemStack> predicate, Function<IDataNode, List<IDataNode>> factory)) {
                replaceItem(node, factory, predicate);
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

                    return result.getFirst();
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
        Optional<? extends Holder.Reference<? extends Registry<?>>> registry = BuiltInRegistries.REGISTRY.get(tag.registry().identifier());

        if (registry.isPresent()) {
            //noinspection unchecked
            Holder.Reference<? extends Registry<T>> reference = (Holder.Reference<? extends Registry<T>>) registry.get();
            //noinspection unchecked
            return reference
                    .value()
                    .get((TagKey<T>) tag)
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
                    Optional<? extends Holder.Reference<? extends Registry<?>>> registry = BuiltInRegistries.REGISTRY.get(tagKey.registry().identifier());

                    if (registry.isPresent()) {
                        //noinspection unchecked
                        Holder.Reference<? extends Registry<T>> reference = (Holder.Reference<? extends Registry<T>>) registry.get();
                        //noinspection unchecked
                        List<ItemStack> stacks = reference
                                .value()
                                .get((TagKey<T>) tagKey)
                                .map((holders) -> holders.stream().map(Holder::value))
                                .orElse(Stream.of())
                                .map((i) -> i.asItem().getDefaultInstance())
                                .toList();
                        return !stacks.isEmpty() && stacks.stream().allMatch(predicate);
                    } else {
                        return false;
                    }
                });
    }
}
