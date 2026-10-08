package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class WizardTrade implements VillagerTrades.ItemListing, IItemListing {
    private final ItemStack costA;
    private final NumberExpr costACount;
    private final ItemStack costB;
    private final NumberExpr costBCount;
    private final ItemStack result;
    private final NumberExpr resultCount;
    private final Function<IServerUtils, IDataNode> resultSlot;
    private final int maxUses;
    private final int xp;
    private final float priceMultiplier;

    private WizardTrade(ItemStack costA, NumberExpr costACount, ItemStack costB, NumberExpr costBCount, ItemStack result, NumberExpr resultCount,
                        Function<IServerUtils, IDataNode> resultSlot, int maxUses, int xp, float priceMultiplier) {
        this.costA = costA;
        this.costACount = costACount;
        this.costB = costB;
        this.costBCount = costBCount;
        this.result = result;
        this.resultCount = resultCount;
        this.resultSlot = resultSlot;
        this.maxUses = maxUses;
        this.xp = xp;
        this.priceMultiplier = priceMultiplier;
    }

    @NotNull
    public static WizardTrade of(ItemStack cost, NumberExpr costCount, ItemStack result, NumberExpr resultCount, int maxUses, int xp, float priceMultiplier) {
        return new WizardTrade(cost, costCount, ItemStack.EMPTY, NumberExpr.constant(1), result, resultCount,
                (ignoredUtils) -> TradeUtils.getItemSlotNode(List.of(result), resultCount, TooltipNode.empty()), maxUses, xp, priceMultiplier);
    }

    @NotNull
    public static WizardTrade of(MerchantOffer offer) {
        return new WizardTrade(offer.getBaseCostA(), NumberExpr.constant(offer.getBaseCostA().getCount()),
                offer.getCostB(), NumberExpr.constant(offer.getCostB().getCount()),
                offer.getResult(), NumberExpr.constant(offer.getResult().getCount()),
                (ignoredUtils) -> TradeUtils.getItemSlotNode(List.of(offer.getResult()), NumberExpr.constant(offer.getResult().getCount()), TooltipNode.empty()), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier());
    }

    @NotNull
    public WizardTrade withResultTooltip(Function<IServerUtils, TooltipNode> tooltip) {
        return withResultSlot((utils) -> TradeUtils.getItemSlotNode(List.of(result), resultCount, tooltip.apply(utils)));
    }

    @NotNull
    public WizardTrade withResultSlot(Function<IServerUtils, IDataNode> slot) {
        return new WizardTrade(costA, costACount, costB, costBCount, result, resultCount, slot, maxUses, xp, priceMultiplier);
    }

    @NotNull
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return new MerchantOffer(cost(costA, costACount).orElseThrow(), cost(costB, costBCount), withCount(result, resultCount), maxUses, xp, priceMultiplier);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(List.of(costA), costACount, TooltipNode.empty()),
                TradeUtils.getItemSlotNode(List.of(costB), costBCount, TooltipNode.empty()),
                resultSlot.apply(utils),
                maxUses,
                xp,
                priceMultiplier,
                conditions
        );
    }

    @NotNull
    private static Optional<ItemCost> cost(ItemStack itemStack, NumberExpr count) {
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ItemCost(itemStack.getItemHolder(), (int) count.bounds().lo(), DataComponentPredicate.allOf(itemStack.getComponents())));
    }

    @NotNull
    private static ItemStack withCount(ItemStack itemStack, NumberExpr count) {
        return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copyWithCount((int) count.bounds().lo());
    }
}
