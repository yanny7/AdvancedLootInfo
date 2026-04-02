package com.yanny.ali.plugin.mods.supplementaries;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.supplementaries.functions.RandomArrowFunction;
import com.yanny.ali.plugin.mods.supplementaries.functions.RandomEnchantFunction;
import com.yanny.ali.plugin.mods.supplementaries.functions.SetChargesFunction;

// @AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "supplementaries";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, RandomArrowFunction.class);
        PluginUtils.registerFunctionTooltip(registry, RandomEnchantFunction.class);
        PluginUtils.registerFunctionTooltip(registry, SetChargesFunction.class);
    }
}
