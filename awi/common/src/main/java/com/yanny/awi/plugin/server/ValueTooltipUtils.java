package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class ValueTooltipUtils {
    @NotNull
    public static TooltipBuilder getMissingValueTooltip(IServerUtils ignoredUtils, Object value) {
        return TooltipBuilder.error("[" + value.getClass().getTypeName() + "]").key("aci.util.missing");
    }

    @NotNull
    public static TooltipBuilder getIntProviderTooltip(IServerUtils utils, IntProvider value) {
        return utils.getIntProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getRuleTestTooltip(IServerUtils utils, RuleTest value) {
        return utils.getRuleTestTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getTargetBlockStateTooltip(IServerUtils utils, OreConfiguration.TargetBlockState value) {
        return TooltipBuilder.array((b) -> b
                        .add(utils.getValueTooltip(utils, value.state).build("awi.property.branch.state"))
                        .add(utils.getValueTooltip(utils, value.target).build("awi.property.branch.target"))
                );
    }

    @NotNull
    public static TooltipBuilder getBlockStateTooltip(IServerUtils utils, BlockState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getBlock()).build("awi.property.value.block"));

            TooltipBuilder array = TooltipBuilder.array((c) -> {
                value.getValues().forEach((p) -> c.add(TooltipBuilder.keyValue(p.valueName(), p.value().toString())));
            });

            b.add(array.build("awi.property.branch.properties"));
        });
    }
}
