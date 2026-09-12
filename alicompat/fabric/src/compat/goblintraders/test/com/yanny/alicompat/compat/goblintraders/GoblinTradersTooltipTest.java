package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.trades.TradeCost;
import com.mrcrayfish.goblintraders.trades.type.BasicTrade;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class GoblinTradersTooltipTest {
    @Test
    public void testBasicTradeListing() {
        BasicTrade trade = new BasicTrade(new ItemStack(Items.DIAMOND, 2), new TradeCost(Items.EMERALD, 4), Optional.of(new TradeCost(Items.GOLD_INGOT, 1)), 0.25F, 6, 3);

        assertTooltip(UTILS.getItemListing(UTILS, new GoblinTradeListing(trade), TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 6",
                "XP: 3",
                "Price Multiplier: 0.25"
        ));
    }
}
