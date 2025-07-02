package com.yanny.ali.plugin.kubejs;

import com.almostreliable.lootjs.core.AbstractLootModification;
import com.almostreliable.lootjs.core.ILootAction;
import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinCompositeLootAction;
import com.yanny.ali.plugin.kubejs.node.AddLootNode;
import com.yanny.ali.plugin.kubejs.node.WeightedAddLootNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public abstract class LootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final Predicate<ItemStack> itemPredicate;
    private final IDataNode node;
    private final Operation operation;

    public LootModifier(IServerUtils utils, AbstractLootModification byBlock) {
        Collection<ILootHandler> handlers = ((MixinCompositeLootAction) byBlock).getHandlers();
        List<LootItemCondition> conditions = new LinkedList<>();
        ILootAction action = null;

        for (ILootHandler handler : handlers) {
            if (handler instanceof LootItemCondition lootItemCondition) {
                conditions.add(lootItemCondition);
            } else if (handler instanceof ILootAction lootAction) {
                action = lootAction;
            } else {
                LOGGER.warn("Invalid handler: {} [{}]", handler, handler.getClass().getCanonicalName());
            }
        }

        if (action != null) {
            if (action instanceof AddLootAction addLootAction) {
                node = new AddLootNode(utils, addLootAction, conditions);
                operation = Operation.ADD;
                itemPredicate = itemStack -> true;
            } else if (action instanceof WeightedAddLootAction addLootAction) {
                node = new WeightedAddLootNode(utils, addLootAction, conditions);
                operation = Operation.ADD;
                itemPredicate = itemStack -> true;
            } else {
                throw new IllegalStateException("Unexpected loot action " + action.getClass().getCanonicalName());
            }

        } else {
            throw new IllegalStateException("Missing loot action");
        }
    }

    @Override
    public boolean itemPredicate(ItemStack item) {
        return itemPredicate.test(item);
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public IDataNode getNode() {
        return node;
    }
}
