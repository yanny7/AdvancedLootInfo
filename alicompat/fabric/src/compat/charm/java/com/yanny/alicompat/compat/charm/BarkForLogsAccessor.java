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
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;

import java.util.List;

public class BarkForLogsAccessor extends BaseAccessor<LumberjackTradeOffers.BarkForLogs> implements IItemListing {
    @FieldAccessor
    private int baseCost;
    @FieldAccessor
    private int extraCost;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public BarkForLogsAccessor(LumberjackTradeOffers.BarkForLogs parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        RangeValue count = new RangeValue(baseCost, baseCost + extraCost);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(Blocks.OAK_LOG.asItem().getDefaultInstance()),
                count,
                utils.getValueTooltip(utils, List.of(Blocks.ACACIA_LOG, Blocks.BIRCH_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG,
                        Blocks.MANGROVE_LOG, Blocks.SPRUCE_LOG)).build(CharmLang.Branch.ALTERNATIVE),
                Either.left(Blocks.OAK_WOOD.asItem().getDefaultInstance()),
                new RangeValue(count),
                utils.getValueTooltip(utils, List.of(Blocks.ACACIA_WOOD, Blocks.BIRCH_WOOD, Blocks.DARK_OAK_WOOD, Blocks.JUNGLE_WOOD,
                        Blocks.MANGROVE_WOOD, Blocks.SPRUCE_WOOD)).build(CharmLang.Branch.ALTERNATIVE),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
