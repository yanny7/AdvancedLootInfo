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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellSpecificEnchantedBookTradeOffer$Factory")
public class SellSpecificEnchantedBookTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private ItemStack currency;

    @FieldAccessor
    private ResourceKey<Enchantment> enchantmentKey;

    @FieldAccessor
    private int level;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellSpecificEnchantedBookTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(currency),
                new RangeValue(currency.getCount()),
                Either.left(getBookStack(utils)),
                new RangeValue(1),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @NotNull
    private ItemStack getBookStack(IServerUtils utils) {
        return utils.lookupProvider().lookupOrThrow(Registries.ENCHANTMENT).get(enchantmentKey)
                .map((holder) -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level)))
                .orElseGet(Items.ENCHANTED_BOOK::getDefaultInstance);
    }
}
