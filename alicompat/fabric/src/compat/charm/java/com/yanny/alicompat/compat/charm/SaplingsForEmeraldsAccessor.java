package com.yanny.alicompat.compat.charm;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;

import java.util.List;

public class SaplingsForEmeraldsAccessor extends BaseAccessor<LumberjackTradeOffers.SaplingsForEmeralds> implements IItemListing {
    @FieldAccessor
    private List<Item> saplings;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public SaplingsForEmeraldsAccessor(LumberjackTradeOffers.SaplingsForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(Items.EMERALD.getDefaultInstance()), NumberExpr.uniformInt(baseEmeralds, baseEmeralds + extraEmeralds), TooltipNode.empty()),
                TradeUtils.getEmptySlotNode(),
                TradeUtils.getItemSlotNode(saplings.stream().map(Item::getDefaultInstance).toList(), NumberExpr.constant(1), TooltipNode.empty()),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
