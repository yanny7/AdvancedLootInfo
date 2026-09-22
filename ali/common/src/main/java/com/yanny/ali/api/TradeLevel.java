package com.yanny.ali.api;

import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.item.trading.TradeSet;

public record TradeLevel(TradeSet tradeSet, float chance, TooltipNode details) {
    public TradeLevel(TradeSet tradeSet) {
        this(tradeSet, 1.0F, TooltipNode.empty());
    }
}
