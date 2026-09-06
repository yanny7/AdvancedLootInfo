package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.trades.*;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class MoreJSCompat implements IModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @NotNull
    @Override
    public String targetModId() {
        return MoreJSLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemListing(SimpleTrade.class, (utils, listing, condition) ->
                ReflectionUtils.copyClassData(SimpleTradeAccessor.class, listing, SimpleTrade.class).getNode(utils, condition));
        registry.registerItemListing(StewTrade.class, (utils, listing, condition) ->
                ReflectionUtils.copyClassData(StewTradeAccessor.class, listing, StewTrade.class).getNode(utils, condition));
        registry.registerItemListing(EnchantedItemTrade.class, (utils, listing, condition) ->
                ReflectionUtils.copyClassData(EnchantedItemTradeAccessor.class, listing, EnchantedItemTrade.class).getNode(utils, condition));
        registry.registerItemListing(PotionTrade.class, (utils, listing, condition) ->
                ReflectionUtils.copyClassData(PotionTradeAccessor.class, listing, PotionTrade.class).getNode(utils, condition));
        registry.registerItemListing(TreasureMapTrade.class, (utils, listing, condition) ->
                ReflectionUtils.copyClassData(TreasureMapTradeAccessor.class, listing, TreasureMapTrade.class).getNode(utils, condition));
        registry.registerItemListing(CustomTrade.class, MoreJSCompat::getCustomTradeNode);
    }

    @NotNull
    private static IDataNode getCustomTradeNode(IServerUtils utils, CustomTrade listing, TooltipNode condition) {
        try {
            //noinspection DataFlowIssue - the script transformer decides whether it needs the trader, there is none to pass
            MerchantOffer offer = listing.getOffer(null, null);

            if (offer != null) {
                return TradeUtils.getNode(utils, offer, condition);
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to resolve custom trade offer: {}", e.getMessage());
        }

        return new MissingNode(MissingTooltipUtils.getMissingItemListingTooltip(utils, listing).build());
    }
}
