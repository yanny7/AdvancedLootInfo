package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonWeightedPoolTradeOffer$Entry")
public class WeightedPoolEntryAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    private int weight;

    @FieldAccessor
    private TradeFactory factory;

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
    public TradeFactory getFactory() {
        return factory;
    }
}
