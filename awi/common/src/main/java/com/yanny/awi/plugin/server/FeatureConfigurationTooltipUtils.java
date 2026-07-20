package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class FeatureConfigurationTooltipUtils {
    @NotNull
    public static TooltipBuilder getCountConfigurationTooltip(IServerUtils utils, CountConfiguration configuration) {
        return array((b) -> b.add(utils.getValueTooltip(utils, configuration.count()).build(Lang.Value.COUNT)), Lang.FeatureConfiguration.COUNT);
    }

    @NotNull
    public static TooltipBuilder getOreConfigurationTooltip(IServerUtils utils, OreConfiguration configuration) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, configuration.discardChanceOnAirExposure).build(Lang.Value.DISCARD_CHANCE_ON_AIR_EXPOSURE));
            b.add(utils.getValueTooltip(utils, configuration.size).build(Lang.Value.SIZE));
            b.add(utils.getValueTooltip(utils, configuration.targetStates).build(Lang.Branch.TARGET_STATES));
        }, Lang.FeatureConfiguration.ORE);
    }
}
