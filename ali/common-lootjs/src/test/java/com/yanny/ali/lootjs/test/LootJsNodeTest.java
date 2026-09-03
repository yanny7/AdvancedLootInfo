package com.yanny.ali.lootjs.test;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsNodeTest {
    @Test
    public void testItemStackNodeTooltip() {
        ItemStackNode node = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND, 3), 0.5F, List.of(), List.of(), null);

        assertTooltip(node.getTooltip(), List.of(
                "Chance: 50%",
                "Count: 3"
        ));
    }

    @Test
    public void testItemStackNodePreservedCount() {
        ItemStackNode node = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND, 3), 1.0F, List.of(), List.of(), new RangeValue(2, 5));

        Assertions.assertEquals("2-5", node.getCount().toIntString());
    }

    @Test
    public void testItemStackNodePreservedCountClampedToMaxStackSize() {
        ItemStackNode node = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND_SWORD), 1.0F, List.of(), List.of(), new RangeValue(3, 8));

        Assertions.assertEquals("1", node.getCount().toIntString());
    }

    @Test
    public void testItemStackNodeModifiers() {
        ItemStackNode node = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND), 1.0F,
                List.of(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)).build()),
                List.of(chance(0.25F)),
                null
        );

        assertTooltip(node.getTooltip(), List.of(
                "Chance: 25%",
                "Count: 2-4",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Chance: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 2-4",
                "  -> Add: false"
        ));
    }

    @Test
    public void testItemStackNodeRetainItems() {
        ItemStackNode node = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND), 1.0F, List.of(), List.of(), null);

        node.retainItems((stack) -> false);
        Assertions.assertTrue(node.getItems().isEmpty());
    }

    @Test
    public void testItemStackNodeModifiedFlag() {
        ItemStackNode modified = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND), 1.0F, true, List.of(), List.of(), null);
        ItemStackNode plain = new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND), 1.0F, List.of(), List.of(), null);

        Assertions.assertTrue(modified.isModified());
        Assertions.assertFalse(plain.isModified());
    }

    @Test
    public void testItemTagNodeTooltip() {
        ItemTagNode node = new ItemTagNode(UTILS, ItemTags.PLANKS, 0.5F, List.of(), List.of(), null);

        assertTooltip(node.getTooltip(), List.of(
                "Chance: 50%",
                "Count: 1"
        ));
    }

    @Test
    public void testItemTagNodeAppliesModifiersRegardlessOfPreservedCount() {
        ItemTagNode preserved = new ItemTagNode(UTILS, ItemTags.PLANKS, 1.0F,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()), List.of(chance(0.25F)), new RangeValue(3));
        ItemTagNode notPreserved = new ItemTagNode(UTILS, ItemTags.PLANKS, 1.0F,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()), List.of(chance(0.25F)), null);

        Assertions.assertEquals("5", preserved.getCount().toIntString());
        Assertions.assertEquals("5", notPreserved.getCount().toIntString());
        assertTooltip(notPreserved.getTooltip(), List.of(
                "Chance: 25%",
                "Count: 5",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Chance: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: false"
        ));
    }

    private static LootItemCondition chance(float value) {
        return LootItemRandomChanceCondition.randomChance(value).build();
    }
}
