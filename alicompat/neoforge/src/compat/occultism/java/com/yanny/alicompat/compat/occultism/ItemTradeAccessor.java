package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.common.entity.spirit.wonderingtrader.WonderingTrades;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.world.item.ItemStack;

public class ItemTradeAccessor extends BaseAccessor<WonderingTrades.ItemTrade> {
    @FieldAccessor
    private ItemStack input;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private ItemStack result;

    public ItemTradeAccessor(WonderingTrades.ItemTrade parent) {
        super(parent);
    }

    public IDataNode getNode(IServerUtils utils, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(input.copyWithCount(1)),
                new RangeValue(input.getCount()),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(result.copyWithCount(1)),
                new RangeValue(result.getCount()),
                TooltipNode.empty(),
                new RangeValue(maxUses),
                new RangeValue(villagerXp),
                condition
        );
    }
}
