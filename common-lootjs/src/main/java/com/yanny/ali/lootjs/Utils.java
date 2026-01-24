package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.LootEntry;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinLootEntry;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.stream.Stream;

public class Utils {
    public static IDataNode getEntry(IServerUtils utils, LootEntry entry, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions, boolean preserveCount) {
        MixinLootEntry mixinLootEntry = (MixinLootEntry) entry;
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), mixinLootEntry.getPostModifications().stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), mixinLootEntry.getConditions().stream()).toList();
        LootEntry.Generator generator = mixinLootEntry.getGenerator();
        int weight = mixinLootEntry.getWeight();

        if (generator instanceof LootEntry.ItemGenerator itemGenerator) {
            return new ItemStackNode(utils, itemGenerator.item(), (float) weight / sumWeight, allFunctions, allConditions, preserveCount);
        } else if (generator instanceof LootEntry.VanillaWrappedLootEntry lootEntry) {
            LootPoolEntryContainer entryContainer = lootEntry.entry();
            return utils.getEntryFactory(utils, entryContainer).create(utils, entryContainer, 1, sumWeight, allFunctions, allConditions);
        } else if (generator instanceof LootEntry.RandomIngredientGenerator ingredientGenerator) {
            Ingredient ingredient = ingredientGenerator.ingredient();

            if (ingredient.values.length > 0) {
                Ingredient.Value value = ingredient.values[0];

                if (value instanceof Ingredient.ItemValue itemValue) {
                    return new ItemStackNode(utils, itemValue.item, (float) weight / sumWeight, allFunctions, allConditions, preserveCount);
                } else if (value instanceof Ingredient.TagValue tagValue) {
                    return new ItemTagNode(utils, tagValue.tag, (float) weight / sumWeight, allFunctions, allConditions, preserveCount);
                } else {
                    throw new IllegalStateException("Invalid ingredient type: " + value.getClass().getCanonicalName());
                }
            } else {
                throw new IllegalStateException("Ingredient has no entries");
            }
        } else {
            throw new IllegalStateException("Invalid generator type: " + generator.getClass().getCanonicalName());
        }
    }
}
