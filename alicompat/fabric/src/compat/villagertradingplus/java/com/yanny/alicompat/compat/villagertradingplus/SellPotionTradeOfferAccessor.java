package com.yanny.alicompat.compat.villagertradingplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellPotionTradeOffer$Factory")
public class SellPotionTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack buy;

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

    public SellPotionTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(PotionUtils.setPotion(buy.copy(), Potions.WATER)),
                new RangeValue(buy.getCount()),
                TooltipNode.empty(),
                Either.left(currency),
                new RangeValue(currency.getCount()),
                TooltipNode.empty(),
                Either.left(new ItemStack(sell.getItem())),
                new RangeValue(1),
                TooltipBuilder.keyOnly(VillagerTradingPlusLang.Functions.RANDOM_POTION).build(),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
