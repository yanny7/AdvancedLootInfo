package com.yanny.alicompat.compat.charm;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.CharmTags;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

public class EmeraldsForFlowersAccessor extends BaseAccessor<BeekeeperTradeOffers.EmeraldsForFlowers> implements IItemListing {
    @FieldAccessor
    private int baseCost;
    @FieldAccessor
    private int extraCost;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public EmeraldsForFlowersAccessor(BeekeeperTradeOffers.EmeraldsForFlowers parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.right(CharmTags.BEEKEEPER_SELLS_FLOWERS),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
