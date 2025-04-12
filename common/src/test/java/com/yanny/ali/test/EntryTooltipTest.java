package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class EntryTooltipTest {
    @Test
    public void testLootTableTooltip() {
        assertTooltip(EntryTooltipUtils.getLootTableTooltip(0, -2, 2.5F), List.of(
                "Selects all entries",
                "Quality: -2",
                "Chance: 2.50%"
        ));
    }

    @Test
    public void testLootPoolTooltip() {
        assertTooltip(EntryTooltipUtils.getLootPoolTooltip(0, new RangeValue(2, 3), new RangeValue(1, 2)), List.of(
                "Selects random entry",
                "Rolls: 3-5x"
        ));
    }

    @Test
    public void testAlternativesTooltip() {
        assertTooltip(EntryTooltipUtils.getAlternativesTooltip(0), List.of(
                "Selects only first successful entry"
        ));
    }

    @Test
    public void testDynamicTooltip() {
        assertTooltip(EntryTooltipUtils.getDynamicTooltip(0, (DynamicLoot) DynamicLoot.dynamicEntry(new ResourceLocation("test")).setWeight(3).setQuality(10).build(), 10), List.of(
                "Dynamic block-specific drops",
                "Quality: 10",
                "Chance: 30%"
        ));
    }

    @Test
    public void testEmptyTooltip() {
        assertTooltip(EntryTooltipUtils.getEmptyTooltip(
                UTILS,
                (EmptyLootItem) EmptyLootItem.emptyItem().setQuality(1).setWeight(1).build(),
                2,
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ), List.of(
                "Quality: 1",
                "Chance: 50%",
                "Count: 1",
                "----- Conditions -----",
                "Must survive explosion",
                "----- Functions -----",
                "Explosion Decay"
        ));
    }

    @Test
    public void testGroupTooltip() {
        assertTooltip(EntryTooltipUtils.getGroupTooltip(0), List.of(
                "Selects all entries"
        ));
    }

    @Test
    public void testSequentialTooltip() {
        assertTooltip(EntryTooltipUtils.getSequentialTooltip(0), List.of(
                "Selects entries sequentially until first failed"
        ));
    }

    @Test
    public void testTooltip() {
        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                LootItem.lootTableItem(Items.ANDESITE).setQuality(3).setWeight(1).build(),
                new RangeValue(1.25F),
                Optional.of(Pair.of(Holder.direct(Enchantments.MOB_LOOTING), Map.of(1, new RangeValue(0.1F), 2, new RangeValue(0.3F)))),
                new RangeValue(1, 5),
                Optional.of(Pair.of(Holder.direct(Enchantments.BLOCK_FORTUNE), Map.of(1, new RangeValue(1, 5), 2, new RangeValue(1, 10)))),
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ), List.of(
                "Quality: 3",
                "Chance: 1.25%",
                "  -> 0.10% (Looting I)",
                "  -> 0.30% (Looting II)",
                "Count: 1-5",
                "  -> 1-5 (Fortune I)",
                "  -> 1-10 (Fortune II)",
                "----- Conditions -----",
                "Must survive explosion",
                "----- Functions -----",
                "Explosion Decay"
        ));
        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                TagEntry.expandTag(ItemTags.LOGS).setQuality(3).setWeight(1).build(),
                new RangeValue(1.25F),
                Optional.of(Pair.of(Holder.direct(Enchantments.MOB_LOOTING), Map.of(1, new RangeValue(0.1F), 2, new RangeValue(0.3F)))),
                new RangeValue(1, 5),
                Optional.of(Pair.of(Holder.direct(Enchantments.BLOCK_FORTUNE), Map.of(1, new RangeValue(1, 5), 2, new RangeValue(1, 10)))),
                List.of(ApplyExplosionDecay.explosionDecay().build()),
                List.of(ExplosionCondition.survivesExplosion().build())
        ), List.of(
                "Quality: 3",
                "Chance: 1.25%",
                "  -> 0.10% (Looting I)",
                "  -> 0.30% (Looting II)",
                "Count: 1-5",
                "  -> 1-5 (Fortune I)",
                "  -> 1-10 (Fortune II)",
                "----- Conditions -----",
                "Must survive explosion",
                "----- Functions -----",
                "Explosion Decay"
        ));
    }
}
