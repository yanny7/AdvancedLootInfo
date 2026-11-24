package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.LootEntry;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.lootjs.Utils;
import com.yanny.ali.lootjs.mixin.MixinLootEntry;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.common.nodes.EmptyNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsUtilsTest {
    @Test
    public void testItemGeneratorNode() {
        ItemStackNode node = (ItemStackNode) getEntry(new ItemStack(Items.DIAMOND, 3), 1, 4, null);

        Assertions.assertEquals(0.25F, node.getChance());
    }

    @Test
    public void testItemGeneratorAirNode() {
        IDataNode node = getEntry(new ItemStack(Items.AIR), 1, 4, null);

        Assertions.assertEquals(EmptyNode.ID, node.getId());
        Assertions.assertEquals(0.25F, node.getChance());
        assertTooltip(node.getTooltip(), List.of(
                "Drops nothing",
                "Chance: 25%"
        ));
    }

    @Test
    public void testItemGeneratorNodeWithPreservedCount() {
        ItemStackNode node = (ItemStackNode) getEntry(new ItemStack(Items.DIAMOND, 3), 1, 1, NumberExpr.constant(6));

        Assertions.assertEquals("6", slot(node));
    }

    @Test
    public void testMergesEntryModifiersWithPassedOnes() {
        LootEntry entry = entry(new ItemStack(Items.DIAMOND), 1,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(5), true).build()),
                List.of(LootItemRandomChanceCondition.randomChance(0.5F).build())
        );
        ItemStackNode node = (ItemStackNode) Utils.getEntry(UTILS, entry, 1, 1,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(2)).build()),
                List.of(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                null
        );

        Assertions.assertEquals("7", slot(node));
        assertTooltip(node.getTooltip(), List.of(
                "Chance: 12.5%",
                "Count: 7",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Probability: 0.25",
                "Random Chance:",
                "  -> Probability: 0.5",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 2",
                "  -> Add: False",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: True"
        ));
    }

    private static IDataNode getEntry(ItemStack itemStack, int weight, int sumWeight, NumberExpr preservedCount) {
        return Utils.getEntry(UTILS, entry(itemStack, weight, List.of(), List.of()), 1, sumWeight, List.of(), List.of(), preservedCount);
    }

    private static LootEntry entry(ItemStack itemStack, int weight, List<LootItemFunction> postModifications, List<LootItemCondition> conditions) {
        LootEntry entry = mock(LootEntry.class, MixinLootEntry.class);

        Mockito.when(((MixinLootEntry) entry).getItemStack()).thenReturn(itemStack);
        Mockito.when(((MixinLootEntry) entry).getPostModifications()).thenReturn(postModifications);
        Mockito.when(((MixinLootEntry) entry).getConditions()).thenReturn(conditions);
        Mockito.when(((MixinLootEntry) entry).getWeight()).thenReturn(weight);
        Mockito.when(entry.getWeight()).thenReturn(weight);
        return entry;
    }

    private static String slot(IItemNode node) {
        return NumberFormatter.slot(TooltipUtils.getSlotCount(node));
    }
}
