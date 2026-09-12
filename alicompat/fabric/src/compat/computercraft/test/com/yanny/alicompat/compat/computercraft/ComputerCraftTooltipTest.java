package com.yanny.alicompat.compat.computercraft;

import dan200.computercraft.shared.data.BlockNamedEntityLootCondition;
import dan200.computercraft.shared.data.HasComputerIdLootCondition;
import dan200.computercraft.shared.data.PlayerCreativeLootCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class ComputerCraftTooltipTest {
    @Test
    public void testBlockNamedEntityCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, BlockNamedEntityLootCondition.INSTANCE).build(), List.of(
                "Block Is Named"
        ));
    }

    @Test
    public void testHasComputerIdCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, HasComputerIdLootCondition.INSTANCE).build(), List.of(
                "Has Computer Id"
        ));
    }

    @Test
    public void testPlayerCreativeCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, PlayerCreativeLootCondition.INSTANCE).build(), List.of(
                "Player In Creative Mode"
        ));
    }
}
