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
        return array((b) -> b.add(utils.getValueTooltip(utils, provider.distribution)), Lang.HeightProvider.WEIGHTED_LIST);
    }

    @NotNull
    public static TooltipBuilder getVerticalAnchorTooltip(IServerUtils utils, VerticalAnchor anchor) {
        return switch (anchor) {
            case VerticalAnchor.Absolute(int y) ->
                    array((b) -> b.add(utils.getValueTooltip(utils, y).build(Lang.Value.ABSOLUTE_Y)));
            case VerticalAnchor.AboveBottom(int offset) ->
                    array((b) -> b.add(utils.getValueTooltip(utils, offset).build(Lang.Value.ABOVE_BOTTOM)));
            case VerticalAnchor.BelowTop(int offset) ->
                    array((b) -> b.add(utils.getValueTooltip(utils, offset).build(Lang.Value.BELOW_TOP)));
            default -> array((b) -> b.add(utils.getValueTooltip(utils, anchor.toString())));
        };
    }
}
