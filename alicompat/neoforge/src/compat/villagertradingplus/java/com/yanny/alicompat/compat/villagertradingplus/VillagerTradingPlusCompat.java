package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.VillagerTradingPlus;
import com.lion.villagertradingplus.config.VTPConfig;
import com.lion.villagertradingplus.tradeoffers.ConditionalTradeFactory;
import com.lion.villagertradingplus.tradeoffers.PricingTradeFactory;
import com.lion.villagertradingplus.tradeoffers.conditions.ParsedConditions;
import com.lion.villagertradingplus.tradeoffers.conditions.TradeCondition;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class VillagerTradingPlusCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return VillagerTradingPlusLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerValueTooltip(ParsedConditions.class, VillagerTradingPlusCompat::getParsedConditionsTooltip);

        registry.registerItemListing(ConditionalTradeFactory.class, VillagerTradingPlusCompat::getConditionalTradeNode);
        registry.registerItemListing(PricingTradeFactory.class, VillagerTradingPlusCompat::getPricingTradeNode);

        PluginUtils.registerItemListing(registry, BuyItemTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellItemTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, MultiInputTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, ProcessItemTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, BuyTaggedItemTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellTaggedItemTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellEnchantedBookTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellSpecificEnchantedBookTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellEnchantedBookFromListTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellEnchantedToolTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellSpecificEnchantedToolTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellPotionTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellStructureMapTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, WeightedPoolTradeOfferAccessor.class);
    }

    @NotNull
    public static TooltipBuilder getParsedConditionsTooltip(IServerUtils utils, ParsedConditions cond) {
        return TooltipBuilder.array((b) -> cond.entries().forEach((e) -> b.add(utils.getValueTooltip(utils, e.description()).build())));
    }

    @NotNull
    private static IDataNode getConditionalTradeNode(IServerUtils utils, ConditionalTradeFactory listing, TooltipNode condition) {
        TradeCondition trade = listing.condition();

        return utils.getItemListing(utils, listing.delegate(), TooltipBuilder.array((b) -> {
            b.add(condition);
            b.add(utils.getValueTooltip(utils, trade).build(isAnyOf(trade) ? Lang.Conditions.ANY_OF : Lang.Conditions.ALL_OF));
        }).build());
    }

    @NotNull
    private static IDataNode getPricingTradeNode(IServerUtils utils, PricingTradeFactory listing, TooltipNode condition) {
        return utils.getItemListing(utils, listing.delegate(), TooltipBuilder.array((b) -> {
            b.add(condition);
            b.add(utils.getValueTooltip(utils, getCostScale()).build(VillagerTradingPlusLang.Value.COST_SCALE));
        }).build());
    }

    @NotNull
    private static RangeValue getCostScale() {
        VTPConfig config = VillagerTradingPlus.CONFIG;

        if (config.enable_time_of_day_pricing) {
            return new RangeValue(config.trade_cost_scale * (1.0f - config.time_of_day_price_variance), config.trade_cost_scale * (1.0f + config.time_of_day_price_variance));
        }

        return new RangeValue(config.trade_cost_scale);
    }

    private static boolean isAnyOf(TradeCondition condition) {
        return condition instanceof ParsedConditions parsed && parsed.orLogic();
    }
}
