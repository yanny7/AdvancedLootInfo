package com.yanny.alicompat.compat.immersiveengineering;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$GroupedListing")
public class GroupedListingAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private VillagerTrades.ItemListing[] listings;

    public GroupedListingAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new SubTradesNode<>(utils, listings, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, VillagerTrades.ItemListing[] listing) {
                return Arrays.stream(listing).map((l) -> utils.getItemListing(utils, l, TooltipNode.empty())).toList();
            }
        };
    }
}
