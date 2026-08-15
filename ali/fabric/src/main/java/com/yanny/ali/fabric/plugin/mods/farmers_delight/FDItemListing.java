package com.yanny.ali.fabric.plugin.mods.farmers_delight;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.entity.npc.villager.VillagerTrades;

@ClassAccessor("vectorwing.farmersdelight.common.event.VillagerEvents$FDItemListing")
public class FDItemListing extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private VillagerTrades.ItemListing listing;

    public FDItemListing(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return utils.getItemListing(utils, listing, conditions);
    }
}
