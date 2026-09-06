package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.SimpleTrade;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;

public class SimpleTradeAccessor extends BaseAccessor<SimpleTrade> implements IItemListing {
    @FieldAccessor
    private TradeItem firstInput;

    @FieldAccessor
    private TradeItem secondInput;

    @FieldAccessor
    private TradeItem output;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int villagerExperience;

    @FieldAccessor
    private float priceMultiplier;

    public SimpleTradeAccessor(SimpleTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TradeItemAccessor first = TradeItemAccessor.of(firstInput);
        TradeItemAccessor second = TradeItemAccessor.of(secondInput);
        TradeItemAccessor result = TradeItemAccessor.of(output);

        return new ItemsToItemsNode(
                utils,
                Either.left(first.getStack()),
                first.getCount(),
                Either.left(second.getStack()),
                second.getCount(),
                Either.left(result.getStack()),
                result.getCount(),
                maxUses,
                villagerExperience,
                priceMultiplier,
                conditions
        );
    }
}
