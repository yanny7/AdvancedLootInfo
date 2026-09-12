package com.yanny.alicompat.compat.deeperdarker;

import com.kyanite.deeperdarker.util.SetPaintingVariantFunction;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class DeeperDarkerTooltipTest {
    @Test
    public void testSetPaintingVariantFunctionWithTag() {
        LootItemFunction function = SetPaintingVariantFunction.withTag(PaintingVariantTags.PLACEABLE).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Set Painting Variant:",
                "  -> Tag: minecraft:placeable"
        ));
    }

    @Test
    public void testSetPaintingVariantFunctionRandom() {
        LootItemFunction function = SetPaintingVariantFunction.random().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Set Painting Variant:"
        ));
    }

    @Test
    public void testSetPaintingVariantFunctionWithPredicate() {
        LootItemFunction function = SetPaintingVariantFunction.random().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Set Painting Variant:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }
}
