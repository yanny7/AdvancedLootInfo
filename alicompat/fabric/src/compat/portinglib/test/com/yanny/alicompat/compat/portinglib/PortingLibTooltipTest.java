package com.yanny.alicompat.compat.portinglib;

import io.github.fabricators_of_create.porting_lib.tool.ItemAbilities;
import io.github.fabricators_of_create.porting_lib.tool.loot.CanItemPerformAbility;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class PortingLibTooltipTest {
    @Test
    public void testCanItemPerformAbilityCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new CanItemPerformAbility(ItemAbilities.AXE_STRIP)).build(), List.of(
                "Can Item Perform Ability:",
                "  -> axe_strip"
        ));
    }
}
