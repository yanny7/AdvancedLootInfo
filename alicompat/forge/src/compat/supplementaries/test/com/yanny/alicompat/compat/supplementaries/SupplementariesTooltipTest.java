package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.tooltip.TooltipNode;
import net.mehvahdjukaar.supplementaries.common.entities.trades.PresentItemListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.RandomAdventurerMapListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.RocketItemListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.StarItemListing;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SupplementariesTooltipTest {
    @Test
    public void testCurseLootFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, curseLoot()).build(), List.of(
                "Curse Loot:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testRandomArrowFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, randomArrow()).build(), List.of(
                "Random Arrows:",
                "  -> Amount: 2-6"
        ));
    }

    @Test
    public void testStarItemListing() {
        StarItemListing listing = new StarItemListing(new ItemStack(Items.EMERALD, 4), ItemStack.EMPTY, 2, 9, 5, 0.05F, 1);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 9",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testRocketItemListing() {
        RocketItemListing listing = new RocketItemListing(new ItemStack(Items.EMERALD, 3), ItemStack.EMPTY, 6, 12, 4, 0.05F, 1);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 12",
                "XP: 2",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testAdventurerMapListing() {
        RandomAdventurerMapListing listing = new RandomAdventurerMapListing(Items.EMERALD, 8, 16, ItemStack.EMPTY, 6, 0.05F, 2);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 6",
                "XP: 12",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testPresentItemListing() {
        StarItemListing original = new StarItemListing(new ItemStack(Items.EMERALD, 4), ItemStack.EMPTY, 2, 9, 5, 0.05F, 1);

        assertTooltip(UTILS.getItemListing(UTILS, new PresentItemListing(original), TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 9",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @NotNull
    private static CurseLootFunction curseLoot() {
        try {
            Constructor<CurseLootFunction> constructor = CurseLootFunction.class.getDeclaredConstructor(LootItemCondition[].class, double.class);

            constructor.setAccessible(true);
            return constructor.newInstance(new LootItemCondition[0], 0.25);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create CurseLootFunction", e);
        }
    }

    @NotNull
    private static RandomArrowFunction randomArrow() {
        try {
            Constructor<RandomArrowFunction> constructor = RandomArrowFunction.class.getDeclaredConstructor(LootItemCondition[].class, int.class, int.class);

            constructor.setAccessible(true);
            return constructor.newInstance(new LootItemCondition[0], 2, 6);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create RandomArrowFunction", e);
        }
    }
}
