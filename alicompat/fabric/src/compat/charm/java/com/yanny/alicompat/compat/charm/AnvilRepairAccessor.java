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
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.trade_improvements.common.Registers;

import java.util.List;

public class AnvilRepairAccessor extends BaseAccessor<Registers.AnvilRepair> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int maxUses;

    public AnvilRepairAccessor(Registers.AnvilRepair parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(Items.CHIPPED_ANVIL.getDefaultInstance(), Items.DAMAGED_ANVIL.getDefaultInstance()), NumberExpr.constant(1), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(List.of(Items.IRON_INGOT.getDefaultInstance()), NumberExpr.uniformInt(5, 9), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(List.of(Items.ANVIL.getDefaultInstance()), NumberExpr.constant(1), TooltipNode.empty()),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
