package com.yanny.alicompat.compat.morered;

import commoble.morered.wires.WireCountLootFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MoreRedTooltipTest {
    @Test
    public void testWireCountFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, WireCountLootFunction.INSTANCE).build(), List.of(
                "Set Wire Count"
        ));
    }
}
