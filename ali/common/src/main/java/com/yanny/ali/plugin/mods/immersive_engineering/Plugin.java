package com.yanny.ali.plugin.mods.immersive_engineering;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.immersive_engineering.functions.*;
import com.yanny.ali.plugin.mods.immersive_engineering.trades.OreveinMapForEmeralds;
import com.yanny.ali.plugin.mods.immersive_engineering.trades.RevolverPieceForEmeralds;
import com.yanny.ali.plugin.mods.immersive_engineering.trades.TradeListing;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "immersiveengineering";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, BluprintzLootFunction.class);
        PluginUtils.registerFunctionTooltip(registry, ConveyorCoverLootFunction.class);
        PluginUtils.registerFunctionTooltip(registry, PropertyCountLootFunction.class);
        PluginUtils.registerFunctionTooltip(registry, RevolverperkLootFunction.class);
        PluginUtils.registerFunctionTooltip(registry, WindmillLootFunction.class);

        PluginUtils.registerItemListing(registry, TradeListing.class);
        PluginUtils.registerItemListing(registry, OreveinMapForEmeralds.class);
        PluginUtils.registerItemListing(registry, RevolverPieceForEmeralds.class);

        PluginUtils.registerItemListingCollector(registry, TradeListing.class);
        PluginUtils.registerItemListingCollector(registry, OreveinMapForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, RevolverPieceForEmeralds.class);
    }
}
