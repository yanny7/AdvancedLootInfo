package com.yanny.awi.test;

import com.yanny.awi.plugin.server.FloatProviderTooltipUtils;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;

public class FloatProviderTooltipTest {
    @Test
    public void testConstantFloatTooltip() {
        assertTooltip(FloatProviderTooltipUtils.getConstantFloatTooltip(UTILS, ConstantFloat.of(5.0f)).build(), List.of(
                "Constant:",
                "  -> Value: 5.0"
        ));
    }

    @Test
    public void testUniformFloatTooltip() {
        assertTooltip(FloatProviderTooltipUtils.getUniformFloatTooltip(UTILS, UniformFloat.of(0.0f, 10.0f)).build(), List.of(
                "Uniform:",
                "  -> Range: 0-10"
        ));
    }

    @Test
    public void testTrapezoidFloatTooltip() {
        assertTooltip(FloatProviderTooltipUtils.getTrapezoidFloatTooltip(UTILS, TrapezoidFloat.of(0.0f, 10.0f, 5.0f)).build(), List.of(
                "Trapezoid:",
                "  -> Plateau: 5.0",
                "  -> Range: 0-10"
        ));
    }

    @Test
    public void testClampedNormalFloatTooltip() {
        assertTooltip(FloatProviderTooltipUtils.getClampedNormalFloatTooltip(UTILS, ClampedNormalFloat.of(5.0f, 2.0f, 0.0f, 10.0f)).build(), List.of(
                "Clamped Normal:",
                "  -> Mean: 5.0",
                "  -> Deviation: 2.0",
                "  -> Range: 0-10"
        ));
    }
}
