package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class HeightProviderTooltipUtils {
    @NotNull
    public static TooltipBuilder getConstantHeightTooltip(IServerUtils utils, ConstantHeight provider) {
        return array((b) -> b.add(getVerticalAnchorTooltip(utils, provider.getValue())), Lang.HeightProvider.CONSTANT);
    }

    @NotNull
    public static TooltipBuilder getUniformHeightTooltip(IServerUtils utils, UniformHeight provider) {
        return array((b) -> {
            b.add(getVerticalAnchorTooltip(utils, provider.minInclusive).build(Lang.Branch.MIN));
            b.add(getVerticalAnchorTooltip(utils, provider.maxInclusive).build(Lang.Branch.MAX));
        }, Lang.HeightProvider.UNIFORM);
    }

    @NotNull
    public static TooltipBuilder getBiasedToBottomHeightTooltip(IServerUtils utils, BiasedToBottomHeight provider) {
        return array((b) -> {
            b.add(getVerticalAnchorTooltip(utils, provider.minInclusive).build(Lang.Branch.MIN));
            b.add(getVerticalAnchorTooltip(utils, provider.maxInclusive).build(Lang.Branch.MAX));
            b.add(utils.getValueTooltip(utils, provider.inner).build(Lang.Value.INNER));
        }, Lang.HeightProvider.BIASED_TO_BOTTOM);
    }

    @NotNull
    public static TooltipBuilder getVeryBiasedToBottomHeightTooltip(IServerUtils utils, VeryBiasedToBottomHeight provider) {
        return array((b) -> {
            b.add(getVerticalAnchorTooltip(utils, provider.minInclusive).build(Lang.Branch.MIN));
            b.add(getVerticalAnchorTooltip(utils, provider.maxInclusive).build(Lang.Branch.MAX));
            b.add(utils.getValueTooltip(utils, provider.inner).build(Lang.Value.INNER));
        }, Lang.HeightProvider.VERY_BIASED_TO_BOTTOM);
    }

    @NotNull
    public static TooltipBuilder getTrapezoidHeightTooltip(IServerUtils utils, TrapezoidHeight provider) {
        return array((b) -> {
            b.add(getVerticalAnchorTooltip(utils, provider.minInclusive).build(Lang.Branch.MIN));
            b.add(getVerticalAnchorTooltip(utils, provider.maxInclusive).build(Lang.Branch.MAX));
            b.add(utils.getValueTooltip(utils, provider.plateau).build(Lang.Value.PLATEAU));
        }, Lang.HeightProvider.TRAPEZOID);
    }

    @NotNull
    public static TooltipBuilder getWeightedListHeightTooltip(IServerUtils utils, WeightedListHeight provider) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, provider.distribution.totalWeight).build(Lang.Value.TOTAL_WEIGHT));
            b.add(utils.getValueTooltip(utils, provider.distribution.unwrap()).build(Lang.Branch.ITEMS));
        }, Lang.HeightProvider.WEIGHTED_LIST);
    }

    @NotNull
    private static TooltipBuilder getVerticalAnchorTooltip(IServerUtils utils, VerticalAnchor anchor) {
        if (anchor instanceof VerticalAnchor.Absolute absolute) {
            return array((b) -> b.add(utils.getValueTooltip(utils, absolute.y()).build(Lang.Value.ABSOLUTE_Y)));
        } else if (anchor instanceof VerticalAnchor.AboveBottom aboveBottom) {
            return array((b) -> b.add(utils.getValueTooltip(utils, aboveBottom.offset()).build(Lang.Value.ABOVE_BOTTOM)));
        } else if (anchor instanceof VerticalAnchor.BelowTop belowTop) {
            return array((b) -> b.add(utils.getValueTooltip(utils, belowTop.offset()).build(Lang.Value.BELOW_TOP)));
        } else {
            return array((b) -> b.add(utils.getValueTooltip(utils, anchor.toString())));
        }
    }
}
