package com.yanny.ali.test;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class EntryTooltipTest {
    @Test
    public void testLootTableTooltip() {
        assertTooltip(EntryTooltipUtils.getLootTableTooltip().build(), List.of(
                "Selects all entries"
        ));
    }

    @Test
    public void testLootPoolTooltip() {
        assertTooltip(EntryTooltipUtils.getLootPoolTooltip(new RangeValue(2, 3), new RangeValue(1, 2)).build(), List.of(
                "Selects random entry",
                "Rolls: 3-5x"
        ));
    }

    @Test
    public void testAlternativesTooltip() {
        assertTooltip(EntryTooltipUtils.getAlternativesTooltip().build(), List.of(
                "Selects only first successful entry"
        ));
    }

    @Test
    public void testDynamicTooltip() {
        assertTooltip(EntryTooltipUtils.getDynamicTooltip(UTILS, 10, 0.3f, List.of(), List.of()).build(), List.of(
                "Dynamic block-specific drops",
                "Quality: 10",
                "Chance: 30%"
        ));
    }

    @Test
    public void testGroupTooltip() {
        assertTooltip(EntryTooltipUtils.getGroupTooltip().build(), List.of(
                "Selects all entries"
        ));
    }

    @Test
    public void testSequentialTooltip() {
        assertTooltip(EntryTooltipUtils.getSequentialTooltip().build(), List.of(
                "Selects entries sequentially until first failed"
        ));
    }

    @Test
    public void testTooltip() {
        EnchantedRanges chanceMap = new EnchantedRanges(1.25F);
        EnchantedRanges countMap = new EnchantedRanges(1, 5);

        chanceMap.computeLevels(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LOOTING).orElseThrow(), (level, value) -> switch (level) {
            case 1 ->  new RangeValue(0.1F);
            case 2 -> new RangeValue(0.3F);
            case 3 -> new RangeValue(0.5F);
            default -> throw new IllegalStateException("Unexpected value: " + level);
        });
        countMap.computeLevels(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), (level, value) -> switch (level) {
            case 1 -> new RangeValue(1, 5);
            case 2 -> new RangeValue(1, 10);
            case 3 -> new RangeValue(1, 15);
            default -> throw new IllegalStateException("Unexpected value: " + level);
        });

        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                3,
                chanceMap,
                countMap,
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ).build(), List.of(
                "Quality: 3",
                "Chance: 1.25%",
                "  -> 0.10% (Looting I)",
                "  -> 0.30% (Looting II)",
                "  -> 0.50% (Looting III)",
                "Count: 1-5",
                "  -> 1-5 (Fortune I)",
                "  -> 1-10 (Fortune II)",
                "  -> 1-15 (Fortune III)",
                "----- Predicates -----",
                "Survives Explosion",
                "----- Modifiers -----",
                "Explosion Decay"
        ));
        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                3,
                chanceMap,
                countMap,
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ).build(), List.of(
                "Quality: 3",
                "Chance: 1.25%",
                "  -> 0.10% (Looting I)",
                "  -> 0.30% (Looting II)",
                "  -> 0.50% (Looting III)",
                "Count: 1-5",
                "  -> 1-5 (Fortune I)",
                "  -> 1-10 (Fortune II)",
                "  -> 1-15 (Fortune III)",
                "----- Predicates -----",
                "Survives Explosion",
                "----- Modifiers -----",
                "Explosion Decay"
        ));
    }
}
