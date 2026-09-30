package com.yanny.ali.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.api.NumberMode;
import com.yanny.aci.api.NumberText;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueCharacterizationTest {
    @Test
    public void testConstantCount() {
        assertEquals(List.of("1.00..1.00"), count(
                SetItemCountFunction.setCount(ConstantValue.exactly(1)).build()
        ));
    }

    @Test
    public void testUniformCount() {
        assertEquals(List.of("0.00..2.00"), count(
                SetItemCountFunction.setCount(UniformGenerator.between(0, 2)).build()
        ));
    }

    @Test
    public void testLootingCount() {
        assertEquals(List.of(
                "0.00..2.00",
                "looting 1: 0.00..3.00 ~1..2 (33%)",
                "looting 2: 0.00..4.00 ~2 (33%)",
                "looting 3: 0.00..5.00 ~2..3 (28%)"
        ), count(
                SetItemCountFunction.setCount(UniformGenerator.between(0, 2)).build(),
                LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 1)).build()
        ));
    }

    @Test
    public void testBinomialBonusCount() {
        assertEquals(List.of(
                "1.00..4.00 ~3 (42%)",
                "fortune 1: 1.00..5.00 ~3 (36%)",
                "fortune 2: 1.00..6.00 ~4 (34%)",
                "fortune 3: 1.00..7.00 ~4..5 (29%)"
        ), count(
                ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3).build()
        ));
    }

    @Test
    public void testOreDropsCount() {
        assertEquals(List.of(
                "1.00..1.00",
                "fortune 1: 1.00..2.00 ~1 (67%)",
                "fortune 2: 1.00..3.00 ~1 (50%)",
                "fortune 3: 1.00..4.00 ~1 (40%)"
        ), count(
                ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE).build()
        ));
    }

    @Test
    public void testClampedUniformBonusCount() {
        assertEquals(List.of(
                "2.00..4.00",
                "fortune 1: 2.00..4.00 ~4 (50%)",
                "fortune 2: 2.00..4.00 ~4 (67%)",
                "fortune 3: 2.00..4.00 ~4 (75%)"
        ), count(
                SetItemCountFunction.setCount(UniformGenerator.between(2, 4)).build(),
                ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE).build(),
                LimitCount.limitCount(IntRange.range(1, 4)).build()
        ));
    }

    @Test
    public void testSetCountAboveMaxStackIsClampedByLimit() {
        NumberExpr count = NodeUtils.getCount(UTILS, List.of(SetItemCountFunction.setCount(ConstantValue.exactly(100)).build())).value();
        NumberInterval limit = NodeUtils.getCountLimit(new ItemStack(Items.STONE));

        assertEquals("100.00..100.00", format(count, 1));
        assertEquals(NumberInterval.point(64), count.bounds().clamp(limit.lo(), limit.hi()));
    }

    @Test
    public void testLootingChance() {
        assertEquals(List.of(
                "2.50..2.50",
                "looting 1: 3.50..3.50",
                "looting 2: 4.50..4.50",
                "looting 3: 5.50..5.50"
        ), chance(
                LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F).build()
        ));
    }

    @Test
    public void testTableBonusChance() {
        assertEquals(List.of(
                "5.00..5.00",
                "fortune 1: 6.25..6.25",
                "fortune 2: 8.33..8.33",
                "fortune 3: 10.00..10.00"
        ), chance(
                BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.05F, 0.0625F, 0.083333336F, 0.1F).build()
        ));
    }

    @Test
    public void testEnchantedItemForEmeraldsPrice() {
        assertEquals("6.00..20.00", format(price(1), 1));
    }

    @Test
    public void testEnchantedItemForEmeraldsPriceCappedAt64() {
        assertEquals("55.00..64.00 ~64 (40%)", format(price(50), 1));
    }

    private static List<String> count(LootItemFunction... functions) {
        return describe(NodeUtils.getCount(UTILS, List.of(functions)).value(), 1);
    }

    private static List<String> chance(LootItemCondition... conditions) {
        return describe(NodeUtils.getChance(UTILS, List.of(conditions), 1), 100);
    }

    private static NumberExpr price(int baseEmeraldCost) {
        VillagerTrades.EnchantedItemForEmeralds listing = new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_SWORD, baseEmeraldCost, 3, 5);
        ItemsToItemsNode node = TradeUtils.getNode(UTILS, listing, TooltipNode.empty());

        return ((IItemNode) node.nodes().get(0)).getCount();
    }

    private static List<String> describe(NumberExpr expr, double scale) {
        List<NumberExpr.Var> levels = expr.vars().stream().filter((v) -> v.type().equals(NumberExpr.LEVEL)).toList();
        List<String> lines = new ArrayList<>();

        lines.add(format(bindLevels(expr, levels, null, 0), scale));

        for (NumberExpr.Var level : levels) {
            String name = ((NumberText.Str) level.args().get(0)).text().replace("minecraft:", "");

            for (int i = 1; i <= level.max(); i++) {
                lines.add(name + " " + i + ": " + format(bindLevels(expr, levels, level, i), scale));
            }
        }

        return lines;
    }

    private static NumberExpr bindLevels(NumberExpr expr, List<NumberExpr.Var> levels, NumberExpr.Var bound, int value) {
        NumberExpr result = expr;

        for (NumberExpr.Var level : levels) {
            result = result.bind(level, level.equals(bound) ? value : 0);
        }

        return result;
    }

    private static String format(NumberExpr value, double scale) {
        NumberInterval bounds = value.bounds();
        Optional<NumberMode> mode = value.mode();
        String text = String.format(Locale.ROOT, "%.2f..%.2f", bounds.lo() * scale, bounds.hi() * scale);

        if (mode.isEmpty() || !mode.get().hasProbability()) {
            return text;
        }

        String modeValue;

        if (mode.get().lo() == mode.get().hi()) {
            modeValue = String.format(Locale.ROOT, "%.0f", mode.get().lo());
        } else {
            modeValue = String.format(Locale.ROOT, "%.0f..%.0f", mode.get().lo(), mode.get().hi());
        }

        return text + " ~" + modeValue + " (" + Math.round(mode.get().probability() * 100) + "%)";
    }
}
