package com.yanny.alicompat.compat.villagertradingplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
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
import java.util.Map;
import java.util.TreeMap;

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
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(Items.ENCHANTED_BOOK.getDefaultInstance()),
                NumberExpr.constant(1),
                tooltip,
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @NotNull
    private NumberExpr getCostRange() {
        Map<Integer, Double> costs = new TreeMap<>();

        for (Object e : entries) {
            EnchantmentEntryAccessor entry = EnchantmentEntryAccessor.of(e);
            int levels = entry.getMaxLevel() - entry.getMinLevel() + 1;

            for (int level = entry.getMinLevel(); level <= entry.getMaxLevel(); level++) {
                costs.merge(getCost(entry.getEnchantment(), level), (double) entry.getWeight() / levels, Double::sum);
            }
        }

        if (costs.isEmpty()) {
            return NumberExpr.constant(MIN_COST);
        }

        return NumberExpr.weighted(costs.entrySet().stream()
                .map((c) -> new NumberExpr.WeightedEntry(c.getValue(), NumberExpr.constant(c.getKey())))
                .toList());
    }

    private int getCost(Enchantment enchantment, int level) {
        int cost = baseCost + level * costPerLevel;

        if (enchantment.isTreasureOnly()) {
            cost *= treasureMultiplier;
        }

        return Mth.clamp(cost, MIN_COST, MAX_COST);
    }
}
