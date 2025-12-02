package com.yanny.ali.plugin.common;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NodeUtils {
    @NotNull
    public static ItemNode getItemNode(IServerUtils utils, LootItem entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getItemNode(utils, entry, (f) -> Either.left(TooltipUtils.getItemStack(utils, entry.item.value().getDefaultInstance(), f)), rawChance, sumWeight, functions, conditions);
    }

    @NotNull
    public static ItemNode getTagNode(IServerUtils utils, TagEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return getItemNode(utils, entry, (f) -> Either.right(entry.tag), rawChance, sumWeight, functions, conditions);
    }

    @NotNull
    public static ItemNode getItemNode(IServerUtils utils, LootPoolSingletonContainer entry, Function<List<LootItemFunction>, Either<ItemStack, TagKey<? extends ItemLike>>> itemGetter, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        float chance = getChance(entry, rawChance, sumWeight);
        RangeValue count = getEnchantedCount(utils, allFunctions).get(null).get(0);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> enchantedChance = getEnchantedChance(utils, allConditions, chance);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> enchantedCount = getEnchantedCount(utils, allFunctions);
        ITooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, entry.quality, enchantedChance, enchantedCount, allFunctions, allConditions);

        return new ItemNode(chance, count, itemGetter.apply(allFunctions), tooltip, allFunctions, allConditions);
    }

    @NotNull
    public static AlternativesNode getAlternativesNode(IServerUtils utils, AlternativesEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, functions, allConditions);
        ITooltipNode tooltip = EntryTooltipUtils.getAlternativesTooltip();

        return new AlternativesNode(children, tooltip);
    }

    @NotNull
    public static DynamicNode getDynamicNode(IServerUtils utils, DynamicLoot entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        ITooltipNode tooltip = EntryTooltipUtils.getDynamicTooltip(utils, entry.quality, chance, allFunctions, allConditions);

        return new DynamicNode(chance, tooltip);
    }

    @NotNull
    public static EmptyNode getEmptyNode(IServerUtils utils, EmptyLootItem entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> enchantedChance = getEnchantedChance(utils, allConditions, chance);
        ITooltipNode tooltip = EntryTooltipUtils.getEmptyTooltip(utils, entry.quality, enchantedChance, allFunctions, allConditions);

        return new EmptyNode(chance, tooltip);
    }

    @NotNull
    public static GroupNode getGroupNode(IServerUtils utils, EntryGroup entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, functions, allConditions);
        ITooltipNode tooltip = EntryTooltipUtils.getGroupTooltip();

        return new GroupNode(children, tooltip);
    }

    @NotNull
    public static SequenceNode getSequenceNode(IServerUtils utils, SequentialEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        List<IDataNode> children = getChildren(utils, entry.children, rawChance, sumWeight, functions, allConditions);
        ITooltipNode tooltip = EntryTooltipUtils.getSequentialTooltip();

        return new SequenceNode(children, tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, LootTableReference entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = getAllFunctions(entry, functions);
        List<LootItemCondition> allConditions = getAllConditions(entry, conditions);
        float chance = getChance(entry, rawChance, sumWeight);
        LootTable lootTable = utils.getLootTable(entry.name);
        ITooltipNode tooltip = EntryTooltipUtils.getReferenceTooltip(entry, rawChance, sumWeight);
        List<IDataNode> children;

        if (lootTable != null) {
            children = Collections.singletonList(getLootTableNode(Collections.emptyList(), utils, lootTable, chance, allFunctions, allConditions));
        } else {
            children = Collections.singletonList(new MissingNode(utils.getValueTooltip(utils, entry.name).build("ali.property.value.loot_table")));
        }

        return new ReferenceNode(children, chance, tooltip);
    }

    @NotNull
    public static ReferenceNode getReferenceNode(IServerUtils utils, ResourceLocation table, List<LootItemCondition> conditions, ITooltipNode tooltip) {
        LootTable lootTable = utils.getLootTable(table);
        List<IDataNode> children;

        if (lootTable != null) {
            children = Collections.singletonList(getLootTableNode(Collections.emptyList(), utils, lootTable, 1, Collections.emptyList(), conditions));
        } else {
            children = Collections.singletonList(new MissingNode(utils.getValueTooltip(utils, table).build("ali.property.value.loot_table")));
        }

        return new ReferenceNode(children, 1, tooltip);
    }

    @NotNull
    public static LootPoolNode getLootPoolNode(IServerUtils utils, LootPool entry, float rawChance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();
        int sumWeight = getTotalWeight(entry.entries);
        ITooltipNode tooltip = EntryTooltipUtils.getLootPoolTooltip(utils.convertNumber(utils, entry.rolls), utils.convertNumber(utils, entry.bonusRolls));
        List<IDataNode> children = getChildren(utils, entry.entries, rawChance, sumWeight, allFunctions, allConditions);

        return new LootPoolNode(children, tooltip);
    }

    @NotNull
    public static LootTableNode getLootTableNode(List<ILootModifier<?>> modifiers) {
        ITooltipNode tooltip = EntryTooltipUtils.getLootTableTooltip();
        List<IDataNode> children = new ArrayList<>();
        LootTableNode node = new LootTableNode(children, tooltip);

        for (ILootModifier<?> modifier : modifiers) {
            processLootModifier(modifier, node);
        }

        return node;
    }

    @NotNull
    public static LootTableNode getLootTableNode(List<ILootModifier<?>> modifiers, IServerUtils utils, LootTable entry, float rawChance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
        ITooltipNode tooltip = EntryTooltipUtils.getLootTableTooltip();
        List<IDataNode> children = entry.pools.stream().map((lootPool) -> (IDataNode) getLootPoolNode(utils, lootPool, rawChance, allFunctions, conditions)).toList();
        LootTableNode node = new LootTableNode(children, tooltip);

        for (ILootModifier<?> modifier : modifiers) {
            processLootModifier(modifier, node);
        }

        return node;
    }

    public static float getChance(LootPoolSingletonContainer entry, float rawChance, int sumWeight) {
        return rawChance * entry.weight / sumWeight;
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
    public static List<IDataNode> getChildren(IServerUtils utils, List<LootPoolEntryContainer> children, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return children.stream().map((c) -> utils.getEntryFactory(utils, c).create(utils, c, chance, sumWeight, functions, conditions)).toList();
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getEnchantedChance(IServerUtils utils, List<LootItemCondition> conditions, float rawChance) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance = new LinkedHashMap<>();

        chance.put(null, Map.of(0, new RangeValue(rawChance * 100)));

        for (LootItemCondition condition : conditions) {
            utils.applyChanceModifier(utils, condition, chance);
        }

        return chance;
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getEnchantedCount(IServerUtils utils, List<LootItemFunction> functions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> count = new LinkedHashMap<>();

        count.put(null, Map.of(0, new RangeValue()));

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        return count;
    }

    public static int getTotalWeight(List<LootPoolEntryContainer> entries) {
        int sum = 0;

        for (LootPoolEntryContainer entry : entries) {
            if (entry instanceof LootPoolSingletonContainer singletonContainer) {
                sum += singletonContainer.weight;
            } else if (entry instanceof CompositeEntryBase compositeEntryBase) {
                if (entry instanceof AlternativesEntry) {
                    sum += LootPoolSingletonContainer.DEFAULT_WEIGHT;
                } else {
                    sum += getTotalWeight(compositeEntryBase.children);
                }
            }
        }

        return sum;
    }

    @NotNull
    public static List<Component> toComponents(List<ITooltipNode> tooltip, int pad, boolean showAdvancedTooltip) {
        List<Component> components = new ArrayList<>();

        for (ITooltipNode node : tooltip) {
            components.addAll(toComponents(node, pad, showAdvancedTooltip));
        }

        return components;
    }

    @NotNull
    public static List<Component> toComponents(ITooltipNode tooltip, int pad, boolean showAdvancedTooltip) {
        return tooltip.getComponents(pad, showAdvancedTooltip);
    }

    public static void processLootModifier(ILootModifier<?> modifier, LootTableNode node) {
        List<IOperation> operations = modifier.getOperations();

        for (IOperation operation : operations) {
            if (operation instanceof IOperation.AddOperation addOperation) {
                node.addChildren(addOperation.node());
            } else if (operation instanceof IOperation.RemoveOperation(Predicate<ItemStack> predicate)) {
                removeItem(node, predicate);
            } else if (operation instanceof IOperation.ReplaceOperation(Predicate<ItemStack> predicate, Function<IDataNode, List<IDataNode>> factory)) {
                replaceItem(node, factory, predicate);
            }
        }
    }

    private static void removeItem(IDataNode node, Predicate<ItemStack> predicate) {
        if (node instanceof ListNode listNode) {
            listNode.nodes().removeIf((n) -> {
                if (n instanceof IItemNode itemNode) {
                    return predicateEither(itemNode, predicate);
                } else {
                    removeItem(n, predicate);
                }

                return false;
            });
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
                } else if (n instanceof ListNode l) {
                    replaceItem(l, factory, predicate);
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

    private static <T extends ItemLike> boolean predicateEither(IItemNode itemNode, Predicate<ItemStack> predicate) {
        return itemNode.getModifiedItem().map(
                predicate::test,
                (tagKey) -> {
                    //noinspection unchecked
                    Registry<T> registry = (Registry<T>)BuiltInRegistries.REGISTRY.get(tagKey.registry().location());

                    if (registry != null) {
                        //noinspection unchecked
                        return registry
                                .getTag((TagKey<T>) tagKey)
                                .map((holders) -> holders.stream().map(Holder::value))
                                .orElse(Stream.of())
                                .map((i) -> i.asItem().getDefaultInstance())
                                .allMatch(predicate);
                    } else {
                        return false;
                    }
                });
    }
}
