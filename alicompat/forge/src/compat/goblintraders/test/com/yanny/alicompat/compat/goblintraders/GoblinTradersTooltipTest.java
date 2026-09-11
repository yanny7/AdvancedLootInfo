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
        GoblinTrade trade = new GoblinTrade(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.EMERALD, 6), ItemStack.EMPTY, 5, 4, 0.75F);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 5",
                "XP: 4",
                "Price Multiplier: 0.75"
        ));
    }
}
