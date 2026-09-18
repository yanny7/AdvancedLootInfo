package com.yanny.alicompat.compat.farmersdelight;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.junit.jupiter.api.Test;
import vectorwing.farmersdelight.common.loot.function.CopyMealFunction;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class FarmersDelightTooltipTest {
    @Test
    public void testCopyMealFunction() {
        LootItemFunction function = CopyMealFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Meal:"
        ));
    }

    @Test
    public void testCopyMealFunctionWithPredicate() {
        LootItemFunction function = CopyMealFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Meal:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testCopySkilletFunction() {
        LootItemFunction function = CopySkilletFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Skillet:"
        ));
    }

    @Test
    public void testCopySkilletFunctionWithPredicate() {
        LootItemFunction function = CopySkilletFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Skillet:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }
}
