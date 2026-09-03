package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.LootType;
import com.almostreliable.lootjs.core.entry.ItemLootEntry;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.lootjs.Utils;
import com.yanny.ali.lootjs.node.ItemStackNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsUtilsTest {
    @Test
    public void testGetCapturedInstancesFindsRequiredType() {
        String captured = "captured";
        Predicate<Object> predicate = (value) -> captured.equals(value);

        Assertions.assertEquals(List.of(captured), Utils.getCapturedInstances(predicate, String.class));
    }

    @Test
    public void testGetCapturedInstancesSkipsOtherTypes() {
        Integer captured = 42;
        Predicate<Object> predicate = (value) -> captured.equals(value);

        Assertions.assertTrue(Utils.getCapturedInstances(predicate, String.class).isEmpty());
    }

    @Test
    public void testGetCapturedInstancesOnPredicateWithoutCaptures() {
        Assertions.assertTrue(Utils.getCapturedInstances((value) -> true, String.class).isEmpty());
    }

    @Test
    public void testTypePredicateMatchesTablePath() {
        Assertions.assertTrue(matches(LootType.BLOCK, "blocks/stone"));
        Assertions.assertTrue(matches(LootType.BLOCK_USE, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.BLOCK, "entities/zombie"));
        Assertions.assertTrue(matches(LootType.CHEST, "chests/igloo_chest"));
        Assertions.assertTrue(matches(LootType.FISHING, "gameplay/fishing/fish"));
        Assertions.assertTrue(matches(LootType.ENTITY, "entities/zombie"));
        Assertions.assertTrue(matches(LootType.EQUIPMENT, "equipment/trial_chamber"));
        Assertions.assertTrue(matches(LootType.ARCHAEOLOGY, "archaeology/desert_pyramid"));
        Assertions.assertTrue(matches(LootType.GIFT, "gameplay/hero_of_the_village/farmer_gift"));
        Assertions.assertTrue(matches(LootType.PIGLIN_BARTER, "gameplay/piglin_bartering"));
        Assertions.assertTrue(matches(LootType.SHEARING, "shearing/bogged"));
        Assertions.assertTrue(matches(LootType.GENERIC, "anything/at/all"));
    }

    @Test
    public void testTypePredicateNeverMatchesUnsupportedTypes() {
        Assertions.assertFalse(matches(LootType.UNKNOWN, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.VAULT, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.ADVANCEMENT_REWARD, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.ADVANCEMENT_ENTITY, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.ADVANCEMENT_LOCATION, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.COMMAND, "blocks/stone"));
        Assertions.assertFalse(matches(LootType.SELECTOR, "blocks/stone"));
    }

    @Test
    public void testGetEntryUsesEntryCount() {
        ItemStackNode node = (ItemStackNode) Utils.getEntry(UTILS, itemEntry(Items.DIAMOND, 4), 1, List.of(), List.of(), null);

        Assertions.assertEquals("4", node.getCount().toIntString());
    }

    @Test
    public void testGetEntryPreservesCount() {
        ItemStackNode node = (ItemStackNode) Utils.getEntry(UTILS, itemEntry(Items.DIAMOND, 4), 1, List.of(), List.of(), new RangeValue(6));

        Assertions.assertEquals("6", node.getCount().toIntString());
    }

    @Test
    public void testGetEntryMergesEntryModifiersWithPassedOnes() {
        ItemLootEntry entry = new ItemLootEntry((LootItem) LootItem.lootTableItem(Items.DIAMOND)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(5)))
                .when(LootItemRandomChanceCondition.randomChance(0.5F))
                .build());
        ItemStackNode node = (ItemStackNode) Utils.getEntry(UTILS, entry, 0.25F,
                List.of(SetItemCountFunction.setCount(ConstantValue.exactly(2), true).build()),
                List.of(LootItemRandomChanceCondition.randomChance(0.25F).build()),
                null
        );

        Assertions.assertEquals("5", node.getCount().toIntString());
        assertTooltip(node.getTooltip(), List.of(
                "Chance: 3.13%",
                "Count: 5",
                "----- Predicates -----",
                "Random Chance:",
                "  -> Chance: 0.25",
                "Random Chance:",
                "  -> Chance: 0.50",
                "----- Modifiers -----",
                "Set Count:",
                "  -> Count: 2",
                "  -> Add: true",
                "Set Count:",
                "  -> Count: 5",
                "  -> Add: false"
        ));
    }

    private static ItemLootEntry itemEntry(Item item, int count) {
        return new ItemLootEntry((LootItem) LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                .build());
    }

    private static boolean matches(LootType type, String path) {
        return Utils.typePredicate(type).test(ResourceLocation.withDefaultNamespace(path));
    }
}
