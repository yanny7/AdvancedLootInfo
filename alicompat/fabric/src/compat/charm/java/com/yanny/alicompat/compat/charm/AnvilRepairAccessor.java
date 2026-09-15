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
                Either.left(Items.CHIPPED_ANVIL.getDefaultInstance()),
                new RangeValue(1),
                utils.getValueTooltip(utils, List.of(Items.DAMAGED_ANVIL)).build(CharmLang.Branch.ALTERNATIVE),
                Either.left(Items.IRON_INGOT.getDefaultInstance()),
                new RangeValue(5, 9),
                TooltipNode.empty(),
                Either.left(Items.ANVIL.getDefaultInstance()),
                new RangeValue(1),
                TooltipNode.empty(),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
