package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getResourceKeyTooltip;

public class EmeraldsForVillagerTypeItemNode extends SubTradesNode<VillagerTrades.EmeraldsForVillagerTypeItem> {
    public EmeraldsForVillagerTypeItemNode(IServerUtils utils, VillagerTrades.EmeraldsForVillagerTypeItem listing, List<ITooltipNode> conditions) {
        super(utils, listing, conditions);
    }

    @Override
    public List<IDataNode> getSubTrades(IServerUtils utils, VillagerTrades.EmeraldsForVillagerTypeItem listing) {
        List<IDataNode> nodes = new ArrayList<>();

        for (Map.Entry<ResourceKey<VillagerType>, Item> entry : listing.trades.entrySet()) {
            ResourceKey<VillagerType> type = entry.getKey();
            Item item = entry.getValue();
            List<ITooltipNode> cond = new ArrayList<>();

            cond.add(getResourceKeyTooltip(utils, "ali.property.value.villager_type", type));
            nodes.add(utils.getItemListing(utils, new VillagerTrades.ItemsForEmeralds(new ItemStack(item), listing.cost, 1, listing.maxUses, listing.villagerXp, 0.05F), cond));
        }

        return nodes;
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EmeraldsForVillagerTypeItem listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                listing.trades.values().stream().toList()
        );
    }
}
