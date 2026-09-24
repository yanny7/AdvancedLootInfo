package com.yanny.ali.test;

import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedChance;
import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedCount;
import static com.yanny.ali.plugin.server.TooltipUtils.getChanceTooltip;
import static com.yanny.ali.plugin.server.TooltipUtils.getCountTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.*;

public class UnwrapperTest {
    @Test
    public void testAllOfChance() {
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.25f)).build()
        ), 1)).build(), List.of("Chance: 25%"));
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                AllOfCondition.allOf(
                        AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.5f)),
                        LootItemRandomChanceCondition.randomChance(0.5f)
                ).build()
        ), 1)).build(), List.of("Chance: 25%"));
    }

    @Test
    public void testInlinePredicateListChance() {
        LootItemCondition composite = AllOfCondition.allOf(HolderSet.direct(Holder.direct(LootItemRandomChanceCondition.randomChance(0.25f).build())));

        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(composite), 1)).build(), List.of("Chance: 25%"));
    }

    @Test
    public void testInexactConditionsKept() {
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                AnyOfCondition.anyOf(LootItemRandomChanceCondition.randomChance(0.25f)).build()
        ), 1)).build(), List.of());
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                LootItemRandomChanceCondition.randomChance(0.25f).invert().build()
        ), 1)).build(), List.of());
    }

    @Test
    public void testFunctionSequenceCount() {
        LootItemFunction sequence = SequenceFunction.of(List.of(Holder.direct(SetItemCountFunction.setCount(ContextIntProviders.exactly(10)).build())));

        assertTooltip(getCountTooltip(getEnchantedCount(UTILS, List.of(sequence))).build(), List.of("Count: 10"));
    }

    @Test
    public void testFunctionSequenceItemStack() {
        LootItemFunction sequence = SequenceFunction.of(List.of(Holder.direct(SetNameFunction.setName(Component.literal("Unwrapped"), SetNameFunction.Target.ITEM_NAME).build())));
        ItemStack stack = TooltipUtils.getItemStack(UTILS, Items.DIAMOND.getDefaultInstance(), List.of(sequence));

        assertEquals("Unwrapped", stack.getHoverName().getString());
    }

    @Test
    public void testHasPredicates() {
        assertFalse(NodeUtils.hasPredicates(UTILS, List.of(
                AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.5f), ExplosionCondition.survivesExplosion()).build()
        )));
        assertTrue(NodeUtils.hasPredicates(UTILS, List.of(
                AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.5f), WeatherCheck.weather().setRaining(true)).build()
        )));
    }
}
