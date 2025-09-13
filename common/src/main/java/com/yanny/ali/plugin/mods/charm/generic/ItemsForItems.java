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
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("svenhjol.charmony.helper.GenericTradeOffers$ItemsForItems")
public class ItemsForItems extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseInput;
    @FieldAccessor
    private int extraInput;
    @FieldAccessor
    private int baseOutput;
    @FieldAccessor
    private int extraOutput;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private ItemLike inputItem;
    @FieldAccessor
    private ItemLike outputItem;

    public ItemsForItems(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(inputItem.asItem().getDefaultInstance()),
                new RangeValue(baseInput, baseInput + extraInput),
                Either.left(outputItem.asItem().getDefaultInstance()),
                new RangeValue(baseOutput, baseOutput + extraOutput),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(inputItem.asItem()), List.of(outputItem.asItem()));
    }
}
