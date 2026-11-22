package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellDyedArmorTradeOffer$Factory")
public class SellDyedArmorTradeOfferAccessor extends BaseAccessor<TradeFactory> implements ITradeFactoryAccessor {
    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private ItemStack currency;

    @FieldAccessor
    private Holder<LootItemFunction> dyes;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellDyedArmorTradeOfferAccessor(TradeFactory parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return VillagerTradingPlusTrades.getTradeNode(
                utils,
                Either.left(currency),
                NumberExpr.constant(currency.getCount()),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(sell),
                NumberExpr.constant(sell.getCount()),
                utils.getValueTooltip(utils, NodeUtils.unwrapFunctions(utils, Optional.of(dyes))).build(),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
