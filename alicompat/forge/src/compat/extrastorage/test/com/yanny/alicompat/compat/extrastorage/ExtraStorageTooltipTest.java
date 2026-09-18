package com.yanny.alicompat.compat.extrastorage;

import edivad.extrastorage.loottable.AdvancedCrafterLootFunction;
import edivad.extrastorage.loottable.StorageBlockLootFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class ExtraStorageTooltipTest {
    @Test
    public void testStorageBlockFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, StorageBlockLootFunction.builder().build()).build(), List.of(
                "Copy Storage Id:"
        ));
    }

    @Test
    public void testStorageBlockFunctionWithPredicate() {
        LootItemFunction function = StorageBlockLootFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Storage Id:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testAdvancedCrafterFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, AdvancedCrafterLootFunction.builder().build()).build(), List.of(
                "Copy Crafter Name:"
        ));
    }

    @Test
    public void testAdvancedCrafterFunctionWithPredicate() {
        LootItemFunction function = AdvancedCrafterLootFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Crafter Name:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }
}
