package com.yanny.alicompat.compat.villagertradingplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellEnchantedBookFromListTradeOffer$Factory")
public class SellEnchantedBookFromListTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final int MIN_COST = 1;
    private static final int MAX_COST = 64;

    @FieldAccessor
    private ItemStack currency;

    @FieldAccessor
    private List<?> entries;

    @FieldAccessor
    private int baseCost;

    @FieldAccessor
    private int costPerLevel;

    @FieldAccessor
    private int treasureMultiplier;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellEnchantedBookFromListTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode tooltip = TooltipBuilder.branch((b) -> entries.forEach((e) -> {
            EnchantmentEntryAccessor entry = EnchantmentEntryAccessor.of(e);

            b.add(TooltipBuilder.array((c) -> {
                c.add(utils.getValueTooltip(utils, entry.getEnchantment()).build(Lang.Value.ENCHANTMENT));
                c.add(utils.getValueTooltip(utils, entry.getLevels()).build(Lang.Value.LEVELS));
            }).build());
        })).build(Lang.Functions.ENCHANT_RANDOMLY);

        return new ItemsToItemsNode(
                utils,
                Either.left(new ItemStack(currency.getItem())),
                getCostRange(),
                TooltipNode.empty(),
                Either.left(Items.BOOK.getDefaultInstance()),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(Items.ENCHANTED_BOOK.getDefaultInstance()),
                new RangeValue(1),
                tooltip,
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @NotNull
    private RangeValue getCostRange() {
        int min = MAX_COST;
        int max = MIN_COST;

        for (Object e : entries) {
            EnchantmentEntryAccessor entry = EnchantmentEntryAccessor.of(e);

            min = Math.min(min, getCost(entry.getEnchantment(), entry.getMinLevel()));
            max = Math.max(max, getCost(entry.getEnchantment(), entry.getMaxLevel()));
        }

        return new RangeValue(Math.min(min, max), max);
    }

    private int getCost(Enchantment enchantment, int level) {
        int cost = baseCost + level * costPerLevel;

        if (enchantment.isTreasureOnly()) {
            cost *= treasureMultiplier;
        }

        return Mth.clamp(cost, MIN_COST, MAX_COST);
    }
}
