package com.yanny.awi.test;

import com.yanny.aci.test.utils.TestUtils;
import com.yanny.aci.tooltip.CoreTooltipUtils;
import com.yanny.aci.tooltip.NumberOptions;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.aci.tooltip.TooltipStyle;
import com.yanny.awi.language.Lang;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntProviderTooltipTest {
    private static final NumberOptions FORMULAS = new NumberOptions(true, false, Locale.ROOT);

    @Test
    public void testSingleKeyGetsValueAsChild() {
        assertTooltip(UTILS.getValueTooltip(UTILS, UniformInt.of(0, 10)).build(Lang.Branch.XZ_SPREAD), List.of(
                "XZ Spread:",
                "  -> 0 to 10"
        ));
    }

    @Test
    public void testMultiKeyMergesValue() {
        assertTooltip(UTILS.getValueTooltip(UTILS, ConstantInt.of(5)).build(Lang.Branch.HEIGHT), List.of(
                "Height: 5"
        ));
    }

    @Test
    public void testClampedUniformInt() {
        TooltipNode node = UTILS.getValueTooltip(UTILS, ClampedInt.of(UniformInt.of(-10, 4), 0, 4)).build(Lang.Branch.HEIGHT);

        assertTooltip(node, List.of("Height: 0 to 4  ~0 (73%)"));
        assertEquals(List.of("Height: clamp(U[−10; 4]; 0; 4)  ~0 (73%)"), lines(node, FORMULAS));
    }

    @Test
    public void testClampedNormalInt() {
        TooltipNode node = UTILS.getValueTooltip(UTILS, ClampedNormalInt.of(3, 1, 1, 5)).build(Lang.Branch.HEIGHT);

        assertTooltip(node, List.of("Height: 1 to 5  ~2 to 3 (34%)"));
        assertEquals(List.of("Height: trunc(clamp(normal(3; 1); 1; 5))  ~2 to 3 (34%)"), lines(node, FORMULAS));
    }

    @Test
    public void testWeightedListInt() {
        TooltipNode node = UTILS.getValueTooltip(UTILS, new WeightedListInt(WeightedList.<IntProvider>builder()
                .add(ConstantInt.of(1), 9)
                .add(ConstantInt.of(2), 1)
                .build())).build(Lang.Branch.HEIGHT);

        assertTooltip(node, List.of(
                "Height: 1 to 2  ~1 (90%)",
                "  -> 1 (90%)",
                "  -> 2 (10%)"
        ));
    }

    @Test
    public void testUniformFloat() {
        TooltipNode node = UTILS.getValueTooltip(UTILS, UniformFloat.of(0.15f, 0.8f)).build(Lang.Branch.HEIGHT);

        assertTooltip(node, List.of("Height: 0.15 to 0.8"));
        assertEquals(List.of("Height: U[0.15; 0.8)"), lines(node, FORMULAS));
    }

    @Test
    public void testClampedNormalFloat() {
        assertTooltip(UTILS.getValueTooltip(UTILS, ClampedNormalFloat.of(4.5f, 1, 2, 7)).build(Lang.Branch.HEIGHT), List.of(
                "Height: 2 to 7  ~4.5"
        ));
    }

    @Test
    public void testBiasedToBottomHeightFormula() {
        TooltipNode node = TooltipBuilder.number(UTILS.convertHeightProvider(UTILS,
                BiasedToBottomHeight.of(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16), 1), new ColumnContext(-64, 384), List.of())).build(Lang.Value.HEIGHT);

        assertTooltip(node, List.of("Height: −64 to 15  ~−64 (6%)"));
        assertEquals(List.of("Height: biased(−64; 16)  ~−64 (6%)"), lines(node, FORMULAS));
    }

    @Test
    public void testTrapezoidInt() {
        TooltipNode node = UTILS.getValueTooltip(UTILS, TrapezoidInt.of(0, 10, 3)).build(Lang.Branch.HEIGHT);

        assertTooltip(node, List.of("Height: 0 to 10  ~3 to 7 (13%)"));
        assertEquals(List.of("Height: trapezoid(0; 10; 3)  ~3 to 7 (13%)"), lines(node, FORMULAS));
    }

    @Test
    public void testTriangleInt() {
        TooltipNode node = UTILS.getValueTooltip(UTILS, TrapezoidInt.triangle(3)).build(Lang.Branch.HEIGHT);

        assertTooltip(node, List.of("Height: −3 to 3  ~0 (25%)"));
        assertEquals(List.of("Height: trapezoid(−3; 3; 0)  ~0 (25%)"), lines(node, FORMULAS));
    }

    private static List<String> lines(TooltipNode node, NumberOptions options) {
        return TestUtils.toComponents(CoreTooltipUtils.toLines(node, 0, true, TooltipStyle.DEFAULT, options)).stream()
                .map(TestUtils::componentToPlainString)
                .toList();
    }
}
