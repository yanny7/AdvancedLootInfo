package com.yanny.alicompat.compat.immersiveengineering;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$OreveinMapForEmeralds")
public class OreveinMapForEmeraldsAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    public OreveinMapForEmeraldsAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(8, 15),
                Either.left(Items.COMPASS.getDefaultInstance()),
                new RangeValue(1),
                Either.left(getOreveinMap()),
                new RangeValue(1),
                1,
                30,
                0.5F,
                conditions
        );
    }

    @NotNull
    private static ItemStack getOreveinMap() {
        return Items.FILLED_MAP.getDefaultInstance().setHoverName(Component.translatable("item.immersiveengineering.map_orevein"));
    }
}
