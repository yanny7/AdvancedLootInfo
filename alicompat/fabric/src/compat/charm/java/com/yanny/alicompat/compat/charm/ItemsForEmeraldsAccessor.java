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
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import svenhjol.charmony.helper.GenericTradeOffers;

public class ItemsForEmeraldsAccessor extends BaseAccessor<GenericTradeOffers.ItemsForEmeralds> implements IItemListing {
    @FieldAccessor
    private ItemLike itemLike;
    @FieldAccessor
    private int baseItems;
    @FieldAccessor
    private int extraItems;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public ItemsForEmeraldsAccessor(GenericTradeOffers.ItemsForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                Either.left(itemLike.asItem().getDefaultInstance()),
                new RangeValue(baseItems, baseItems + extraItems),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }
}
