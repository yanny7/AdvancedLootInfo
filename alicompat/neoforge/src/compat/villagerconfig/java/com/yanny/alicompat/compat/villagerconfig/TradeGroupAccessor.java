package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.TradeGroupNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import me.drex.villagerconfig.common.data.BehaviorTrade;
import me.drex.villagerconfig.common.data.TradeGroup;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TradeGroupAccessor extends BaseAccessor<TradeGroup> {
    @FieldAccessor
    private Holder<ContextIntProvider> numToSelect;
    @FieldAccessor
    private List<BehaviorTrade> trades;

    public TradeGroupAccessor(TradeGroup parent) {
        super(parent);
    }

    @NotNull
    public NumberExpr getSelectionCount(IServerUtils utils) {
        return NumberExpr.min(utils.convertContextInt(utils, numToSelect, new ArrayList<>()), NumberExpr.constant(trades.size()));
    }

    @NotNull
    public List<IDataNode> getTrades(IServerUtils utils) {
        return trades.stream().map((t) -> ReflectionUtils.copyClassData(BehaviorTradeAccessor.class, t, BehaviorTrade.class).getNode(utils)).toList();
    }

    @NotNull
    public IDataNode getNode(IServerUtils utils) {
        return new TradeGroupNode(utils.convertContextInt(utils, numToSelect, new ArrayList<>()), getTrades(utils));
    }
}
