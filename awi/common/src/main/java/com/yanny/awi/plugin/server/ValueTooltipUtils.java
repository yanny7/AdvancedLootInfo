package com.yanny.awi.plugin.server;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
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
                value.getValues().forEach((p, v) -> c.add(TooltipBuilder.keyValue(p.getName(), v.toString())));
            });

            b.add(array.build(Lang.Branch.PROPERTIES));
        });
    }

    @NotNull
    public static TooltipBuilder getBlockInfoTooltip(IServerUtils utils, NodeUtils.BlockInfo info) {
        return TooltipBuilder.array((b) -> {
            RangeValue fistValue = info.ranges().get(0);

            b.add(utils.getValueTooltip(utils, info.block()).build(Lang.Value.BLOCK));

            switch (info.storageType()) {
                case RELATIVE -> b.add(utils.getValueTooltip(utils, fistValue).build(Lang.Value.DEPTH_BELOW_SURFACE));
                case ABSOLUTE -> b.add(utils.getValueTooltip(utils, info.ranges()).build(Lang.Branch.ABSOLUTE_Y));
                case LAYERED -> b.add(utils.getValueTooltip(utils, info.ranges()).build(Lang.Branch.LAYERS_AT_Y));
            }

            switch (info.water()) {
                case UNDERWATER -> b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(Lang.Placement.UNDERWATER.singular())).build(Lang.Value.PLACEMENT));
                case DRY -> b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(Lang.Placement.ON_LAND.singular())).build(Lang.Value.PLACEMENT));
                case ANY -> {} // indifferent to the water level — no placement line
            }

            switch (info.placement()) {
                case CEILING -> b.add(utils.getValueTooltip(utils, TooltipBuilder.translate(Lang.Placement.ON_CEILING.singular())).build(Lang.Value.PLACEMENT));
                case FLOOR, ANY -> {} // normal below-surface placement — no overhang line
            }
        });
    }
}
