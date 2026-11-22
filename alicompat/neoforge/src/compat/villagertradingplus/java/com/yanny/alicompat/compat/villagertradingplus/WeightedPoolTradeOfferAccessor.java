package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.TradeGroupNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonWeightedPoolTradeOffer$Factory")
public class WeightedPoolTradeOfferAccessor extends BaseAccessor<TradeFactory> implements ITradeFactoryAccessor {
    @FieldAccessor
    private List<?> entries;

    @FieldAccessor
    private int totalWeight;

    public WeightedPoolTradeOfferAccessor(TradeFactory parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new TradeGroupNode(NumberExpr.constant(1), entries.stream().map((e) -> getEntryNode(utils, WeightedPoolEntryAccessor.of(e), conditions)).toList());
    }

    @NotNull
    private IDataNode getEntryNode(IServerUtils utils, WeightedPoolEntryAccessor entry, TooltipNode conditions) {
        return VillagerTradingPlusTrades.getNode(utils, entry.getFactory(), TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.percent(NumberExpr.constant(getChance(entry.getWeight()))).build(Lang.Description.CHANCE));
            b.add(conditions);
        }).build());
    }

    private float getChance(int weight) {
        if (totalWeight <= 0) {
            return 0.0f;
        }

        return (float) weight / totalWeight;
    }
}
