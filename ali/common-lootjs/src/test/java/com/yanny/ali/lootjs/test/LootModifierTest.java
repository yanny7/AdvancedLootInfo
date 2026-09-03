package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.entry.ItemLootEntry;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.modifier.LootAction;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.almostreliable.lootjs.loot.modifier.handler.*;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.AbstractLootModifier;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.LootPoolNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootModifierTest {
    @Test
    public void testUnknownActionIsSkipped() {
        Assertions.assertTrue(operations(List.of(), List.of(), Mockito.mock(LootAction.class)).isEmpty());
    }

    @Test
    public void testAddLootActionBecomesOneOperationPerEntry() {
        List<IOperation> operations = operations(List.of(), List.of(), new AddLootAction(
                itemEntry(Items.DIAMOND),
                itemEntry(Items.EMERALD)
        ));

        Assertions.assertEquals(2, operations.size());
        Assertions.assertEquals(ItemNode.ID, node(operations, 0).getId());
        Assertions.assertEquals(ItemNode.ID, node(operations, 1).getId());
        Assertions.assertTrue(operations.get(0).predicate().test(new ItemStack(Items.STICK)));
    }

    @Test
    public void testLootPoolActionBecomesAddOperation() {
        List<IOperation> operations = operations(List.of(), List.of(), new LootPoolAction(
                LootPool.lootPool().add(LootItem.lootTableItem(Items.DIAMOND)).build()
        ));

        Assertions.assertEquals(LootPoolNode.ID, node(operations, 0).getId());
    }

    @Test
    public void testModifierConditionsAndFunctionsApplyToAddedEntries() {
        List<IOperation> operations = operations(
                List.of(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(3)).build()),
                new AddLootAction(itemEntry(Items.DIAMOND))
        );

        assertTooltip(node(operations, 0).getTooltip(), List.of(
                "Chance: 25%",
                "Count: 3",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Chance: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 3",
                "  -> Add: false"
        ));
    }

    @Test
    public void testRemoveActionWithoutConditionsRemovesNode() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(List.of(), List.of(), removeLootAction()).get(0);

        Assertions.assertNull(operation.factory().apply(itemNode(3)));
    }

    @Test
    public void testRemoveActionKeepsOwnNodes() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(List.of(), List.of(), removeLootAction()).get(0);
        IDataNode own = itemStackNode();

        Assertions.assertSame(own, operation.factory().apply(own));
    }

    @Test
    public void testRemoveActionWithConditionsAddsInvertedCondition() {
        IOperation.RemoveOperation operation = removeOperationWithConditions();
        IDataNode result = operation.factory().apply(itemNode(3));

        Assertions.assertInstanceOf(ItemNode.class, result);
        assertTooltip(result.getTooltip(), List.of(
                "Count: 3",
                "----- Predicates -----",
                "Inverted:",
                "  -> All Of:",
                "    -> Random Chance:",
                "      -> Chance: 0.25"
        ));
    }

    @Test
    public void testRemoveActionWithConditionsDerivesCountFromFunctions() {
        IOperation.RemoveOperation operation = removeOperationWithConditions();
        IDataNode result = operation.factory().apply(itemNode(3, SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()));

        assertTooltip(result.getTooltip(), List.of(
                "Count: 5",
                "----- Predicates -----",
                "Inverted:",
                "  -> All Of:",
                "    -> Random Chance:",
                "      -> Chance: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: false"
        ));
    }

    @Test
    public void testReplaceActionPreservedCountWinsOverEntryCountFunction() {
        IOperation.ReplaceOperation operation = replaceOperation(List.of(), itemEntry(Items.EMERALD, 7), true);
        ItemStackNode result = (ItemStackNode) operation.factory().apply(itemNode(3)).get(0);

        Assertions.assertEquals("3", result.getCount().toIntString());
        assertTooltip(result.getTooltip(), List.of(
                "Count: 3",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 7",
                "  -> Add: false"
        ));
    }

    @Test
    public void testReplaceActionUsesEntryCount() {
        IOperation.ReplaceOperation operation = replaceOperation(List.of(), itemEntry(Items.EMERALD, 7), false);
        List<IDataNode> result = operation.factory().apply(itemNode(3));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("7", ((ItemStackNode) result.get(0)).getCount().toIntString());
    }

    @Test
    public void testReplaceActionPreservesReplacedCount() {
        IOperation.ReplaceOperation operation = replaceOperation(List.of(), itemEntry(Items.EMERALD, 7), true);
        List<IDataNode> result = operation.factory().apply(itemNode(3));

        Assertions.assertEquals("3", ((ItemStackNode) result.get(0)).getCount().toIntString());
    }

    @Test
    public void testReplaceActionWithConditionsWrapsInModifiedNode() {
        IOperation.ReplaceOperation operation = replaceOperation(
                List.of(LootItemRandomChanceCondition.randomChance(0.25F).build()), itemEntry(Items.EMERALD), false);
        List<IDataNode> result = operation.factory().apply(itemNode(3));

        Assertions.assertInstanceOf(ModifiedNode.class, result.get(0));
    }

    @Test
    public void testModifyActionMarksNodeModifiedAndKeepsCount() {
        IOperation.ReplaceOperation operation = modifyOperation();
        ItemStackNode result = (ItemStackNode) operation.factory().apply(itemNode(3)).get(0);

        Assertions.assertTrue(result.isModified());
        Assertions.assertEquals("3", result.getCount().toIntString());
        assertTooltip(result.getTooltip(), List.of(
                "Count: 3",
                "----- Modifiers -----",
                "Modified dynamically!"
        ));
    }

    private static IOperation.RemoveOperation removeOperationWithConditions() {
        return (IOperation.RemoveOperation) operations(
                List.of(LootItemRandomChanceCondition.randomChance(0.25F).build()), List.of(), removeLootAction()).get(0);
    }

    private static IOperation.ReplaceOperation replaceOperation(List<LootItemCondition> conditions, ItemLootEntry entry, boolean preserveCount) {
        return (IOperation.ReplaceOperation) operations(conditions, List.of(),
                new ReplaceLootAction(ItemFilter.ANY, entry, preserveCount)).get(0);
    }

    private static ItemLootEntry itemEntry(Item item) {
        return new ItemLootEntry((LootItem) LootItem.lootTableItem(item).build());
    }

    private static ItemLootEntry itemEntry(Item item, int count) {
        return new ItemLootEntry((LootItem) LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                .build());
    }

    private static IOperation.ReplaceOperation modifyOperation() {
        return (IOperation.ReplaceOperation) operations(List.of(), List.of(),
                new ModifyLootAction(ItemFilter.ANY, (stack) -> stack)).get(0);
    }

    private static RemoveLootAction removeLootAction() {
        return new RemoveLootAction(ItemFilter.ANY);
    }

    private static IDataNode node(List<IOperation> operations, int index) {
        return ((IOperation.AddOperation) operations.get(index)).node();
    }

    private static List<IOperation> operations(List<LootItemCondition> conditions, List<LootItemFunction> functions, LootAction... actions) {
        LootModifier modifier = new LootModifier((context) -> true, ConstantValue.exactly(1), conditions, functions,
                List.of(actions), "test", ItemFilter.ANY, false);

        return new TestLootModifier(UTILS, modifier).getOperations();
    }

    private static ItemNode itemNode(int count, LootItemFunction... functions) {
        return new ItemNode(1.0F, new RangeValue(count), new ItemStack(Items.DIAMOND), TooltipBuilder.empty().build(), List.of(functions), List.of());
    }

    private static ItemStackNode itemStackNode() {
        return new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND), 1.0F, List.of(), List.of(), null);
    }

    private static class TestLootModifier extends AbstractLootModifier<ResourceLocation> {
        TestLootModifier(IServerUtils utils, LootModifier modifier) {
            super(utils, modifier);
        }

        @Override
        public boolean predicate(ResourceLocation value) {
            return true;
        }

        @NotNull
        @Override
        public ILootModifier.IType<ResourceLocation> getType() {
            return ILootModifier.IType.LOOT_TABLE;
        }
    }
}
