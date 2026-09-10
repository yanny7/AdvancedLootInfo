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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        ItemStack sapling = saplings.isEmpty() ? ItemStack.EMPTY : saplings.get(0).getDefaultInstance();

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(sapling),
                new RangeValue(1),
                utils.getValueTooltip(utils, saplings.stream().skip(1).toList()).build(CharmLang.Branch.ALTERNATIVE),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
