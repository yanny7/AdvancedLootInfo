package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.SubTradesNode;

import java.util.List;

final class ConditionalTradesNode extends SubTradesNode<List<IDataNode>> {
    ConditionalTradesNode(IServerUtils utils, List<IDataNode> trades, TooltipNode condition) {
        super(utils, trades, condition);
    }

    @Override
    public List<IDataNode> getSubTrades(IServerUtils utils, List<IDataNode> trades) {
        return trades;
    }
}
