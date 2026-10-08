package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.PluginUtils;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.player.AdditionalWanderingTrades;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;

public class WanderingTrades {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private static final String TRADES_CLASS = AdditionalWanderingTrades.class.getName();
    private static final ResourceLocation BASIC_CURIOS = new ResourceLocation(IronsSpellbooksLang.MOD_ID, "magic_items/basic_curios");
    private static final ResourceLocation SCROLL_POUCH = new ResourceLocation(IronsSpellbooksLang.MOD_ID, "magic_items/scroll_pouch");

    public static void register(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, AdditionalWanderingTrades.RandomScrollTrade.class, RandomScrollTradeAccessor.class);
        registry.registerItemListing(AdditionalWanderingTrades.InkBuyTrade.class,
                (utils, listing, condition) -> inkNode(utils, listing, condition, true));
        registry.registerItemListing(AdditionalWanderingTrades.InkSellTrade.class,
                (utils, listing, condition) -> inkNode(utils, listing, condition, false));
        registry.registerItemListing(AdditionalWanderingTrades.SimpleTrade.class, WanderingTrades::rolledNode);

        registerNested(registry, "RandomCurioTrade", (utils, condition) ->
                lootTableNode(utils, condition, BASIC_CURIOS, NumberExpr.constant(64)));
        registerNested(registry, "ScrollPouchTrade", WanderingTrades::scrollPouchNode);
    }

    @NotNull
    private static IDataNode inkNode(IServerUtils utils, AdditionalWanderingTrades.SimpleTrade listing, TooltipNode condition, boolean buy) {
        InkItem ink = SimpleTradeAccessor.of(listing).getCaptured(InkItem.class);

        if (ink == null) {
            return rolledNode(utils, listing, condition);
        }

        return (buy ? WizardTrades.inkBuy(ink) : WizardTrades.inkSell(ink)).getNode(utils, condition);
    }

    @NotNull
    private static IDataNode rolledNode(IServerUtils utils, AdditionalWanderingTrades.SimpleTrade listing, TooltipNode condition) {
        try {
            //noinspection DataFlowIssue - none of these lambdas reads the trader, only the random source
            MerchantOffer offer = listing.getOffer(null, RandomSource.create());

            if (offer != null) {
                return TradeUtils.getNode(utils, offer, condition);
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to roll trade offer for {}: {}", listing.getClass().getName(), e.getMessage());
        }

        return new MissingNode(MissingTooltipUtils.getMissingItemListingTooltip(utils, listing).build());
    }

    @NotNull
    private static IDataNode lootTableNode(IServerUtils utils, TooltipNode condition, ResourceLocation lootTable, NumberExpr cost) {
        return WizardTrade.of(new ItemStack(Items.EMERALD), cost, ItemStack.EMPTY, NumberExpr.constant(1), 1, 5, 0.5f)
                .withResultSlot((u) -> TradeUtils.getSlotNode(u, LootTableReference.lootTableReference(lootTable).build()))
                .getNode(utils, condition);
    }

    @NotNull
    private static IDataNode scrollPouchNode(IServerUtils utils, TooltipNode condition) {
        return WizardTrade.of(new ItemStack(Items.EMERALD), scrollPouchCost(utils), scrollPouch(), NumberExpr.constant(1), 1, 5, 0.5f)
                .withResultTooltip((u) -> u.getValueTooltip(u, SCROLL_POUCH).build(Lang.Value.LOOT_TABLE))
                .getNode(utils, condition);
    }

    @NotNull
    private static NumberExpr scrollPouchCost(IServerUtils utils) {
        NumberInterval rolls = totalRolls(utils, SCROLL_POUCH).bounds();
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;

        for (SpellRarity rarity : SpellRarity.values()) {
            minValue = Math.min(minValue, rarity.getValue());
            maxValue = Math.max(maxValue, rarity.getValue());
        }

        NumberExpr quality = NumberExpr.range(rolls.lo() * (minValue + 1), Math.max(1, rolls.hi()) * (maxValue + 1));

        return NumberExpr.add(NumberExpr.mul(NumberExpr.constant(4), quality), NumberExpr.uniformInt(8, 16));
    }

    @NotNull
    private static NumberExpr totalRolls(IServerUtils utils, ResourceLocation lootTable) {
        LootTable table = utils.getLootTable(lootTable);
        NumberExpr rolls = NumberExpr.constant(0);

        if (table != null) {
            for (LootPool pool : utils.getLootPools(table)) {
                rolls = NumberExpr.add(rolls, utils.convertIntNumber(utils, pool.rolls, new ArrayList<>()));
            }
        }

        return rolls;
    }

    @NotNull
    private static ItemStack scrollPouch() {
        return new ItemStack(Items.BUNDLE).setHoverName(Component.translatable("item." + IronsSpellbooksLang.MOD_ID + ".scroll_pouch"));
    }

    private static void registerNested(IServerRegistry registry, String name, NestedNode nodeFactory) {
        try {
            //noinspection unchecked
            Class<VillagerTrades.ItemListing> type = (Class<VillagerTrades.ItemListing>) Class.forName(TRADES_CLASS + "$" + name);

            registry.registerItemListing(type, (utils, ignoredListing, condition) -> nodeFactory.create(utils, condition));
        } catch (Throwable e) {
            LOGGER.warn("Failed to register item listing for {}${}: {}", TRADES_CLASS, name, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface NestedNode {
        IDataNode create(IServerUtils utils, TooltipNode condition);
    }
}
