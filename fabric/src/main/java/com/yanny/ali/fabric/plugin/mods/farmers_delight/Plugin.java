package com.yanny.ali.fabric.plugin.mods.farmers_delight;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "farmersdelight";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopySkilletFunction.class);
        PluginUtils.registerFunctionTooltip(registry, SmokerCookFunction.class);

        PluginUtils.registerConditionTooltip(registry, CanItemPerformAbilityCondition.class);

        PluginUtils.registerItemListing(registry, FDItemListing.class);
        PluginUtils.registerItemListingCollector(registry, FDItemListing.class);
    }
}
