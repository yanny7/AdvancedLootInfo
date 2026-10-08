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
import svenhjol.charm.feature.beekeepers.common.Trades;

import java.util.List;
import java.util.stream.Stream;

public class TallFlowerForEmeraldsAccessor extends BaseAccessor<Trades.TallFlowerForEmeralds> implements IItemListing {
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public TallFlowerForEmeraldsAccessor(Trades.TallFlowerForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(Items.EMERALD.getDefaultInstance()), NumberExpr.uniformInt(baseEmeralds, baseEmeralds + extraEmeralds), TooltipNode.empty()),
                TradeUtils.getEmptySlotNode(),
                TradeUtils.getItemSlotNode(Stream.of(Items.SUNFLOWER, Items.LILAC, Items.PEONY, Items.ROSE_BUSH).map(Item::getDefaultInstance).toList(), NumberExpr.constant(1), TooltipNode.empty()),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
