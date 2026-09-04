package com.yanny.alicompat.compat.farlanders;

import com.legacy.farlanders.entity.util.FarlanderTrades;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.ItemStack;

public class TradeAccessor extends BaseAccessor<FarlanderTrades.Trade> implements IItemListing {
    @FieldAccessor
    private ItemStack itemGiven1;

    @FieldAccessor
    private int itemGiven1Count;

    @FieldAccessor
    private ItemStack itemGiven2;

    @FieldAccessor
    private int itemGiven2Count;

    @FieldAccessor
    private ItemStack itemSold;

    @FieldAccessor
    private int soldItemCount;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int givenXP;

    @FieldAccessor
    private float priceMultiplier;

    public TradeAccessor(FarlanderTrades.Trade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(itemGiven1),
                new RangeValue(itemGiven1Count),
                Either.left(itemGiven2 != null ? itemGiven2 : ItemStack.EMPTY),
                new RangeValue(itemGiven2Count),
                Either.left(itemSold),
                new RangeValue(soldItemCount),
                maxUses,
                givenXP,
                priceMultiplier,
                conditions
        );
    }
}
