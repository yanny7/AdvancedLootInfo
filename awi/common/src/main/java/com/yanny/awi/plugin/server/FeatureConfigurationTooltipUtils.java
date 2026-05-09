package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.NotNull;

public class FeatureConfigurationTooltipUtils {
    @NotNull
    public static TooltipNode getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
        //TODO auto detected placed feature
        return TooltipBuilder.error("Not implemented").build();
    }

    @NotNull
    public static TooltipNode getCountConfigurationTooltip(IServerUtils utils, CountConfiguration configuration) {
        return utils.getValueTooltip(utils, configuration.count()).build("awi.feature_configuration.count");
    }

    @NotNull
    public static TooltipNode getOreConfigurationTooltip(IServerUtils utils, OreConfiguration configuration) {
        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, configuration.discardChanceOnAirExposure).build("awi.property.value.discard_chance_on_air_exposure"))
                .add(utils.getValueTooltip(utils, configuration.size).build("awi.property.value.size"))
                .add(utils.getValueTooltip(utils, configuration.targetStates).build("awi.property.branch.target_states"))
        ).build("awi.feature_configuration.ore");
    }
}
