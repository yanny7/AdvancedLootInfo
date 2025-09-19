package com.yanny.ali.plugin.mods.cultural_delights;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;

//@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "culturaldelights";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, EmeraldToItemOffer.class);
        PluginUtils.registerItemListingCollector(registry, EmeraldToItemOffer.class);
    }
}
