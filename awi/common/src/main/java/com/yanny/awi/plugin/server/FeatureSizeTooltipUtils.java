package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import org.jetbrains.annotations.NotNull;

public class FeatureSizeTooltipUtils {
    @NotNull
    public static TooltipBuilder getTwoLayersFeatureSizeTooltip(IServerUtils utils, TwoLayersFeatureSize value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.minClippedHeight()).build(Lang.Value.MIN_CLIPPED_HEIGHT));
            b.add(utils.getValueTooltip(utils, value.limit).build(Lang.Value.LIMIT));
            b.add(utils.getValueTooltip(utils, value.lowerSize).build(Lang.Value.LOWER_SIZE));
            b.add(utils.getValueTooltip(utils, value.upperSize).build(Lang.Value.UPPER_SIZE));
        }, Lang.FeatureSize.TWO_LAYERS);
    }

    @NotNull
    public static TooltipBuilder getThreeLayersFeatureSizeTooltip(IServerUtils utils, ThreeLayersFeatureSize value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.minClippedHeight()).build(Lang.Value.MIN_CLIPPED_HEIGHT));
            b.add(utils.getValueTooltip(utils, value.limit).build(Lang.Value.LIMIT));
            b.add(utils.getValueTooltip(utils, value.upperLimit).build(Lang.Value.UPPER_LIMIT));
            b.add(utils.getValueTooltip(utils, value.lowerSize).build(Lang.Value.LOWER_SIZE));
            b.add(utils.getValueTooltip(utils, value.middleSize).build(Lang.Value.MIDDLE_SIZE));
            b.add(utils.getValueTooltip(utils, value.upperSize).build(Lang.Value.UPPER_SIZE));
        }, Lang.FeatureSize.THREE_LAYERS);
    }
}
