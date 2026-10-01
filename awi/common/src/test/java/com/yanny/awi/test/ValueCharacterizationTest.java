package com.yanny.awi.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.api.NumberMode;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import org.junit.jupiter.api.Test;

import static com.yanny.awi.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueCharacterizationTest {
    private static final ColumnContext CTX = new ColumnContext(-64, 384);

    @Test
    public void testClampedUniformInt() {
        NumberExpr expr = UTILS.convertIntProvider(UTILS, ClampedInt.of(UniformInt.of(-10, 4), 0, 4));

        assertEquals(NumberInterval.closed(0, 4), expr.bounds());
        assertMode(expr, 0, 0, 11.0 / 15);
    }

    @Test
    public void testClampedNormalInt() {
        NumberExpr expr = UTILS.convertIntProvider(UTILS, ClampedNormalInt.of(3, 1, 1, 5));

        assertEquals(NumberInterval.closed(1, 5), expr.bounds());
        assertMode(expr, 2, 3, 0.3413);
    }

    @Test
    public void testWeightedListInt() {
        WeightedList<IntProvider> distribution = WeightedList.<IntProvider>builder()
                .add(ConstantInt.of(1), 9)
                .add(ConstantInt.of(2), 1)
                .build();
        NumberExpr expr = UTILS.convertIntProvider(UTILS, new WeightedListInt(distribution));

        assertEquals(NumberInterval.closed(1, 2), expr.bounds());
        assertMode(expr, 1, 1, 0.9);
    }

    @Test
    public void testBiasedToBottomHeight() {
        NumberExpr expr = UTILS.convertHeightProvider(UTILS, BiasedToBottomHeight.of(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16), 1), CTX);
        double probability = 0;

        for (int k = 1; k <= 80; k++) {
            probability += 1.0 / 80 / k;
        }

        assertEquals(NumberInterval.closed(-64, 15), expr.bounds(), "vanilla never reaches max with inner = 1");
        assertMode(expr, -64, -64, probability);
    }

    private static void assertMode(NumberExpr expr, double lo, double hi, double probability) {
        NumberMode mode = expr.mode().orElseThrow();

        assertEquals(lo, mode.lo());
        assertEquals(hi, mode.hi());
        assertEquals(probability, mode.probability(), 1e-4);
    }
}
