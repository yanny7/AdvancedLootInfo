package com.yanny.alicompat.compat.adastra;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffers$SellItemFactory")
public class SellItemFactoryAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private int price;

    @FieldAccessor
    private int count;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellItemFactoryAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(price),
                Either.left(sell.copyWithCount(count)),
                NumberExpr.constant(count),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }
}
