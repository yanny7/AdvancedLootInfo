package com.yanny.alicompat.compat.occultism;

import com.yanny.aci.language.ITooltipKey;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public record ConditionalListing(VillagerTrades.ItemListing listing, ITooltipKey condition) implements VillagerTrades.ItemListing, IItemListing {
    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return listing.getOffer(trader, random);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return utils.getItemListing(utils, listing, TooltipBuilder.keyOnly(condition).build());
    }
}
