package com.yanny.alicompat.compat.advancedperipherals;

import com.yanny.aci.tooltip.TooltipNode;
import de.srendi.advancedperipherals.common.village.VillagerTrade;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class AdvancedPeripheralsTooltipTest {
    @Test
    public void testEmeraldForItemListing() {
        VillagerTrade trade = trade(VillagerTrade.Type.EMERALD_FOR_ITEM, 2, 4, 12, 5, Items.DIAMOND, ItemStack.EMPTY);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 1.0"
        ));
    }

    @Test
    public void testItemForEmeraldListing() {
        VillagerTrade trade = trade(VillagerTrade.Type.ITEM_FOR_EMERALD, 3, 1, 8, 2, Items.IRON_INGOT, ItemStack.EMPTY);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 8",
                "XP: 2",
                "Price Multiplier: 1.0"
        ));
    }

    @NotNull
    private static VillagerTrade trade(VillagerTrade.Type type, int emeraldAmount, int itemAmount, int maxUses, int xp, ItemLike item, ItemStack itemStack) {
        try {
            Constructor<VillagerTrade> constructor = VillagerTrade.class.getDeclaredConstructor(
                    VillagerTrade.Type.class, int.class, int.class, int.class, int.class, ItemLike.class, ItemStack.class);

            constructor.setAccessible(true);
            return constructor.newInstance(type, emeraldAmount, itemAmount, maxUses, xp, item, itemStack);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create VillagerTrade", e);
        }
    }
}
