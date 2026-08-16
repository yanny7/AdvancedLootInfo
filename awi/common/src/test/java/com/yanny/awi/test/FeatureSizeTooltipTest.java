package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FeatureSizeTooltipUtils;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class FeatureSizeTooltipTest {
    @Test
    public void testTwoLayersFeatureSizeTooltip() {
        assertTooltip(FeatureSizeTooltipUtils.getTwoLayersFeatureSizeTooltip(UTILS, new TwoLayersFeatureSize(1, 0, 1, OptionalInt.of(5))).build(), List.of(
                "Two Layers:",
                "  -> Min Clipped Height: 5",
                "  -> Limit: 1",
                "  -> Lower Size: 0",
                "  -> Upper Size: 1"
        ));
    }

    @Test
    public void testThreeLayersFeatureSizeTooltip() {
        assertTooltip(FeatureSizeTooltipUtils.getThreeLayersFeatureSizeTooltip(UTILS, new ThreeLayersFeatureSize(1, 1, 0, 1, 1, OptionalInt.empty())).build(), List.of(
                "Three Layers:",
                "  -> Limit: 1",
                "  -> Upper Limit: 1",
                "  -> Lower Size: 0",
                "  -> Middle Size: 1",
                "  -> Upper Size: 1"
        ));
    }
}
