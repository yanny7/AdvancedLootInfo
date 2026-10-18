package com.yanny.alicompat.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.loot.DungeonLootTables;
import com.hollingsworth.arsnouveau.common.event.EventHandler;
import com.hollingsworth.arsnouveau.setup.registry.VillagerRegistry;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ShardsTraderTrades {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final String LAMBDA_PREFIX = EventHandler.class.getName() + "$$Lambda";

    @Nullable
    public static Int2ObjectMap<TradeLevel> getTradeLevels(IServerUtils utils, ResourceLocation traderId) {
        if (!traderId.equals(VillagerRegistry.SHARDS_TRADER.getId())) {
            return null;
        }

        Int2ObjectMap<VillagerTrades.ItemListing[]> trades = VillagerTrades.TRADES.get(VillagerRegistry.SHARDS_TRADER.get());

        if (trades == null) {
            return null;
        }

        Int2ObjectMap<TradeLevel> levels = new Int2ObjectOpenHashMap<>();

        trades.int2ObjectEntrySet().forEach((entry) -> levels.put(entry.getIntKey(),
                new TradeLevel.OfTrades(new TradeLevelInfo(NumberExpr.constant(2)), (u) -> getNodes(u, entry.getValue()))));
        return levels;
    }

    @NotNull
    private static List<IDataNode> getNodes(IServerUtils utils, VillagerTrades.ItemListing[] listings) {
        List<ItemStack> rareLoot = DungeonLootTables.RARE_LOOT.stream().map(ShardsTraderTrades::getRareLoot).filter((stack) -> !stack.isEmpty()).toList();

        return Arrays.stream(listings).map((listing) -> getNode(utils, listing, rareLoot)).toList();
    }

    @NotNull
    private static IDataNode getNode(IServerUtils utils, VillagerTrades.ItemListing listing, List<ItemStack> rareLoot) {
        if (listing.getClass().getName().startsWith(LAMBDA_PREFIX)) {
            MerchantOffer offer = getOffer(listing);

            if (offer != null && rareLoot.stream().anyMatch((stack) -> ItemStack.isSameItem(stack, offer.getResult()))) {
                ItemStack cost = offer.getBaseCostA();

                return new ItemsToItemsNode(utils,
                        TradeUtils.getItemSlotNode(List.of(cost), NumberExpr.constant(cost.getCount()), TooltipNode.empty()),
                        TradeUtils.getEmptySlotNode(),
                        TradeUtils.getItemSlotNode(rareLoot, NumberExpr.constant(1), TooltipNode.empty()),
                        offer.getMaxUses(), offer.getXp(), offer.getPriceMultiplier(), TooltipNode.empty());
            }
        }

        return utils.getItemListing(utils, listing, TooltipNode.empty());
    }

    @NotNull
    private static ItemStack getRareLoot(Supplier<ItemStack> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            LOGGER.warn("Failed to get rare loot of the shards trader: {}", e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    @Nullable
    private static MerchantOffer getOffer(VillagerTrades.ItemListing listing) {
        try {
            return listing.getOffer(null, null);
        } catch (Throwable e) {
            return null;
        }
    }
}
