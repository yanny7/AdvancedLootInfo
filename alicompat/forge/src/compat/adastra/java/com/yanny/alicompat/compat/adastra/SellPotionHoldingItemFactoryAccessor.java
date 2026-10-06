package com.yanny.alicompat.compat.adastra;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffer$SellPotionHoldingItemFactory")
public class SellPotionHoldingItemFactoryAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private int sellCount;

    @FieldAccessor
    private int price;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private Item secondBuy;

    @FieldAccessor
    private int secondCount;

    @FieldAccessor
    private float priceMultiplier;

    public SellPotionHoldingItemFactoryAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(price),
                TooltipNode.empty(),
                Either.left(secondBuy.getDefaultInstance()),
                NumberExpr.constant(secondCount),
                TooltipNode.empty(),
                Either.left(new ItemStack(sell.getItem())),
                NumberExpr.constant(sellCount),
                TooltipBuilder.keyOnly(AdAstraLang.Functions.RANDOM_POTION).build(),
                maxUses,
                experience,
                priceMultiplier,
                conditions
        );
    }
}
