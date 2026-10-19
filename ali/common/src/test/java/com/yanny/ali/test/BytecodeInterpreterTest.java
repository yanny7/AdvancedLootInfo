package com.yanny.ali.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeGroupNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.bytecode.BytecodeInterpreter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class BytecodeInterpreterTest {
    @Test
    public void testVanillaItemsForEmeralds() {
        VillagerTrades.ItemsForEmeralds listing = new VillagerTrades.ItemsForEmeralds(Items.BREAD, 1, 6, 16, 1);

        assertSameSlots(TradeUtils.getNode(UTILS, listing, TooltipNode.empty()), interpret(listing));
    }

    @Test
    public void testVanillaEmeraldForItems() {
        VillagerTrades.EmeraldForItems listing = new VillagerTrades.EmeraldForItems(Items.WHEAT, 20, 16, 2);

        assertSameSlots(TradeUtils.getNode(UTILS, listing, TooltipNode.empty()), interpret(listing));
    }

    @Test
    public void testVanillaEnchantedItemForEmeralds() {
        VillagerTrades.EnchantedItemForEmeralds listing = new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_SWORD, 2, 3, 1);
        IDataNode expected = TradeUtils.getNode(UTILS, listing, TooltipNode.empty());
        IDataNode actual = interpret(listing);

        Assertions.assertEquals(slot(expected, 0).getCount().bounds(), slot(actual, 0).getCount().bounds());
        Assertions.assertEquals(NumberInterval.closed(7, 21), slot(actual, 0).getCount().bounds());
        Assertions.assertSame(Items.IRON_SWORD, slot(actual, 2).getItem().left().orElseThrow().getItem());
        assertTooltip(slot(actual, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testRandomCounts() {
        IDataNode trade = interpret(new RandomCountListing());

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.uniformInt(1, 4));
    }

    @Test
    public void testRandomPickFromList() {
        IDataNode trade = interpret(new PickListing());
        IDataNode result = ((ListNode) trade).nodes().get(2);

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD, Items.CARROT), ((ListNode) result).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testRandomPickOfStacks() {
        IDataNode trade = interpret(new StackPickListing());
        List<ItemNode> options = ((ListNode) ((ListNode) trade).nodes().get(2)).nodes().stream().map(ItemNode.class::cast).toList();

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), options.stream().map((n) -> n.getItem().left().orElseThrow().getItem()).toList());
        Assertions.assertEquals(List.of(NumberExpr.constant(2), NumberExpr.constant(3)), options.stream().map(ItemNode::getCount).toList());
    }

    @Test
    public void testRandomPickAppliedToStack() {
        IDataNode trade = interpret(new PotionPickListing());
        List<ItemNode> options = ((ListNode) ((ListNode) trade).nodes().get(2)).nodes().stream().map(ItemNode.class::cast).toList();

        Assertions.assertEquals(List.of(Potions.NIGHT_VISION, Potions.INVISIBILITY, Potions.LEAPING), options.stream()
                .map((n) -> PotionUtils.getPotion(n.getItem().left().orElseThrow())).toList());
    }

    @Test
    public void testRandomPickPassedToConstructor() {
        IDataNode trade = interpret(new EnchantmentPickListing());

        Assertions.assertEquals(List.of(Map.of(Enchantments.SHARPNESS, 5), Map.of(Enchantments.MENDING, 1), Map.of(Enchantments.UNBREAKING, 3)),
                enchantments(trade, 1));
    }

    @Test
    public void testRandomNumberPassedToConstructor() {
        IDataNode trade = interpret(new EnchantmentLevelListing());

        Assertions.assertEquals(NumberInterval.closed(5, 45), slot(trade, 0).getCount().bounds());
        Assertions.assertEquals(List.of(Map.of(Enchantments.UNBREAKING, 1), Map.of(Enchantments.UNBREAKING, 2), Map.of(Enchantments.UNBREAKING, 3)),
                enchantments(trade, 2));
    }

    @Test
    public void testBoxedBooleanPredicate() {
        assertSlot(interpret(new PredicateListing(() -> true)), 2, Items.APPLE, NumberExpr.constant(1));
        assertSlot(interpret(new PredicateListing(new FlagSupplier(true))), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testMethodOverwrittenByMixin() throws ReflectiveOperationException {
        IntUnaryOperator original = Prices.create(false);
        IntUnaryOperator overwritten = Prices.create(true);

        assertSlot(interpret(new PriceListing(original, true)), 0, Items.EMERALD, NumberExpr.uniformInt(1, 3));
        assertSlot(interpret(new PriceListing(overwritten, false)), 0, Items.EMERALD, NumberExpr.constant(3));
        Assertions.assertNull(BytecodeInterpreter.getItemListing(UTILS, new PriceListing(overwritten, true), TooltipNode.empty()));
    }

    @Test
    public void testRandomPickWithMissingValues() {
        IDataNode trade = interpret(new NullablePickListing());

        Assertions.assertEquals(List.of(Items.APPLE, Items.BARRIER, Items.BREAD), ((ListNode) ((ListNode) trade).nodes().get(2)).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testRandomPickRemovedFromList() {
        Assertions.assertNull(BytecodeInterpreter.getItemListing(UTILS, new RemovingPickListing(), TooltipNode.empty()));
    }

    @Test
    public void testVanillaTreasureMap() {
        VillagerTrades.TreasureMapForEmeralds listing = new VillagerTrades.TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS,
                "filled_map.monument", MapDecoration.Type.MONUMENT, 12, 5);
        IDataNode expected = TradeUtils.getNode(UTILS, listing, TooltipNode.empty());
        IDataNode actual = interpret(listing);
        ItemStack map = slot(actual, 2).getItem().left().orElseThrow();
        List<String> tooltip = List.of(
                "Destination: minecraft:on_ocean_explorer_maps",
                "Map Decoration: Ocean Monument"
        );

        assertSlot(actual, 0, Items.EMERALD, NumberExpr.constant(13));
        assertSlot(actual, 1, Items.COMPASS, NumberExpr.constant(1));
        Assertions.assertSame(Items.FILLED_MAP, map.getItem());
        Assertions.assertEquals(slot(expected, 2).getItem().left().orElseThrow().getHoverName(), map.getHoverName());
        assertTooltip(slot(expected, 2).getTooltip(), tooltip);
        assertTooltip(slot(actual, 2).getTooltip(), tooltip);
    }

    @Test
    public void testMapWithBackupStructure() {
        List<IDataNode> trades = ((ListNode) interpret(new BackupMapListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trades.get(0).getTooltip(), List.of(
                "Structure: minecraft:on_ocean_explorer_maps",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
        assertTooltip(trades.get(1).getTooltip(), List.of(
                "All Of:",
                "  -> Inverted:",
                "    -> Structure: minecraft:on_ocean_explorer_maps",
                "  -> Structure: minecraft:on_woodland_explorer_maps",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testOffersDependingOnDimension() {
        List<IDataNode> trades = ((ListNode) interpret(new DimensionListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Dimension: minecraft:the_nether",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Dimension: minecraft:the_end",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.CARROT).getTooltip(), List.of(
                "Inverted:",
                "  -> Any Of:",
                "    -> Dimension: minecraft:the_end",
                "    -> Dimension: minecraft:the_nether",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testOfferOnlyInOneDimension() {
        IDataNode trade = interpret(new NetherOnlyListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertTooltip(trade.getTooltip(), List.of(
                "Dimension: minecraft:the_nether",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testOffersDependingOnBiomeTag() {
        List<IDataNode> trades = ((ListNode) interpret(new BiomeListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Biome: minecraft:is_ocean",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Inverted:",
                "  -> Biome: minecraft:is_ocean",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testOffersDependingOnVillagerType() {
        List<IDataNode> trades = ((ListNode) interpret(new VillagerTypeListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Villager Type: minecraft:desert",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Villager Type: minecraft:snow",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.CARROT).getTooltip(), List.of(
                "Inverted:",
                "  -> Any Of:",
                "    -> Villager Type: minecraft:desert",
                "    -> Villager Type: minecraft:snow",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testOffersDependingOnUnknownPredicate() {
        List<IDataNode> trades = ((ListNode) interpret(new HeightListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Unknown additional predicate",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Unknown additional predicate",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testOffersDependingOnRandomBranch() {
        List<IDataNode> trades = ((ListNode) interpret(new CoinListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Random chance",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Random chance",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testOffersDependingOnRandomSwitch() {
        List<IDataNode> trades = ((ListNode) interpret(new DiceListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.CARROT).getTooltip(), List.of(
                "Random chance",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testRandomBranchToTheSameOffer() {
        IDataNode trade = interpret(new SameCoinListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertTooltip(trade.getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testTraderWithOneLevel() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OneLevelTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());

        TradeLevel.OfListings level = (TradeLevel.OfListings) levels.get(1);

        Assertions.assertSame(Traders.COMMON, level.listings());
        Assertions.assertEquals(NumberExpr.constant(2), level.levelInfo().offers());
    }

    @Test
    public void testTraderWithTwoGroupsInOneLevel() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(TwoGroupTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());

        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);
        List<IDataNode> groups = level.trades().apply(UTILS);

        Assertions.assertEquals(NumberInterval.point(2), level.levelInfo().offers().bounds());
        Assertions.assertEquals(2, groups.size());
        Assertions.assertEquals(2, ((TradeGroupNode) groups.get(0)).nodes().size());
        Assertions.assertFalse(groups.get(1) instanceof TradeGroupNode);
    }

    @Test
    public void testTraderWithGroupsFromMapKeys() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(KeyedGroupTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertSame(Traders.COMMON, ((TradeLevel.OfListings) levels.get(1)).listings());
        Assertions.assertEquals(NumberExpr.constant(2), ((TradeLevel.OfListings) levels.get(1)).levelInfo().offers());
        Assertions.assertSame(Traders.RARE, ((TradeLevel.OfListings) levels.get(2)).listings());
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfListings) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderWithLevelFromItsState() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(LeveledTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertSame(Traders.COMMON, ((TradeLevel.OfListings) levels.get(1)).listings());
        Assertions.assertSame(Traders.RARE, ((TradeLevel.OfListings) levels.get(2)).listings());
    }

    @Test
    public void testTraderWithRandomNumberOfOffers() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RandomCountTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(new NumberInterval(7, 9, true, true), ((TradeLevel.OfListings) levels.get(1)).levelInfo().offers().bounds());
    }

    @Test
    public void testTraderBuildingItsOwnOffers() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OwnOffersTrader.class);

        Assertions.assertNotNull(levels);

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(1, trades.size());
        assertSlot(trades.get(0), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testTraderWithOwnLazyOffersAndOwnSelection() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OwnSelectionTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), listingItems(((TradeLevel.OfListings) levels.get(1)).listings()));
        Assertions.assertEquals(NumberExpr.constant(2), ((TradeLevel.OfListings) levels.get(1)).levelInfo().offers());
        Assertions.assertEquals(List.of(Items.CARROT), listingItems(((TradeLevel.OfListings) levels.get(2)).listings()));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfListings) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderWithRandomIndexSet() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(IndexSetTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), listingItems(((TradeLevel.OfListings) levels.get(1)).listings()));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfListings) levels.get(1)).levelInfo().offers());
        Assertions.assertEquals(List.of(Items.CARROT), listingItems(((TradeLevel.OfListings) levels.get(2)).listings()));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfListings) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderWithShuffledIndices() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ShuffleTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), listingItems(((TradeLevel.OfListings) levels.get(1)).listings()));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfListings) levels.get(1)).levelInfo().offers());
    }

    @Test
    public void testTraderWithGroupsIncludedByChance() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ChanceTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());

        TradeLevel.OfListings common = (TradeLevel.OfListings) levels.get(1);
        TradeLevel.OfListings rare = (TradeLevel.OfListings) levels.get(2);

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), listingItems(common.listings()));
        Assertions.assertEquals(1F, common.levelInfo().chance(), 1e-6);
        Assertions.assertEquals(List.of(Items.CARROT), listingItems(rare.listings()));
        Assertions.assertEquals(0.25F, rare.levelInfo().chance(), 1e-6);
        Assertions.assertEquals(new NumberInterval(1, 1, true, true), rare.levelInfo().offers().bounds());
    }

    @Test
    public void testTraderWithGroupsFromOneRoll() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OneRollTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(1F / 3, ((TradeLevel.OfListings) levels.get(1)).levelInfo().chance(), 1e-6);
        Assertions.assertEquals(1F / 3, ((TradeLevel.OfListings) levels.get(2)).levelInfo().chance(), 1e-6);
    }

    @Test
    public void testTraderWithEmptyGroup() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(EmptyGroupTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), listingItems(((TradeLevel.OfListings) levels.get(1)).listings()));
    }

    @Test
    public void testTraderWithTradesDependingOnItsState() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(EmptyHandTrader.class);

        Assertions.assertNotNull(levels);

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(1, trades.size());
        assertTooltip(trades.get(0).getTooltip(), List.of(
                "Unknown additional predicate",
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testTraderFillingItsOffersOnInteraction() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(InteractionTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(2, trades.size());
    }

    @Test
    public void testTraderRepeatingAnOffer() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RepeatingTrader.class);

        Assertions.assertNotNull(levels);

        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);
        List<IDataNode> trades = level.trades().apply(UTILS);

        Assertions.assertEquals(NumberExpr.constant(4), level.levelInfo().offers());
        Assertions.assertEquals(2, trades.size());
        Assertions.assertEquals(NumberExpr.constant(3), ((TradeGroupNode) trades.get(0)).selectionCount);
        Assertions.assertEquals(1, ((TradeGroupNode) trades.get(0)).nodes().size());
    }

    @Test
    public void testTraderWithSomeTradesUnknown() {
        BytecodeInterpreter.ComputedTrades trades = BytecodeInterpreter.getTradeLevels(UTILS, PartlyKnownTrader.class);

        Assertions.assertNotNull(trades);
        Assertions.assertTrue(trades.incomplete());

        List<IDataNode> nodes = ((TradeLevel.OfTrades) trades.levels().get(1)).trades().apply(UTILS);

        Assertions.assertEquals(3, nodes.size());
        Assertions.assertInstanceOf(MissingNode.class, nodes.get(2));
        assertTooltip(nodes.get(2).getTooltip(), List.of("Unable to auto-detect some custom trades"));
    }

    @Test
    public void testTraderWithAllTradesUnknown() {
        BytecodeInterpreter.ComputedTrades trades = BytecodeInterpreter.getTradeLevels(UTILS, UnknownTrader.class);

        Assertions.assertNotNull(trades);
        Assertions.assertTrue(trades.incomplete());
        Assertions.assertTrue(trades.levels().isEmpty());
    }

    @Test
    public void testDistinctListings() {
        VillagerTrades.ItemListing apple = new PlainListing(Items.APPLE);
        VillagerTrades.ItemListing[] distinct = TradeUtils.distinctListings(new VillagerTrades.ItemListing[]{apple, new PlainListing(Items.APPLE), new PlainListing(Items.BREAD)});

        Assertions.assertEquals(2, distinct.length);
        Assertions.assertSame(apple, distinct[0]);
        Assertions.assertEquals(Items.BREAD, ((PlainListing) distinct[1]).item);
    }

    @Test
    public void testTraderBuildingEqualListingsOnEveryPath() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RebuildingTrader.class);
        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trades.get(0).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Price Multiplier: 0.05"
        ));
    }

    @Test
    public void testTraderWithoutTrades() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(NoTradesTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertTrue(levels.isEmpty());
    }

    @Test
    public void testMutatedFieldStaysUntouched() {
        MutatingListing listing = new MutatingListing();
        IDataNode trade = interpret(listing);

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(1, 4));
        Assertions.assertEquals(1, listing.price.getCount());
    }

    @Test
    public void testLambdasWithTheSameSignature() {
        IDataNode first = interpret(Lambdas.FIRST);
        IDataNode second = interpret(Lambdas.SECOND);

        assertSlot(first, 0, Items.EMERALD, NumberExpr.uniformInt(1, 3));
        assertSlot(first, 2, Items.APPLE, NumberExpr.constant(1));
        assertSlot(second, 0, Items.EMERALD, NumberExpr.uniformInt(5, 9));
        assertSlot(second, 2, Items.BREAD, NumberExpr.constant(1));
    }

    @Test
    public void testEntityDependentListing() {
        Assertions.assertNull(BytecodeInterpreter.getItemListing(UTILS, new EntityListing(), TooltipNode.empty()));
    }

    @Test
    public void testSuppliersWithStaticRandom() {
        Random random = Suppliers.random;
        BytecodeInterpreter.Stacks arrows = BytecodeInterpreter.getItemStack(Suppliers.ARROWS);
        BytecodeInterpreter.Stacks apple = BytecodeInterpreter.getItemStack(Suppliers.APPLE);
        BytecodeInterpreter.Stacks pick = BytecodeInterpreter.getItemStack(Suppliers.PICK);

        Assertions.assertNotNull(arrows);
        Assertions.assertNotNull(apple);
        Assertions.assertNotNull(pick);
        Assertions.assertEquals(NumberExpr.uniformInt(16, 31), arrows.count());
        Assertions.assertEquals(List.of(Items.ARROW), arrows.options().stream().map(ItemStack::getItem).toList());
        Assertions.assertEquals(NumberExpr.constant(1), apple.count());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), pick.options().stream().map(ItemStack::getItem).toList());
        Assertions.assertSame(random, Suppliers.random);
    }

    private static IDataNode interpret(VillagerTrades.ItemListing listing) {
        IDataNode node = BytecodeInterpreter.getItemListing(UTILS, listing, TooltipNode.empty());

        Assertions.assertNotNull(node);
        return node;
    }

    private static IDataNode trade(List<IDataNode> trades, Item result) {
        return trades.stream().filter((t) -> slot(t, 2).getItem().left().orElseThrow().is(result)).findFirst().orElseThrow();
    }

    private static List<Item> listingItems(VillagerTrades.ItemListing[] listings) {
        return java.util.Arrays.stream(listings).map((l) -> ((OfferListing) l).item()).toList();
    }

    private static MerchantOffer offerFor(Item result) {
        return new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(result), 12, 5, 0.05F);
    }

    private static ItemNode slot(IDataNode trade, int index) {
        return (ItemNode) ((ListNode) trade).nodes().get(index);
    }

    private static List<Map<Enchantment, Integer>> enchantments(IDataNode trade, int index) {
        return ((ListNode) ((ListNode) trade).nodes().get(index)).nodes().stream()
                .map((n) -> EnchantmentHelper.getEnchantments(((ItemNode) n).getItem().left().orElseThrow())).toList();
    }

    private static void assertSlot(IDataNode trade, int index, Item item, NumberExpr count) {
        ItemNode node = slot(trade, index);

        Assertions.assertSame(item, node.getItem().left().orElseThrow().getItem());
        Assertions.assertEquals(count, node.getCount());
    }

    private static void assertSameSlots(IDataNode expected, IDataNode actual) {
        for (int i = 0; i < 3; i++) {
            ItemNode expectedSlot = slot(expected, i);
            ItemNode actualSlot = slot(actual, i);

            if (expectedSlot.getItem().left().orElseThrow().isEmpty()) {
                Assertions.assertTrue(actualSlot.getItem().left().orElseThrow().isEmpty());
                continue;
            }

            Assertions.assertSame(expectedSlot.getItem().left().orElseThrow().getItem(), actualSlot.getItem().left().orElseThrow().getItem());
            Assertions.assertEquals(expectedSlot.getCount().bounds(), actualSlot.getCount().bounds());
        }
    }

    private static final class RandomCountListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, 2 + random.nextInt(3)), new ItemStack(Items.APPLE, random.nextIntBetweenInclusive(1, 4)), 12, 5, 0.05F);
        }
    }

    private static final class PickListing implements VillagerTrades.ItemListing {
        private final List<Item> items = List.of(Items.APPLE, Items.BREAD, Items.CARROT);

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(items.get(random.nextInt(items.size()))), 12, 5, 0.05F);
        }
    }

    private static final class PotionPickListing implements VillagerTrades.ItemListing {
        private final List<Potion> potions = List.of(Potions.NIGHT_VISION, Potions.INVISIBILITY, Potions.LEAPING);

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            Potion potion = potions.get(random.nextInt(potions.size()));

            return new MerchantOffer(new ItemStack(Items.EMERALD), PotionUtils.setPotion(new ItemStack(Items.POTION), potion), 12, 5, 0.05F);
        }
    }

    private static final class StackPickListing implements VillagerTrades.ItemListing {
        private final List<ItemStack> stacks = List.of(new ItemStack(Items.APPLE, 2), new ItemStack(Items.BREAD, 3));

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(Items.EMERALD), stacks.get(random.nextInt(stacks.size())), 12, 5, 0.05F);
        }
    }

    private static final class EnchantmentPickListing implements VillagerTrades.ItemListing {
        private final List<Enchantment> enchantments = List.of(Enchantments.SHARPNESS, Enchantments.MENDING, Enchantments.UNBREAKING);

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            Enchantment enchantment = enchantments.get(random.nextInt(enchantments.size()));
            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel()));

            return new MerchantOffer(new ItemStack(Items.BOOK), book, new ItemStack(Items.EMERALD), 3, 3, 0.2F);
        }
    }

    private static final class EnchantmentLevelListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            Enchantment enchantment = Enchantments.UNBREAKING;
            int level = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
            int price = 2 + random.nextInt(5 + level * 10) + 3 * level;

            if (enchantment.isTreasureOnly()) {
                price *= 2;
            }
            if (price > 64) {
                price = 64;
            }

            return new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(Items.BOOK), book, 12, 1, 0.2F);
        }
    }

    private record PredicateListing(Supplier<Boolean> predicate) implements VillagerTrades.ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (!predicate.get()) {
                return null;
            }

            return new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private record FlagSupplier(Boolean value) implements Supplier<Boolean> {
        @Override
        public Boolean get() {
            return value;
        }
    }

    private record PriceListing(IntUnaryOperator price, boolean random) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, price.applyAsInt(this.random ? random.nextInt(3) : 2)), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class Prices extends ClassLoader {
        private static final String NAME = "com/yanny/ali/test/GeneratedPrices";
        private final byte[] bytes;

        private Prices(byte[] bytes) {
            super(BytecodeInterpreterTest.class.getClassLoader());
            this.bytes = bytes;
        }

        static IntUnaryOperator create(boolean overwritten) throws ReflectiveOperationException {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, NAME, null, "java/lang/Object", new String[]{"java/util/function/IntUnaryOperator"});

            MethodVisitor init = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);

            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            init.visitInsn(Opcodes.RETURN);
            init.visitMaxs(0, 0);

            MethodVisitor apply = writer.visitMethod(Opcodes.ACC_PUBLIC, "applyAsInt", "(I)I", null, null);

            if (overwritten) {
                AnnotationVisitor annotation = apply.visitAnnotation("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;", true);

                annotation.visit("mixin", "com.yanny.ali.test.PricesMixin");
                annotation.visitEnd();
            }

            apply.visitVarInsn(Opcodes.ILOAD, 1);
            apply.visitInsn(Opcodes.ICONST_1);
            apply.visitInsn(Opcodes.IADD);
            apply.visitInsn(Opcodes.IRETURN);
            apply.visitMaxs(0, 0);
            writer.visitEnd();

            Prices loader = new Prices(writer.toByteArray());

            return (IntUnaryOperator) loader.loadClass(NAME.replace('/', '.')).getConstructor().newInstance();
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(NAME.replace('/', '.'))) {
                return defineClass(name, bytes, 0, bytes.length);
            }

            return super.findClass(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return name.equals(NAME + ".class") ? new ByteArrayInputStream(bytes) : super.getResourceAsStream(name);
        }
    }

    private static final class NullablePickListing implements VillagerTrades.ItemListing {
        private final List<String> keys = List.of("apple", "none", "bread");
        private final Map<String, Item> items = Map.of("apple", Items.APPLE, "bread", Items.BREAD);

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            Item item = Optional.ofNullable(items.get(keys.get(random.nextInt(keys.size())))).orElse(Items.BARRIER);

            return new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(item), 12, 5, 0.05F);
        }
    }

    private static final class RemovingPickListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            List<Item> items = new ArrayList<>(List.of(Items.APPLE, Items.BREAD, Items.CARROT));
            Item picked = items.get(random.nextInt(items.size()));

            items.remove(picked);
            return new MerchantOffer(new ItemStack(Items.EMERALD, items.size()), new ItemStack(picked), 12, 5, 0.05F);
        }
    }

    private static final class BackupMapListing implements VillagerTrades.ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (!(trader.level() instanceof ServerLevel level)) {
                return null;
            }

            BlockPos pos = level.findNearestMapStructure(StructureTags.ON_OCEAN_EXPLORER_MAPS, trader.blockPosition(), 100, true);

            if (pos == null) {
                pos = level.findNearestMapStructure(StructureTags.ON_WOODLAND_EXPLORER_MAPS, trader.blockPosition(), 100, true);
            }
            if (pos == null) {
                return null;
            }

            ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);

            MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecoration.Type.MONUMENT);
            return new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(Items.COMPASS), map, 12, 5, 0.2F);
        }
    }

    private static final class DimensionListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            if (trader.level().dimension() == Level.NETHER) {
                return offerFor(Items.APPLE);
            }
            if (trader.level().dimension().equals(Level.END)) {
                return offerFor(Items.BREAD);
            }

            return offerFor(Items.CARROT);
        }
    }

    private static final class NetherOnlyListing implements VillagerTrades.ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return Level.NETHER.equals(trader.level().dimension()) ? offerFor(Items.APPLE) : null;
        }
    }

    private static final class BiomeListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return offerFor(trader.level().getBiome(trader.blockPosition()).is(BiomeTags.IS_OCEAN) ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class VillagerTypeListing implements VillagerTrades.ItemListing {
        private static final Map<VillagerType, Item> ITEMS = Map.of(VillagerType.DESERT, Items.APPLE, VillagerType.SNOW, Items.BREAD);

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return offerFor(ITEMS.getOrDefault(((VillagerDataHolder) trader).getVillagerData().getType(), Items.CARROT));
        }
    }

    private static final class HeightListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return offerFor(trader.getY() < 0 ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class CoinListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return offerFor(random.nextBoolean() ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class DiceListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return switch (random.nextInt(3)) {
                case 0 -> offerFor(Items.APPLE);
                case 1 -> offerFor(Items.BREAD);
                default -> offerFor(Items.CARROT);
            };
        }
    }

    private static final class SameCoinListing implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            Item item = Items.APPLE;

            if (random.nextFloat() < 0.5F) {
                item = Items.APPLE;
            }

            return offerFor(item);
        }
    }

    private static final class Traders {
        private static final VillagerTrades.ItemListing[] COMMON = {new OfferListing(Items.APPLE), new OfferListing(Items.BREAD)};
        private static final VillagerTrades.ItemListing[] RARE = {new OfferListing(Items.CARROT)};
        private static final Int2ObjectMap<VillagerTrades.ItemListing[]> BY_LEVEL = new Int2ObjectOpenHashMap<>(Map.of(1, COMMON, 2, RARE));
    }

    private static Int2ObjectMap<TradeLevel> computedLevels(Class<?> trader) {
        BytecodeInterpreter.ComputedTrades trades = BytecodeInterpreter.getTradeLevels(UTILS, trader);

        Assertions.assertNotNull(trades);
        Assertions.assertFalse(trades.incomplete());
        return trades.levels();
    }

    private static final class PlainListing implements VillagerTrades.ItemListing {
        private final Item item;

        private PlainListing(Item item) {
            this.item = item;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return offerFor(item);
        }
    }

    private record OfferListing(Item item) implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return offerFor(item);
        }
    }

    private abstract static class OneLevelTrader extends AbstractVillager {
        private OneLevelTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            addOffersFromItemListings(getOffers(), Traders.COMMON, 2);
        }
    }

    private abstract static class TwoGroupTrader extends AbstractVillager {
        private TwoGroupTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            MerchantOffers offers = getOffers();

            addOffersFromItemListings(offers, Traders.COMMON, 1);
            addOffersFromItemListings(offers, Traders.RARE, 1);
        }
    }

    private abstract static class KeyedGroupTrader extends AbstractVillager {
        private KeyedGroupTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            MerchantOffers offers = getOffers();

            addOffersFromItemListings(offers, Traders.BY_LEVEL.get(1), 2);
            addOffersFromItemListings(offers, Traders.BY_LEVEL.get(2), 1);
        }
    }

    private abstract static class LeveledTrader extends AbstractVillager {
        private int tradeLevel;

        private LeveledTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        private int getTradeLevel() {
            return tradeLevel;
        }

        @Override
        protected void updateTrades() {
            VillagerTrades.ItemListing[] listings = Traders.BY_LEVEL.get(getTradeLevel());

            if (listings != null) {
                addOffersFromItemListings(getOffers(), listings, 2);
            }
        }
    }

    private abstract static class RandomCountTrader extends AbstractVillager {
        private RandomCountTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            addOffersFromItemListings(getOffers(), Traders.COMMON, random.nextInt(3) + 7);
        }
    }

    private abstract static class OwnOffersTrader extends AbstractVillager {
        private OwnOffersTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            offers.add(offerFor(Items.APPLE));
            return offers;
        }
    }

    private abstract static class OwnSelectionTrader extends AbstractVillager {
        @Nullable
        private MerchantOffers offers;

        private OwnSelectionTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        public MerchantOffers getOffers() {
            if (offers == null) {
                offers = new MerchantOffers();
                populate();
            }

            return offers;
        }

        private void populate() {
            MerchantOffers offers = getOffers();

            addRandomOffers(offers, Traders.BY_LEVEL.get(1), 2);
            addRandomOffers(offers, Traders.BY_LEVEL.get(2), 4);
        }

        private void addRandomOffers(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count) {
            ArrayList<VillagerTrades.ItemListing> remaining = com.google.common.collect.Lists.newArrayList(listings);
            int added = 0;

            while (added < count && !remaining.isEmpty()) {
                MerchantOffer offer = remaining.remove(random.nextInt(remaining.size())).getOffer(this, random);

                if (offer != null) {
                    offers.add(offer);
                    added++;
                }
            }
        }
    }

    private abstract static class IndexSetTrader extends AbstractVillager {
        private IndexSetTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            addTrades(getOffers(), Traders.BY_LEVEL.get(1), 1);
            addTrades(getOffers(), Traders.BY_LEVEL.get(2), 3);
        }

        private void addTrades(MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count) {
            java.util.Set<Integer> picked = com.google.common.collect.Sets.newHashSet();

            if (listings.length > count) {
                while (picked.size() < count) {
                    picked.add(random.nextInt(listings.length));
                }
            } else {
                for (int i = 0; i < listings.length; i++) {
                    picked.add(i);
                }
            }

            for (Integer index : picked) {
                MerchantOffer offer = listings[index].getOffer(this, random);

                if (offer != null) {
                    offers.add(offer);
                }
            }
        }
    }

    private abstract static class ShuffleTrader extends AbstractVillager {
        private static final List<VillagerTrades.ItemListing> LISTINGS = List.of(Traders.COMMON);

        private ShuffleTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            List<Integer> indexes = java.util.stream.IntStream.range(0, LISTINGS.size()).boxed().collect(java.util.stream.Collectors.toList());

            java.util.Collections.shuffle(indexes);

            for (Integer index : indexes.subList(0, Math.min(LISTINGS.size(), 1))) {
                MerchantOffer offer = LISTINGS.get(index).getOffer(this, random);

                if (offer != null) {
                    getOffers().add(offer);
                }
            }
        }
    }

    private enum Rarity {
        COMMON,
        RARE,
    }

    private abstract static class OneRollTrader extends AbstractVillager {
        private OneRollTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            int roll = random.nextInt(3);

            if (roll == 0) {
                addOffersFromItemListings(getOffers(), Traders.BY_LEVEL.get(1), 1);
            } else if (roll == 1) {
                addOffersFromItemListings(getOffers(), Traders.BY_LEVEL.get(2), 1);
            }
        }
    }

    private abstract static class EmptyGroupTrader extends AbstractVillager {
        private static final Map<Rarity, List<VillagerTrades.ItemListing>> TRADES = Map.of(Rarity.COMMON, List.of(Traders.COMMON), Rarity.RARE, List.of());

        private EmptyGroupTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            for (Rarity rarity : Rarity.values()) {
                List<VillagerTrades.ItemListing> listings = TRADES.get(rarity);
                List<Integer> indexes = java.util.stream.IntStream.range(0, listings.size()).boxed().collect(java.util.stream.Collectors.toList());
                int count = 1 + random.nextInt(2);

                java.util.Collections.shuffle(indexes);

                for (Integer index : indexes.subList(0, Math.min(listings.size(), count))) {
                    MerchantOffer offer = listings.get(index).getOffer(this, random);

                    if (offer != null) {
                        getOffers().add(offer);
                    }
                }
            }
        }
    }

    private abstract static class ChanceTrader extends AbstractVillager {
        private static final Map<Rarity, List<VillagerTrades.ItemListing>> TRADES = Map.of(Rarity.COMMON, List.of(Traders.COMMON), Rarity.RARE, List.of(Traders.RARE));
        private static final Map<Rarity, Double> CHANCES = Map.of(Rarity.COMMON, 1.0, Rarity.RARE, 0.25);

        private ChanceTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            for (Rarity rarity : Rarity.values()) {
                double chance = CHANCES.get(rarity);

                if (chance < 1 && random.nextDouble() > chance) {
                    continue;
                }

                List<VillagerTrades.ItemListing> listings = TRADES.get(rarity);
                List<Integer> indexes = java.util.stream.IntStream.range(0, listings.size()).boxed().collect(java.util.stream.Collectors.toList());

                java.util.Collections.shuffle(indexes);

                for (Integer index : indexes.subList(0, Math.min(listings.size(), 2))) {
                    MerchantOffer offer = listings.get(index).getOffer(this, random);

                    if (offer != null) {
                        getOffers().add(offer);
                    }
                }
            }
        }
    }

    private abstract static class EmptyHandTrader extends AbstractVillager {
        private EmptyHandTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            if (getMainHandItem().isEmpty()) {
                offers.add(offerFor(Items.APPLE));
            }

            return offers;
        }
    }

    private abstract static class InteractionTrader extends AbstractVillager {
        private InteractionTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        public MerchantOffers getOffers() {
            if (offers == null) {
                offers = new MerchantOffers();
                offers.clear();
            }

            return offers;
        }

        @Override
        protected void updateTrades() {
        }

        @Override
        public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
            if (offers == null || offers.isEmpty()) {
                offers = generateOffers();
            }

            return super.mobInteract(player, hand);
        }

        private static MerchantOffers generateOffers() {
            MerchantOffers generated = new MerchantOffers();

            generated.clear();
            generated.add(offerFor(Items.APPLE));
            generated.add(offerFor(Items.BREAD));
            return generated;
        }
    }

    private abstract static class RepeatingTrader extends AbstractVillager {
        private RepeatingTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            MerchantOffers offers = getOffers();

            for (int i = 0; i < 3; i++) {
                offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1 + random.nextInt(3)), new ItemStack(Items.APPLE), 12, 5, 0.05F));
            }

            offers.add(offerFor(Items.BREAD));
            offers.sort(java.util.Comparator.comparingInt(MerchantOffer::getXp));
            java.util.Collections.reverse(offers);
        }
    }

    private abstract static class PartlyKnownTrader extends AbstractVillager {
        private PartlyKnownTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            if (getMainHandItem().isEmpty()) {
                addOffersFromItemListings(getOffers(), Traders.COMMON, 2);
            } else {
                getOffers().add(new MerchantOffer(new ItemStack(Items.EMERALD, (int) random.nextLong()), new ItemStack(Items.APPLE), 12, 5, 0.05F));
            }
        }
    }

    private abstract static class UnknownTrader extends AbstractVillager {
        private UnknownTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            getOffers().add(new MerchantOffer(new ItemStack(Items.EMERALD, (int) random.nextLong()), new ItemStack(Items.APPLE), 12, 5, 0.05F));
        }
    }

    private abstract static class RebuildingTrader extends AbstractVillager {
        private RebuildingTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
            VillagerTrades.ItemListing apple = new PlainListing(Items.APPLE);

            getOffers().add(apple.getOffer(this, random));

            if (!getMainHandItem().isEmpty()) {
                getOffers().add(new PlainListing(Items.BREAD).getOffer(this, random));
            }
        }
    }

    private abstract static class NoTradesTrader extends AbstractVillager {
        private NoTradesTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades() {
        }
    }

    private static final class MutatingListing implements VillagerTrades.ItemListing {
        private final ItemStack price = new ItemStack(Items.EMERALD, 1);

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            price.setCount(1 + random.nextInt(4));
            return new MerchantOffer(price, new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class EntityListing implements VillagerTrades.ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemStack(Items.EMERALD, trader.getId()), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class Lambdas {
        static final VillagerTrades.ItemListing FIRST = (trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, random.nextIntBetweenInclusive(1, 3)), new ItemStack(Items.APPLE), 1, 1, 0.05F);
        static final VillagerTrades.ItemListing SECOND = (trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, random.nextIntBetweenInclusive(5, 9)), new ItemStack(Items.BREAD), 1, 1, 0.05F);
    }

    private static final class Suppliers {
        static Random random = new Random();
        static final Supplier<ItemStack> ARROWS = () -> new ItemStack(Items.ARROW, 16 + random.nextInt(16));
        static final Supplier<ItemStack> APPLE = () -> new ItemStack(Items.APPLE);
        static final Supplier<ItemStack> PICK = () -> new ItemStack(List.of(Items.APPLE, Items.BREAD).get(random.nextInt(2)));
    }
}
