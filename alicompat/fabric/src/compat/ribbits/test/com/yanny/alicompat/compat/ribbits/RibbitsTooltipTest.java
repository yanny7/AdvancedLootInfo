package com.yanny.alicompat.compat.ribbits;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.alicompat.accessor.ReflectionUtils;
import com.yungnickyoung.minecraft.ribbits.entity.trade.*;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class RibbitsTooltipTest {
    @Test
    public void testItemsForAmethystsListing() {
        ItemsForAmethysts listing = new ItemsForAmethysts(Items.BREAD, 2, 4, 1, 3, 8);

        assertTooltip(tooltip(ReflectionUtils.copyClassData(ItemsForAmethystsAccessor.class, listing, ItemsForAmethysts.class)), List.of(
                "Uses: 8",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testAmethystForItemsListing() {
        AmethystForItems listing = new AmethystForItems(Items.WHEAT, 5, 2, 7, 6, 3);

        assertTooltip(tooltip(ReflectionUtils.copyClassData(AmethystForItemsAccessor.class, listing, AmethystForItems.class)), List.of(
                "Uses: 3",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testItemsAndAmethystsToItemsListing() {
        ItemsAndAmethystsToItems listing = new ItemsAndAmethystsToItems(Items.WHEAT, 4, 2, Items.BREAD, 9, 5);

        assertTooltip(tooltip(ReflectionUtils.copyClassData(ItemsAndAmethystsToItemsAccessor.class, listing, ItemsAndAmethystsToItems.class)), List.of(
                "Uses: 5",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEnchantedItemForAmethystListing() {
        EnchantedItemForAmethyst listing = new EnchantedItemForAmethyst(Items.DIAMOND_SWORD, 6, 4, 10);

        assertTooltip(tooltip(ReflectionUtils.copyClassData(EnchantedItemForAmethystAccessor.class, listing, EnchantedItemForAmethyst.class)), List.of(
                "Uses: 10",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testPotionForAmethystListing() {
        PotionForAmethyst listing = new PotionForAmethyst(Items.GLASS_BOTTLE, Potions.HEALING, 3, 5, 7, 2);

        assertTooltip(tooltip(ReflectionUtils.copyClassData(PotionForAmethystAccessor.class, listing, PotionForAmethyst.class)), List.of(
                "Uses: 2",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @NotNull
    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        return UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip();
    }
}
