package com.yanny.alicompat.compat.villagertradingplus;

import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonWeightedPoolTradeOffer$Entry")
public class WeightedPoolEntryAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    private int weight;

    @FieldAccessor
    private VillagerTrades.ItemListing factory;

    public WeightedPoolEntryAccessor(Object parent) {
        super(parent);
    }

    @NotNull
    public static WeightedPoolEntryAccessor of(Object entry) {
        return ReflectionUtils.copyClassData(WeightedPoolEntryAccessor.class, entry);
    }

    public int getWeight() {
        return weight;
    }

    @NotNull
    public VillagerTrades.ItemListing getFactory() {
        return factory;
    }
}
