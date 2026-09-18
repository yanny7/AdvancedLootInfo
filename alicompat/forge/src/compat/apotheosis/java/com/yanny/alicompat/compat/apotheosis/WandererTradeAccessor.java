package com.yanny.alicompat.compat.apotheosis;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import dev.shadowsoffire.apotheosis.village.wanderer.WandererTrade;
import net.minecraft.world.item.ItemStack;

public class WandererTradeAccessor extends BaseAccessor<WandererTrade> implements IItemListing {
    @FieldAccessor
    private ItemStack price;

    @FieldAccessor
    private ItemStack price2;

    @FieldAccessor
    private ItemStack forSale;

    @FieldAccessor
    private int maxTrades;

    @FieldAccessor
    private int xp;

    @FieldAccessor
    private float priceMult;

    public WandererTradeAccessor(WandererTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(price),
                new RangeValue(price.getCount()),
                Either.left(price2),
                new RangeValue(price2.getCount()),
                Either.left(forSale),
                new RangeValue(forSale.getCount()),
                maxTrades,
                xp,
                priceMult,
                conditions
        );
    }
}
