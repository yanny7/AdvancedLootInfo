package com.yanny.ali.plugin.mods.supplementaries.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ClassAccessor("net.mehvahdjukaar.supplementaries.common.entities.trades.RandomAdventurerMapListing")
public class RandomAdventurerMapListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private Item emerald;
    @FieldAccessor
    private int priceMin;
    @FieldAccessor
    private int priceMax;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @FieldAccessor
    private Optional<ItemCost> priceSecondary;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private float priceMult;

    public RandomAdventurerMapListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.adventure"));
        return new ItemsToItemsNode(
                utils,
                Either.left(emerald.getDefaultInstance()),
                new RangeValue(priceMax, priceMax + priceMin),
                Either.left(priceSecondary.map((u) -> u.item().value().getDefaultInstance()).orElse(ItemStack.EMPTY)),
                new RangeValue(priceSecondary.map(ItemCost::count).orElse(1)),
                Either.left(map),
                new RangeValue(),
                maxTrades,
                (int)((6 * 12) / (float) maxTrades),
                priceMult,
                condition
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        List<Item> input = new ArrayList<>();

        input.add(emerald);
        priceSecondary.ifPresent((i) -> input.add(i.item().value()));

        return new Pair<>(input, List.of(Items.MAP));
    }
}
