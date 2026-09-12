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
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import svenhjol.charmony.helper.GenericTradeOffers;

public class ItemsForItemsAccessor extends BaseAccessor<GenericTradeOffers.ItemsForItems> implements IItemListing {
    @FieldAccessor
    private ItemLike inputItem;
    @FieldAccessor
    private ItemLike outputItem;
    @FieldAccessor
    private int baseInput;
    @FieldAccessor
    private int extraInput;
    @FieldAccessor
    private int baseOutput;
    @FieldAccessor
    private int extraOutput;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public ItemsForItemsAccessor(GenericTradeOffers.ItemsForItems parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(inputItem.asItem().getDefaultInstance()),
                new RangeValue(baseInput, baseInput + extraInput),
                Either.left(outputItem.asItem().getDefaultInstance()),
                new RangeValue(baseOutput, baseOutput + extraOutput),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }
}
