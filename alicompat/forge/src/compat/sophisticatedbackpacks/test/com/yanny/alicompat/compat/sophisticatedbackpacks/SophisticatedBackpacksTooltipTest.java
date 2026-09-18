package com.yanny.alicompat.compat.sophisticatedbackpacks;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.p3pp3rf1y.sophisticatedbackpacks.data.CopyBackpackDataFunction;
import net.p3pp3rf1y.sophisticatedbackpacks.data.SBLootEnabledCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SophisticatedBackpacksTooltipTest {
    @Test
    public void testCopyBackpackDataFunction() {
        LootItemFunction function = CopyBackpackDataFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Backpack Data:"
        ));
    }

    @Test
    public void testCopyBackpackDataFunctionWithPredicate() {
        LootItemFunction function = CopyBackpackDataFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Backpack Data:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testLootEnabledCondition() {
        LootItemCondition condition = SBLootEnabledCondition.builder().build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Chest Loot Enabled"
        ));
    }
}
