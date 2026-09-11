package com.yanny.alicompat.compat.enderio;

import com.enderio.armory.common.item.darksteel.upgrades.direct.DirectUpgradeLootCondition;
import com.enderio.base.common.loot.SetLootCapacitorFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class EnderIoTooltipTest {
    @Test
    public void testSetLootCapacitorFunction() {
        LootItemFunction function = SetLootCapacitorFunction.setLootCapacitor(UniformGenerator.between(1.0F, 4.0F)).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Set Loot Capacitor:",
                "  -> Range: 1-4"
        ));
    }

    @Test
    public void testDirectUpgradeCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new DirectUpgradeLootCondition()).build(), List.of(
                "Has Direct Upgrade"
        ));
    }
}
