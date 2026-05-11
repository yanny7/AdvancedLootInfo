package com.yanny.ali.api;

import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Map;

public interface IServerUtils extends ICoreServerUtils<IServerUtils>, ICommonUtils {
    @NotNull
    <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry);

    @NotNull
    <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function);

    @NotNull
    <T extends LootPoolEntryContainer> IServerRegistry.EntryFactory<T> getEntryFactory(IServerUtils utils, T type);

    @NotNull
    <T extends LootItemFunction> TooltipBuilder getFunctionTooltip(IServerUtils utils, T function);

    @NotNull
    <T extends LootItemCondition> TooltipBuilder getConditionTooltip(IServerUtils utils, T condition);

    @NotNull
    <T extends Ingredient> TooltipBuilder getIngredientTooltip(IServerUtils utils, T ingredient);

    <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Enchantment, Map<Integer, RangeValue>> count);

    <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Enchantment, Map<Integer, RangeValue>> chance);

    @NotNull
    <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack);

    @NotNull
    <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, TooltipNode condition);

    @NotNull
    <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry);

    @NotNull
    RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider);

    @Nullable
    LootContext getLootContext();

    @Nullable
    LootTable getLootTable(ResourceLocation location);

    @NotNull
    List<LootPool> getLootPools(LootTable lootTable);
}
