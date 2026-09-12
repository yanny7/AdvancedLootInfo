package com.yanny.alicompat.compat.sawmill;

import com.yanny.aci.tooltip.TooltipNode;
import net.mehvahdjukaar.sawmill.CarpenterTrades;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SawmillTooltipTest {
    @Test
    public void testWoodToItemListing() {
        CarpenterTrades.WoodToItemListing listing = new CarpenterTrades.WoodToItemListing(true, "log", 6, new ItemStack(Items.EMERALD, 2), 8, 4, 0.15F, 1, true);

        assertTooltip(tooltip(listing), List.of(
                "Uses: 8",
                "XP: 4",
                "Price Multiplier: 0.15"
        ));
    }

    @Test
    public void testLogStrippingListing() {
        CarpenterTrades.LogStrippingListing listing = new CarpenterTrades.LogStrippingListing(new ItemStack(Items.EMERALD, 3), 5, 9, 7, 0.15F, 2);

        assertTooltip(tooltip(listing), List.of(
                "Uses: 9",
                "XP: 7",
                "Price Multiplier: 0.15"
        ));
    }

    @NotNull
    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        return UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip();
    }
}
