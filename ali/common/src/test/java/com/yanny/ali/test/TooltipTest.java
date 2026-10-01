package com.yanny.ali.test;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.plugin.common.NodeUtils.getChance;
import static com.yanny.ali.plugin.common.NodeUtils.getCount;
import static com.yanny.ali.plugin.server.TooltipUtils.getChanceTooltip;
import static com.yanny.ali.plugin.server.TooltipUtils.getCountTooltip;
import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class TooltipTest {
    @Test
    public void testChanceTooltip() {
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(), 1)).build(), List.of());

        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceCondition.randomChance(0.25f).build()), 1)).build(), List.of("Chance: 25%"));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceCondition.randomChance(0.25f).build()), 0.5f)).build(), List.of("Chance: 12.5%"));

        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(LOOKUP, 0.1f, 0.2f).build()), 1)).build(), List.of(
                "Chance: 10%",
                "  -> Looting I: 30%",
                "  -> Looting II: 50%",
                "  -> Looting III: 70%"
        ));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(LOOKUP, 0.1f, 0.2f).build()), 0.5f)).build(), List.of(
                "Chance: 5%",
                "  -> Looting I: 15%",
                "  -> Looting II: 25%",
                "  -> Looting III: 35%"
        ));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(new LootItemRandomChanceWithEnchantedBonusCondition(
                    0.1f,
                    LevelBasedValue.lookup(List.of(0.2f, 0.3f, 0.4f), LevelBasedValue.constant(0)),
                    LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING)
                )), 1)).build(), List.of(
                "Chance: 10%",
                "  -> Looting I: 20%",
                "  -> Looting II: 30%",
                "  -> Looting III: 40%"
        ));

        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(BonusLevelTableCondition.bonusLevelFlatChance(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 0.1f, 0.2f, 0.3f, 0.4f).build()), 1)).build(), List.of(
                "Chance: 10%",
                "  -> Fortune I: 20%",
                "  -> Fortune II: 30%",
                "  -> Fortune III: 40%"
        ));
        assertTooltip(getChanceTooltip(getChance(UTILS, List.of(BonusLevelTableCondition.bonusLevelFlatChance(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 0.1f, 0.2f, 0.3f, 0.4f).build()), 0.5f)).build(), List.of(
                "Chance: 5%",
                "  -> Fortune I: 10%",
                "  -> Fortune II: 15%",
                "  -> Fortune III: 20%"
        ));
    }

    @Test
    public void testCountTooltip() {
        assertTooltip(getCountTooltip(getCount(UTILS, List.of()), null).build(), List.of("Count: 1"));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(SetItemCountFunction.setCount(ConstantValue.exactly(10)).build())), null).build(), List.of("Count: 10"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(ConstantValue.exactly(5), false).build(),
                SetItemCountFunction.setCount(ConstantValue.exactly(5), true).build()
        )), null).build(), List.of("Count: 10"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(5, 0.5f)).build())), null).build(), List.of("Count: 0 to 5  ~2 to 3 (31%)"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(SetItemCountFunction.setCount(UniformGenerator.between(1, 9)).build())), null).build(), List.of("Count: 1 to 9"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(UniformGenerator.between(1, 4)).build(),
                SetItemCountFunction.setCount(ConstantValue.exactly(2), true).build()
        )), null).build(), List.of("Count: 3 to 6"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(UniformGenerator.between(1, 4)).build(),
                SetItemCountFunction.setCount(UniformGenerator.between(2, 4), true).build()
        )), null).build(), List.of("Count: 3 to 8  ~5 to 6 (25%)"));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addOreBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow()).build())), null).build(), List.of(
                "Count: 1",
                "  -> Fortune I: 1 to 2  ~1 (67%)",
                "  -> Fortune II: 1 to 3  ~1 (50%)",
                "  -> Fortune III: 1 to 4  ~1 (40%)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addUniformBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow()).build())), null).build(), List.of(
                "Count: 1",
                "  -> Fortune I: 1 to 2",
                "  -> Fortune II: 1 to 3",
                "  -> Fortune III: 1 to 4"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addUniformBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 2).build())), null).build(), List.of(
                "Count: 1",
                "  -> Fortune I: 1 to 3",
                "  -> Fortune II: 1 to 5",
                "  -> Fortune III: 1 to 7"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(ApplyBonusCount.addBonusBinomialDistributionCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 0.5f, 3).build())), null).build(), List.of(
                "Count: 1 to 4  ~2 to 3 (38%)",
                "  -> Fortune I: 1 to 5  ~3 (38%)",
                "  -> Fortune II: 1 to 6  ~3 to 4 (31%)",
                "  -> Fortune III: 1 to 7  ~4 (31%)"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.range(1, 5)).build())), null).build(), List.of("Count: 1"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.range(2, 5)).build())), null).build(), List.of("Count: 2"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.exact(3)).build())), null).build(), List.of("Count: 3"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.lowerBound(4)).build())), null).build(), List.of("Count: 4"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(LimitCount.limitCount(IntRange.upperBound(0)).build())), null).build(), List.of("Count: 0"));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 2).build(),
                LimitCount.limitCount(IntRange.upperBound(6)).build()
        )), null).build(), List.of(
                "Count: 1",
                "  -> Fortune I: 1 to 3",
                "  -> Fortune II: 1 to 5",
                "  -> Fortune III: 1 to 6  ~6 (29%)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 2).build(),
                LimitCount.limitCount(IntRange.lowerBound(2)).build()
        )), null).build(), List.of(
                "Count: 2",
                "  -> Fortune I: 2 to 3  ~2 (67%)",
                "  -> Fortune II: 2 to 5  ~2 (40%)",
                "  -> Fortune III: 2 to 7  ~2 (29%)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 2).build(),
                LimitCount.limitCount(IntRange.range(2, 6)).build()
        )), null).build(), List.of(
                "Count: 2",
                "  -> Fortune I: 2 to 3  ~2 (67%)",
                "  -> Fortune II: 2 to 5  ~2 (40%)",
                "  -> Fortune III: 2 to 6"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, ConstantValue.exactly(2)).build())), null).build(), List.of(
                "Count: 1",
                "  -> Looting I: 3",
                "  -> Looting II: 5",
                "  -> Looting III: 7"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, BinomialDistributionGenerator.binomial(3, 0.5f)).build())), null).build(), List.of(
                "Count: 1",
                "  -> Looting I: 1 to 4  ~2 to 3 (38%)",
                "  -> Looting II: 1 to 7",
                "  -> Looting III: 1 to 10"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, UniformGenerator.between(1, 4)).build())), null).build(), List.of(
                "Count: 1",
                "  -> Looting I: 2 to 5  ~3 to 4 (33%)",
                "  -> Looting II: 3 to 9  ~4 to 8 (17%)",
                "  -> Looting III: 4 to 13  ~5 to 12 (11%)"
        ));
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, UniformGenerator.between(1, 4)).setLimit(12).build())), null).build(), List.of(
                "Count: 1",
                "  -> Looting I: 2 to 5  ~3 to 4 (33%)",
                "  -> Looting II: 3 to 9  ~4 to 8 (17%)",
                "  -> Looting III: 4 to 12  ~12 (17%)"
        ));
    }

    @Test
    public void testItemTooltipHidesFoldedFunctions() {
        List<LootItemFunction> functions = List.of(SetItemCountFunction.setCount(ConstantValue.exactly(10)).build());

        assertTooltip(itemTooltip(functions, List.of()), false, List.of(
                "Count: 10"
        ));
        assertTooltip(itemTooltip(functions, List.of()), true, List.of(
                "Count: 10",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 10",
                "  -> Add: false"
        ));
    }

    @Test
    public void testItemTooltipHidesFoldedConditions() {
        List<LootItemCondition> conditions = List.of(LootItemRandomChanceCondition.randomChance(0.25f).build());

        assertTooltip(itemTooltip(List.of(), conditions), false, List.of(
                "Chance: 25%",
                "Count: 1"
        ));
        assertTooltip(itemTooltip(List.of(), conditions), true, List.of(
                "Chance: 25%",
                "Count: 1",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Chance: 0.25"
        ));
    }

    @Test
    public void testItemTooltipKeepsConditionalFunctions() {
        List<LootItemFunction> functions = List.of(SetItemCountFunction.setCount(ConstantValue.exactly(10))
                .when(ExplosionCondition.survivesExplosion()).build());

        assertTooltip(itemTooltip(functions, List.of()), false, List.of(
                "Count: ",
                "  -> 10",
                "    -> Survives Explosion",
                "  -> otherwise 1",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 10",
                "  -> Add: false",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testConditionalCount() {
        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(ConstantValue.exactly(10)).when(ExplosionCondition.survivesExplosion()).build()
        )), null).build(), List.of(
                "Count: ",
                "  -> 10",
                "    -> Survives Explosion",
                "  -> otherwise 1"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(ConstantValue.exactly(5)).build(),
                SetItemCountFunction.setCount(ConstantValue.exactly(2), true).when(ExplosionCondition.survivesExplosion()).build()
        )), null).build(), List.of(
                "Count: ",
                "  -> 7",
                "    -> Survives Explosion",
                "  -> otherwise 5"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(UniformGenerator.between(4, 8)).build(),
                LimitCount.limitCount(IntRange.upperBound(6)).when(ExplosionCondition.survivesExplosion()).build()
        )), null).build(), List.of(
                "Count: ",
                "  -> 4 to 6  ~6 (60%)",
                "    -> Survives Explosion",
                "  -> otherwise 4 to 8"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                ApplyBonusCount.addUniformBonusCount(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 2).when(ExplosionCondition.survivesExplosion()).build()
        )), null).build(), List.of(
                "Count: ",
                "  -> 1",
                "    -> Survives Explosion",
                "    -> Fortune I: 1 to 3",
                "    -> Fortune II: 1 to 5",
                "    -> Fortune III: 1 to 7",
                "  -> otherwise 1"
        ));

        assertTooltip(getCountTooltip(getCount(UTILS, List.of(
                SetItemCountFunction.setCount(ConstantValue.exactly(10)).when(LootItemRandomChanceCondition.randomChance(0.25f)).build()
        )), null).build(), List.of(
                "Count: 1 to 10  ~1 (75%)",
                "  -> 10 (25%)",
                "  -> 1 (75%)"
        ));
    }

    @NotNull
    private static TooltipNode itemTooltip(List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return TooltipUtils.getTooltip(UTILS, LootPoolSingletonContainer.DEFAULT_QUALITY,
                getChance(UTILS, conditions, 1), getCount(UTILS, functions), null, functions, conditions).build();
    }
}
