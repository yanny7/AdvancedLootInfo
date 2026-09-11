package com.yanny.alicompat.compat.mekanism;

import mekanism.common.item.loot.PersonalStorageContentsLootFunction;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MekanismTooltipTest {
    @Test
    public void testPersonalStorageContentsFunction() {
        LootItemFunction function = PersonalStorageContentsLootFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Personal Storage Contents"
        ));
    }

    @Test
    public void testFullCanteenValue() {
        assertTooltip(UTILS.getValueTooltip(UTILS, FullCanteenItemPredicate.INSTANCE).build(), List.of(
                "Full Canteen"
        ));
    }
}
