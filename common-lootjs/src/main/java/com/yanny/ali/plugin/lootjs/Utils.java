package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.LootEntry;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.mixin.MixinLootEntry;
import com.yanny.ali.plugin.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.lootjs.node.ItemTagNode;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Utils {
    @NotNull
    public static <T> List<T> getCapturedInstances(Predicate<?> predicate, Class<T> requiredType) {
        List<T> instances = new ArrayList<>();

        try {
            Field[] fields = predicate.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object entry = field.get(predicate);

                if (requiredType.isInstance(entry)) {
                    instances.add(requiredType.cast(entry));
                }
            }
        } catch (IllegalAccessException e) {
            System.err.println("Error while accessing field: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error while accessing field: " + e.getMessage());
        }

        return instances;
    }

    public static IDataNode getEntry(IServerUtils utils, LootEntry entry, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        MixinLootEntry mixinLootEntry = (MixinLootEntry) entry;
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), mixinLootEntry.getPostModifications().stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), mixinLootEntry.getConditions().stream()).toList();
        LootEntry.Generator generator = mixinLootEntry.getGenerator();
        int weight = mixinLootEntry.getWeight();

        if (generator instanceof LootEntry.ItemGenerator itemGenerator) {
            return new ItemStackNode(utils, itemGenerator.item(), (float) weight / sumWeight, allFunctions, allConditions);
        } else if (generator instanceof LootEntry.VanillaWrappedLootEntry lootEntry) {
            LootPoolEntryContainer entryContainer = lootEntry.entry();
            return utils.getEntryFactory(utils, entryContainer).create(utils, entryContainer, 1, sumWeight, allFunctions, allConditions);
        } else if (generator instanceof LootEntry.RandomIngredientGenerator ingredientGenerator) {
            Ingredient ingredient = ingredientGenerator.ingredient();

            if (ingredient.values.length > 0) {
                Ingredient.Value value = ingredient.values[0];

                if (value instanceof Ingredient.ItemValue itemValue) {
                    return new ItemStackNode(utils, itemValue.item, (float) weight / sumWeight, allFunctions, allConditions);
                } else if (value instanceof Ingredient.TagValue tagValue) {
                    return new ItemTagNode(utils, tagValue.tag, (float) weight / sumWeight, allFunctions, allConditions);
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
