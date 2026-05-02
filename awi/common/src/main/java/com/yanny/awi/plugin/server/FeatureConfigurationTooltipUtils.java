package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.plugin.common.tooltip.ErrorTooltipNode;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.NotNull;

public class FeatureConfigurationTooltipUtils {
    @NotNull
    public static ITooltipNode getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
        //TODO auto detected placed feature
        return ErrorTooltipNode.error("Not implemented").build();
    }

    @NotNull
    public static ITooltipNode getCountConfigurationTooltip(IServerUtils utils, CountConfiguration configuration) {
        return utils.getValueTooltip(utils, configuration.count()).build("awi.feature_configuration.count");
    }

    @NotNull
    public static ITooltipNode getOreConfigurationTooltip(IServerUtils utils, OreConfiguration configuration) {
        return utils.getBranchNode()
                .add(utils.getValueTooltip(utils, configuration.discardChanceOnAirExposure).build("awi.property.value.discard_chance_on_air_exposure"))
                .add(utils.getValueTooltip(utils, configuration.size).build("awi.property.value.size"))
                .add(utils.getValueTooltip(utils, configuration.targetStates).build("awi.property.branch.target_states"))
                .build("awi.feature_configuration.ore");
    }
}
