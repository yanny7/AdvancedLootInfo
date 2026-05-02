package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FeatureConfigurationTooltipUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class FeatureConfigurationTooltipTest {
    @Test
    public void testCountConfiguration() {
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(5)), List.of("Count: 5"));
        assertTooltip(FeatureConfigurationTooltipUtils.getCountConfigurationTooltip(UTILS, new CountConfiguration(UniformInt.of(1, 2))), List.of("Count: [1-2]"));
    }

    @Disabled
    @Test
    public void testOreConfiguration() {
        assertTooltip(FeatureConfigurationTooltipUtils.getOreConfigurationTooltip(UTILS, new OreConfiguration(Collections.emptyList(), 5, 0.5f)), List.of(
                "Ore:",
                "  -> Discard Chance On Air Exposure: 0.5",
                "  -> Size: 5"
        ));
        assertTooltip(FeatureConfigurationTooltipUtils.getOreConfigurationTooltip(UTILS, new OreConfiguration(AlwaysTrueTest.INSTANCE, Blocks.FURNACE.defaultBlockState(), 2, 0.25f)), List.of(
                "Ore:",
                "  -> Discard Chance On Air Exposure: 0.25",
                "  -> Size: 2",
                "  -> Target States:",
                "    -> Values:",
                "      -> State:",
                "        -> Block: Furnace",
                "        -> Properties:",
                "          -> facing: north",
                "          -> lit: false",
                "      -> Target:",
                "        -> Always True"
        ));
    }
}
