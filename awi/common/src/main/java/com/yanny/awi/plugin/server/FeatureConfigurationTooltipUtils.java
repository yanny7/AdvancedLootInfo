package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.plugin.common.tooltip.EmptyTooltipNode;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jetbrains.annotations.NotNull;

public class FeatureConfigurationTooltipUtils {
    @NotNull
    public static ITooltipNode getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
        //TODO auto detected placed feature
        return EmptyTooltipNode.EMPTY;
    }

    @NotNull
    public static ITooltipNode getCountConfigurationTooltip(IServerUtils utils, CountConfiguration configuration) {
        return utils.getValueTooltip(utils, configuration.count()).build("awi.property.value.count");
    }
}
