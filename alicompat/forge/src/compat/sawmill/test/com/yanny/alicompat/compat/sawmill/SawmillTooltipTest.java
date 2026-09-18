package com.yanny.alicompat.compat.sawmill;

import com.yanny.aci.tooltip.TooltipNode;
import net.mehvahdjukaar.sawmill.CarpenterTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SawmillTooltipTest {
    @Test
    public void testWoodToItemListing() {
        CarpenterTrades.WoodToItemListing listing = new CarpenterTrades.WoodToItemListing(
                true, "log", 6, new ItemStack(Items.EMERALD, 2), 8, 3, 0.05F, 1, true);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 8",
                "XP: 3",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testLogStrippingListing() {
        CarpenterTrades.LogStrippingListing listing = new CarpenterTrades.LogStrippingListing(
                new ItemStack(Items.EMERALD), 4, 10, 2, 0.05F, 1);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 10",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }
}
