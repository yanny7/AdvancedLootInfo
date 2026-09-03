package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.almostreliable.lootjs.loot.action.GroupedLootAction;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import com.google.common.collect.ImmutableList;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.lootjs.mixin.*;
import com.yanny.ali.lootjs.node.*;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
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
                "  -> Probability: 0.25",
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
    public void testItemTagNodeTooltip() {
        ItemTagNode node = new ItemTagNode(UTILS, ItemTags.PLANKS, 0.5F, List.of(), List.of(), null);

        assertTooltip(node.getTooltip(), List.of(
                "Chance: 50%",
                "Count: 1"
        ));
    }

    @Test
    public void testItemTagNodeAppliesModifiersWithoutPreservedCount() {
        ItemTagNode node = new ItemTagNode(UTILS, ItemTags.PLANKS, 1.0F,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()), List.of(chance(0.25F)), null);

        Assertions.assertEquals("5", node.getCount().toIntString());
        assertTooltip(node.getTooltip(), List.of(
                "Chance: 25%",
                "Count: 5",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Probability: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: false"
        ));
    }

    @Test
    public void testItemTagNodePreservedCount() {
        ItemTagNode node = new ItemTagNode(UTILS, ItemTags.PLANKS, 1.0F, List.of(), List.of(), new RangeValue(2, 5));

        Assertions.assertEquals("2-5", node.getCount().toIntString());
    }

    @Test
    public void testItemTagNodePreservedCountWinsOverCountFunction() {
        ItemTagNode node = new ItemTagNode(UTILS, ItemTags.PLANKS, 1.0F,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()), List.of(), new RangeValue(3));

        Assertions.assertEquals("3", node.getCount().toIntString());
        assertTooltip(node.getTooltip(), List.of(
                "Count: 3",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: false"
        ));
    }

    @Test
    public void testAddLootNodeDefault() {
        AddLootNode node = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.DEFAULT,
                itemEntry(new ItemStack(Items.DIAMOND), 1),
                itemEntry(new ItemStack(Items.EMERALD), 3)
        ), List.of(), List.of());

        assertTooltip(node.getTooltip(), List.of("Selects all entries"));
        Assertions.assertEquals(0.25F, child(node, 0).getChance());
        Assertions.assertEquals(0.75F, child(node, 1).getChance());
    }

    @Test
    public void testAddLootNodeSequence() {
        AddLootNode node = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.SEQUENCE,
                itemEntry(new ItemStack(Items.DIAMOND), 1)
        ), List.of(), List.of());

        assertTooltip(node.getTooltip(), List.of("Selects entries sequentially until first failed"));
    }

    @Test
    public void testAddLootNodeAlternatives() {
        AddLootNode node = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.ALTERNATIVES,
                itemEntry(new ItemStack(Items.DIAMOND), 1)
        ), List.of(), List.of());

        assertTooltip(node.getTooltip(), List.of("Selects only first successful entry"));
    }

    @Test
    public void testAddLootNodeVanillaWrappedEntry() {
        AddLootNode node = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.SEQUENCE,
                entry(new LootEntry.VanillaWrappedLootEntry(LootItem.lootTableItem(Items.DIAMOND).build()), 1)
        ), List.of(), List.of());

        Assertions.assertEquals(ItemNode.ID, child(node, 0).getId());
    }

    @Test
    public void testAddLootNodeIngredientEntry() {
        AddLootNode itemNode = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.SEQUENCE,
                entry(new LootEntry.RandomIngredientGenerator(Ingredient.of(Items.DIAMOND)), 1)
        ), List.of(), List.of());
        AddLootNode tagNode = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.SEQUENCE,
                entry(new LootEntry.RandomIngredientGenerator(Ingredient.of(ItemTags.PLANKS)), 1)
        ), List.of(), List.of());

        Assertions.assertEquals(ItemStackNode.ID, child(itemNode, 0).getId());
        Assertions.assertEquals(ItemTagNode.ID, child(tagNode, 0).getId());
    }

    @Test
    public void testAddLootNodeEmptyIngredientEntry() {
        AddLootNode node = new AddLootNode(UTILS, addLootAction(AddLootAction.AddType.SEQUENCE,
                entry(new LootEntry.RandomIngredientGenerator(Ingredient.EMPTY), 1)
        ), List.of(), List.of());

        Assertions.assertEquals(MissingNode.ID, child(node, 0).getId());
    }

    @Test
    public void testWeightedAddLootNodeTooltip() {
        WeightedAddLootNode node = new WeightedAddLootNode(UTILS, weightedAddLootAction(
                itemEntry(new ItemStack(Items.DIAMOND), 1),
                itemEntry(new ItemStack(Items.EMERALD), 3)
        ), List.of(), List.of());

        assertTooltip(node.getTooltip(), List.of(
                "Selects random entry",
                "Rolls: 2x",
                "Allow Duplicate Loot: true"
        ));
        Assertions.assertEquals(0.25F, child(node, 0).getChance());
        Assertions.assertEquals(0.75F, child(node, 1).getChance());
    }

    @Test
    public void testGroupLootNodeTooltip() {
        GroupLootNode node = new GroupLootNode(UTILS, groupedLootAction(
                addLootAction(AddLootAction.AddType.DEFAULT, itemEntry(new ItemStack(Items.DIAMOND), 1)),
                weightedAddLootAction(itemEntry(new ItemStack(Items.EMERALD), 1))
        ), List.of(), List.of());

        assertTooltip(node.getTooltip(), List.of(
                "Selects random entry",
                "Rolls: 1x"
        ));
        Assertions.assertEquals(AddLootNode.ID, child(node, 0).getId());
        Assertions.assertEquals(WeightedAddLootNode.ID, child(node, 1).getId());
    }

    @Test
    public void testGroupLootNodeSkipsUnknownHandler() {
        GroupLootNode node = new GroupLootNode(UTILS, groupedLootAction(Mockito.mock(ILootHandler.class)), List.of(), List.of());

        Assertions.assertTrue(node.nodes().isEmpty());
    }

    private static IDataNode child(ListNode node, int index) {
        return node.nodes().get(index);
    }

    private static LootItemCondition chance(float value) {
        return LootItemRandomChanceCondition.randomChance(value).build();
    }

    private static LootEntry itemEntry(ItemStack itemStack, int weight) {
        return entry(new LootEntry.ItemGenerator(itemStack), weight);
    }

    private static LootEntry entry(LootEntry.Generator generator, int weight) {
        LootEntry entry = mock(LootEntry.class, MixinLootEntry.class);

        Mockito.when(((MixinLootEntry) entry).getGenerator()).thenReturn(generator);
        Mockito.when(((MixinLootEntry) entry).getPostModifications()).thenReturn(List.of());
        Mockito.when(((MixinLootEntry) entry).getConditions()).thenReturn(List.of());
        Mockito.when(((MixinLootEntry) entry).getWeight()).thenReturn(weight);
        Mockito.when(entry.getWeight()).thenReturn(weight);
        return entry;
    }

    private static AddLootAction addLootAction(AddLootAction.AddType type, LootEntry... entries) {
        AddLootAction action = mock(AddLootAction.class, MixinAddLootAction.class);

        Mockito.when(((MixinAddLootAction) action).getEntries()).thenReturn(entries);
        Mockito.when(((MixinAddLootAction) action).getType()).thenReturn(type);
        return action;
    }

    @SuppressWarnings("unchecked")
    private static WeightedAddLootAction weightedAddLootAction(LootEntry... entries) {
        ImmutableList.Builder<WeightedEntry.Wrapper<LootEntry>> items = ImmutableList.builder();
        int totalWeight = 0;

        for (LootEntry entry : entries) {
            items.add(WeightedEntry.wrap(entry, entry.getWeight()));
            totalWeight += entry.getWeight();
        }

        SimpleWeightedRandomList<LootEntry> list = mock(SimpleWeightedRandomList.class, MixinWeightedRandomList.class);

        Mockito.when(((MixinWeightedRandomList<WeightedEntry.Wrapper<LootEntry>>) list).getAliItems()).thenReturn(items.build());
        Mockito.when(((MixinWeightedRandomList<WeightedEntry.Wrapper<LootEntry>>) list).getAliTotalWeight()).thenReturn(totalWeight);

        WeightedAddLootAction action = mock(WeightedAddLootAction.class, MixinWeightedAddLootAction.class);

        Mockito.when(((MixinWeightedAddLootAction) action).getWeightedRandomList()).thenReturn(list);
        Mockito.when(((MixinWeightedAddLootAction) action).getNumberProvider()).thenReturn(ConstantValue.exactly(2));
        Mockito.when(((MixinWeightedAddLootAction) action).getAllowDuplicateLoot()).thenReturn(true);
        return action;
    }

    private static GroupedLootAction groupedLootAction(ILootHandler... handlers) {
        List<ILootHandler> list = List.of(handlers);
        GroupedLootAction action = mock(GroupedLootAction.class, MixinGroupedLootAction.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinGroupedLootAction) action).getNumberProvider()).thenReturn(ConstantValue.exactly(1));
        Mockito.when(((MixinCompositeLootAction) action).getHandlers()).thenReturn(list);
        return action;
    }
}
