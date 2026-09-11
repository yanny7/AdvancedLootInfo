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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

import java.util.List;

public class TallFlowerForEmeraldsAccessor extends BaseAccessor<BeekeeperTradeOffers.TallFlowerForEmeralds> implements IItemListing {
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public TallFlowerForEmeraldsAccessor(BeekeeperTradeOffers.TallFlowerForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(Items.SUNFLOWER.getDefaultInstance()),
                new RangeValue(1),
                utils.getValueTooltip(utils, List.of(Items.LILAC, Items.PEONY, Items.ROSE_BUSH)).build(CharmLang.Branch.ALTERNATIVE),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
