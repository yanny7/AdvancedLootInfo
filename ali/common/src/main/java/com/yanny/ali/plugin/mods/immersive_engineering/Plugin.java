package com.yanny.ali.plugin.mods.immersive_engineering;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.immersive_engineering.functions.*;
import com.yanny.ali.plugin.mods.immersive_engineering.trades.GroupedListing;
import com.yanny.ali.plugin.mods.immersive_engineering.trades.RerollingItemListing;
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
        PluginUtils.registerItemListing(registry, RerollingItemListing.class);
        PluginUtils.registerItemListing(registry, GroupedListing.class);

        PluginUtils.registerItemListingCollector(registry, TradeListing.class);
        PluginUtils.registerItemListingCollector(registry, RerollingItemListing.class);
        PluginUtils.registerItemListingCollector(registry, GroupedListing.class);
    }
}
