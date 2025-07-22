package com.yanny.ali.test;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.plugin.server.EntryTooltipUtils.getChanceTooltip;
import static com.yanny.ali.plugin.server.EntryTooltipUtils.getCountTooltip;
import static com.yanny.ali.plugin.server.TooltipUtils.getChance;
import static com.yanny.ali.plugin.server.TooltipUtils.getCount;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class TooltipTest {
    @Test
    public void testChanceTooltip() {
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(), 1)), List.of());

        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceCondition.randomChance(0.25f).build()), 1)), List.of("Chance: 25%"));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceCondition.randomChance(0.25f).build()), 0.5f)), List.of("Chance: 12.50%"));

        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.1f, 0.2f).build()), 1)), List.of(
                "Chance: 10%",
                "  -> 30% (Looting I)",
                "  -> 50% (Looting II)",
                "  -> 70% (Looting III)"
        ));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.1f, 0.2f).build()), 0.5f)), List.of(
                "Chance: 5%",
                "  -> 15% (Looting I)",
                "  -> 25% (Looting II)",
                "  -> 35% (Looting III)"
        ));

        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1f, 0.2f, 0.3f, 0.4f).build()), 1)), List.of(
                "Chance: 10%",
                "  -> 20% (Fortune I)",
                "  -> 30% (Fortune II)",
                "  -> 40% (Fortune III)"
        ));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1f, 0.2f, 0.3f, 0.4f).build()), 0.5f)), List.of(
                "Chance: 5%",
                "  -> 10% (Fortune I)",
                "  -> 15% (Fortune II)",
                "  -> 20% (Fortune III)"
        ));
    }

    @Test
    public void testCountTooltip() {
        assertTooltip(getCountTooltip(getCount(UTILS, List.of())), List.of("Count: 1"));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(SetItemCountFunction.setCount(ConstantValue.exactly(10)).build()))), List.of("Count: 10"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(ConstantValue.exactly(5), false).build(),
                SetItemCountFunction.setCount(ConstantValue.exactly(5), true).build()
        ))), List.of("Count: 10"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(5, 0.5f)).build()))), List.of("Count: 0-5"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(SetItemCountFunction.setCount(UniformGenerator.between(1, 9)).build()))), List.of("Count: 1-9"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(UniformGenerator.between(1, 4)).build(),
                SetItemCountFunction.setCount(ConstantValue.exactly(2), true).build()
        ))), List.of("Count: 3-6"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(UniformGenerator.between(1, 4)).build(),
                SetItemCountFunction.setCount(UniformGenerator.between(2, 4), true).build()
        ))), List.of("Count: 3-8"));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE).build()))), List.of(
                "Count: 1",
                "  -> 1-2 (Fortune I)",
                "  -> 1-3 (Fortune II)",
                "  -> 1-4 (Fortune III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE).build()))), List.of(
                "Count: 1",
                "  -> 1-2 (Fortune I)",
                "  -> 1-3 (Fortune II)",
                "  -> 1-4 (Fortune III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2).build()))), List.of(
                "Count: 1",
                "  -> 1-3 (Fortune I)",
                "  -> 1-5 (Fortune II)",
                "  -> 1-7 (Fortune III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5f, 3).build()))), List.of(
                "Count: 1-4",
                "  -> 1-5 (Fortune I)",
                "  -> 1-6 (Fortune II)",
                "  -> 1-7 (Fortune III)"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.range(1, 5)).build()))), List.of("Count: 1"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.range(2, 5)).build()))), List.of("Count: 2"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.exact(3)).build()))), List.of("Count: 3"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.lowerBound(4)).build()))), List.of("Count: 4"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.upperBound(0)).build()))), List.of("Count: 0"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2).build(),
                LimitCount.limitCount(IntRange.upperBound(6)).build()
        ))), List.of(
                "Count: 1",
                "  -> 1-3 (Fortune I)",
                "  -> 1-5 (Fortune II)",
                "  -> 1-6 (Fortune III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2).build(),
                LimitCount.limitCount(IntRange.lowerBound(2)).build()
        ))), List.of(
                "Count: 2",
                "  -> 2-3 (Fortune I)",
                "  -> 2-5 (Fortune II)",
                "  -> 2-7 (Fortune III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2).build(),
                LimitCount.limitCount(IntRange.range(2, 6)).build()
        ))), List.of(
                "Count: 2",
                "  -> 2-3 (Fortune I)",
                "  -> 2-5 (Fortune II)",
                "  -> 2-6 (Fortune III)"
        ));
        
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LootingEnchantFunction.lootingMultiplier(ConstantValue.exactly(2)).build()))), List.of(
                "Count: 1",
                "  -> 3 (Looting I)",
                "  -> 5 (Looting II)",
                "  -> 7 (Looting III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LootingEnchantFunction.lootingMultiplier(BinomialDistributionGenerator.binomial(3, 0.5f)).build()))), List.of(
                "Count: 1",
                "  -> 1-4 (Looting I)",
                "  -> 1-7 (Looting II)",
                "  -> 1-10 (Looting III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(1, 4)).build()))), List.of(
                "Count: 1",
                "  -> 2-5 (Looting I)",
                "  -> 3-9 (Looting II)",
                "  -> 4-13 (Looting III)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(1, 4)).setLimit(12).build()))), List.of(
                "Count: 1",
                "  -> 2-5 (Looting I)",
                "  -> 3-9 (Looting II)",
                "  -> 4-12 (Looting III)"
        ));
    }
}
