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
import com.yungnickyoung.minecraft.ribbits.entity.trade.AmethystForItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public class AmethystForItemsAccessor extends BaseAccessor<AmethystForItems> implements VillagerTrades.ItemListing, IItemListing {
    @FieldAccessor
    private Item item;

    @FieldAccessor
    private int costCountMin;

    @FieldAccessor
    private int costCountMax;

    @FieldAccessor
    private int resultCountMin;

    @FieldAccessor
    private int resultCountMax;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private float priceMultiplier;

    public AmethystForItemsAccessor(AmethystForItems parent) {
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
                Either.left(item.getDefaultInstance()),
                new RangeValue(costCountMin, costCountMax),
                Either.left(Items.AMETHYST_SHARD.getDefaultInstance()),
                new RangeValue(resultCountMin, resultCountMax),
                maxUses,
                0,
                priceMultiplier,
                conditions
        );
    }
}
