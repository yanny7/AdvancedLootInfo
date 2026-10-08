package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import me.drex.villagerconfig.data.TradeGroup;
import me.drex.villagerconfig.data.TradeTier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TradeTierAccessor extends BaseAccessor<TradeTier> {
    @FieldAccessor
    private List<TradeGroup> groups;

    public TradeTierAccessor(TradeTier parent) {
        super(parent);
    }

    @NotNull
    public TradeLevel getLevel(IServerUtils utils) {
        List<TradeGroupAccessor> accessors = groups.stream().map((g) -> ReflectionUtils.copyClassData(TradeGroupAccessor.class, g, TradeGroup.class)).toList();
        NumberExpr offers = accessors.stream().map((a) -> a.getSelectionCount(utils)).reduce(NumberExpr.constant(0), NumberExpr::add);

        if (accessors.size() == 1) {
            return new TradeLevel.OfTrades(new TradeLevelInfo(offers), accessors.get(0)::getTrades);
        }

        return new TradeLevel.OfTrades(new TradeLevelInfo(offers), (u) -> accessors.stream().map((a) -> a.getNode(u)).toList());
    }
}
