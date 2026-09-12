package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;

@ClassAccessor("net.mehvahdjukaar.moonlight.api.trades.ItemListingRegistry$SpecialListing")
public class SpecialListingAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private VillagerTrades.ItemListing listing;

    public SpecialListingAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return utils.getItemListing(utils, listing, conditions);
    }
}
