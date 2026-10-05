package com.yanny.alicompat.compat.undergarden;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import quek.undergarden.entity.stoneborn.trading.SellForRegaliumTrade;
import quek.undergarden.registry.UGItems;

public class SellForRegaliumTradeAccessor extends BaseAccessor<SellForRegaliumTrade> implements IItemListing {
    private static final int XP = 0;
    private static final float PRICE_MULTIPLIER = 0F;

    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private int regaliumCount;

    @FieldAccessor
    private int sellCount;

    @FieldAccessor
    private int maxUses;

    public SellForRegaliumTradeAccessor(SellForRegaliumTrade parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(new ItemStack(sell.getItem())),
                NumberExpr.constant(sellCount),
                Either.left(UGItems.REGALIUM_CRYSTAL.get().getDefaultInstance()),
                NumberExpr.constant(regaliumCount),
                maxUses,
                XP,
                PRICE_MULTIPLIER,
                conditions
        );
    }
}
