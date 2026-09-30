package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.LootEntry;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.number.NumberFormatter;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.lootjs.Utils;
import com.yanny.ali.lootjs.mixin.MixinLootEntry;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import com.yanny.ali.plugin.client.TooltipUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsUtilsTest {
    @Test
    public void testItemGeneratorNode() {
        ItemStackNode node = (ItemStackNode) getEntry(new LootEntry.ItemGenerator(new ItemStack(Items.DIAMOND, 3)), 1, 4, null);

        Assertions.assertEquals(0.25F, node.getChance());
    }

    @Test
    public void testItemGeneratorNodeWithPreservedCount() {
        ItemStackNode node = (ItemStackNode) getEntry(new LootEntry.ItemGenerator(new ItemStack(Items.DIAMOND, 3)), 1, 1, NumberExpr.constant(6));

        Assertions.assertEquals("6", slot(node));
    }

    @Test
    public void testVanillaWrappedGeneratorNode() {
        IDataNode node = getEntry(new LootEntry.VanillaWrappedLootEntry(LootItem.lootTableItem(Items.DIAMOND).build()), 1, 1, null);

        Assertions.assertEquals(ItemNode.ID, node.getId());
        assertTooltip(node.getTooltip(), List.of("Count: 1"));
    }

    @Test
    public void testVanillaWrappedGeneratorNodeWithPreservedCount() {
        ItemStackNode node = (ItemStackNode) getEntry(new LootEntry.VanillaWrappedLootEntry(
                LootItem.lootTableItem(Items.DIAMOND).apply(SetItemCountFunction.setCount(ConstantValue.exactly(5))).build()
        ), 1, 1, NumberExpr.constant(3));

        Assertions.assertEquals("3", slot(node));
    }

    @Test
    public void testVanillaWrappedTagGeneratorNodeWithPreservedCount() {
        ItemTagNode node = (ItemTagNode) getEntry(new LootEntry.VanillaWrappedLootEntry(
                TagEntry.expandTag(ItemTags.PLANKS).build()
        ), 1, 1, NumberExpr.constant(4));

        Assertions.assertEquals("4", slot(node));
    }

    @Test
    public void testRandomIngredientGeneratorItemValue() {
        ItemStackNode node = (ItemStackNode) getEntry(new LootEntry.RandomIngredientGenerator(Ingredient.of(Items.DIAMOND)), 1, 2, null);

        Assertions.assertEquals(0.5F, node.getChance());
    }

    @Test
    public void testRandomIngredientGeneratorTagValue() {
        ItemTagNode node = (ItemTagNode) getEntry(new LootEntry.RandomIngredientGenerator(Ingredient.of(ItemTags.PLANKS)), 1, 2, null);

        Assertions.assertEquals(0.5F, node.getChance());
    }

    @Test
    public void testRandomIngredientGeneratorEmptyIngredient() {
        IDataNode node = getEntry(new LootEntry.RandomIngredientGenerator(Ingredient.EMPTY), 1, 1, null);

        Assertions.assertEquals(MissingNode.ID, node.getId());
        assertTooltip(node.getTooltip(), List.of("Not implemented: [net.minecraft.world.item.crafting.Ingredient]"));
    }

    @Test
    public void testRandomIngredientGeneratorUnknownValue() throws NoSuchFieldException, IllegalAccessException {
        Ingredient ingredient = Ingredient.of(Items.DIAMOND);
        Field values = Ingredient.class.getDeclaredField("values");

        values.setAccessible(true);
        values.set(ingredient, new Ingredient.Value[]{Mockito.mock(Ingredient.Value.class)});

        IDataNode node = getEntry(new LootEntry.RandomIngredientGenerator(ingredient), 1, 1, null);

        Assertions.assertEquals(MissingNode.ID, node.getId());
    }

    @Test
    public void testUnknownGenerator() {
        IDataNode node = getEntry(Mockito.mock(LootEntry.Generator.class), 1, 1, null);

        Assertions.assertEquals(MissingNode.ID, node.getId());
    }

    @Test
    public void testMergesEntryModifiersWithPassedOnes() {
        LootEntry entry = entry(new LootEntry.ItemGenerator(new ItemStack(Items.DIAMOND)), 1,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(5), true).build()),
                List.of(LootItemRandomChanceCondition.randomChance(0.5F).build())
        );
        ItemStackNode node = (ItemStackNode) Utils.getEntry(UTILS, entry, 1,
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
                "  -> Add: false",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: true"
        ));
    }

    private static IDataNode getEntry(LootEntry.Generator generator, int weight, int sumWeight, NumberExpr preservedCount) {
        return Utils.getEntry(UTILS, entry(generator, weight, List.of(), List.of()), sumWeight, List.of(), List.of(), preservedCount);
    }

    private static LootEntry entry(LootEntry.Generator generator, int weight, List<LootItemFunction> postModifications, List<LootItemCondition> conditions) {
        LootEntry entry = mock(LootEntry.class, MixinLootEntry.class);

        Mockito.when(((MixinLootEntry) entry).getGenerator()).thenReturn(generator);
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
