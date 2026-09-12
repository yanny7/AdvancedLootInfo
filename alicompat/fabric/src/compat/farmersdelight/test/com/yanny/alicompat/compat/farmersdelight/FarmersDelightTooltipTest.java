package com.yanny.alicompat.compat.farmersdelight;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;
import vectorwing.farmersdelight.common.loot.function.SmokerCookFunction;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class FarmersDelightTooltipTest {
    @Test
    public void testSmokerCookFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, smokerCook()).build(), List.of(
                "Smoker Cook:"
        ));
    }

    @Test
    public void testSmokerCookFunctionWithPredicate() {
        LootItemFunction function = smokerCook(ExplosionCondition.survivesExplosion().build());

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Smoker Cook:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testCopySkilletFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, CopySkilletFunction.builder().build()).build(), List.of(
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

    @NotNull
    private static LootItemFunction smokerCook(LootItemCondition... conditions) {
        try {
            var constructor = SmokerCookFunction.class.getDeclaredConstructor(List.class);

            constructor.setAccessible(true);
            return constructor.newInstance(List.of(conditions));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create SmokerCookFunction", e);
        }
    }
}
