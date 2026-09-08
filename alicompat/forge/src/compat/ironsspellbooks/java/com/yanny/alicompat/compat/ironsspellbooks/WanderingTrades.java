package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
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
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;


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
                lootTableNode(utils, condition, BASIC_CURIOS, new RangeValue(64), null));
        registerNested(registry, "ScrollPouchTrade", (utils, condition) ->
                lootTableNode(utils, condition, SCROLL_POUCH, scrollPouchCost(utils), scrollPouch()));
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
    private static IDataNode lootTableNode(IServerUtils utils, TooltipNode condition, ResourceLocation lootTable, RangeValue cost, @Nullable ItemStack forSale) {
        ItemStack result = forSale != null ? forSale : firstItem(utils, lootTable);

        return WizardTrade.of(new ItemStack(Items.EMERALD), cost, result, new RangeValue(1), 1, 5, 0.5f)
                .withResultTooltip((u) -> u.getValueTooltip(u, lootTable).build(Lang.Value.LOOT_TABLE))
                .getNode(utils, condition);
    }

    @NotNull
    private static RangeValue scrollPouchCost(IServerUtils utils) {
        int rolls = Math.max(1, (int) totalRolls(utils, SCROLL_POUCH).max());
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;

        for (SpellRarity rarity : SpellRarity.values()) {
            minValue = Math.min(minValue, rarity.getValue());
            maxValue = Math.max(maxValue, rarity.getValue());
        }

        return new RangeValue(rolls * (minValue + 1) * 4 + 8, rolls * (maxValue + 1) * 4 + 16);
    }

    @NotNull
    private static RangeValue totalRolls(IServerUtils utils, ResourceLocation lootTable) {
        LootTable table = utils.getLootTable(lootTable);
        RangeValue rolls = new RangeValue(0);

        if (table != null) {
            for (LootPool pool : utils.getLootPools(table)) {
                rolls.add(utils.convertNumber(utils, pool.rolls));
            }
        }

        return rolls;
    }

    @NotNull
    private static ItemStack firstItem(IServerUtils utils, ResourceLocation lootTable) {
        LootTable table = utils.getLootTable(lootTable);

        if (table != null) {
            for (LootPool pool : utils.getLootPools(table)) {
                for (LootPoolEntryContainer entry : pool.entries) {
                    if (entry instanceof LootItem lootItem) {
                        return new ItemStack(lootItem.item);
                    }
                }
            }
        }

        LOGGER.warn("No item entry found in loot table {}", lootTable);
        return new ItemStack(Items.BARRIER);
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
