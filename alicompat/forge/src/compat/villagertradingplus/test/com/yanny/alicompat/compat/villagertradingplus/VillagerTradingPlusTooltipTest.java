package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.conditions.ParsedConditions;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class VillagerTradingPlusTooltipTest {
    @Test
    public void testBuyItemListing() {
        VillagerTrades.ItemListing listing = tradeOffer("com.lion.villagertradingplus.tradeoffers.trades.JsonBuyItemTradeOffer$Factory",
                new ItemStack(Items.WHEAT, 20), new ItemStack(Items.EMERALD), 12, 2, 0.05F, 0);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 12",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testSellItemListing() {
        VillagerTrades.ItemListing listing = tradeOffer("com.lion.villagertradingplus.tradeoffers.trades.JsonSellItemTradeOffer$Factory",
                new ItemStack(Items.BREAD, 6), new ItemStack(Items.EMERALD, 2), 16, 1, 0.1F, 0);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 16",
                "XP: 1",
                "Price Multiplier: 0.1"
        ));
    }

    @Test
    public void testParsedConditionsValue() {
        ParsedConditions conditions = new ParsedConditions(List.of(
                new ParsedConditions.Entry(Component.literal("During the day"), (entity) -> true),
                new ParsedConditions.Entry(Component.literal("In a village"), (entity) -> true)
        ), false);

        assertTooltip(UTILS.getValueTooltip(UTILS, conditions).build(), List.of(
                "During the day",
                "In a village"
        ));
    }

    @NotNull
    private static VillagerTrades.ItemListing tradeOffer(String className, ItemStack item, ItemStack currency, int maxUses, int experience, float multiplier, int demand) {
        try {
            Constructor<?> constructor = Class.forName(className).getConstructor(
                    ItemStack.class, ItemStack.class, int.class, int.class, float.class, int.class);

            constructor.setAccessible(true);
            return (VillagerTrades.ItemListing) constructor.newInstance(item, currency, maxUses, experience, multiplier, demand);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + className, e);
        }
    }
}
