package com.yanny.alicompat.compat.snowrealmagic;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.junit.jupiter.api.Test;
import snownee.snow.loot.NormalizeLoot;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SnowRealMagicTooltipTest {
    @Test
    public void testNormalizeEntry() {
        LootPoolEntryContainer entry = NormalizeLoot.builder().setWeight(4).setQuality(2).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Covered Block Drops:",
                "  -> Weight: 4",
                "  -> Quality: 2"
        ));
    }
}
