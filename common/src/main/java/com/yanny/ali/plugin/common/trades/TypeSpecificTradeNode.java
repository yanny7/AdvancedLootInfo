package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeSpecificTradeNode extends SubTradesNode<VillagerTrades.TypeSpecificTrade> {
    public TypeSpecificTradeNode(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing, ITooltipNode conditions) {
        super(utils, listing, conditions);
    }

    @Override
    public List<IDataNode> getSubTrades(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing) {
        List<IDataNode> nodes = new ArrayList<>();

        for (Map.Entry<ResourceKey<VillagerType>, VillagerTrades.ItemListing> entry : listing.trades().entrySet()) {
            ResourceKey<VillagerType> type = entry.getKey();
            VillagerTrades.ItemListing item = entry.getValue();
            nodes.add(utils.getItemListing(utils, item, utils.getValueTooltip(utils, type).build("ali.property.value.villager_type")));
        }

        return nodes;
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();

        listing.trades().values().forEach((itemListing) -> {
            Pair<List<Item>, List<Item>> pair = utils.collectItems(utils, itemListing);

            inputs.addAll(pair.getA());
            outputs.addAll(pair.getB());
        });

        return new Pair<>(inputs, outputs);
    }
}
