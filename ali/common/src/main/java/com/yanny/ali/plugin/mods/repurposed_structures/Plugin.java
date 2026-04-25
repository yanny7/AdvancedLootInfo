package com.yanny.ali.plugin.mods.repurposed_structures;

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
        return "repurposed_structures";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, TreasureMapForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, TreasureMapForEmeralds.class);
    }
}
