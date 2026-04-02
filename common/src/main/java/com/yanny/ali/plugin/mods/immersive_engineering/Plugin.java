package com.yanny.ali.plugin.mods.immersive_engineering;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.immersive_engineering.functions.*;

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
    }
}
