package com.yanny.alicompat.compat.villagerconfig;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.TradeGroupNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import me.drex.villagerconfig.data.BehaviorTrade;
import me.drex.villagerconfig.data.TradeGroup;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TradeGroupAccessor extends BaseAccessor<TradeGroup> {
    @FieldAccessor
    private NumberProvider numToSelect;
    @FieldAccessor
    private List<BehaviorTrade> trades;

    public TradeGroupAccessor(TradeGroup parent) {
        super(parent);
    }

    @NotNull
    public NumberExpr getSelectionCount(IServerUtils utils) {
        return NumberExpr.min(utils.convertIntNumber(utils, numToSelect, new ArrayList<>()), NumberExpr.constant(trades.size()));
    }

    @NotNull
    public List<IDataNode> getTrades(IServerUtils utils) {
        return trades.stream().map((t) -> utils.getItemListing(utils, t, TooltipNode.empty())).toList();
    }

    @NotNull
    public IDataNode getNode(IServerUtils utils) {
        return new TradeGroupNode(utils.convertIntNumber(utils, numToSelect, new ArrayList<>()), getTrades(utils));
    }
}
