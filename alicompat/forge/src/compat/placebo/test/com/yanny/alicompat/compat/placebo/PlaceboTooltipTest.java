package com.yanny.alicompat.compat.placebo;

import dev.shadowsoffire.placebo.loot.StackLootEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class PlaceboTooltipTest {
    @Test
    public void testStackEntry() {
        StackLootEntry entry = new StackLootEntry(new ItemStack(Items.DIAMOND), 2, 5, 3, 1);

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Stack:",
                "  -> Item:",
                "    -> Item: minecraft:diamond",
                "    -> Count: 1",
                "  -> Count: 2-5",
                "  -> Weight: 3",
                "  -> Quality: 1"
        ));
    }
}
