package com.yanny.alicompat.compat.ribbits;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemsForAmethysts;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public class ItemsForAmethystsAccessor extends BaseAccessor<ItemsForAmethysts> implements VillagerTrades.ItemListing, IItemListing {
    @FieldAccessor
    private ItemStack itemStack;

    @FieldAccessor
    private int amethystCostMin;

    @FieldAccessor
    private int amethystCostMax;

    @FieldAccessor
    private int amountMin;

    @FieldAccessor
    private int amountMax;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private float priceMultiplier;

    public ItemsForAmethystsAccessor(ItemsForAmethysts parent) {
        super(parent);
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return parent.getOffer(trader, random);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.AMETHYST_SHARD.getDefaultInstance()),
                new RangeValue(amethystCostMin, amethystCostMax),
                Either.left(itemStack.getItem().getDefaultInstance()),
                new RangeValue(amountMin, amountMax),
                maxUses,
                0,
                priceMultiplier,
                conditions
        );
    }
}
