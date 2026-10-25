package com.yanny.ali.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.common.trades.EmeraldsForVillagerTypeItemNode;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class NodeTest {
    @Test
    public void testSpiderStringDrop() {
        IDataNode node = NodeUtils.getItemNode(
                UTILS,
                (LootItem) LootItem.lootTableItem(Items.STRING)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 2)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, UniformGenerator.between(0, 1)))
                        .build(),
                NumberExpr.constant(1),
                NumberExpr.constant(1),
                List.of(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTooltip(node.getTooltip(), List.of(
                "Count: 0 to 2",
                "  -> Looting I: 0 to 3  ~1 to 2 (33%)",
                "  -> Looting II: 0 to 4  ~2 (33%)",
                "  -> Looting III: 0 to 5  ~2 to 3 (28%)",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 0 to 2",
                "  -> Add: False",
                "Enchanted Count Increase:",
                "  -> Enchantment: minecraft:looting",
                "  -> Value: 0 to 1"
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
                NumberExpr.constant(1),
                NumberExpr.constant(1),
                List.of(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTooltip(node.getTooltip(), List.of(
                "Count: 0 to 1  ~0 (67%)",
                "  -> Looting I: 0 to 2  ~1 (50%)",
                "  -> Looting II: 0 to 3  ~1 (42%)",
                "  -> Looting III: 0 to 4  ~2 (33%)",
                "----- Predicates -----",
                "Killed by player",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: −1 to 1",
                "  -> Add: False",
                "Enchanted Count Increase:",
                "  -> Enchantment: minecraft:looting",
                "  -> Value: 0 to 1"
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
                NumberExpr.constant(1),
                NumberExpr.constant(1),
                List.of(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTooltip(node.getTooltip(), List.of(
                "Chance: 5%",
                "  -> Fortune I: 6.25%",
                "  -> Fortune II: 8.33%",
                "  -> Fortune III: 10%",
                "Count: 1",
                "----- Predicates -----",
                "Survives Explosion",
                "Table Bonus:",
                "  -> Enchantment: minecraft:fortune",
                "  -> Values: 5%",
                "    -> Fortune I: 6.25%",
                "    -> Fortune II: 8.33%",
                "    -> Fortune III: 10%"
        ));
    }

    @Test
    public void testRemoveKeepsNonItemLeaves() {
        LootTableNode table = table(pool(item(Items.DIAMOND), item(Items.EMERALD), empty(), dynamic(), missing()));

        NodeUtils.processOperations(List.of(removeDiamond()), table);

        Assertions.assertEquals(List.of(LootPoolNode.ID), ids(table));
        Assertions.assertEquals(List.of(ItemNode.ID, EmptyNode.ID, DynamicNode.ID, MissingNode.ID), ids(child(table, 0)));
    }

    @Test
    public void testRemoveDropsPoolLeftWithEmptyOnly() {
        LootTableNode table = table(pool(item(Items.DIAMOND), empty()), pool(item(Items.EMERALD)));

        NodeUtils.processOperations(List.of(removeDiamond()), table);

        Assertions.assertEquals(List.of(LootPoolNode.ID), ids(table));
        Assertions.assertEquals(List.of(ItemNode.ID), ids(child(table, 0)));
    }

    @Test
    public void testRemoveKeepsPoolLeftWithDynamicOnly() {
        LootTableNode table = table(pool(item(Items.DIAMOND), dynamic()));

        NodeUtils.processOperations(List.of(removeDiamond()), table);

        Assertions.assertEquals(List.of(LootPoolNode.ID), ids(table));
        Assertions.assertEquals(List.of(DynamicNode.ID), ids(child(table, 0)));
    }

    @Test
    public void testRemoveKeepsGlobalLootModifierNode() {
        LootTableNode table = table(pool(item(Items.DIAMOND)));

        NodeUtils.processOperations(List.of(new IOperation.AddOperation((i) -> true, new GlobalLootModifierNode(TooltipNode.empty())), removeDiamond()), table);

        Assertions.assertEquals(List.of(GlobalLootModifierNode.ID), ids(table));
    }

    @Test
    public void testRemoveKeepsReferenceToMissingTable() {
        LootTableNode table = table(pool(item(Items.DIAMOND), new ReferenceNode(List.of(missing()), 1, TooltipNode.empty())));

        NodeUtils.processOperations(List.of(removeDiamond()), table);

        Assertions.assertEquals(List.of(ReferenceNode.ID), ids(child(table, 0)));
        Assertions.assertEquals(List.of(MissingNode.ID), ids(child(child(table, 0), 0)));
    }

    @Test
    public void testRemoveDropsEmptiedComposites() {
        LootTableNode table = table(pool(
                new GroupNode(List.of(item(Items.DIAMOND)), TooltipNode.empty()),
                new AlternativesNode(List.of(item(Items.DIAMOND), empty()), TooltipNode.empty()),
                new SequenceNode(List.of(item(Items.DIAMOND), dynamic()), TooltipNode.empty()),
                item(Items.EMERALD)
        ));

        NodeUtils.processOperations(List.of(removeDiamond()), table);

        Assertions.assertEquals(List.of(SequenceNode.ID, ItemNode.ID), ids(child(table, 0)));
        Assertions.assertEquals(List.of(DynamicNode.ID), ids(child(child(table, 0), 0)));
    }

    @Test
    public void testConditionalRemoveKeepsEmpty() {
        ItemNode replacement = item(Items.DIAMOND);
        LootTableNode table = table(pool(item(Items.DIAMOND), empty()));

        NodeUtils.processOperations(List.of(new IOperation.RemoveOperation((i) -> i.is(Items.DIAMOND), (n) -> replacement)), table);

        Assertions.assertEquals(List.of(ItemNode.ID, EmptyNode.ID), ids(child(table, 0)));
        Assertions.assertSame(replacement, child(table, 0).nodes().get(0));
    }

    @Test
    public void testReplaceEmptyItems() {
        TooltipNode tooltip = TooltipNode.empty();
        LootTableNode table = table(pool(
                new ItemNode(0.25F, NumberExpr.constant(1), ItemStack.EMPTY, tooltip, List.of(), List.of()),
                new GroupNode(List.of(new ItemNode(1, NumberExpr.constant(1), new ItemStack(Items.AIR), tooltip, List.of(), List.of())), TooltipNode.empty()),
                item(Items.DIAMOND)
        ));

        NodeUtils.replaceEmptyItems(table);

        IDataNode empty = child(table, 0).nodes().get(0);

        Assertions.assertEquals(List.of(EmptyNode.ID, GroupNode.ID, ItemNode.ID), ids(child(table, 0)));
        Assertions.assertEquals(0.25F, empty.getChance());
        Assertions.assertSame(tooltip, empty.getTooltip());
        Assertions.assertEquals(List.of(EmptyNode.ID), ids(child(child(table, 0), 1)));
    }

    @Test
    public void testBlockTagSurvivesTheRoundTrip() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

        new ItemNode(1, NumberExpr.constant(1), BlockTags.WOODEN_DOORS, TooltipNode.empty(), List.of(), List.of()).encode(UTILS, buf);

        TagKey<?> decoded = new ItemNode(mock(IClientUtils.class, RETURNS_DEEP_STUBS), buf).getItem().right().orElseThrow();

        Assertions.assertEquals(BlockTags.WOODEN_DOORS, decoded);
    }

    private static IOperation removeDiamond() {
        return new IOperation.RemoveOperation((i) -> i.is(Items.DIAMOND), (n) -> null);
    }

    private static LootTableNode table(IDataNode... children) {
        return new LootTableNode(List.of(children), TooltipNode.empty());
    }

    private static LootPoolNode pool(IDataNode... children) {
        return new LootPoolNode(List.of(children), TooltipNode.empty());
    }

    private static ItemNode item(Item item) {
        return new ItemNode(1, NumberExpr.constant(1), new ItemStack(item), TooltipNode.empty(), List.of(), List.of());
    }

    private static EmptyNode empty() {
        return new EmptyNode(1, TooltipNode.empty());
    }

    private static DynamicNode dynamic() {
        return new DynamicNode(1, TooltipNode.empty());
    }

    private static MissingNode missing() {
        return new MissingNode(TooltipNode.empty());
    }

    private static ListNode child(ListNode node, int index) {
        return (ListNode) node.nodes().get(index);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVillagerTypeTradesSkipForeignKeys() {
        Map<ResourceKey<VillagerType>, Item> trades = new HashMap<>();

        BuiltInRegistries.VILLAGER_TYPE.registryKeySet().forEach((type) -> trades.put(type, Items.OAK_BOAT));

        VillagerTrades.EmeraldsForVillagerTypeItem listing = new VillagerTrades.EmeraldsForVillagerTypeItem(1, 12, 5, trades);

        ((Map<Object, Item>) (Map<?, ?>) trades).put(BuiltInRegistries.VILLAGER_TYPE.getValue(VillagerType.PLAINS), Items.APPLE);

        EmeraldsForVillagerTypeItemNode node = new EmeraldsForVillagerTypeItemNode(UTILS, listing, TooltipNode.empty());

        Assertions.assertEquals(BuiltInRegistries.VILLAGER_TYPE.size(), node.nodes().size());
    }

    private static List<Identifier> ids(ListNode node) {
        return node.nodes().stream().map(IDataNode::getId).toList();
    }
}
