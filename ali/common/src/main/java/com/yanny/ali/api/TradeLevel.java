package com.yanny.ali.api;

import net.minecraft.world.entity.npc.villager.VillagerTrades;

import java.util.List;
import java.util.function.Function;

public sealed interface TradeLevel {
    record OfListings(VillagerTrades.ItemListing[] listings, TradeLevelInfo levelInfo) implements TradeLevel {}

    record OfTrades(TradeLevelInfo levelInfo, Function<IServerUtils, List<IDataNode>> trades) implements TradeLevel {}
}
