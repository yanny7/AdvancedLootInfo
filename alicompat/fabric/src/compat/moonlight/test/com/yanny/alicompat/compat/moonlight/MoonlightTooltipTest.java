package com.yanny.alicompat.compat.moonlight;

import com.yanny.aci.tooltip.TooltipNode;
import net.mehvahdjukaar.moonlight.api.trades.SimpleItemListing;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MoonlightTooltipTest {
    @Test
    public void testOptionalItemPoolEntry() {
        LootPoolEntryContainer entry = OptionalItemPool.lootTableOptionalItem("minecraft:apple").setWeight(3).setQuality(2).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Optional Item:",
                "  -> Item: minecraft:apple",
                "  -> Name: minecraft:apple",
                "  -> Weight: 3",
                "  -> Quality: 2"
        ));
    }

    @Test
    public void testOptionalPropertyCondition() {
        StatePropertiesPredicate properties = StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.LIT, true).build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition(new ResourceLocation("minecraft:furnace"), Blocks.FURNACE, properties)).build(), List.of(
                "Optional Block State Property:",
                "  -> Block: minecraft:furnace",
                "  -> Properties:",
                "    -> lit: true"
        ));
    }

    @Test
    public void testSimpleItemListing() {
        SimpleItemListing listing = new SimpleItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BREAD, 4), 7, 5, 0.1F);

        assertTooltip(UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 7",
                "XP: 5",
                "Price Multiplier: 0.1"
        ));
    }

    @Test
    public void testSpecialListing() {
        VillagerTrades.ItemListing listing = new VillagerTrades.ItemsForEmeralds(Items.BREAD, 3, 2, 7, 4);

        assertTooltip(UTILS.getItemListing(UTILS, specialListing(listing), TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 7",
                "XP: 4",
                "Price Multiplier: 0.05"
        ));
    }

    @NotNull
    private static VillagerTrades.ItemListing specialListing(VillagerTrades.ItemListing listing) {
        try {
            Class<?> type = Class.forName("net.mehvahdjukaar.moonlight.api.trades.ItemListingRegistry$SpecialListing");
            var constructor = type.getDeclaredConstructor(VillagerTrades.ItemListing.class, int.class);

            constructor.setAccessible(true);
            return (VillagerTrades.ItemListing) constructor.newInstance(listing, 1);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create SpecialListing", e);
        }
    }

    @NotNull
    private static OptionalPropertyCondition condition(ResourceLocation blockId, Block block, StatePropertiesPredicate properties) {
        try {
            var constructor = OptionalPropertyCondition.class.getDeclaredConstructor(ResourceLocation.class, Block.class, StatePropertiesPredicate.class);

            constructor.setAccessible(true);
            return constructor.newInstance(blockId, block, properties);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create OptionalPropertyCondition", e);
        }
    }
}
