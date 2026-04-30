package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FeatureConfigurationTooltipUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class FeatureConfigurationTooltipTest {
    @Disabled
    @Test
    public void testCountConfiguration() {
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(5)), List.of("Count: 5"));
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(UniformInt.of(1, 2))), List.of("Count: [1-2]"));
    }
}
