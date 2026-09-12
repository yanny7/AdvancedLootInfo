package com.yanny.alicompat.compat.charm;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;
import svenhjol.charmony.helper.GenericTradeOffers;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class CharmTooltipTest {
    @Test
    public void testEmeraldsForItemsListing() {
        assertTooltip(tooltip(new GenericTradeOffers.EmeraldsForItems(Items.APPLE, 4, 2, 1, 1, 12, 5)), List.of(
                "Uses: 5",
                "XP: 12",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testItemsForEmeraldsListing() {
        assertTooltip(tooltip(new GenericTradeOffers.ItemsForEmeralds(Items.BREAD, 3, 1, 2, 2, 9, 4)), List.of(
                "Uses: 4",
                "XP: 9",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testItemsForItemsListing() {
        assertTooltip(tooltip(new GenericTradeOffers.ItemsForItems(Items.WHEAT, Items.BREAD, 5, 1, 7, 3)), List.of(
                "Uses: 3",
                "XP: 7",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEmeraldsForTagListing() {
        assertTooltip(tooltip(new GenericTradeOffers.EmeraldsForTag<>(ItemTags.FLOWERS, 6, 2, 1, 1, 8, 2)), List.of(
                "Uses: 2",
                "XP: 8",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEmeraldsForTwoTagsListing() {
        assertTooltip(tooltip(new GenericTradeOffers.EmeraldsForTwoTags<>(ItemTags.FLOWERS, ItemTags.SAPLINGS, 6, 2, 1, 1, 8, 2)), List.of(
                "Uses: 2",
                "XP: 8",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testTagForEmeraldsListing() {
        assertTooltip(tooltip(new GenericTradeOffers.TagForEmeralds<>(ItemTags.SAPLINGS, 2, 1, 4, 2)), List.of(
                "Uses: 2",
                "XP: 4",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testEmeraldsForFlowersListing() {
        assertTooltip(tooltip(new BeekeeperTradeOffers.EmeraldsForFlowers(8, 4, 1, 1, 10, 6)), List.of(
                "Uses: 6",
                "XP: 10",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testEnchantedShearsForEmeraldsListing() {
        assertTooltip(tooltip(new BeekeeperTradeOffers.EnchantedShearsForEmeralds(12, 6, 5, 7)), List.of(
                "Uses: 7",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testPopulatedBeehiveForEmeraldsListing() {
        assertTooltip(tooltip(new BeekeeperTradeOffers.PopulatedBeehiveForEmeralds(20, 8, 3, 9)), List.of(
                "Uses: 9",
                "XP: 3",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testTallFlowerForEmeraldsListing() {
        assertTooltip(tooltip(new BeekeeperTradeOffers.TallFlowerForEmeralds(5, 2, 6, 4)), List.of(
                "Uses: 4",
                "XP: 6",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testBarkForLogsListing() {
        assertTooltip(tooltip(new LumberjackTradeOffers.BarkForLogs(4, 2, 11, 7)), List.of(
                "Uses: 7",
                "XP: 11",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSaplingsForEmeraldsListing() {
        assertTooltip(tooltip(new LumberjackTradeOffers.SaplingsForEmeralds(List.of(Items.OAK_SAPLING, Items.BIRCH_SAPLING), 3, 1, 13, 8)), List.of(
                "Uses: 8",
                "XP: 13",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testAnvilRepairListing() {
        assertTooltip(tooltip(anvilRepair(14, 9)), List.of(
                "Uses: 9",
                "XP: 14",
                "Price Multiplier: 0.2"
        ));
    }

    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        IDataNode node = UTILS.getItemListing(UTILS, listing, TooltipNode.empty());

        return node.getTooltip();
    }

    private static VillagerTrades.ItemListing anvilRepair(int villagerXp, int maxUses) {
        try {
            Class<?> type = Class.forName("svenhjol.charm.feature.extra_trades.ExtraTrades$AnvilRepair");
            var constructor = type.getDeclaredConstructor(int.class, int.class);

            constructor.setAccessible(true);
            return (VillagerTrades.ItemListing) constructor.newInstance(villagerXp, maxUses);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create AnvilRepair trade", e);
        }
    }
}
