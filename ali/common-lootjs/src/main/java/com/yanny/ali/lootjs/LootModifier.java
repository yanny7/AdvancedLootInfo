package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.AbstractLootModification;
import com.almostreliable.lootjs.core.ILootAction;
import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.lootjs.mixin.MixinCompositeLootAction;
import com.yanny.ali.lootjs.mixin.MixinModifyLootAction;
import com.yanny.ali.lootjs.mixin.MixinRemoveLootAction;
import com.yanny.ali.lootjs.mixin.MixinReplaceLootAction;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.lootjs.node.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedChance;
import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedCount;

public abstract class LootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final List<IOperation> operations = new ArrayList<>();

    public LootModifier(IServerUtils utils, AbstractLootModification byBlock) {
        Collection<ILootHandler> handlers = ((MixinCompositeLootAction) byBlock).getHandlers();
        List<LootItemCondition> conditions = new LinkedList<>();
        List<LootItemFunction> functions = new LinkedList<>();

        for (ILootHandler handler : handlers) {
            if (handler instanceof LootItemCondition lootItemCondition) {
                conditions.add(lootItemCondition);
            } else if (handler instanceof LootItemFunctionWrapperAction lootFunctionAction) {
                functions.add(lootFunctionAction.getLootItemFunction());
            } else if (handler instanceof CustomPlayerAction customPlayerAction) {
                functions.add(new CustomPlayerFunction(customPlayerAction));
            } else if (handler instanceof ILootAction lootAction) {
                processAction(utils, lootAction, new ArrayList<>(conditions), new ArrayList<>(functions));
            } else {
                LOGGER.warn("Unhandled handler: {} [{}]", handler, handler.getClass().getCanonicalName());
            }
        }
    }

    @NotNull
    @Override
    public List<IOperation> getOperations() {
        return operations;
    }

    private void processAction(IServerUtils utils, ILootAction action, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        if (action instanceof AddLootAction addLootAction) {
            operations.add(new IOperation.AddOperation((itemStack) -> true, new AddLootNode(utils, addLootAction, functions, conditions)));
        } else if (action instanceof WeightedAddLootAction addLootAction) {
            operations.add(new IOperation.AddOperation((itemStack) -> true, new WeightedAddLootNode(utils, addLootAction, functions, conditions)));
        } else if (action instanceof GroupedLootAction groupedLootAction) {
            operations.add(new IOperation.AddOperation((itemStack) -> true, new GroupLootNode(utils, groupedLootAction, functions, conditions)));
        } else if (action instanceof RemoveLootAction removeLootAction) {
            Function<IDataNode, IDataNode> factory = (c) -> {
                if (c instanceof ItemStackNode || c instanceof ItemTagNode || c instanceof ModifiedNode) {
                    return c; // do not replace self!
                }
                if (conditions.isEmpty()) {
                    return null; // remove item
                }

                if (c instanceof ItemNode i) {
                    Map<Enchantment, Map<Integer, RangeValue>> enchantedChance = getEnchantedChance(utils, i.getConditions(), i.getChance());
                    Map<Enchantment, Map<Integer, RangeValue>> enchantedCount = getEnchantedCount(utils, i.getFunctions());
                    List<LootItemCondition> allConditions = new LinkedList<>(i.getConditions());

                    allConditions.add(new InvertedLootItemCondition(new AllOfCondition(conditions.toArray(LootItemCondition[]::new))));
                    TooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, enchantedChance, enchantedCount, i.getFunctions(), allConditions);
                    return new ItemNode(i.getChance(), i.getCount(), i.getModifiedItem(), tooltip, i.getFunctions(), i.getConditions());
                }

                return c;
            };
            operations.add(new IOperation.RemoveOperation(((MixinRemoveLootAction) removeLootAction).getPredicate(), factory));
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

            operations.add(new IOperation.ReplaceOperation(lootAction.getPredicate(), factory));
        } else {
            LOGGER.warn("Skipping unexpected loot action {}", action.getClass().getCanonicalName());
        }
    }

    private static IDataNode constructEither(IServerUtils utils, Either<ItemStack, TagKey<? extends ItemLike>> either, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return either.map(
                (itemStack) -> new ItemStackNode(utils, itemStack, chance, true, functions, conditions, true),
                (tagKey) -> new ItemTagNode(utils, tagKey, chance, true, functions, conditions, true)
        );
    }
}
