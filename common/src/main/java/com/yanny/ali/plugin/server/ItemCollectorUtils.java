package com.yanny.ali.plugin.server;

import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.SetItemFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ItemCollectorUtils {
    @NotNull
    public static List<Item> collectLootTable(IServerUtils utils, LootTable lootTable) {
        List<Item> result = new LinkedList<>();

        for (LootPool pool : lootTable.pools) {
            result.addAll(collectLootPool(utils, pool));
        }

        result.addAll(lootTable.functions.stream().map((f) -> utils.collectItems(utils, List.copyOf(result), f)).flatMap(Collection::stream).toList());

        return result;
    }

    @NotNull
    public static List<Item> collectLootPool(IServerUtils utils, LootPool pool) {
        List<Item> result = new LinkedList<>();

        for (LootPoolEntryContainer entry : pool.entries) {
            result.addAll(utils.collectItems(utils, entry));
        }

        result.addAll(pool.conditions.stream().map((c) -> utils.collectItems(utils, List.copyOf(result), c)).flatMap(Collection::stream).toList());
        result.addAll(pool.functions.stream().map((f) -> utils.collectItems(utils, List.copyOf(result), f)).flatMap(Collection::stream).toList());

        return result;
    }
    
    @Unmodifiable
    @NotNull
    public static List<Item> collectItems(IServerUtils utils, LootItem entry) {
        LinkedList<Item> result = new LinkedList<>(List.of(entry.item.value()));

        result.addAll(entry.functions.stream().map((f) -> utils.collectItems(utils, List.copyOf(result), f)).flatMap(Collection::stream).toList());

        return result;
    }

    @Unmodifiable
    @NotNull
    public static List<Item> collectTags(IServerUtils utils, TagEntry entry) {
        LinkedList<Item> result = new LinkedList<>(BuiltInRegistries.ITEM.get(entry.tag).map((tag) -> tag.stream().map(Holder::value).toList()).orElse(List.of()));

        result.addAll(entry.functions.stream().map((f) -> utils.collectItems(utils, List.copyOf(result), f)).flatMap(Collection::stream).toList());

        return result;
    }

    @NotNull
    public static List<Item> collectComposite(IServerUtils utils, CompositeEntryBase entry) {
        List<Item> result = new LinkedList<>();

        for (LootPoolEntryContainer child : entry.children) {
            result.addAll(utils.collectItems(utils, child));
        }

        return result;
    }

    @Unmodifiable
    @NotNull
    public static List<Item> collectSingleton(IServerUtils utils, LootPoolSingletonContainer entry) {
        return entry.functions.stream().map((f) -> utils.collectItems(utils, List.of(), f)).flatMap(Collection::stream).toList();
    }

    @NotNull
    public static List<Item> collectReference(IServerUtils utils, NestedLootTable entry) {
        List<Item> result = new LinkedList<>();
        LootTable lootTable = utils.getLootTable(entry.contents);

        if (lootTable != null) {
            result.addAll(collectLootTable(utils, lootTable));
            result.addAll(entry.functions.stream().map((f) -> utils.collectItems(utils, List.copyOf(result), f)).flatMap(Collection::stream).toList());
        }

        return result;
    }

    @NotNull
    public static List<Item> collectFurnaceSmelt(IServerUtils utils, List<Item> items, SmeltItemFunction function) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            return items.stream().map((i) -> level.recipeAccess()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(i.getDefaultInstance()), level)
                    .map((l) -> List.of(l.value().result().getItem())).orElse(List.of())).flatMap(Collection::stream).toList();
        }

        return List.of();
    }

    @Unmodifiable
    @NotNull
    public static List<Item> collectSetItem(IServerUtils utils, List<Item> items, SetItemFunction function) {
        return List.of(function.item.value());
    }
}
