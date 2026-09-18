package com.yanny.alicompat.compat.villagertradingplus;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WeightedPoolTradesNode extends SubTradesNode<WeightedPoolTradeOfferAccessor> {
    public WeightedPoolTradesNode(IServerUtils utils, WeightedPoolTradeOfferAccessor listing, TooltipNode conditions) {
        super(utils, listing, conditions);
    }

    @NotNull
    @Override
    public List<IDataNode> getSubTrades(IServerUtils utils, WeightedPoolTradeOfferAccessor listing) {
        List<IDataNode> nodes = new ArrayList<>();

        for (Object e : listing.getEntries()) {
            WeightedPoolEntryAccessor entry = WeightedPoolEntryAccessor.of(e);
            TooltipNode chance = TooltipBuilder.value(new RangeValue(listing.getChance(entry.getWeight()) * 100), "%").build(Lang.Description.CHANCE);

            nodes.add(utils.getItemListing(utils, entry.getFactory(), chance));
        }

        return nodes;
    }
}
