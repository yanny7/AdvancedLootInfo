package com.yanny.ali.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.number.NumberEvaluator;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.GroupNode;
import com.yanny.ali.plugin.common.nodes.LootPoolNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.yanny.aci.api.NumberExpr.constant;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class EntryWeightTest {
    @Test
    public void testSingletons() {
        assertEquals(constant(4), weight(List.of(item(1), item(3))));
    }

    @Test
    public void testGroup() {
        assertEquals(constant(8), weight(List.of(EntryGroup.list(itemBuilder(2), itemBuilder(2)).build(), item(4))));
    }

    @Test
    public void testSequence() {
        assertEquals(constant(8), weight(List.of(SequentialEntry.sequential(itemBuilder(2), itemBuilder(2)).build(), item(4))));
    }

    @Test
    public void testNestedGroup() {
        LootPoolEntryContainer entry = EntryGroup.list(EntryGroup.list(itemBuilder(2), itemBuilder(3)), itemBuilder(5)).build();

        assertEquals(constant(10), weight(List.of(entry)));
    }

    @Test
    public void testAlternatives() {
        assertEquals(constant(1), weight(List.of(AlternativesEntry.alternatives(itemBuilder(1), itemBuilder(1)).build())));
    }

    @Test
    public void testModdedSingleton() {
        assertEquals(constant(7), weight(List.of(new TestSingleton(7))));
    }

    @Test
    public void testUnknownEntry() {
        assertEquals(constant(0), weight(List.of(new TestEntry())));
    }

    @Test
    public void testQuality() {
        LootPoolEntryContainer entry = LootItem.lootTableItem(Items.STONE).setWeight(2).setQuality(1).build();
        assertEquals(NumberInterval.closed(1, 6), NumberEvaluator.bounds(weight(List.of(entry))));
    }

    @Test
    public void testQualityChanceRows() {
        LootPool pool = LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.STONE).setWeight(10).setQuality(-2))
                .add(LootItem.lootTableItem(Items.DIRT).setWeight(5).setQuality(2))
                .build();
        LootPoolNode node = NodeUtils.getLootPoolNode(UTILS, pool, constant(1), List.of(), Collections.emptyList(), Collections.emptyList());
        assertTooltip(node.nodes().get(0).getTooltip(), List.of(
                "Quality: -2",
                "Chance: 66.67%",
                "  -> Bad Luck: 80%",
                "  -> Luck 1: 53.33%",
                "  -> Luck 2: 40%",
                "  -> Luck 3: 26.67%",
                "  -> Luck 4: 13.33%",
                "Count: 1"
        ));
    }

    @Test
    public void testChildren() {
        EntryGroup group = (EntryGroup) EntryGroup.list(itemBuilder(1), itemBuilder(2)).build();

        assertEquals(List.of(group.children), UTILS.getEntryChildren(UTILS, group));
        assertEquals(List.of(), UTILS.getEntryChildren(UTILS, group.children[0]));
        assertEquals(List.of(), UTILS.getEntryChildren(UTILS, new TestEntry()));
    }

    @Test
    public void testPoolChances() {
        LootPool pool = LootPool.lootPool()
                .add(EntryGroup.list(itemBuilder(2), itemBuilder(2)))
                .add(itemBuilder(4))
                .build();
        LootPoolNode node = NodeUtils.getLootPoolNode(UTILS, pool, constant(1), List.of(), Collections.emptyList(), Collections.emptyList());
        List<IDataNode> nodes = node.nodes();

        assertEquals(2, nodes.size());
        GroupNode group = assertInstanceOf(GroupNode.class, nodes.get(0));
        assertEquals(2, group.nodes().size());
        assertEquals(0.25f, group.nodes().get(0).getChance(), 1e-6);
        assertEquals(0.25f, group.nodes().get(1).getChance(), 1e-6);
        assertEquals(0.5f, nodes.get(1).getChance(), 1e-6);
    }

    @Test
    public void testConditionalWeightReachesChance() {
        PluginManager.getInstance().serverRegistry.registerEntryWeight(ConditionalSingleton.class, (u, e, c) -> {
            int index = c.size();

            c.add(u.getConditionTooltip(u, ExplosionCondition.survivesExplosion().build()).build());
            return NumberExpr.cond(List.of(new NumberExpr.Branch(index, constant(3))), constant(1));
        });

        LootPoolEntryContainer[] entries = {new ConditionalSingleton(), item(1)};
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr sumWeight = NodeUtils.getTotalWeight(UTILS, List.of(entries), conditions);
        List<IDataNode> nodes = NodeUtils.getChildren(UTILS, entries, constant(1), sumWeight, conditions, List.of(), List.of());

        assertTooltip(nodes.get(1).getTooltip(), List.of(
                "Chance: 25% to 50%",
                "  -> Survives Explosion",
                "Count: 1"
        ));
    }

    @NotNull
    private static NumberExpr weight(List<LootPoolEntryContainer> entries) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr weight = NodeUtils.getTotalWeight(UTILS, entries, conditions);

        assertEquals(List.of(), conditions);
        return weight;
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> itemBuilder(int weight) {
        return LootItem.lootTableItem(Items.STONE).setWeight(weight);
    }

    @NotNull
    private static LootPoolEntryContainer item(int weight) {
        return itemBuilder(weight).build();
    }

    private static class TestSingleton extends LootPoolSingletonContainer {
        TestSingleton(int weight) {
            super(weight, 0, new LootItemCondition[0], new LootItemFunction[0]);
        }

        @Override
        protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        }

        @NotNull
        @Override
        public LootPoolEntryType getType() {
            return LootPoolEntries.ITEM;
        }
    }

    private static class ConditionalSingleton extends TestSingleton {
        ConditionalSingleton() {
            super(1);
        }
    }

    private static class TestEntry extends LootPoolEntryContainer {
        TestEntry() {
            super(new LootItemCondition[0]);
        }

        @Override
        public boolean expand(LootContext context, Consumer<LootPoolEntry> consumer) {
            return false;
        }

        @NotNull
        @Override
        public LootPoolEntryType getType() {
            return LootPoolEntries.ITEM;
        }
    }
}
