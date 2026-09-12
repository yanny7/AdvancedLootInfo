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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

public class PopulatedBeehiveForEmeraldsAccessor extends BaseAccessor<BeekeeperTradeOffers.PopulatedBeehiveForEmeralds> implements IItemListing {
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public PopulatedBeehiveForEmeraldsAccessor(BeekeeperTradeOffers.PopulatedBeehiveForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        ItemStack beehive = Items.BEEHIVE.getDefaultInstance();

        beehive.setHoverName(Component.translatable("item.charm.populated_beehive"));
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                Either.left(beehive),
                new RangeValue(1),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
