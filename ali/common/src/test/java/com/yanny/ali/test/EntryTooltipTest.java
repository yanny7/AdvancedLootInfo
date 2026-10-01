package com.yanny.ali.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class EntryTooltipTest {
    @Test
    public void testLootTableTooltip() {
        assertTooltip(TooltipUtils.getLootTableTooltip().build(), List.of(
                "Selects all entries"
        ));
    }

    @Test
    public void testLootPoolTooltip() {
        assertTooltip(TooltipUtils.getLootPoolTooltip(TooltipUtils.rolls(UTILS, UniformGenerator.between(2, 3), ConstantValue.exactly(0))).build(), List.of(
                "Selects random entry",
                "Rolls: 2 to 3"
        ));
        assertTooltip(TooltipUtils.getLootPoolTooltip(TooltipUtils.rolls(UTILS, UniformGenerator.between(2, 3), UniformGenerator.between(1, 2))).build(), List.of(
                "Selects random entry",
                "Rolls: 0 to 10 (luck)"
        ));
    }

    @Test
    public void testAlternativesTooltip() {
        assertTooltip(TooltipUtils.getAlternativesTooltip().build(), List.of(
                "Selects only first successful entry"
        ));
    }

    @Test
    public void testDynamicTooltip() {
        assertTooltip(TooltipUtils.getDynamicTooltip(UTILS, 10, 0.3f, List.of(), List.of()).build(), List.of(
                "Dynamic block-specific drops",
                "Quality: 10",
                "Chance: 30%"
        ));
    }

    @Test
    public void testGroupTooltip() {
        assertTooltip(TooltipUtils.getGroupTooltip().build(), List.of(
                "Selects all entries"
        ));
    }

    @Test
    public void testSequentialTooltip() {
        assertTooltip(TooltipUtils.getSequentialTooltip().build(), List.of(
                "Selects entries sequentially until first failed"
        ));
    }

    @Test
    public void testTooltip() {
        NumberExpr chance = NumberExpr.lookup(TooltipUtils.level(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LOOTING).orElseThrow()), List.of(
                NumberExpr.constant(0.0125), NumberExpr.constant(0.001), NumberExpr.constant(0.003), NumberExpr.constant(0.005)
        ), null);
        LootCount count = LootCount.of(NumberExpr.lookup(TooltipUtils.level(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow()), List.of(
                NumberExpr.range(1, 5), NumberExpr.range(1, 5), NumberExpr.range(1, 10), NumberExpr.range(1, 15)
        ), null));

        assertTooltip(TooltipUtils.getTooltip(
                UTILS,
                3,
                chance,
                count,
                null,
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ).build(), List.of(
                "Quality: 3",
                "Chance: 1.25%",
                "  -> Looting I: 0.1%",
                "  -> Looting II: 0.3%",
                "  -> Looting III: 0.5%",
                "Count: 1 to 5",
                "  -> Fortune I: 1 to 5",
                "  -> Fortune II: 1 to 10",
                "  -> Fortune III: 1 to 15",
                "----- Predicates -----",
                "Survives Explosion",
                "----- Modifiers -----",
                "Explosion Decay"
        ));
        assertTooltip(TooltipUtils.getTooltip(
                UTILS,
                3,
                chance,
                count,
                null,
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ).build(), List.of(
                "Quality: 3",
                "Chance: 1.25%",
                "  -> Looting I: 0.1%",
                "  -> Looting II: 0.3%",
                "  -> Looting III: 0.5%",
                "Count: 1 to 5",
                "  -> Fortune I: 1 to 5",
                "  -> Fortune II: 1 to 10",
                "  -> Fortune III: 1 to 15",
                "----- Predicates -----",
                "Survives Explosion",
                "----- Modifiers -----",
                "Explosion Decay"
        ));
    }
}
