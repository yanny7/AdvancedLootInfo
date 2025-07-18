package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.loot.modifier.LootAction;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.almostreliable.lootjs.loot.modifier.handler.AddLootAction;
import com.almostreliable.lootjs.loot.modifier.handler.ModifyLootAction;
import com.almostreliable.lootjs.loot.modifier.handler.RemoveLootAction;
import com.almostreliable.lootjs.loot.modifier.handler.ReplaceLootAction;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.plugin.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.lootjs.node.ItemTagNode;
import com.yanny.ali.plugin.lootjs.node.ModifiedNode;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractLootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final List<IOperation> operations = new ArrayList<>();

    public AbstractLootModifier(IServerUtils utils, LootModifier modifier) {
        IGroupedLootActionAccessor mixin = (IGroupedLootActionAccessor) modifier;

        List<LootItemCondition> conditions = mixin.ali_$getInjectedConditions();
        List<LootItemFunction> functions = mixin.getFunctions();
        List<LootAction> actions = mixin.getActions();

        for (LootAction action : actions) {
            switch (action) {
                case AddLootAction addLootAction -> {
                    MixinAddLootAction mixinAddLootAction = (MixinAddLootAction) addLootAction;

                    for (LootPoolEntryContainer entry : mixinAddLootAction.getEntries()) {
                        operations.add(new IOperation.AddOperation((s) -> true, utils.getEntryFactory(utils, entry).create(utils, entry, 1, 1, functions, conditions)));
                    }
                }
                case RemoveLootAction removeLootAction ->
                        operations.add(new IOperation.RemoveOperation(((MixinRemoveLootAction) removeLootAction).getFilter()::test));
                case ReplaceLootAction replaceLootAction -> {
                    MixinReplaceLootAction lootAction = (MixinReplaceLootAction) replaceLootAction;
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {

                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        //noinspection unchecked
                        MixinAbstractSimpleLootEntry<LootItem> entry = (MixinAbstractSimpleLootEntry<LootItem>) lootAction.getItemLootEntry();
                        boolean preserveCount = lootAction.getPreserveCount(); //FIXME custom itemNode with preserveCount
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), node.getFunctions().stream()).toList();

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, new ItemNode(utils, entry.getVanillaEntry(), 1, 1, allFunctions, allConditions)));
                        } else {
                            nodes.add(new ItemNode(utils, entry.getVanillaEntry(), 1, 1, allFunctions, allConditions));
                        }

                        return nodes;
                    };
                    operations.add(new IOperation.ReplaceOperation(lootAction.getFilter()::test, factory));
                }
                case ModifyLootAction modifyLootAction -> {
                    MixinModifyLootAction lootAction = (MixinModifyLootAction) modifyLootAction;
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {
                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = new ArrayList<>();
                        Either<ItemStack, TagKey<Item>> either = node.getModifiedItem();

                        allFunctions.add(new ModifiedItemFunction());
                        allFunctions.addAll(Stream.concat(functions.stream(), node.getFunctions().stream()).toList());

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, constructEither(utils, either, node.getChance(), allFunctions, allConditions)));
                        } else {
                            nodes.add(constructEither(utils, either, node.getChance(), allFunctions, allConditions));
                        }

                        return nodes;
                    };

                    operations.add(new IOperation.ReplaceOperation(lootAction.getPredicate()::test, factory));
                }
                default -> LOGGER.warn("Skipping unexpected loot action {}", action.getClass().getCanonicalName());
            }
        }
    }

    @Override
    public List<IOperation> getOperations() {
        return operations;
    }

    private static IDataNode constructEither(IServerUtils utils, Either<ItemStack, TagKey<Item>> either, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return either.map(
                (itemStack) -> new ItemStackNode(utils, itemStack, chance, true, functions, conditions, true),
                (tagKey) -> new ItemTagNode(utils, tagKey, chance, true, functions, conditions, true)
        );
    }
}
