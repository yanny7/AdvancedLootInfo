package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import vectorwing.farmersdelight.common.loot.function.CopyMealFunction;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;

import java.util.List;
import java.util.function.Supplier;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class FarmersDelightTooltipTest {
    @Test
    public void testCopyMealFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, CopyMealFunction.builder().build()).build(), List.of(
                "Copy Meal:"
        ));
    }

    @Test
    public void testCopyMealFunctionWithPredicate() {
        LootItemFunction function = CopyMealFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Meal:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testCopySkilletFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, CopySkilletFunction.builder().build()).build(), List.of(
                "Copy Skillet:"
        ));
    }

    @Test
    public void testCopySkilletFunctionWithPredicate() {
        LootItemFunction function = CopySkilletFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Skillet:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testFdItemListing() {
        VillagerTrades.ItemListing listing = new VillagerTrades.ItemsForEmeralds(Items.BREAD, 3, 2, 7, 4);

        assertTooltip(UTILS.getItemListing(UTILS, fdItemListing(listing), TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 7",
                "XP: 4",
                "Price Multiplier: 0.05"
        ));
    }

    @NotNull
    private static VillagerTrades.ItemListing fdItemListing(VillagerTrades.ItemListing listing) {
        try {
            Class<?> type = Class.forName("vectorwing.farmersdelight.common.event.VillagerEvents$FDItemListing");
            var constructor = type.getDeclaredConstructor(VillagerTrades.ItemListing.class, Supplier.class);

            constructor.setAccessible(true);
            return (VillagerTrades.ItemListing) constructor.newInstance(listing, (Supplier<Boolean>) () -> true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create FDItemListing", e);
        }
    }
}
