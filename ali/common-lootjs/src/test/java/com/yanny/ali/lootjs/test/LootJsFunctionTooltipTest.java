package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.loot.action.CustomPlayerAction;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.lootjs.server.LootJsFunctionTooltipUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsFunctionTooltipTest {
    @Test
    public void testCustomPlayerTooltip() {
        assertTooltip(LootJsFunctionTooltipUtils.customPlayerTooltip(UTILS, new CustomPlayerFunction(new CustomPlayerAction((a) -> {}))).build(), List.of(
                "Custom Player Modifier:",
                "  -> Detail Not Available"
        ));
    }

    @Test
    public void testModifiedItemTooltip() {
        assertTooltip(LootJsFunctionTooltipUtils.modifiedItemTooltip(UTILS, new ModifiedItemFunction()).build(), List.of(
                "Modified dynamically!"
        ));
    }
}
