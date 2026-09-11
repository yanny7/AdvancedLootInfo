package com.yanny.alicompat.compat.sophisticatedstorage;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.p3pp3rf1y.sophisticatedstorage.data.CopyStorageDataFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SophisticatedStorageTooltipTest {
    @Test
    public void testCopyStorageDataFunction() {
        LootItemFunction function = CopyStorageDataFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Storage Data:"
        ));
    }

    @Test
    public void testCopyStorageDataFunctionWithPredicate() {
        LootItemFunction function = CopyStorageDataFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Storage Data:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }
}
