package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class ValueTooltipUtils {
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
                        .add(utils.getValueTooltip(utils, value.state).build(Lang.Branch.STATE))
                        .add(utils.getValueTooltip(utils, value.target).build(Lang.Branch.TARGET))
                );
    }

    @NotNull
    public static TooltipBuilder getBlockStateTooltip(IServerUtils utils, BlockState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getBlock()).build(Lang.Value.BLOCK));

            TooltipBuilder array = TooltipBuilder.array((c) -> {
                value.getValues().forEach((p) -> c.add(TooltipBuilder.keyValue(p.valueName(), p.value().toString())));
            });

            b.add(array.build(Lang.Branch.PROPERTIES));
        });
    }
}
