package com.yanny.ali.api;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;

import java.util.List;
import java.util.function.Function;

public sealed interface TradeLevel {
    record OfSet(ResourceKey<TradeSet> tradeSet) implements TradeLevel {}

    record OfTrades(TradeLevelInfo levelInfo, Function<IServerUtils, List<IDataNode>> trades) implements TradeLevel {}

    static Int2ObjectMap<TradeLevel> ofSets(Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel) {
        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();

        tradeSetsByLevel.int2ObjectEntrySet().forEach((e) -> levels.put(e.getIntKey(), new OfSet(e.getValue())));
        return levels;
    }
}
