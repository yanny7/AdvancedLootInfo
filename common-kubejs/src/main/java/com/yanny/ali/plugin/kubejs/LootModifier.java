package com.yanny.ali.plugin.kubejs;

import com.almostreliable.lootjs.core.AbstractLootModification;
import com.almostreliable.lootjs.core.ILootAction;
import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.almostreliable.lootjs.loot.action.RemoveLootAction;
import com.almostreliable.lootjs.loot.action.ReplaceLootAction;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinCompositeLootAction;
import com.yanny.ali.mixin.MixinRemoveLootAction;
import com.yanny.ali.mixin.MixinReplaceLootAction;
import com.yanny.ali.plugin.kubejs.node.AddLootNode;
import com.yanny.ali.plugin.kubejs.node.LootEntryNode;
import com.yanny.ali.plugin.kubejs.node.WeightedAddLootNode;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class LootModifier<T> implements ILootModifier<T> {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private final IOperation operation;

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
                operation = new IOperation.AddOperation((itemStack) -> true, new AddLootNode(utils, addLootAction, conditions));
            } else if (action instanceof WeightedAddLootAction addLootAction) {
                operation = new IOperation.AddOperation((itemStack) -> true, new WeightedAddLootNode(utils, addLootAction, conditions));
            } else if (action instanceof RemoveLootAction removeLootAction) {
                operation = new IOperation.RemoveOperation(((MixinRemoveLootAction) removeLootAction).getPredicate());
            } else if (action instanceof ReplaceLootAction replaceLootAction) {
                MixinReplaceLootAction lootAction = (MixinReplaceLootAction) replaceLootAction;

                operation = new IOperation.ReplaceOperation(lootAction.getPredicate(), new LootEntryNode(utils, lootAction.getLootEntry(), 1, conditions));
            } else {
                throw new IllegalStateException("Unexpected loot action " + action.getClass().getCanonicalName());
            }

        } else {
            throw new IllegalStateException("Missing loot action");
        }
    }

    @Override
    public IOperation getOperation() {
        return operation;
    }
}
