package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.EnchantedItemTrade;
import com.almostreliable.morejs.features.villager.trades.PotionTrade;
import com.almostreliable.morejs.features.villager.trades.SimpleTrade;
import com.almostreliable.morejs.features.villager.trades.StewTrade;
import com.almostreliable.morejs.util.ResourceOrTag;
import com.almostreliable.morejs.util.WeightedList;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MoreJSTooltipTest {
    @Test
    public void testSimpleTradeListing() {
        SimpleTrade trade = new SimpleTrade(inputs(), TradeItem.of(new ItemStack(Items.DIAMOND)));

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 16",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testStewTradeListing() {
        StewTrade trade = new StewTrade(inputs(), new MobEffect[]{MobEffects.REGENERATION}, 200);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 16",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEnchantedItemTradeListing() {
        EnchantedItemTrade trade = new EnchantedItemTrade(inputs(), Items.DIAMOND_SWORD);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 16",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testPotionTradeListing() {
        PotionTrade trade = new PotionTrade(inputs());

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 16",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testDestinationsTooltip() {
        WeightedList<Object> destinations = new WeightedList.Builder<>()
                .add(3, ResourceOrTag.get("minecraft:village_plains", Registries.STRUCTURE))
                .add(1, ResourceOrTag.get("#minecraft:is_jungle", Registries.BIOME))
                .build();

        assertTooltip(UTILS.getValueTooltip(UTILS, destinations).build(MoreJSLang.Branch.DESTINATIONS), List.of(
                "Destinations:",
                "  -> Entry:",
                "    -> Weight: 3",
                "    -> Structure: minecraft:village_plains",
                "  -> Entry:",
                "    -> Weight: 1",
                "    -> Biome: minecraft:is_jungle"
        ));
    }

    private static TradeItem[] inputs() {
        return new TradeItem[]{TradeItem.of(new ItemStack(Items.EMERALD, 4)), TradeItem.EMPTY};
    }
}
