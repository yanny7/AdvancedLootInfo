package com.yanny.alicompat.compat.supplementaries;

import com.yanny.aci.tooltip.TooltipNode;
import net.mehvahdjukaar.supplementaries.common.entities.trades.*;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class SupplementariesTooltipTest {
    @Test
    public void testCurseLootFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, curseLoot(0.25)).build(), List.of(
                "Curse Loot:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testRandomArrowFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, randomArrow(2, 6)).build(), List.of(
                "Random Arrows:",
                "  -> Amount: 2-6"
        ));
    }

    @Test
    public void testStarItemListing() {
        StarItemListing listing = new StarItemListing(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.GUNPOWDER, 2), 3, 8, 5, 0.2F, 1);

        assertTooltip(tooltip(listing), List.of(
                "Uses: 8",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testRocketItemListing() {
        RocketItemListing listing = new RocketItemListing(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.PAPER, 1), 4, 9, 6, 0.2F, 2);

        assertTooltip(tooltip(listing), List.of(
                "Uses: 9",
                "XP: 10",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testStructureMapListing() {
        StructureMapListing listing = new StructureMapListing(Items.EMERALD, 5, 9, new ItemStack(Items.COMPASS), HolderSet.direct(List.of()), 7, 0.2F, 3,
                "filled_map.adventure", 0, new ResourceLocation("supplementaries", "adventure"));

        assertTooltip(tooltip(listing), List.of(
                "Uses: 7",
                "XP: 10",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testRandomAdventurerMapListing() {
        RandomAdventurerMapListing listing = new RandomAdventurerMapListing(Items.EMERALD, 6, 12, new ItemStack(Items.COMPASS), 8, 0.2F, 2);

        assertTooltip(tooltip(listing), List.of(
                "Uses: 8",
                "XP: 9",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testPresentItemListing() {
        StarItemListing original = new StarItemListing(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.GUNPOWDER, 2), 3, 8, 5, 0.2F, 1);

        assertTooltip(tooltip(new PresentItemListing(original)), List.of(
                "Uses: 8",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
    }

    @NotNull
    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        return UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip();
    }

    @NotNull
    private static LootItemFunction curseLoot(double chance) {
        return construct("net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction", new Class<?>[]{LootItemCondition[].class, double.class},
                new LootItemCondition[0], chance);
    }

    @NotNull
    private static LootItemFunction randomArrow(int min, int max) {
        return construct("net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction", new Class<?>[]{LootItemCondition[].class, int.class, int.class},
                new LootItemCondition[0], min, max);
    }

    @NotNull
    private static LootItemFunction construct(String className, Class<?>[] types, Object... args) {
        try {
            var constructor = Class.forName(className).getDeclaredConstructor(types);

            constructor.setAccessible(true);
            return (LootItemFunction) constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + className, e);
        }
    }
}
