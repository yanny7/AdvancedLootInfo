package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.lion.villagertradingplus.tradeoffers.util.Ingredient;
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

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonBuyTaggedItemTradeOffer$Factory")
public class BuyTaggedItemTradeOfferAccessor extends BaseAccessor<TradeFactory> implements ITradeFactoryAccessor {
    @FieldAccessor
    private Ingredient buy;

    @FieldAccessor
    private ItemStack reward;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public BuyTaggedItemTradeOfferAccessor(TradeFactory parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        IngredientAccessor ingredient = IngredientAccessor.of(buy);

        return VillagerTradingPlusTrades.getTradeNode(
                utils,
                ingredient.getItem(),
                ingredient.getCount(),
                Either.left(reward),
                NumberExpr.constant(reward.getCount()),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
