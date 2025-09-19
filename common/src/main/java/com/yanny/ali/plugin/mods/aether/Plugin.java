package com.yanny.ali.plugin.mods.aether;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.aether.conditions.ConfigEnabled;
import com.yanny.ali.plugin.mods.aether.functions.DoubleDrops;
import com.yanny.ali.plugin.mods.aether.functions.SpawnTNT;
import com.yanny.ali.plugin.mods.aether.functions.SpawnXP;
import com.yanny.ali.plugin.mods.aether.functions.WhirlwindSpawnEntity;

//@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "aether";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, SpawnXP.class);
        PluginUtils.registerFunctionTooltip(registry, SpawnTNT.class);
        PluginUtils.registerFunctionTooltip(registry, DoubleDrops.class);
        PluginUtils.registerFunctionTooltip(registry, WhirlwindSpawnEntity.class);

        PluginUtils.registerConditionTooltip(registry, ConfigEnabled.class);
    }
}
