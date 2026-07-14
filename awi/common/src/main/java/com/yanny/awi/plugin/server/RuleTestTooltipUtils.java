package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import org.jetbrains.annotations.NotNull;

public class RuleTestTooltipUtils {
    @NotNull
    public static TooltipBuilder getAlwaysTrueTooltip(IServerUtils utils, AlwaysTrueTest test) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.RuleTest.ALWAYS_TRUE);
    }
}
