package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootEntry;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinLootEntry;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public class Utils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(com.yanny.ali.Utils.MOD_ID);

    public static IDataNode getEntry(IServerUtils utils, LootEntry entry, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions, @Nullable RangeValue preservedCount) {
        MixinLootEntry mixinLootEntry = (MixinLootEntry) entry;
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), mixinLootEntry.getPostModifications().stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), mixinLootEntry.getConditions().stream()).toList();
        LootEntry.Generator generator = mixinLootEntry.getGenerator();
        int weight = mixinLootEntry.getWeight();

        if (generator instanceof LootEntry.ItemGenerator itemGenerator) {
            return new ItemStackNode(utils, itemGenerator.item(), (float) weight / sumWeight, allFunctions, allConditions, preservedCount);
        } else if (generator instanceof LootEntry.VanillaWrappedLootEntry lootEntry) {
            LootPoolEntryContainer entryContainer = lootEntry.entry();

            if (preservedCount != null) {
                if (entryContainer instanceof LootItem lootItem) {
                    return new ItemStackNode(utils, lootItem.item.getDefaultInstance(), NodeUtils.getChance(lootItem, 1, sumWeight),
                            NodeUtils.getAllFunctions(lootItem, allFunctions), NodeUtils.getAllConditions(lootItem, allConditions), preservedCount);
                } else if (entryContainer instanceof TagEntry tagEntry) {
                    return new ItemTagNode(utils, tagEntry.tag, NodeUtils.getChance(tagEntry, 1, sumWeight),
                            NodeUtils.getAllFunctions(tagEntry, allFunctions), NodeUtils.getAllConditions(tagEntry, allConditions), preservedCount);
                }
            }

            return utils.getEntryFactory(utils, entryContainer).create(utils, entryContainer, 1, sumWeight, allFunctions, allConditions);
        } else if (generator instanceof LootEntry.RandomIngredientGenerator ingredientGenerator) {
            Ingredient ingredient = ingredientGenerator.ingredient();

            if (ingredient.values.length > 0) {
                Ingredient.Value value = ingredient.values[0];

                if (value instanceof Ingredient.ItemValue itemValue) {
                    return new ItemStackNode(utils, itemValue.item, (float) weight / sumWeight, allFunctions, allConditions, preservedCount);
                } else if (value instanceof Ingredient.TagValue tagValue) {
                    return new ItemTagNode(utils, tagValue.tag, (float) weight / sumWeight, allFunctions, allConditions, preservedCount);
                } else {
                    LOGGER.warn("Unexpected ingredient type {}", value.getClass().getCanonicalName());
                    return getMissingNode(utils, value);
                }
            } else {
                LOGGER.warn("Ingredient of {} has no entries", generator.getClass().getCanonicalName());
                return getMissingNode(utils, ingredient);
            }
        } else {
            LOGGER.warn("Unexpected generator type {}", generator.getClass().getCanonicalName());
            return getMissingNode(utils, generator);
        }
    }

    @NotNull
    private static IDataNode getMissingNode(IServerUtils utils, Object value) {
        return new MissingNode(MissingTooltipUtils.getMissingValueTooltip(utils, value).build());
    }
}
