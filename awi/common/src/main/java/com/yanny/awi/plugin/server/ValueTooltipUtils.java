package com.yanny.awi.plugin.server;

import com.yanny.awi.api.IKeyTooltipNode;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.common.tooltip.ArrayTooltipNode;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class ValueTooltipUtils {
    @NotNull
    public static IKeyTooltipNode getIntProviderTooltip(IServerUtils utils, IntProvider value) {
        return utils.getIntProviderTooltip(utils, value);
    }

    @NotNull
    public static IKeyTooltipNode getRuleTestTooltip(IServerUtils utils, RuleTest value) {
        return utils.getRuleTestTooltip(utils, value);
    }

    @NotNull
    public static IKeyTooltipNode getTargetBlockStateTooltip(IServerUtils utils, OreConfiguration.TargetBlockState value) {
        return utils.getBranchNode()
                .add(utils.getValueTooltip(utils, value.state).build("awi.property.branch.state"))
                .add(utils.getValueTooltip(utils, value.target).build("awi.property.branch.target"));
    }

    @NotNull
    public static IKeyTooltipNode getBlockStateTooltip(IServerUtils utils, BlockState value) {
        IKeyTooltipNode tooltip = utils.getBranchNode()
                .add(utils.getValueTooltip(utils, value.getBlock()).build("awi.property.value.block"));
        ArrayTooltipNode.Builder array = new ArrayTooltipNode.Builder();

        value.getValues().forEach((p) -> array.add(utils.getKeyValueNode(p.valueName(), p.value().toString()).build("aci.util.null")));
        tooltip.add(utils.getBranchNode().add(array.build()).build("awi.property.branch.properties"));
        return tooltip;
    }
}
