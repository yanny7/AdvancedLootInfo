package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.AbstractLootModification;
import com.almostreliable.lootjs.core.ILootAction;
import com.almostreliable.lootjs.core.ILootHandler;
import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.*;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.LootModifier;
import com.yanny.ali.lootjs.mixin.*;
import com.yanny.ali.lootjs.node.AddLootNode;
import com.yanny.ali.lootjs.node.GroupLootNode;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.WeightedAddLootNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.wrap;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootModifierTest {
    @Test
    public void testConditionAndFunctionHandlersApplyToLaterActions() {
        List<IOperation> operations = operations(
                wrap(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                new LootItemFunctionWrapperAction(SetItemCountFunction.setCount(ConstantValue.exactly(3)).build()),
                addLootAction(itemEntry(Items.DIAMOND))
        );
        AddLootNode node = (AddLootNode) ((IOperation.AddOperation) operations.get(0)).node();

        Assertions.assertEquals(1, operations.size());
        assertTooltip(node.nodes().get(0).getTooltip(), List.of(
                "Count: 3",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Probability: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 3",
                "  -> Add: false"
        ));
    }

    @Test
    public void testCustomPlayerActionBecomesFunction() {
        List<IOperation> operations = operations(
                new CustomPlayerAction((player) -> {}),
                addLootAction(itemEntry(Items.DIAMOND))
        );
        AddLootNode node = (AddLootNode) ((IOperation.AddOperation) operations.get(0)).node();

        assertTooltip(node.nodes().get(0).getTooltip(), List.of(
                "Count: 1",
                "----- Modifiers -----",
                "Custom Player Modifier:",
                "  -> Detail Not Available"
        ));
    }

    @Test
    public void testUnknownHandlerIsSkipped() {
        Assertions.assertTrue(operations(Mockito.mock(ILootHandler.class)).isEmpty());
    }

    @Test
    public void testUnknownActionIsSkipped() {
        Assertions.assertTrue(operations(Mockito.mock(ILootAction.class)).isEmpty());
    }

    @Test
    public void testAddActionsBecomeAddOperations() {
        List<IOperation> operations = operations(
                addLootAction(itemEntry(Items.DIAMOND)),
                weightedAddLootAction(itemEntry(Items.EMERALD)),
                groupedLootAction(addLootAction(itemEntry(Items.GOLD_INGOT)))
        );

        Assertions.assertEquals(AddLootNode.ID, node(operations, 0).getId());
        Assertions.assertEquals(WeightedAddLootNode.ID, node(operations, 1).getId());
        Assertions.assertEquals(GroupLootNode.ID, node(operations, 2).getId());
        Assertions.assertTrue(((IOperation.AddOperation) operations.get(0)).predicate().test(new ItemStack(Items.STICK)));
    }

    @Test
    public void testRemoveActionWithoutConditionsRemovesNode() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(removeLootAction()).get(0);

        Assertions.assertNull(operation.factory().apply(itemNode(3)));
    }

    @Test
    public void testRemoveActionKeepsOwnNodes() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(removeLootAction()).get(0);
        IDataNode own = itemStackNode();

        Assertions.assertSame(own, operation.factory().apply(own));
    }

    @Test
    public void testRemoveActionWithConditionsAddsInvertedCondition() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(
                wrap(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                removeLootAction()
        ).get(0);
        IDataNode result = operation.factory().apply(itemNode(3));

        Assertions.assertInstanceOf(ItemNode.class, result);
        assertTooltip(result.getTooltip(), List.of(
                "Count: 3",
                "----- Predicates -----",
                "Inverted:",
                "  -> All Of:",
                "    -> Random Chance:",
                "      -> Probability: 0.25"
        ));
    }

    @Test
    public void testRemoveActionWithConditionsDerivesCountFromFunctions() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(
                wrap(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                removeLootAction()
        ).get(0);
        IDataNode result = operation.factory().apply(itemNode(3, SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()));

        assertTooltip(result.getTooltip(), List.of(
                "Count: 5",
                "----- Predicates -----",
                "Inverted:",
                "  -> All Of:",
                "    -> Random Chance:",
                "      -> Probability: 0.25",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: false"
        ));
    }

    @Test
    public void testRemoveActionWithConditionsKeepsForeignNode() {
        IOperation.RemoveOperation operation = (IOperation.RemoveOperation) operations(
                wrap(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                removeLootAction()
        ).get(0);
        IDataNode foreign = node(operations(addLootAction(itemEntry(Items.DIAMOND))), 0);

        Assertions.assertSame(foreign, operation.factory().apply(foreign));
    }

    @Test
    public void testReplaceActionKeepsOwnNodes() {
        IOperation.ReplaceOperation operation = (IOperation.ReplaceOperation) operations(replaceLootAction(itemEntry(Items.EMERALD), false)).get(0);
        IDataNode own = itemStackNode();

        Assertions.assertEquals(List.of(own), operation.factory().apply(own));
    }

    @Test
    public void testReplaceActionUsesEntryCount() {
        IOperation.ReplaceOperation operation = (IOperation.ReplaceOperation) operations(
                replaceLootAction(itemEntry(new ItemStack(Items.EMERALD, 7)), false)
        ).get(0);
        List<IDataNode> result = operation.factory().apply(itemNode(3));

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("7", ((ItemStackNode) result.get(0)).getCount().toIntString());
    }

    @Test
    public void testReplaceActionPreservesReplacedCount() {
        IOperation.ReplaceOperation operation = (IOperation.ReplaceOperation) operations(
                replaceLootAction(itemEntry(new ItemStack(Items.EMERALD, 7)), true)
        ).get(0);
        List<IDataNode> result = operation.factory().apply(itemNode(3));

        Assertions.assertEquals("3", ((ItemStackNode) result.get(0)).getCount().toIntString());
    }

    @Test
    public void testReplaceActionWithConditionsWrapsInModifiedNode() {
        IOperation.ReplaceOperation operation = (IOperation.ReplaceOperation) operations(
                wrap(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                replaceLootAction(itemEntry(Items.EMERALD), false)
        ).get(0);
        List<IDataNode> result = operation.factory().apply(itemNode(3));

        Assertions.assertInstanceOf(ModifiedNode.class, result.get(0));
    }

    @Test
    public void testModifyActionKeepsOwnNodes() {
        IOperation.ReplaceOperation operation = (IOperation.ReplaceOperation) operations(modifyLootAction()).get(0);
        IDataNode own = itemStackNode();

        Assertions.assertEquals(List.of(own), operation.factory().apply(own));
    }

    @Test
    public void testModifyActionMarksNodeModifiedAndKeepsCount() {
        IOperation.ReplaceOperation operation = (IOperation.ReplaceOperation) operations(modifyLootAction()).get(0);
        ItemStackNode result = (ItemStackNode) operation.factory().apply(itemNode(3)).get(0);

        Assertions.assertTrue(result.isModified());
        Assertions.assertEquals("3", result.getCount().toIntString());
        assertTooltip(result.getTooltip(), List.of(
                "Count: 3",
                "----- Modifiers -----",
                "Modified dynamically!"
        ));
    }

    private static IDataNode node(List<IOperation> operations, int index) {
        return ((IOperation.AddOperation) operations.get(index)).node();
    }

    private static List<IOperation> operations(ILootHandler... handlers) {
        List<ILootHandler> list = List.of(handlers);
        AbstractLootModification modification = mock(AbstractLootModification.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinCompositeLootAction) modification).getHandlers()).thenReturn(list);
        return new TestLootModifier(UTILS, modification).getOperations();
    }

    private static ItemNode itemNode(int count, LootItemFunction... functions) {
        return new ItemNode(1.0F, new RangeValue(count), new ItemStack(Items.DIAMOND), TooltipBuilder.empty().build(), List.of(functions), List.of());
    }

    private static ItemStackNode itemStackNode() {
        return new ItemStackNode(UTILS, new ItemStack(Items.DIAMOND), 1.0F, List.of(), List.of(), null);
    }

    private static LootEntry itemEntry(ItemStack itemStack) {
        LootEntry entry = mock(LootEntry.class, MixinLootEntry.class);

        Mockito.when(((MixinLootEntry) entry).getGenerator()).thenReturn(new LootEntry.ItemGenerator(itemStack));
        Mockito.when(((MixinLootEntry) entry).getPostModifications()).thenReturn(List.of());
        Mockito.when(((MixinLootEntry) entry).getConditions()).thenReturn(List.of());
        Mockito.when(((MixinLootEntry) entry).getWeight()).thenReturn(1);
        Mockito.when(entry.getWeight()).thenReturn(1);
        return entry;
    }

    private static LootEntry itemEntry(net.minecraft.world.item.Item item) {
        return itemEntry(new ItemStack(item));
    }

    private static AddLootAction addLootAction(LootEntry... entries) {
        AddLootAction action = mock(AddLootAction.class, MixinAddLootAction.class);

        Mockito.when(((MixinAddLootAction) action).getEntries()).thenReturn(entries);
        Mockito.when(((MixinAddLootAction) action).getType()).thenReturn(AddLootAction.AddType.DEFAULT);
        return action;
    }

    @SuppressWarnings("unchecked")
    private static WeightedAddLootAction weightedAddLootAction(LootEntry... entries) {
        com.google.common.collect.ImmutableList.Builder<net.minecraft.util.random.WeightedEntry.Wrapper<LootEntry>> items = com.google.common.collect.ImmutableList.builder();
        int totalWeight = 0;

        for (LootEntry entry : entries) {
            items.add(net.minecraft.util.random.WeightedEntry.wrap(entry, entry.getWeight()));
            totalWeight += entry.getWeight();
        }

        net.minecraft.util.random.SimpleWeightedRandomList<LootEntry> list = mock(net.minecraft.util.random.SimpleWeightedRandomList.class, MixinWeightedRandomList.class);

        Mockito.when(((MixinWeightedRandomList<net.minecraft.util.random.WeightedEntry.Wrapper<LootEntry>>) list).getAliItems()).thenReturn(items.build());
        Mockito.when(((MixinWeightedRandomList<net.minecraft.util.random.WeightedEntry.Wrapper<LootEntry>>) list).getAliTotalWeight()).thenReturn(totalWeight);

        WeightedAddLootAction action = mock(WeightedAddLootAction.class, MixinWeightedAddLootAction.class);

        Mockito.when(((MixinWeightedAddLootAction) action).getWeightedRandomList()).thenReturn(list);
        Mockito.when(((MixinWeightedAddLootAction) action).getNumberProvider()).thenReturn(ConstantValue.exactly(1));
        Mockito.when(((MixinWeightedAddLootAction) action).getAllowDuplicateLoot()).thenReturn(false);
        return action;
    }

    private static GroupedLootAction groupedLootAction(ILootHandler... handlers) {
        List<ILootHandler> list = List.of(handlers);
        GroupedLootAction action = mock(GroupedLootAction.class, MixinGroupedLootAction.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinGroupedLootAction) action).getNumberProvider()).thenReturn(ConstantValue.exactly(1));
        Mockito.when(((MixinCompositeLootAction) action).getHandlers()).thenReturn(list);
        return action;
    }

    private static RemoveLootAction removeLootAction() {
        RemoveLootAction action = mock(RemoveLootAction.class, MixinRemoveLootAction.class);

        Mockito.when(((MixinRemoveLootAction) action).getPredicate()).thenReturn((itemStack) -> true);
        return action;
    }

    private static ReplaceLootAction replaceLootAction(LootEntry entry, boolean preserveCount) {
        ReplaceLootAction action = mock(ReplaceLootAction.class, MixinReplaceLootAction.class);

        Mockito.when(((MixinReplaceLootAction) action).getPredicate()).thenReturn((itemStack) -> true);
        Mockito.when(((MixinReplaceLootAction) action).getLootEntry()).thenReturn(entry);
        Mockito.when(((MixinReplaceLootAction) action).getPreserveCount()).thenReturn(preserveCount);
        return action;
    }

    private static ModifyLootAction modifyLootAction() {
        ModifyLootAction action = mock(ModifyLootAction.class, MixinModifyLootAction.class);

        Mockito.when(((MixinModifyLootAction) action).getPredicate()).thenReturn((itemStack) -> true);
        return action;
    }

    private static class TestLootModifier extends LootModifier<ResourceLocation> {
        TestLootModifier(IServerUtils utils, AbstractLootModification modification) {
            super(utils, modification);
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
