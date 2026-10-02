package com.yanny.alicompat.compat.bumblezone;

import com.mojang.datafixers.util.Either;
import com.telepathicgrunt.the_bumblezone.entities.BasicItemTrade;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class BasicItemTradeAccessor extends BaseAccessor<BasicItemTrade> implements IItemListing {
    @FieldAccessor
    private Item itemToTrade;
    @FieldAccessor
    private Item itemToReceive;
    @FieldAccessor
    private int amountToGive;
    @FieldAccessor
    private int amountToReceive;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int experience;
    @FieldAccessor
    private float multiplier;

    public BasicItemTradeAccessor(BasicItemTrade parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(itemToTrade.getDefaultInstance()),
                NumberExpr.constant(amountToGive),
                Either.left(itemToReceive.getDefaultInstance()),
                NumberExpr.constant(amountToReceive),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
