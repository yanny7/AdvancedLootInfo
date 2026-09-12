package com.yanny.alicompat.compat.portinglib;

import io.github.fabricators_of_create.porting_lib.tool.ToolActions;
import io.github.fabricators_of_create.porting_lib.tool.loot.CanToolPerformAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class PortingLibTooltipTest {
    @Test
    public void testCanToolPerformActionCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new CanToolPerformAction(ToolActions.AXE_STRIP)).build(), List.of(
                "Can Tool Perform Action:",
                "  -> axe_strip"
        ));
    }
}
