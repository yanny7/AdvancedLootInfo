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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import svenhjol.charmony.helper.GenericTradeOffers;

public class EmeraldsForTwoTagsAccessor extends BaseAccessor<GenericTradeOffers.EmeraldsForTwoTags<?, ?>> implements IItemListing {
    @FieldAccessor
    private TagKey<? extends ItemLike> tag1;
    @FieldAccessor
    private TagKey<? extends ItemLike> tag2;
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

    public EmeraldsForTwoTagsAccessor(GenericTradeOffers.EmeraldsForTwoTags<?, ?> parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.right(tag1),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.right(tag2),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }
}
