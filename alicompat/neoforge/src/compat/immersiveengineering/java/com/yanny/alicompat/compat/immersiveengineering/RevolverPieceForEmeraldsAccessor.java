package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RevolverPieceForEmeraldsAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    public RevolverPieceForEmeraldsAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(Items.EMERALD.getDefaultInstance()), NumberExpr.add(NumberExpr.mul(NumberExpr.constant(5), NumberExpr.range(1, 5)), NumberExpr.uniformInt(0, 4)), TooltipNode.empty()),
                TradeUtils.getEmptySlotNode(),
                TradeUtils.getItemSlotNode(List.of(new ItemStack(Ingredients.GUNPART_BARREL), new ItemStack(Ingredients.GUNPART_DRUM), new ItemStack(Ingredients.GUNPART_HAMMER)), NumberExpr.constant(1), TooltipNode.empty()),
                1,
                45,
                0.25F,
                conditions
        );
    }
}
