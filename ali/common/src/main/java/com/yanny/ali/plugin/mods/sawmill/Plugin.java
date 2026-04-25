package com.yanny.ali.plugin.mods.sawmill;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class Plugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "sawmill";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, LogStrippingListing.class);
        PluginUtils.registerItemListing(registry, WoodToItemListing.class);

        PluginUtils.registerItemListingCollector(registry, LogStrippingListing.class);
        PluginUtils.registerItemListingCollector(registry, WoodToItemListing.class);
    }
}
