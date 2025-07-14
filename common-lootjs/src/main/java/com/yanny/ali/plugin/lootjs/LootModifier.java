package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.AbstractLootModification;
import com.almostreliable.lootjs.core.ILootAction;
import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinCompositeLootAction;
import com.yanny.ali.mixin.MixinModifyLootAction;
import com.yanny.ali.mixin.MixinRemoveLootAction;
import com.yanny.ali.mixin.MixinReplaceLootAction;
import com.yanny.ali.plugin.lootjs.modifier.*;
import com.yanny.ali.plugin.lootjs.node.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class LootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final List<IOperation> operations = new ArrayList<>();

    public LootModifier(IServerUtils utils, AbstractLootModification byBlock) {
        Collection<ILootHandler> handlers = ((MixinCompositeLootAction) byBlock).getHandlers();
        List<LootItemCondition> conditions = new LinkedList<>();
        List<LootItemFunction> functions = new LinkedList<>();
        List<ILootAction> actions = new LinkedList<>();

        for (ILootHandler handler : handlers) {
            if (handler instanceof LootItemCondition lootItemCondition) {
                conditions.add(lootItemCondition);
            } else if (handler instanceof LootItemFunctionWrapperAction lootFunctionAction) {
                functions.add(lootFunctionAction.getLootItemFunction());
            } else if (handler instanceof CustomPlayerAction customPlayerAction) {
                functions.add(new CustomPlayerFunction(customPlayerAction));
            } else if (handler instanceof DropExperienceAction dropExperienceAction) {
                functions.add(new DropExperienceFunction(dropExperienceAction));
            } else if (handler instanceof ExplodeAction explodeAction) {
                functions.add(new ExplodeFunction(explodeAction));
            } else if (handler instanceof LightningStrikeAction lightningStrikeAction) {
                functions.add(new LightningStrikeFunction(lightningStrikeAction));
            } else if (handler instanceof ILootAction lootAction) {
                actions.add(lootAction); // must be last - functions are also instances of ILootAction
            } else {
                LOGGER.warn("Unhandled handler: {} [{}]", handler, handler.getClass().getCanonicalName());
            }
        }

        if (!actions.isEmpty()) {
            for (ILootAction action : actions) {
                if (action instanceof AddLootAction addLootAction) {
                    operations.add(new IOperation.AddOperation((itemStack) -> true, new AddLootNode(utils, addLootAction, functions, conditions)));
                } else if (action instanceof WeightedAddLootAction addLootAction) {
                    operations.add(new IOperation.AddOperation((itemStack) -> true, new WeightedAddLootNode(utils, addLootAction, functions, conditions)));
                } else if (action instanceof GroupedLootAction groupedLootAction) {
                    operations.add(new IOperation.AddOperation((itemStack) -> true, new GroupLootNode(utils, groupedLootAction, functions, conditions)));
                } else if (action instanceof RemoveLootAction removeLootAction) {
                    operations.add(new IOperation.RemoveOperation(((MixinRemoveLootAction) removeLootAction).getPredicate()));
                } else if (action instanceof ReplaceLootAction replaceLootAction) {
                    MixinReplaceLootAction lootAction = (MixinReplaceLootAction) replaceLootAction;
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {
                        if (c instanceof ItemStackNode || c instanceof ItemTagNode || c instanceof ModifiedNode) {
                            return Collections.singletonList(c); // do not replace self!
                        }

                        List<IDataNode> nodes = new ArrayList<>();
                        IItemNode node = (IItemNode) c;
                        LootEntry entry = lootAction.getLootEntry();
                        boolean preserveCount = lootAction.getPreserveCount();
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
                        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), node.getFunctions().stream()).toList();

                        if (!conditions.isEmpty()) {
                            nodes.add(new ModifiedNode(utils, c, Utils.getEntry(utils, entry, 1, allFunctions, allConditions, preserveCount)));
                        } else {
                            nodes.add(Utils.getEntry(utils, entry, 1, allFunctions, allConditions, preserveCount));
                        }

                        return nodes;
                    };

                    operations.add(new IOperation.ReplaceOperation(lootAction.getPredicate(), factory));
                } else if (action instanceof ModifyLootAction modifyLootAction) {
                    MixinModifyLootAction lootAction = (MixinModifyLootAction) modifyLootAction;
                    Function<IDataNode, List<IDataNode>> factory = (c) -> {
                        if (c instanceof ItemStackNode || c instanceof ItemTagNode || c instanceof ModifiedNode) {
                            return Collections.singletonList(c); // do not modify self!
                        }

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

                    operations.add(new IOperation.ReplaceOperation(lootAction.getPredicate(), factory));
                } else {
                    LOGGER.warn("Skipping unexpected loot action {}", action.getClass().getCanonicalName());
                }
            }
        } else {
            throw new IllegalStateException("Missing loot action");
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
