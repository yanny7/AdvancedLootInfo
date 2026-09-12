package com.yanny.alicompat.compat.farlanders;

import com.legacy.farlanders.entity.util.FarlanderTrades;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class FarlandersTooltipTest {
    @Test
    public void testTradeListing() {
        FarlanderTrades.Trade trade = new FarlanderTrades.Trade(Items.EMERALD, 4, Items.ENDER_PEARL, 2, Items.DIAMOND, 1, 7, 3);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 7",
                "XP: 3",
                "Price Multiplier: 0.05"
        ));
    }
}
