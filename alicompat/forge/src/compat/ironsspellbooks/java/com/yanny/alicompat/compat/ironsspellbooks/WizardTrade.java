package com.yanny.alicompat.compat.ironsspellbooks;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class WizardTrade implements VillagerTrades.ItemListing, IItemListing {
    private final ItemStack costA;
    private final NumberExpr costACount;
    private final ItemStack costB;
    private final NumberExpr costBCount;
    private final ItemStack result;
    private final NumberExpr resultCount;
    private final Function<IServerUtils, TooltipNode> resultTooltip;
    private final int maxUses;
    private final int xp;
    private final float priceMultiplier;

    private WizardTrade(ItemStack costA, NumberExpr costACount, ItemStack costB, NumberExpr costBCount, ItemStack result, NumberExpr resultCount,
                        Function<IServerUtils, TooltipNode> resultTooltip, int maxUses, int xp, float priceMultiplier) {
        this.costA = costA;
        this.costACount = costACount;
        this.costB = costB;
        this.costBCount = costBCount;
        this.result = result;
        this.resultCount = resultCount;
        this.resultTooltip = resultTooltip;
        this.maxUses = maxUses;
        this.xp = xp;
        this.priceMultiplier = priceMultiplier;
    }

    @NotNull
    public static WizardTrade of(ItemStack cost, NumberExpr costCount, ItemStack result, NumberExpr resultCount, int maxUses, int xp, float priceMultiplier) {
        return new WizardTrade(cost, costCount, ItemStack.EMPTY, NumberExpr.constant(1), result, resultCount,
                (ignoredUtils) -> TooltipNode.empty(), maxUses, xp, priceMultiplier);
    }

    @NotNull
    public static WizardTrade of(MerchantOffer offer) {
        return new WizardTrade(offer.getBaseCostA(), NumberExpr.constant(offer.getBaseCostA().getCount()),
                offer.getCostB(), NumberExpr.constant(offer.getCostB().getCount()),
                offer.getResult(), NumberExpr.constant(offer.getResult().getCount()),
                (ignoredUtils) -> TooltipNode.empty(), offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier());
    }

    @NotNull
    public WizardTrade withResultTooltip(Function<IServerUtils, TooltipNode> tooltip) {
        return new WizardTrade(costA, costACount, costB, costBCount, result, resultCount, tooltip, maxUses, xp, priceMultiplier);
    }

    @NotNull
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return new MerchantOffer(withCount(costA, costACount), withCount(costB, costBCount), withCount(result, resultCount), maxUses, xp, priceMultiplier);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(costA),
                costACount,
                TooltipNode.empty(),
                Either.left(costB),
                costBCount,
                TooltipNode.empty(),
                Either.left(result),
                resultCount,
                resultTooltip.apply(utils),
                maxUses,
                xp,
                priceMultiplier,
                conditions
        );
    }

    @NotNull
    private static ItemStack withCount(ItemStack itemStack, NumberExpr count) {
        return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copyWithCount((int) count.bounds().lo());
    }
}
