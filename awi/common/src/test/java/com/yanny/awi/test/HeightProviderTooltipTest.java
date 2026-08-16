package com.yanny.awi.test;

import com.yanny.awi.plugin.server.HeightProviderTooltipUtils;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class HeightProviderTooltipTest {
    @Test
    public void testConstantHeightTooltip() {
        assertTooltip(HeightProviderTooltipUtils.getConstantHeightTooltip(UTILS, ConstantHeight.of(VerticalAnchor.absolute(5))).build(), List.of(
                "Constant:",
                "  -> Absolute Y: 5"
        ));
        assertTooltip(HeightProviderTooltipUtils.getConstantHeightTooltip(UTILS, ConstantHeight.of(VerticalAnchor.aboveBottom(3))).build(), List.of(
                "Constant:",
                "  -> Above Bottom: 3"
        ));
        assertTooltip(HeightProviderTooltipUtils.getConstantHeightTooltip(UTILS, ConstantHeight.of(VerticalAnchor.belowTop(2))).build(), List.of(
                "Constant:",
                "  -> Below Top: 2"
        ));
    }

    @Test
    public void testUniformHeightTooltip() {
        assertTooltip(HeightProviderTooltipUtils.getUniformHeightTooltip(UTILS, UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10))).build(), List.of(
                "Uniform:",
                "  -> Min:",
                "    -> Absolute Y: 0",
                "  -> Max:",
                "    -> Absolute Y: 10"
        ));
    }

    @Test
    public void testBiasedToBottomHeightTooltip() {
        assertTooltip(HeightProviderTooltipUtils.getBiasedToBottomHeightTooltip(UTILS, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10), 3)).build(), List.of(
                "Biased To Bottom:",
                "  -> Min:",
                "    -> Absolute Y: 0",
                "  -> Max:",
                "    -> Absolute Y: 10",
                "  -> Inner: 3"
        ));
    }

    @Test
    public void testVeryBiasedToBottomHeightTooltip() {
        assertTooltip(HeightProviderTooltipUtils.getVeryBiasedToBottomHeightTooltip(UTILS, VeryBiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10), 3)).build(), List.of(
                "Very Biased To Bottom:",
                "  -> Min:",
                "    -> Absolute Y: 0",
                "  -> Max:",
                "    -> Absolute Y: 10",
                "  -> Inner: 3"
        ));
    }

    @Test
    public void testTrapezoidHeightTooltip() {
        assertTooltip(HeightProviderTooltipUtils.getTrapezoidHeightTooltip(UTILS, TrapezoidHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10))).build(), List.of(
                "Trapezoid:",
                "  -> Min:",
                "    -> Absolute Y: 0",
                "  -> Max:",
                "    -> Absolute Y: 10",
                "  -> Plateau: 0"
        ));
        assertTooltip(HeightProviderTooltipUtils.getTrapezoidHeightTooltip(UTILS, TrapezoidHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(10), 5)).build(), List.of(
                "Trapezoid:",
                "  -> Min:",
                "    -> Absolute Y: 0",
                "  -> Max:",
                "    -> Absolute Y: 10",
                "  -> Plateau: 5"
        ));
    }

    @Test
    public void testWeightedListHeightTooltip() {
        assertTooltip(HeightProviderTooltipUtils.getWeightedListHeightTooltip(UTILS, new WeightedListHeight(
                SimpleWeightedRandomList.<HeightProvider>builder()
                        .add(ConstantHeight.of(VerticalAnchor.absolute(5)), 2)
                        .build()
        )).build(), List.of(
                "Weighted List:",
                "  -> Total Weight: 2",
                "  -> Items:",
                "    -> Weight: 2",
                "    -> Data:",
                "      -> Constant:",
                "        -> Absolute Y: 5"
        ));
    }
}
