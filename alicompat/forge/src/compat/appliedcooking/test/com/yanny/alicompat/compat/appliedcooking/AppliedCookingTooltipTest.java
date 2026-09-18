package com.yanny.alicompat.compat.appliedcooking;

import dev.smolinacadena.appliedcooking.lootable.KitchenStationBlockLootFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class AppliedCookingTooltipTest {
    @Test
    public void testKitchenStationFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, new KitchenStationBlockLootFunction(new LootItemCondition[0])).build(), List.of(
                "Copy Kitchen Station Data:"
        ));
    }

    @Test
    public void testKitchenStationFunctionWithPredicate() {
        LootItemCondition[] conditions = {ExplosionCondition.survivesExplosion().build()};

        assertTooltip(UTILS.getFunctionTooltip(UTILS, new KitchenStationBlockLootFunction(conditions)).build(), List.of(
                "Copy Kitchen Station Data:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }
}
