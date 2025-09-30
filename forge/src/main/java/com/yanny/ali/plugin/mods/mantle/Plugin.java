package com.yanny.ali.plugin.mods.mantle;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.mantle.conditions.BlockTagLootCondition;
import com.yanny.ali.plugin.mods.mantle.functions.RetexturedLootFunction;
import com.yanny.ali.plugin.mods.mantle.functions.SetFluidLootFunction;
import com.yanny.ali.plugin.mods.mantle.trades.AncientToolItemListing;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "mantle";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, RetexturedLootFunction.class);
        PluginUtils.registerFunctionTooltip(registry, SetFluidLootFunction.class);

        PluginUtils.registerConditionTooltip(registry, BlockTagLootCondition.class);

        PluginUtils.registerItemListing(registry, AncientToolItemListing.class);
        PluginUtils.registerItemListingCollector(registry, AncientToolItemListing.class);
    }
}
