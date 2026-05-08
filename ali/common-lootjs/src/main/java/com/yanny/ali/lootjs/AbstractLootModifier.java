package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.entry.ItemLootEntry;
import com.almostreliable.lootjs.loot.modifier.LootAction;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.almostreliable.lootjs.loot.modifier.handler.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedChance;
import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedCount;

public abstract class AbstractLootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final List<IOperation> operations = new ArrayList<>();

    public AbstractLootModifier(IServerUtils utils, LootModifier modifier) {
        List<LootItemCondition> conditions = modifier.conditions();
        List<LootItemFunction> functions = modifier.functions();
        List<LootAction> actions = modifier.actions();

        for (LootAction action : actions) {
            switch (action) {
                case AddLootAction addLootAction -> {
                    for (LootPoolEntryContainer entry : addLootAction.entries()) {
                        operations.add(new IOperation.AddOperation((s) -> true, utils.getEntryFactory(utils, entry).create(utils, entry, 1, 1, functions, conditions)));
                    }
                }
                case LootPoolAction lootPoolAction ->
                        operations.add(new IOperation.AddOperation((s) -> true, NodeUtils.getLootPoolNode(utils, lootPoolAction.pool(), 1, functions, conditions)));
                case RemoveLootAction removeLootAction -> {
                    Function<IDataNode, IDataNode> factory = (c) -> {
                        if (c instanceof ItemStackNode || c instanceof ItemTagNode || c instanceof ModifiedNode) {
                            return c; // do not replace self!
                        }
                        if (conditions.isEmpty()) {
                            return null; // remove item
                        }

                        if (c instanceof ItemNode i) {
                            Map<Holder<Enchantment>, Map<Integer, RangeValue>> enchantedChance = getEnchantedChance(utils, i.getConditions(), i.getChance());
                            Map<Holder<Enchantment>, Map<Integer, RangeValue>> enchantedCount = getEnchantedCount(utils, i.getFunctions());
                            List<LootItemCondition> allConditions = new LinkedList<>(i.getConditions());

                            allConditions.add(new InvertedLootItemCondition(new AllOfCondition(conditions)));
                            TooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, enchantedChance, enchantedCount, i.getFunctions(), allConditions);
                            return new ItemNode(i.getChance(), i.getCount(), i.getModifiedItem(), tooltip, i.getFunctions(), i.getConditions());
                        }

                        return c;
                    };
                        operations.add(new IOperation.RemoveOperation(removeLootAction.filter()::test, factory));
                }
                case ReplaceLootAction replaceLootAction -> {
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {

                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        ItemLootEntry entry = replaceLootAction.itemLootEntry();
                        boolean preserveCount = replaceLootAction.preserveCount(); //FIXME custom itemNode with preserveCount
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), node.getFunctions().stream()).toList();

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, NodeUtils.getItemNode(utils, entry.getVanillaEntry(), 1, 1, allFunctions, allConditions)));
                        } else {
                            nodes.add(NodeUtils.getItemNode(utils, entry.getVanillaEntry(), 1, 1, allFunctions, allConditions));
                        }

                        return nodes;
                    };
                    operations.add(new IOperation.ReplaceOperation(replaceLootAction.filter()::test, factory));
                }
                case ModifyLootAction modifyLootAction -> {
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {
                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = new ArrayList<>();
                        Either<ItemStack, TagKey<? extends ItemLike>> either = node.getModifiedItem();

                        allFunctions.add(new ModifiedItemFunction());
                        allFunctions.addAll(Stream.concat(functions.stream(), node.getFunctions().stream()).toList());

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, constructEither(utils, either, node.getChance(), allFunctions, allConditions)));
                        } else {
                            nodes.add(constructEither(utils, either, node.getChance(), allFunctions, allConditions));
                        }

                        return nodes;
                    };

                    operations.add(new IOperation.ReplaceOperation(modifyLootAction.predicate()::test, factory));
                }
                default -> LOGGER.warn("Skipping unexpected loot action {}", action.getClass().getCanonicalName());
            }
        }
    }

    @NotNull
    @Override
    public List<IOperation> getOperations() {
        return operations;
    }

    private static IDataNode constructEither(IServerUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> either, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return either.map(
                (itemStack) -> new ItemStackNode(utils, itemStack, chance, true, functions, conditions, true),
                (tagKey) -> new ItemTagNode(utils, tagKey, chance, true, functions, conditions, true)
        );
    }
}
