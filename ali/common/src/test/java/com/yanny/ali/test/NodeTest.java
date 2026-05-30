package com.yanny.ali.test;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class NodeTest {
    @Test
    public void testSpiderStringDrop() {
        IDataNode node = NodeUtils.getItemNode(
                UTILS,
                (LootItem) LootItem.lootTableItem(Items.STRING)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, UniformGenerator.between(0, 1)))
                        .build(),
                1,
                1,
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTooltip(node.getTooltip(), List.of(
                "Count: 0-2",
                "  -> 0-3 (Looting I)",
                "  -> 0-4 (Looting II)",
                "  -> 0-5 (Looting III)",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 0-2",
                "  -> Add: false",
                "Enchanted Count Increase:",
                "  -> Enchantment: minecraft:looting",
                "  -> Count: 0-1"
        ));
    }

    @Test
    public void testSpiderEyeDrop() {
        IDataNode node = NodeUtils.getItemNode(
                UTILS,
                (LootItem) LootItem.lootTableItem(Items.SPIDER_EYE)
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(-1, 1)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, UniformGenerator.between(0, 1)))
                        .build(),
                1,
                1,
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTooltip(node.getTooltip(), List.of(
                "Count: 0-1",
                "  -> 0-2 (Looting I)",
                "  -> 0-3 (Looting II)",
                "  -> 0-4 (Looting III)",
                "----- Predicates -----",
                "Killed by player",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: -1-1",
                "  -> Add: false",
                "Enchanted Count Increase:",
                "  -> Enchantment: minecraft:looting",
                "  -> Count: 0-1"
        ));
    }

    @Test
    public void testSaplingDrop() {
        IDataNode node = NodeUtils.getItemNode(
                UTILS,
                (LootItem) LootItem.lootTableItem(Items.SPRUCE_SAPLING)
                        .when(ExplosionCondition.survivesExplosion())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), 0.05f, 0.0625f, 0.083333336f, 0.1f))
                        .build(),
                1,
                1,
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTooltip(node.getTooltip(), List.of(
                "Chance: 5%",
                "  -> 6.25% (Fortune I)",
                "  -> 8.33% (Fortune II)",
                "  -> 10% (Fortune III)",
                "Count: 1",
                "----- Predicates -----",
                "Survives Explosion",
                "Table Bonus:",
                "  -> Enchantment: minecraft:fortune",
                "  -> Values: [0.05, 0.0625, 0.0833, 0.1]"
        ));
    }
}
