package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmeraldsForVillagerTypeItemNode extends SubTradesNode<VillagerTrades.EmeraldsForVillagerTypeItem> {
    public EmeraldsForVillagerTypeItemNode(IServerUtils utils, VillagerTrades.EmeraldsForVillagerTypeItem listing, TooltipNode conditions) {
        super(utils, listing, conditions);
    }

    @Override
    public List<IDataNode> getSubTrades(IServerUtils utils, VillagerTrades.EmeraldsForVillagerTypeItem listing) {
        List<IDataNode> nodes = new ArrayList<>();

        for (Map.Entry<VillagerType, Item> entry : listing.trades.entrySet()) {
            VillagerType type = entry.getKey();
            Item item = entry.getValue();
            TooltipNode cond = utils.getValueTooltip(utils, type.toString()).build(Lang.Value.VILLAGER_TYPE);

            nodes.add(utils.getItemListing(utils, new VillagerTrades.ItemsForEmeralds(new ItemStack(item), listing.cost, 1, listing.maxUses, listing.villagerXp, 0.05F), cond));
        }

        return nodes;
    }
}
