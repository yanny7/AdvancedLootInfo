package com.yanny.alicompat.compat.charm;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import svenhjol.charm.feature.beekeepers.common.Trades;
import svenhjol.charm.charmony.common.villages.GenericTrades;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class CharmTooltipTest {
    @Test
    public void testEmeraldsForItemsListing() {
        assertTooltip(tooltip(new GenericTrades.EmeraldsForItems(Items.APPLE, 4, 2, 1, 1, 12, 5)), List.of(
                "Uses: 5",
                "XP: 12",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testItemsForEmeraldsListing() {
        assertTooltip(tooltip(new GenericTrades.ItemsForEmeralds(Items.BREAD, 3, 1, 2, 2, 9, 4)), List.of(
                "Uses: 4",
                "XP: 9",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testItemsForItemsListing() {
        assertTooltip(tooltip(new GenericTrades.ItemsForItems(Items.WHEAT, Items.BREAD, 5, 1, 7, 3)), List.of(
                "Uses: 3",
                "XP: 7",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEmeraldsForTagListing() {
        assertTooltip(tooltip(new GenericTrades.EmeraldsForTag<>(ItemTags.FLOWERS, 6, 2, 1, 1, 8, 2)), List.of(
                "Uses: 2",
                "XP: 8",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEmeraldsForTwoTagsListing() {
        assertTooltip(tooltip(new GenericTrades.EmeraldsForTwoTags<>(ItemTags.FLOWERS, ItemTags.SAPLINGS, 6, 2, 1, 1, 8, 2)), List.of(
                "Uses: 2",
                "XP: 8",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testTagForEmeraldsListing() {
        assertTooltip(tooltip(new GenericTrades.TagForEmeralds<>(ItemTags.SAPLINGS, 2, 1, 4, 2)), List.of(
                "Uses: 2",
                "XP: 4",
                "Price Multiplier: 0.05"
        ));
    }


    @Test
    public void testEnchantedShearsForEmeraldsListing() {
        assertTooltip(tooltip(new Trades.EnchantedShearsForEmeralds(12, 6, 5, 7)), List.of(
                "Uses: 7",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testPopulatedBeehiveForEmeraldsListing() {
        assertTooltip(tooltip(new Trades.PopulatedBeehiveForEmeralds(20, 8, 3, 9)), List.of(
                "Uses: 9",
                "XP: 3",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testTallFlowerForEmeraldsListing() {
        assertTooltip(tooltip(new Trades.TallFlowerForEmeralds(5, 2, 6, 4)), List.of(
                "Uses: 4",
                "XP: 6",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testBarkForLogsListing() {
        assertTooltip(tooltip(new svenhjol.charm.feature.lumberjacks.common.Trades.BarkForLogs(4, 2, 11, 7)), List.of(
                "Uses: 7",
                "XP: 11",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSaplingsForEmeraldsListing() {
        assertTooltip(tooltip(new svenhjol.charm.feature.lumberjacks.common.Trades.SaplingsForEmeralds(List.of(Items.OAK_SAPLING, Items.BIRCH_SAPLING), 3, 1, 13, 8)), List.of(
                "Uses: 8",
                "XP: 13",
                "Price Multiplier: 0.2"
        ));
    }

    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        IDataNode node = UTILS.getItemListing(UTILS, listing, TooltipNode.empty());

        return node.getTooltip();
    }
}
