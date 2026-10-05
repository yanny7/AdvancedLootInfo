package com.yanny.alicompat.compat.grimoireofgaia;

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
import org.jetbrains.annotations.NotNull;

@ClassAccessor("gaia.util.GaiaMerchantTrades$ItemsToItems")
public class ItemsToItemsAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack fromItem;

    @FieldAccessor
    private int fromCount;

    @FieldAccessor
    private ItemStack toItem;

    @FieldAccessor
    private int toCount;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int XP;

    @FieldAccessor
    private float priceMultiplier;

    public ItemsToItemsAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(new ItemStack(fromItem.getItem(), fromCount)),
                NumberExpr.constant(fromCount),
                Either.left(new ItemStack(toItem.getItem(), toCount)),
                NumberExpr.constant(toCount),
                maxUses,
                XP,
                priceMultiplier,
                conditions
        );
    }
}
