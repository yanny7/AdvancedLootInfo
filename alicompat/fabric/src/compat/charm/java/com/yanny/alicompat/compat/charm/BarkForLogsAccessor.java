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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import svenhjol.charm.feature.lumberjacks.common.Trades;

import java.util.List;

public class BarkForLogsAccessor extends BaseAccessor<Trades.BarkForLogs> implements IItemListing {
    private static final List<Block> LOGS = List.of(Blocks.ACACIA_LOG, Blocks.BIRCH_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG, Blocks.MANGROVE_LOG, Blocks.OAK_LOG, Blocks.SPRUCE_LOG);
    private static final List<Block> WOODS = List.of(Blocks.ACACIA_WOOD, Blocks.BIRCH_WOOD, Blocks.DARK_OAK_WOOD, Blocks.JUNGLE_WOOD, Blocks.MANGROVE_WOOD, Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD);

    @FieldAccessor
    private int baseCost;
    @FieldAccessor
    private int extraCost;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public BarkForLogsAccessor(Trades.BarkForLogs parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        NumberExpr count = NumberExpr.uniformInt(baseCost, baseCost + extraCost);

        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(Items.EMERALD.getDefaultInstance()), NumberExpr.constant(1), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(getStacks(LOGS), count, TooltipNode.empty()),
                TradeUtils.getItemSlotNode(getStacks(WOODS), count, TooltipNode.empty()),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }

    @Unmodifiable
    @NotNull
    private static List<ItemStack> getStacks(List<Block> blocks) {
        return blocks.stream().map((b) -> b.asItem().getDefaultInstance()).toList();
    }
}
