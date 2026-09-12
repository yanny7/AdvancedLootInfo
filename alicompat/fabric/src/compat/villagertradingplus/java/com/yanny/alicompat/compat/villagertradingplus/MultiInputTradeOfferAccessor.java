package com.yanny.alicompat.compat.villagertradingplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
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
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonMultiInputTradeOffer$Factory")
public class MultiInputTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
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

    public MultiInputTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(inputA),
                new RangeValue(inputA.getCount()),
                Either.left(inputB),
                new RangeValue(inputB.getCount()),
                Either.left(sell),
                new RangeValue(sell.getCount()),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
