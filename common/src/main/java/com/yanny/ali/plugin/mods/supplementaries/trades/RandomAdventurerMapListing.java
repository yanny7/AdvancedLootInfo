package com.yanny.ali.plugin.mods.supplementaries.trades;

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

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.entities.trades.RandomAdventurerMapListing")
public class RandomAdventurerMapListing implements IItemListing {
    @FieldAccessor("emerald")
    private Item emerald;
    @FieldAccessor("priceMin")
    private int priceMin;
    @FieldAccessor("priceMax")
    private int priceMax;
    @FieldAccessor("priceSecondary")
    private ItemStack priceSecondary;
    @FieldAccessor("maxTrades")
    private int maxTrades;
    @FieldAccessor("priceMult")
    private float priceMult;

    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(emerald.getDefaultInstance()),
                new RangeValue(priceMax, priceMax + priceMin),
                Either.left(priceSecondary),
                new RangeValue(),
                Either.left(Items.MAP.getDefaultInstance()),
                new RangeValue(),
                maxTrades,
                (int)((6 * 12) / (float) maxTrades),
                priceMult,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(emerald, priceSecondary.getItem()), List.of(Items.MAP));
    }
}
