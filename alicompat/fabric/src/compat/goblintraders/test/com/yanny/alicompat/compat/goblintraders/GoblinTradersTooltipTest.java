package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.trades.GoblinTrade;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class GoblinTradersTooltipTest {
    @Test
    public void testGoblinTradeListing() {
        GoblinTrade trade = new GoblinTrade(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.EMERALD, 4), new ItemStack(Items.GOLD_INGOT, 1), 6, 3, 0.25F);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 6",
                "XP: 3",
                "Price Multiplier: 0.25"
        ));
    }
}
