package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$GroupedListing")
public class GroupedListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private VillagerTrades.ItemListing[] listings;

    public GroupedListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        return new SubTradesNode<>(utils, this, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils ignoredUtils, GroupedListing ignoredListing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (VillagerTrades.ItemListing listing : listings) {
                    nodes.add(utils.getItemListing(utils, listing, EmptyTooltipNode.EMPTY));
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();

        for (VillagerTrades.ItemListing listing : listings) {
            Pair<List<Item>, List<Item>> pair = utils.collectItems(utils, listing);

            inputs.addAll(pair.getA());
            outputs.addAll(pair.getB());
        }

        return new Pair<>(inputs, outputs);
    }
}
