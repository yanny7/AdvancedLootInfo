package com.yanny.alicompat.compat.villagertradingplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
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
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellSpecificEnchantedToolTradeOffer$Factory")
public class SellSpecificEnchantedToolTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private ItemStack currency;

    @Nullable
    @FieldAccessor
    private Enchantment enchantment;

    @FieldAccessor
    private int level;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellSpecificEnchantedToolTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(currency),
                new RangeValue(currency.getCount()),
                Either.left(getToolStack()),
                new RangeValue(sell.getCount()),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @NotNull
    private ItemStack getToolStack() {
        ItemStack stack = sell.copy();

        if (enchantment != null) {
            stack.enchant(enchantment, level);
        }

        return stack;
    }
}
