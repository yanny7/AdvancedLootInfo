package com.yanny.alicompat.compat.ribbits;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.alicompat.accessor.ReflectionUtils;
import com.yungnickyoung.minecraft.ribbits.entity.trade.AmethystForItems;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemsAndAmethystsToItems;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemsForAmethysts;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class RibbitsTooltipTest {
    @Test
    public void testItemsForAmethystsListing() {
        ItemsForAmethysts listing = new ItemsForAmethysts(Items.DIAMOND, 2, 5, 1, 3, 8);
        ItemsForAmethystsAccessor accessor = ReflectionUtils.copyClassData(ItemsForAmethystsAccessor.class, listing, ItemsForAmethysts.class);

        assertTooltip(UTILS.getItemListing(UTILS, accessor, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 8",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testAmethystForItemsListing() {
        AmethystForItems listing = new AmethystForItems(Items.IRON_INGOT, 4, 6, 1, 3, 9);
        AmethystForItemsAccessor accessor = ReflectionUtils.copyClassData(AmethystForItemsAccessor.class, listing, AmethystForItems.class);

        assertTooltip(UTILS.getItemListing(UTILS, accessor, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 9",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testItemsAndAmethystsToItemsListing() {
        ItemsAndAmethystsToItems listing = new ItemsAndAmethystsToItems(Items.OAK_LOG, 3, 2, Items.OAK_PLANKS, 4, 7);
        ItemsAndAmethystsToItemsAccessor accessor = ReflectionUtils.copyClassData(ItemsAndAmethystsToItemsAccessor.class, listing, ItemsAndAmethystsToItems.class);

        assertTooltip(UTILS.getItemListing(UTILS, accessor, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 7",
                "XP: 0",
                "Price Multiplier: 0.05"
        ));
    }
}
