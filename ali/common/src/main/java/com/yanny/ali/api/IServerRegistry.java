package com.yanny.ali.api;

import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.plugin.server.EnchantedRanges;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public interface IServerRegistry extends ICoreServerRegistry<IServerUtils> {
    <T extends LootPoolEntryContainer> void registerItemCollector(Class<T> type, BiFunction<IServerUtils, T, List<Item>> itemSupplier);

    <T extends LootItemFunction> void registerItemCollector(Class<T> type, TriFunction<IServerUtils, List<Item>, T, List<Item>> itemSupplier);

    <T extends LootPoolEntryContainer> void registerEntry(Class<T> type, EntryFactory<T> entryFactory);

    <T extends LootPoolEntryContainer> void registerEntryTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends LootItemFunction> void registerFunctionTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends LootItemCondition> void registerConditionTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends Ingredient> void registerIngredientTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends NumberProvider> void registerNumberProvider(Class<T> type, BiFunction<IServerUtils, T, RangeValue> converter);

    <T extends LootItemFunction> void registerCountModifier(Class<T> type, TriConsumer<IServerUtils, T, EnchantedRanges> consumer);

    <T extends LootItemCondition> void registerChanceModifier(Class<T> type, TriConsumer<IServerUtils, T, EnchantedRanges> consumer);

    <T extends LootItemFunction> void registerItemStackModifier(Class<T> type, TriFunction<IServerUtils, T, ItemStack, ItemStack> consumer);

    void registerLootModifiers(Function<IServerUtils, List<ILootModifier<?>>> getter);

    <T extends VillagerTrades.ItemListing> void registerItemListing(Class<T> type, TriFunction<IServerUtils, T, TooltipNode, IDataNode> supplier);

    void registerTrades(ResourceLocation traderId, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo);

    /**
     * @deprecated use {@link #registerEnumTranslation(Class, String, String)}
     */
    @Deprecated(forRemoval = true, since = "2.2.0")
    void registerEnumTranslation(Class<? extends Enum<?>> type, String owner);

    void registerEnumTranslation(Class<? extends Enum<?>> type, String modId, String owner);

    @FunctionalInterface
    interface EntryFactory<T extends LootPoolEntryContainer> {
        IDataNode create(IServerUtils utils, T entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions);
    }
}
