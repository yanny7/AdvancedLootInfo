package com.yanny.alicompat.compat.twilightforest;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.junit.jupiter.api.Test;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;
import twilightforest.loot.functions.ModItemSwap;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class TwilightForestTooltipTest {
    @Test
    public void testGiantPickUsedCondition() {
        LootItemCondition condition = GiantPickUsedCondition.builder(LootContext.EntityTarget.THIS).build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Giant Pick Used:",
                "  -> This Entity"
        ));
    }

    @Test
    public void testModExistsCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new ModExistsCondition("twilightforest")).build(), List.of(
                "Mod Exists:",
                "  -> twilightforest"
        ));
    }

    @Test
    public void testIsMinionCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new IsMinionCondition(false)).build(), List.of(
                "Is Minion:",
                "  -> true"
        ));
        assertTooltip(UTILS.getConditionTooltip(UTILS, new IsMinionCondition(true)).build(), List.of(
                "Is Minion:",
                "  -> false"
        ));
    }

    @Test
    public void testUncraftingTableEnabledCondition() {
        LootItemCondition condition = UncraftingTableEnabledCondition.uncraftingTableEnabled().build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Uncrafting Table Enabled"
        ));
    }

    @Test
    public void testModItemSwapFunction() {
        LootItemFunction function = ModItemSwap.builder().apply("twilightforest", Items.DIAMOND, Items.IRON_INGOT).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Mod Item Swap:",
                "  -> Item: minecraft:diamond",
                "  -> Default Item: minecraft:iron_ingot"
        ));
    }
}
