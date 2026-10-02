package com.yanny.alicompat.compat.villagertradingplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellEnchantedToolTradeOffer$Factory")
public class SellEnchantedToolTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final int MIN_LEVELS = 5;
    private static final int MAX_LEVELS = 19;
    private static final int MAX_COST = 64;

    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private ItemStack currency;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellEnchantedToolTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode tooltip = TooltipBuilder.branch((b) -> {
            b.add(utils.getValueTooltip(utils, NumberExpr.uniformInt(MIN_LEVELS, MAX_LEVELS)).build(Lang.Value.LEVELS));
            b.add(utils.getValueTooltip(utils, false).build(Lang.Value.TREASURE));
        }).build(Lang.Functions.ENCHANT_WITH_LEVELS);

        return new ItemsToItemsNode(
                utils,
                Either.left(new ItemStack(currency.getItem())),
                NumberExpr.min(NumberExpr.add(NumberExpr.constant(currency.getCount()), NumberExpr.uniformInt(MIN_LEVELS, MAX_LEVELS)), NumberExpr.constant(MAX_COST)),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(new ItemStack(sell.getItem())),
                NumberExpr.constant(1),
                tooltip,
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
