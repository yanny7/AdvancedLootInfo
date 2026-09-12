package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipNode;
import dev.shadowsoffire.apotheosis.village.wanderer.WandererTrade;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class ApotheosisTooltipTest {
    @Test
    public void testWandererTradeListing() {
        WandererTrade trade = new WandererTrade(new ItemStack(Items.EMERALD, 5), ItemStack.EMPTY, new ItemStack(Items.DIAMOND, 2), 6, 3, 1.0F, false);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 6",
                "XP: 3",
                "Price Multiplier: 1.0"
        ));
    }
}
