package com.yanny.ali.plugin.mods.charm.generic;

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
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("svenhjol.charmony.helper.GenericTradeOffers$EmeraldsForItems")
public class EmeraldsForItems extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseCost;
    @FieldAccessor
    private int extraCost;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private ItemLike itemLike;

    public EmeraldsForItems(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(itemLike.asItem().getDefaultInstance()),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(itemLike.asItem()), List.of(Items.EMERALD));
    }
}
