package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.CustomPlayerAction;
import com.almostreliable.lootjs.loot.action.DropExperienceAction;
import com.almostreliable.lootjs.loot.action.ExplodeAction;
import com.almostreliable.lootjs.loot.action.LightningStrikeAction;
import com.almostreliable.lootjs.loot.action.LootItemFunctionWrapperAction;
import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinDropExperienceAction;
import com.yanny.ali.lootjs.mixin.MixinExplodeAction;
import com.yanny.ali.lootjs.mixin.MixinLightningStrikeAction;
import com.yanny.ali.lootjs.mixin.MixinLootEntry;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.DropExperienceFunction;
import com.yanny.ali.lootjs.modifier.ExplodeFunction;
import com.yanny.ali.lootjs.modifier.LightningStrikeFunction;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.EmptyNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Utils {
    public static IDataNode getEntry(IServerUtils utils, LootEntry entry, float rawChance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions, @Nullable NumberExpr preservedCount) {
        MixinLootEntry mixinLootEntry = (MixinLootEntry) entry;
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), mixinLootEntry.getPostModifications().stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), mixinLootEntry.getConditions().stream()).toList();
        ItemStack itemStack = mixinLootEntry.getItemStack();
        int weight = mixinLootEntry.getWeight();

        if (itemStack.isEmpty()) {
            return getEmptyNode(utils, rawChance * weight / sumWeight, allFunctions, allConditions);
        }

        return new ItemStackNode(utils, itemStack, rawChance * weight / sumWeight, allFunctions, allConditions, preservedCount);
    }

    public static boolean collectHandler(ILootHandler handler, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        if (handler instanceof LootItemCondition lootItemCondition) {
            conditions.add(lootItemCondition);
        } else if (handler instanceof LootItemFunctionWrapperAction) {
            return true;
        } else if (handler instanceof CustomPlayerAction customPlayerAction) {
            functions.add(new CustomPlayerFunction(customPlayerAction));
        } else if (handler instanceof DropExperienceAction action) {
            functions.add(new DropExperienceFunction(((MixinDropExperienceAction) action).getAmount()));
        } else if (handler instanceof ExplodeAction action) {
            MixinExplodeAction explode = (MixinExplodeAction) action;

            functions.add(new ExplodeFunction(explode.getRadius(), explode.getMode(), explode.getFire()));
        } else if (handler instanceof LightningStrikeAction action) {
            functions.add(new LightningStrikeFunction(((MixinLightningStrikeAction) action).getShouldDamageEntity()));
        } else {
            return false;
        }

        return true;
    }

    @NotNull
    public static List<LootItemFunction> withLaterFunctions(List<LootItemFunction> functions, List<ILootHandler> handlers, int index) {
        List<LootItemFunction> result = new ArrayList<>(functions);

        for (ILootHandler handler : handlers.subList(index + 1, handlers.size())) {
            if (handler instanceof LootItemFunctionWrapperAction lootFunctionAction) {
                result.add(lootFunctionAction.getLootItemFunction());
            }
        }

        return result;
    }

    @NotNull
    private static IDataNode getEmptyNode(IServerUtils utils, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        LootCount chanceExpr = NodeUtils.getChance(utils, conditions, chance);

        return new EmptyNode(chance, TooltipUtils.getEmptyTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chanceExpr, functions, conditions).build());
    }
}
