package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonMultiInputTradeOffer$Factory")
public class MultiInputTradeOfferAccessor extends BaseAccessor<TradeFactory> implements ITradeFactoryAccessor {
    @FieldAccessor
    private ItemStack inputA;

    @FieldAccessor
    private ItemStack inputB;

    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public MultiInputTradeOfferAccessor(TradeFactory parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return VillagerTradingPlusTrades.getTradeNode(
                utils,
                Either.left(inputA),
                NumberExpr.constant(inputA.getCount()),
                Either.left(inputB),
                NumberExpr.constant(inputB.getCount()),
                Either.left(sell),
                NumberExpr.constant(sell.getCount()),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
