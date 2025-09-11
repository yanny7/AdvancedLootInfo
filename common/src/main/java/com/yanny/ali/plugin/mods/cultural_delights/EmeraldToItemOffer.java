package com.yanny.ali.plugin.mods.cultural_delights;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("dev.sterner.culturaldelights.CulturalDelights$EmeraldToItemOffer")
public class EmeraldToItemOffer implements IItemListing {
    @FieldAccessor("sell")
    private ItemStack sell;
    @FieldAccessor("price")
    private int price;
    @FieldAccessor("maxUses")
    private int maxUses;
    @FieldAccessor("experience")
    private int experience;
    @FieldAccessor("multiplier")
    private float multiplier;

    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(price, price + 3),
                Either.left(sell),
                new RangeValue(sell.getCount()),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), List.of(sell.getItem()));
    }
}
