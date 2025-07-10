package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.AbstractLootModification;
import com.almostreliable.lootjs.core.ILootAction;
import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.loot.action.*;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinCompositeLootAction;
import com.yanny.ali.mixin.MixinModifyLootAction;
import com.yanny.ali.mixin.MixinRemoveLootAction;
import com.yanny.ali.mixin.MixinReplaceLootAction;
import com.yanny.ali.plugin.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.plugin.lootjs.modifier.DropExperienceFunction;
import com.yanny.ali.plugin.lootjs.modifier.ExplodeFunction;
import com.yanny.ali.plugin.lootjs.modifier.LightningStrikeFunction;
import com.yanny.ali.plugin.lootjs.node.AddLootNode;
import com.yanny.ali.plugin.lootjs.node.GroupLootNode;
import com.yanny.ali.plugin.lootjs.node.WeightedAddLootNode;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
                    Function<IDataNode, IDataNode> factory = (c) -> {
                        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), ((IItemNode)c).getConditions().stream()).toList(); //TODO preserve count?
                        return Utils.getEntry(utils, lootAction.getLootEntry(), 1, functions, allConditions);
                    };

                    operations.add(new IOperation.ReplaceOperation(lootAction.getPredicate(), factory));
                } else if (action instanceof ModifyLootAction modifyLootAction) {
                    MixinModifyLootAction lootAction = (MixinModifyLootAction) modifyLootAction;
                    Function<IDataNode, IDataNode> factory = (c) -> {
                        return c; //TODO wrap with icon saying that it's loot was modified by JS
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
}
