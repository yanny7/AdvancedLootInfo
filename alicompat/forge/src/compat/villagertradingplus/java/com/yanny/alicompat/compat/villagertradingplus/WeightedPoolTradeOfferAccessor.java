package com.yanny.alicompat.compat.villagertradingplus;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonWeightedPoolTradeOffer$Factory")
public class WeightedPoolTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private List<?> entries;

    @FieldAccessor
    private int totalWeight;

    public WeightedPoolTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new WeightedPoolTradesNode(utils, this, conditions);
    }

    @NotNull
    public List<?> getEntries() {
        return entries;
    }

    public float getChance(int weight) {
        if (totalWeight <= 0) {
            return 0.0f;
        }

        return (float) weight / totalWeight;
    }
}
