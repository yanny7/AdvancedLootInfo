package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class RuleTestTooltipUtils {
    @NotNull
    public static TooltipBuilder getMissingRuleTestTooltip(IServerUtils utils, RuleTest test) {
        //TODO auto detected placed feature
        return TooltipBuilder.error(test.getClass().getTypeName());
    }

    @NotNull
    public static TooltipBuilder getAlwaysTrueTooltip(IServerUtils utils, AlwaysTrueTest test) {
        return TooltipBuilder.keyOnly("awi.rule_test.always_true");
    }
}
