package com.yanny.alicompat.compat.goblintraders;

import com.mojang.datafixers.util.Either;
import com.mrcrayfish.goblintraders.trades.TradeCost;
import com.mrcrayfish.goblintraders.trades.type.BasicTrade;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class BasicTradeAccessor extends BaseAccessor<BasicTrade> implements IItemListing {
    @FieldAccessor
    private ItemStack offerStack;
    @FieldAccessor
    private TradeCost primaryPayment;
    @FieldAccessor
    private Optional<TradeCost> secondaryPayment;
    @FieldAccessor
    private float priceMultiplier;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private int experience;

    public BasicTradeAccessor(BasicTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(GoblinTradeUtils.getStack(primaryPayment)),
                GoblinTradeUtils.getCount(primaryPayment),
                Either.left(GoblinTradeUtils.getStack(secondaryPayment)),
                GoblinTradeUtils.getCount(secondaryPayment),
                Either.left(offerStack),
                new RangeValue(offerStack.getCount()),
                maxTrades,
                experience,
                priceMultiplier,
                conditions
        );
    }
}
