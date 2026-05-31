package com.yanny.awi.plugin.server;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.api.FeatureHolder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MissingTooltipUtils {
    @NotNull
    public static TooltipBuilder getMissingFeatureConfigurationTooltip(IServerUtils utils, FeatureConfiguration configuration) {
        //TODO auto detected placed feature
        return TooltipBuilder.error("Not implemented");
    }

    @NotNull
    public static FeatureHolder getMissingPlacedFeature(IServerUtils utils, PlacedFeature placedFeature) {
        //TODO auto detected placed feature
        return new FeatureHolder(Collections.emptyList(), TooltipNode.empty());
    }

    @NotNull
    public static TooltipBuilder getMissingIntProviderTooltip(IServerUtils utils, IntProvider provider) {
        //TODO auto detected placed feature
        return TooltipBuilder.error(provider.getClass().getTypeName());
    }

    @NotNull
    public static TooltipBuilder getMissingRuleTestTooltip(IServerUtils utils, RuleTest test) {
        //TODO auto detected placed feature
        return TooltipBuilder.error(test.getClass().getTypeName());
    }
    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object value) {
        return TooltipBuilder.error("[" + value.getClass().getTypeName() + "]").key(CoreLang.Utils.NOT_IMPLEMENTED);
    }
}
