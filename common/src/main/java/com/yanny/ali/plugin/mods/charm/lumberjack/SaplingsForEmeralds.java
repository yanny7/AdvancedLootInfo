package com.yanny.ali.plugin.mods.charm.lumberjack;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClassAccessor("svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers$SaplingsForEmeralds")
public class SaplingsForEmeralds implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private List<Item> saplings;

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new SubTradesNode<>(utils, this, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, SaplingsForEmeralds listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Item sapling : saplings) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(Items.EMERALD.getDefaultInstance()),
                            new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                            Either.left(sapling.getDefaultInstance()),
                            new RangeValue(),
                            maxUses,
                            villagerXp,
                            0.2F,
                            Collections.emptyList()
                    ));
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), saplings);
    }
}
