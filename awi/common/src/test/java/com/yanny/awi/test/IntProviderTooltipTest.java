package com.yanny.awi.test;

import com.yanny.awi.plugin.server.IntProviderTooltipUtils;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.awi.test.utils.TestUtils.assertTooltip;

public class IntProviderTooltipTest {
    @Test
    public void testConstantIntTooltip() {
        assertTooltip(IntProviderTooltipUtils.getConstantIntTooltip(UTILS, ConstantInt.of(5)).build(), List.of(
                "Constant:",
                "  -> Value: 5"
        ));
    }

    @Test
    public void testUniformIntTooltip() {
        assertTooltip(IntProviderTooltipUtils.getUniformIntTooltip(UTILS, UniformInt.of(0, 10)).build(), List.of(
                "Uniform:",
                "  -> Range: 0-10"
        ));
    }

    @Test
    public void testBiasedToBottomIntTooltip() {
        assertTooltip(IntProviderTooltipUtils.getBiasedToBottomIntTooltip(UTILS, BiasedToBottomInt.of(0, 10)).build(), List.of(
                "Biased To Bottom:",
                "  -> Range: 0-10"
        ));
    }

    @Test
    public void testClampedIntTooltip() {
        assertTooltip(IntProviderTooltipUtils.getClampedIntTooltip(UTILS, ClampedInt.of(UniformInt.of(0, 10), 2, 8)).build(), List.of(
                "Clamped:",
                "  -> Source:",
                "    -> Uniform:",
                "      -> Range: 0-10",
                "  -> Range: 2-8"
        ));
    }

    @Test
    public void testWeightedListIntTooltip() {
        assertTooltip(IntProviderTooltipUtils.getWeightedListIntTooltip(UTILS, new WeightedListInt(
                SimpleWeightedRandomList.<IntProvider>builder()
                        .add(ConstantInt.of(5), 2)
                        .add(ConstantInt.of(10), 1)
                        .build()
        )).build(), List.of(
                "Weighted List:",
                "  -> Total Weight: 3",
                "  -> Items:",
                "    -> Entry:",
                "      -> Weight: 2",
                "      -> Data:",
                "        -> Constant:",
                "          -> Value: 5",
                "    -> Entry:",
                "      -> Weight: 1",
                "      -> Data:",
                "        -> Constant:",
                "          -> Value: 10",
                "  -> Range: 5-10"
        ));
    }

    @Test
    public void testClampedNormalIntTooltip() {
        assertTooltip(IntProviderTooltipUtils.getClampedNormalIntTooltip(UTILS, ClampedNormalInt.of(5.0f, 2.0f, 0, 10)).build(), List.of(
                "Clamped Normal:",
                "  -> Mean: 5.0",
                "  -> Deviation: 2.0",
                "  -> Range: 0-10"
        ));
    }
}
