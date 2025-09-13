package com.yanny.ali.plugin.mods.villagers_plus;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;

//@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "villagersplus";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, JsonSellPotionTradeOffer.class);
        PluginUtils.registerItemListingCollector(registry, JsonSellPotionTradeOffer.class);
    }
}
