package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.common.tooltip.ErrorTooltipNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class RuleTestTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getMissingRuleTestTooltip(IServerUtils utils, RuleTest test) {
        //TODO auto detected placed feature
        return ErrorTooltipNode.error(test.getClass().getTypeName());
    }

    @NotNull
    public static IKeyTooltipNode getAlwaysTrueTooltip(IServerUtils utils, AlwaysTrueTest test) {
        return utils.getComponentNode(Component.translatable("awi.rule_test.always_true"));
    }
}
