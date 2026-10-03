package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.entry.LootEntry;
import com.almostreliable.lootjs.loot.modifier.LootAction;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.almostreliable.lootjs.loot.modifier.handler.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.lootjs.modifier.PreserveComponentsFunction;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.common.NodeUtils.getChance;
import static com.yanny.ali.plugin.common.NodeUtils.getCount;
import static com.yanny.ali.plugin.common.NodeUtils.getCountLimit;

public abstract class AbstractLootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final List<IOperation> operations = new ArrayList<>();

    public AbstractLootModifier(IServerUtils utils, LootModifier modifier) {
        List<LootItemCondition> conditions = modifier.conditions();
        List<LootAction> actions = modifier.actions();
        List<LootItemFunction> functions = new ArrayList<>(modifier.functions());

        for (LootAction action : actions) {
            if (action instanceof CustomPlayerAction customPlayerAction) {
                functions.add(new CustomPlayerFunction(customPlayerAction));
            }
        }

        for (LootAction action : actions) {
            switch (action) {
                case CustomPlayerAction ignoredAction -> {}
                case AddLootAction addLootAction -> {
                    for (LootPoolEntryContainer entry : addLootAction.entries()) {
                        operations.add(new IOperation.AddOperation((s) -> true, utils.getEntryFactory(utils, entry).create(utils, entry, NumberExpr.constant(1), NumberExpr.constant(1), List.of(), functions, conditions)));
                    }
                }
                case LootPoolAction lootPoolAction ->
                        operations.add(new IOperation.AddOperation((s) -> true, NodeUtils.getLootPoolNode(utils, lootPoolAction.pool(), NumberExpr.constant(1), List.of(), functions, conditions)));
                case RemoveLootAction removeLootAction -> {
                    Function<IDataNode, IDataNode> factory = (c) -> {
                        if (isOwnNode(c)) {
                            return c;
                        }

                        if (conditions.isEmpty()) {
                            return null; // remove item
                        }

                        if (c instanceof ItemNode i) {
                            LootCount chance = getChance(utils, i.getConditions(), i.getChance());
                            LootCount count;

                            if (i.getFunctions().isEmpty()) {
                                count = LootCount.of(i.getCount());
                            } else {
                                count = getCount(utils, i.getFunctions());
                            }

                            List<LootItemCondition> allConditions = new LinkedList<>(i.getConditions());

                            allConditions.add(new InvertedLootItemCondition(new AllOfCondition(conditions)));
                            TooltipNode tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, getCountLimit(i.getItem()), i.getFunctions(), allConditions).build();
                            return new ItemNode(i.getChance(), i.getCount(), i.getItem(), tooltip, i.getFunctions(), i.getConditions());
                        }

                        return c;
                    };
                        operations.add(new IOperation.RemoveOperation(removeLootAction.filter()::test, factory));
                }
                case ReplaceLootAction replaceLootAction -> {
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {
                        if (isOwnNode(c)) {
                            return List.of(c);
                        }

                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        LootEntry entry = replaceLootAction.lootEntry();
                        NumberExpr preservedCount = replaceLootAction.preserveCount() ? node.getCount() : null;
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = new ArrayList<>();

                        if (replaceLootAction.preserveComponentTypes().length > 0) {
                            allFunctions.add(new PreserveComponentsFunction(replaceLootAction.preserveComponentTypes()));
                        }

                        allFunctions.addAll(Stream.concat(functions.stream(), node.getFunctions().stream()).toList());

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, Utils.getEntry(utils, entry, 1, allFunctions, allConditions, preservedCount)));
                        } else {
                            nodes.add(Utils.getEntry(utils, entry, 1, allFunctions, allConditions, preservedCount));
                        }

                        return nodes;
                    };
                    operations.add(new IOperation.ReplaceOperation(replaceLootAction.filter()::test, factory));
                }
                case ModifyLootAction modifyLootAction -> {
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {
                        if (isOwnNode(c)) {
                            return List.of(c);
                        }

                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = new ArrayList<>();
                        Either<ItemStack, TagKey<? extends ItemLike>> either = node.getItem();

                        allFunctions.add(new ModifiedItemFunction());
                        allFunctions.addAll(Stream.concat(functions.stream(), node.getFunctions().stream()).toList());

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, constructEither(utils, either, node.getChance(), node.getCount(), allFunctions, allConditions)));
                        } else {
                            nodes.add(constructEither(utils, either, node.getChance(), node.getCount(), allFunctions, allConditions));
                        }

                        return nodes;
                    };

                    operations.add(new IOperation.ReplaceOperation(modifyLootAction.predicate()::test, factory));
                }
                default -> LOGGER.warn("Skipping unexpected loot action {}", action.getClass().getCanonicalName());
            }
        }
    }

    private static boolean isOwnNode(IDataNode node) {
        return node instanceof ItemStackNode || node instanceof ItemTagNode || node instanceof ModifiedNode;
    }

    @NotNull
    @Override
    public List<IOperation> getOperations() {
        return operations;
    }

    private static IDataNode constructEither(IServerUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> either, float chance, NumberExpr preservedCount, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return either.map(
                (itemStack) -> new ItemStackNode(utils, itemStack, chance, true, functions, conditions, preservedCount),
                (tagKey) -> new ItemTagNode(utils, tagKey, chance, true, functions, conditions, preservedCount)
        );
    }
}
