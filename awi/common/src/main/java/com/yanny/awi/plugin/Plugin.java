package com.yanny.awi.plugin;

import com.yanny.awi.api.AwiEntrypoint;
import com.yanny.awi.api.IPlugin;
import com.yanny.awi.api.IServerRegistry;
import com.yanny.awi.plugin.server.FeatureConfigurationTooltipUtils;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import org.jetbrains.annotations.NotNull;

@AwiEntrypoint
public class Plugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "awi";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFeatureTooltip(CountConfiguration.class, FeatureConfigurationTooltipUtils::getCountConfigurationTooltip);
    }
}
