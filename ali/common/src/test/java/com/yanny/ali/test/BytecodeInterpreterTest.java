package com.yanny.ali.test;

import com.mojang.serialization.Lifecycle;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.*;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.common.trades.TradeGroupNode;
import com.yanny.ali.plugin.common.trades.TradeNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.bytecode.BytecodeInterpreter;
import com.yanny.ali.plugin.server.bytecode.TestTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.aci.test.utils.TestUtils.componentToPlainString;
import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.mockito.Mockito.*;

public class BytecodeInterpreterTest {
    private static final java.lang.reflect.Method GET_OFFER = ItemListing.class.getMethods()[0];

    public interface ItemListing {
        @Nullable
        MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random);
    }

    @Test
    public void testRandomCounts() {
        IDataNode trade = interpret(new RandomCountListing());

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.uniformInt(1, 4));
    }

    @Test
    public void testInterfaceDefaultMethod() {
        assertSlot(interpret(new DefaultMethodListing()), 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
    }

    @Test
    public void testRandomPickFromList() {
        IDataNode trade = interpret(new PickListing());
        IDataNode result = ((ListNode) trade).nodes().get(2);

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD, Items.CARROT), ((ListNode) result).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testRandomPickFromLargeTag() {
        TagPickListing listing = new TagPickListing();
        IDataNode result = ((ListNode) interpret(listing)).nodes().get(2);

        Assertions.assertEquals(listing.items, ((ListNode) result).nodes().stream().map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testRandomPickFromItemTagContents() {
        bindDoorTags();

        IDataNode result = ((ListNode) interpret(new TagValuesListing(false))).nodes().get(2);

        Assertions.assertEquals(DOORS, ((ItemNode) result).getItem().right().orElseThrow());
        Assertions.assertEquals(NumberExpr.constant(1), ((ItemNode) result).getCount());
    }

    @Test
    public void testRandomPickFromBlockTagContents() {
        bindDoorTags();

        IDataNode result = ((ListNode) interpret(new BlockTagValuesListing(BLOCK_DOORS))).nodes().get(2);

        Assertions.assertEquals(BLOCK_DOORS, ((ItemNode) result).getItem().right().orElseThrow());
    }

    @Test
    public void testRandomPickFromBlockTagWithItemlessAndRepeatedBlocks() {
        bindDoorTags();

        IDataNode result = ((ListNode) interpret(new BlockTagValuesListing(BLOCK_TORCHES))).nodes().get(2);

        Assertions.assertEquals(BLOCK_TORCHES, ((ItemNode) result).getItem().right().orElseThrow());
    }

    @Test
    public void testRandomPickFromTagContentsPassedToStackFactory() {
        bindDoorTags();

        IDataNode result = ((ListNode) interpret(new TagValuesFactoryListing())).nodes().get(2);

        Assertions.assertEquals(DOORS, ((ItemNode) result).getItem().right().orElseThrow());
    }

    @Test
    public void testRandomPickFromChangedTagContents() {
        bindDoorTags();

        IDataNode result = ((ListNode) interpret(new TagValuesListing(true))).nodes().get(2);

        Assertions.assertEquals(List.of(Items.SPRUCE_DOOR, Items.BIRCH_DOOR), ((ListNode) result).nodes().stream()
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
                .map((n) -> Objects.requireNonNull(n.getItem().left().orElseThrow().get(DataComponents.POTION_CONTENTS)).potion().orElseThrow()).toList());
    }

    @Test
    public void testRandomPickPassedToConstructor() {
        IDataNode trade = interpret(new EnchantmentPickListing());

        Assertions.assertEquals(List.of(Map.of(Enchantments.SHARPNESS, 5), Map.of(Enchantments.MENDING, 1), Map.of(Enchantments.UNBREAKING, 3)),
                enchantments(trade, 2));
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
        Assertions.assertNull(TestTrades.interpret(UTILS, GET_OFFER, new PriceListing(overwritten, true), TooltipNode.empty()));
    }

    @Test
    public void testRandomPickWithMissingValues() {
        IDataNode trade = interpret(new NullablePickListing());

        Assertions.assertEquals(List.of(Items.APPLE, Items.BARRIER, Items.BREAD), ((ListNode) ((ListNode) trade).nodes().get(2)).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testRandomPickRemovedFromList() {
        IDataNode trade = interpret(new RemovingPickListing());
        IDataNode result = ((ListNode) trade).nodes().get(2);

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.constant(2));
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD, Items.CARROT), ((ListNode) result).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testMapWithBackupStructure() {
        List<IDataNode> trades = ((ListNode) interpret(new BackupMapListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trades.getFirst().getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Structure: minecraft:on_ocean_explorer_maps"
        ));
        assertTooltip(trades.get(1).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "All Of:",
                "  -> Inverted:",
                "    -> Structure: minecraft:on_ocean_explorer_maps",
                "  -> Structure: minecraft:on_woodland_explorer_maps"
        ));
    }

    @Test
    public void testOffersDependingOnDimension() {
        List<IDataNode> trades = ((ListNode) interpret(new DimensionListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Dimension: minecraft:the_nether"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Dimension: minecraft:the_end"
        ));
        assertTooltip(trade(trades, Items.CARROT).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Inverted:",
                "  -> Any Of:",
                "    -> Dimension: minecraft:the_end",
                "    -> Dimension: minecraft:the_nether"
        ));
    }

    @Test
    public void testOfferOnlyInOneDimension() {
        IDataNode trade = interpret(new NetherOnlyListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertTooltip(trade.getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Dimension: minecraft:the_nether"
        ));
    }

    @Test
    public void testOffersDependingOnBiomeTag() {
        List<IDataNode> trades = ((ListNode) interpret(new BiomeListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Biome: minecraft:is_ocean"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Inverted:",
                "  -> Biome: minecraft:is_ocean"
        ));
    }

    @Test
    public void testOffersDependingOnVillagerType() {
        List<IDataNode> trades = ((ListNode) interpret(new VillagerTypeListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Value: minecraft:desert"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Value: minecraft:snow"
        ));
        assertTooltip(trade(trades, Items.CARROT).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Inverted:",
                "  -> Any Of:",
                "    -> Value: minecraft:desert",
                "    -> Value: minecraft:snow"
        ));
    }

    @Test
    public void testOffersDependingOnUnknownPredicate() {
        List<IDataNode> trades = ((ListNode) interpret(new HeightListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Unknown additional predicate"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Unknown additional predicate"
        ));
    }

    @Test
    public void testOffersDependingOnRandomBranch() {
        List<IDataNode> trades = ((ListNode) interpret(new CoinListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Random chance"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Random chance"
        ));
    }

    @Test
    public void testOffersDependingOnRandomSwitch() {
        List<IDataNode> trades = ((ListNode) interpret(new DiceListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.CARROT).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Random chance"
        ));
    }

    @Test
    public void testRandomBranchToTheSameOffer() {
        IDataNode trade = interpret(new SameCoinListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertTooltip(trade.getTooltip(), List.of(
                "Uses: 12",
                "XP: 5"
        ));
    }

    @Test
    public void testTraderAddingTradeSet() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(withTradeSets(), TradeSetTrader.class);

        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());
        Assertions.assertEquals(new TradeLevel.OfSet(FIRST_SET), levels.get(1));
    }

    @Test
    public void testTraderAddingTradeSetPerLevel() {
        IServerUtils utils = withTradeSets();
        Int2ObjectMap<TradeLevel> levels = computedLevels(utils, LeveledTradeSetTrader.class);
        TradeNode node = new TradeNode(utils, EntityTypes.VILLAGER, levels);

        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(new TradeLevel.OfSet(FIRST_SET), levels.get(1));
        Assertions.assertEquals(new TradeLevel.OfSet(SECOND_SET), levels.get(2));
        Assertions.assertEquals(List.of(3, 1), node.nodes().stream().map((l) -> ((ListNode) l).nodes().size()).toList());
    }

    @Test
    public void testTraderAddingTradeSetAndOwnOffer() {
        IServerUtils utils = withTradeSets();
        TradeLevel.OfTrades level = (TradeLevel.OfTrades) computedLevels(utils, TradeSetAndOfferTrader.class).get(1);
        List<IDataNode> trades = level.trades().apply(utils);
        TradeGroupNode set = (TradeGroupNode) trades.stream().filter(TradeGroupNode.class::isInstance).findFirst().orElseThrow();

        Assertions.assertEquals(NumberInterval.point(3), level.levelInfo().offers().bounds());
        Assertions.assertEquals(2, trades.size());
        Assertions.assertEquals(NumberInterval.point(2), set.selectionCount.bounds());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD, Items.CARROT), set.nodes().stream().map((t) -> slot(t, 2).getItem().left().orElseThrow().getItem()).toList());
        assertSlot(trades.stream().filter((t) -> !(t instanceof TradeGroupNode)).findFirst().orElseThrow(), 2, Items.DIAMOND, NumberExpr.constant(1));
    }

    @Test
    public void testTraderAddingTradeSetByChance() {
        IServerUtils utils = withTradeSets();
        TradeLevel.OfTrades level = (TradeLevel.OfTrades) computedLevels(utils, ChanceTradeSetTrader.class).get(1);
        List<IDataNode> trades = level.trades().apply(utils);

        Assertions.assertEquals(0.25F, level.levelInfo().chance(), 1e-6);
        Assertions.assertEquals(NumberInterval.point(2), level.levelInfo().offers().bounds());
        Assertions.assertEquals(1, trades.size());
        Assertions.assertEquals(3, ((TradeGroupNode) trades.get(0)).nodes().size());
    }

    @Test
    public void testTraderAddingTradeSetThatDoesNotResolve() {
        IServerUtils utils = withTradeSets();
        TradeLevel.OfTrades level = (TradeLevel.OfTrades) computedLevels(utils, MissingTradeSetTrader.class).get(1);
        List<IDataNode> trades = level.trades().apply(utils);

        Assertions.assertEquals(NumberInterval.point(1), level.levelInfo().offers().bounds());
        Assertions.assertEquals(1, trades.size());
        assertSlot(trades.get(0), 2, Items.DIAMOND, NumberExpr.constant(1));
    }

    @Test
    public void testTraderWithOneLevel() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OneLevelTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());

        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);

        Assertions.assertEquals(listingItems(Traders.COMMON), levelItems(level));
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
        Assertions.assertEquals(2, ((TradeGroupNode) groups.getFirst()).nodes().size());
        Assertions.assertFalse(groups.get(1) instanceof TradeGroupNode);
    }

    @Test
    public void testTraderWithGroupsFromMapKeys() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(KeyedGroupTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(listingItems(Traders.COMMON), levelItems((TradeLevel.OfTrades) levels.get(1)));
        Assertions.assertEquals(NumberExpr.constant(2), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers());
        Assertions.assertEquals(listingItems(Traders.RARE), levelItems((TradeLevel.OfTrades) levels.get(2)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderPickingListingAtRandomIndex() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RandomIndexTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(List.of(Items.CARROT), levelItems((TradeLevel.OfTrades) levels.get(1)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), levelItems((TradeLevel.OfTrades) levels.get(2)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderWithTradesByProfessionAndLevel() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ProfessionTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5), levels.keySet().intStream().sorted().boxed().toList());

        for (TradeLevel level : levels.values()) {
            List<IDataNode> groups = ((TradeLevel.OfTrades) level).trades().apply(UTILS);

            Assertions.assertEquals(ProfessionTrader.TRADES.size(), groups.size());
            Assertions.assertTrue(groups.stream().allMatch(TradeGroupNode.class::isInstance));
        }

        assertTooltip(((TradeGroupNode) ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS).getFirst()).nodes().getFirst().getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "All Of:",
                "  -> Value: minecraft:armorer",
                "  -> Unknown additional predicate"
        ));
    }

    @Test
    public void testTraderWithProfessionFromRegistry() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RegistryProfessionTrader.class);
        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());
        Assertions.assertEquals(3, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Value: ali:farmer"
        ));
        assertTooltip(trade(trades, Items.IRON_INGOT).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Value: ali:smith"
        ));
    }

    @Test
    public void testTraderPassingTwoStacksToListingFactory() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(TwoStackTrader.class);
        List<IDataNode> trades = levelTrades(levels.get(1));
        IDataNode trade = trades.get(0);

        Assertions.assertEquals(1, trades.size());
        assertSlot(trade, 0, Items.EMERALD, NumberExpr.constant(10));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testTraderPickingOwnTradeObjects() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ShuffledTradesTrader.class);

        Assertions.assertNotNull(levels);

        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);
        List<IDataNode> trades = levelTrades(level);

        Assertions.assertEquals(NumberExpr.constant(2), level.levelInfo().offers());
        Assertions.assertEquals(1F, level.levelInfo().chance());
        Assertions.assertEquals(3, trades.size());
        assertSlot(trades.get(0), 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(trades.get(0), 2, Items.APPLE, NumberExpr.constant(1));
        assertSlot(trades.get(1), 2, Items.BREAD, NumberExpr.constant(1));
        assertSlot(trades.get(2), 2, Items.CARROT, NumberExpr.constant(1));
    }

    @Test
    public void testTraderWithLevelFromItsState() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(LeveledTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(listingItems(Traders.COMMON), levelItems((TradeLevel.OfTrades) levels.get(1)));
        Assertions.assertEquals(listingItems(Traders.RARE), levelItems((TradeLevel.OfTrades) levels.get(2)));
    }

    @Test
    public void testTraderWithRandomNumberOfOffers() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RandomCountTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(new NumberInterval(1, 2, true, true), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers().bounds());
    }

    @Test
    public void testTraderBuildingItsOwnOffers() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OwnOffersTrader.class);

        Assertions.assertNotNull(levels);

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(1, trades.size());
        assertSlot(trades.getFirst(), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testTraderWithOwnLazyOffersAndOwnSelection() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OwnSelectionTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), levelItems((TradeLevel.OfTrades) levels.get(1)));
        Assertions.assertEquals(NumberExpr.constant(2), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers());
        Assertions.assertEquals(List.of(Items.CARROT), levelItems((TradeLevel.OfTrades) levels.get(2)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderWithRandomIndexSet() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(IndexSetTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), levelItems((TradeLevel.OfTrades) levels.get(1)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers());
        Assertions.assertEquals(List.of(Items.CARROT), levelItems((TradeLevel.OfTrades) levels.get(2)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(2)).levelInfo().offers());
    }

    @Test
    public void testTraderWithShuffledIndices() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ShuffleTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), levelItems((TradeLevel.OfTrades) levels.get(1)));
        Assertions.assertEquals(NumberExpr.constant(1), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers());
    }

    @Test
    public void testTraderWithGroupsIncludedByChance() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ChanceTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());

        TradeLevel.OfTrades common = (TradeLevel.OfTrades) levels.get(1);
        TradeLevel.OfTrades rare = (TradeLevel.OfTrades) levels.get(2);

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), levelItems(common));
        Assertions.assertEquals(1F, common.levelInfo().chance(), 1e-6);
        Assertions.assertEquals(List.of(Items.CARROT), levelItems(rare));
        Assertions.assertEquals(0.25F, rare.levelInfo().chance(), 1e-6);
        Assertions.assertEquals(new NumberInterval(1, 1, true, true), rare.levelInfo().offers().bounds());
    }

    @Test
    public void testTraderWithGroupsFromOneRoll() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(OneRollTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1, 2), levels.keySet().intStream().sorted().boxed().toList());
        Assertions.assertEquals(1F / 3, ((TradeLevel.OfTrades) levels.get(1)).levelInfo().chance(), 1e-6);
        Assertions.assertEquals(1F / 3, ((TradeLevel.OfTrades) levels.get(2)).levelInfo().chance(), 1e-6);
    }

    @Test
    public void testTraderWithEmptyGroup() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(EmptyGroupTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertEquals(List.of(1), levels.keySet().intStream().boxed().toList());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), levelItems((TradeLevel.OfTrades) levels.get(1)));
    }

    @Test
    public void testTraderWithTradesDependingOnItsState() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(EmptyHandTrader.class);

        Assertions.assertNotNull(levels);

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(1, trades.size());
        assertTooltip(trades.getFirst().getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Unknown additional predicate"
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
        Assertions.assertEquals(NumberExpr.constant(3), ((TradeGroupNode) trades.getFirst()).selectionCount);
        Assertions.assertEquals(1, ((TradeGroupNode) trades.getFirst()).nodes().size());
    }

    @Test
    public void testTraderRemovingMissingOffers() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(NullFilteringTrader.class);

        Assertions.assertNotNull(levels);

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(Set.of(Items.CARROT, Items.APPLE), trades.stream().map((t) -> slot(t, 2).getItem().left().orElseThrow().getItem()).collect(Collectors.toSet()));
        Assertions.assertEquals(2, trades.size());
    }

    @Test
    public void testTraderCollectingPickedListings() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(CollectedListingsTrader.class);

        Assertions.assertNotNull(levels);

        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD, Items.CARROT), levelItems(level));
        Assertions.assertEquals(oneOrTwo(), level.levelInfo().offers());
        Assertions.assertEquals(1F, level.levelInfo().chance());
    }

    @Test
    public void testTraderCollectingPickedOffers() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(CollectedOffersTrader.class);

        Assertions.assertNotNull(levels);

        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);
        List<IDataNode> trades = level.trades().apply(UTILS);

        Assertions.assertEquals(oneOrTwo(), level.levelInfo().offers());
        Assertions.assertEquals(1F, level.levelInfo().chance());
        Assertions.assertEquals(3, trades.size());
        assertSlot(trades.getFirst(), 2, Items.APPLE, NumberExpr.constant(1));
        assertSlot(trades.get(1), 2, Items.BREAD, NumberExpr.constant(1));
        assertSlot(trades.get(2), 2, Items.CARROT, NumberExpr.constant(1));
    }

    @NotNull
    private static NumberExpr oneOrTwo() {
        return NumberExpr.weighted(List.of(new NumberExpr.WeightedEntry(0.5, NumberExpr.constant(2)), new NumberExpr.WeightedEntry(0.5, NumberExpr.constant(1))));
    }

    @Test
    public void testTraderAddingOffersFromCollection() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(CollectionTrader.class);

        Assertions.assertNotNull(levels);

        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(2, trades.size());
        assertSlot(trades.getFirst(), 2, Items.BREAD, NumberExpr.constant(1));
        assertSlot(trades.get(1), 2, Items.APPLE, NumberExpr.constant(1));
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
        ItemListing apple = new PlainListing(Items.APPLE);
        Object[] distinct = TradeUtils.distinctListings(new Object[]{apple, new PlainListing(Items.APPLE), new PlainListing(Items.BREAD)});

        Assertions.assertEquals(2, distinct.length);
        Assertions.assertSame(apple, distinct[0]);
        Assertions.assertEquals(Items.BREAD, ((PlainListing) distinct[1]).item);
    }

    @Test
    public void testMinecraftRecordsHoldingStacksCompareByContents() {
        Object emeralds = TradeUtils.valueKey(new Display.ItemDisplay.ItemRenderState(new ItemStack(Items.EMERALD, 2), ItemDisplayContext.NONE));

        Assertions.assertEquals(emeralds, TradeUtils.valueKey(new Display.ItemDisplay.ItemRenderState(new ItemStack(Items.EMERALD, 2), ItemDisplayContext.NONE)));
        Assertions.assertNotEquals(emeralds, TradeUtils.valueKey(new Display.ItemDisplay.ItemRenderState(new ItemStack(Items.EMERALD, 3), ItemDisplayContext.NONE)));
    }

    @Test
    public void testTraderBuildingEqualListingsOnEveryPath() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(RebuildingTrader.class);
        List<IDataNode> trades = ((TradeLevel.OfTrades) levels.get(1)).trades().apply(UTILS);

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trades.getFirst().getTooltip(), List.of(
                "Uses: 12",
                "XP: 5"
        ));
    }

    @Test
    public void testTraderWithoutTrades() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(NoTradesTrader.class);

        Assertions.assertNotNull(levels);
        Assertions.assertTrue(levels.isEmpty());
    }

    @Test
    public void testVillagerWithoutOwnTrades() {
        Assertions.assertTrue(computedLevels(IdleVillager.class).isEmpty());
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
    public void testListingBuildingEntityInTradersLevel() {
        IDataNode trade = interpret(new BeehiveListing());

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(trade, 2, Items.BEEHIVE, NumberExpr.constant(1));
        assertTooltip(slot(trade, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testListingLoggingWithoutWritingToTheLog() {
        AtomicInteger calls = new AtomicInteger();
        Logger logger = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class<?>[]{Logger.class}, (proxy, method, args) -> {
            calls.incrementAndGet();
            return null;
        });
        IDataNode trade = interpret(new LoggingListing(logger));

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        Assertions.assertEquals(0, calls.get());
    }

    @Test
    public void testListingCopyingRandomSelection() {
        Assertions.assertNull(TestTrades.interpret(UTILS, GET_OFFER, new CopiedSelectionListing(), TooltipNode.empty()));
    }

    @Test
    public void testListingReshufflingRandomSelection() {
        Assertions.assertNull(TestTrades.interpret(UTILS, GET_OFFER, new ReshuffledListing(), TooltipNode.empty()));
    }

    @Test
    public void testListingPuttingRandomLevelIntoMap() {
        List<IDataNode> trades = ((ListNode) interpret(new ShearsListing())).nodes();
        IDataNode unbreaking = trades.stream().filter((t) -> slot(t, 0).getCount().equals(NumberExpr.constant(14))).findFirst().orElseThrow();
        IDataNode random = trades.stream().filter((t) -> slot(t, 0).getCount().equals(NumberExpr.constant(5))).findFirst().orElseThrow();

        Assertions.assertEquals(3, trades.size());
        Assertions.assertEquals(3, EnchantmentHelper.getItemEnchantmentLevel(enchantment(Enchantments.UNBREAKING), slot(unbreaking, 2).getItem().left().orElseThrow()));
        assertTooltip(slot(random, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testEntityDependentListing() {
        Assertions.assertNull(TestTrades.interpret(UTILS, GET_OFFER, new EntityListing(), TooltipNode.empty()));
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
        Assertions.assertEquals(List.of(Items.ARROW), arrows.options().stream().map((o) -> o.stack().getItem()).toList());
        Assertions.assertEquals(NumberExpr.uniformInt(16, 31), arrows.options().getFirst().count());
        Assertions.assertEquals(NumberExpr.constant(1), apple.options().getFirst().count());
        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), pick.options().stream().map((o) -> o.stack().getItem()).toList());
        Assertions.assertEquals(List.of(0.5, 0.5), pick.options().stream().map(BytecodeInterpreter.Option::chance).toList());
        Assertions.assertSame(random, Suppliers.random);
    }

    @Test
    public void testListingWithSomeVariantsUnknown() {
        IDataNode trade = interpret(new PartlyRandomListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertTooltip(trade.getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Unable to auto-detect some variants of this trade",
                "Random chance"
        ));
        Assertions.assertEquals(List.of("Unable to auto-detect some variants of this trade"), errorLines(trade.getTooltip()));
    }

    @Test
    public void testUnknownPredicateInErrorColor() {
        List<IDataNode> trades = ((ListNode) interpret(new HeightListing())).nodes();

        Assertions.assertEquals(List.of("Unknown additional predicate"), errorLines(trades.getFirst().getTooltip()));
    }

    @Test
    public void testFailingWorldCallIsUnknownPredicate() {
        List<IDataNode> trades = ((ListNode) interpret(new RoomListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertTooltip(trade(trades, Items.APPLE).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Unknown additional predicate"
        ));
        assertTooltip(trade(trades, Items.BREAD).getTooltip(), List.of(
                "Uses: 12",
                "XP: 5",
                "Unknown additional predicate"
        ));
    }

    @Test
    public void testHeavyWorldCallIsOneDecision() {
        IDataNode node = interpret(new ManyChecksListing());
        List<IDataNode> trades = ((ListNode) node).nodes();

        Assertions.assertEquals(List.of(), errorLines(node.getTooltip()));
        Assertions.assertEquals(5, trades.size());
        Assertions.assertEquals(Set.of(Items.APPLE, Items.BREAD, Items.CARROT, Items.WHEAT, Items.POTATO),
                trades.stream().map((t) -> slot(t, 2).getItem().left().orElseThrow().getItem()).collect(Collectors.toSet()));
        trades.forEach((t) -> Assertions.assertEquals(List.of("Unknown additional predicate"), errorLines(t.getTooltip()).stream().distinct().toList()));
    }

    @Test
    public void testWorldCallOverBudgetIsOneDecision() {
        IDataNode trade = interpret(new HeavyCallsListing(1));

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        Assertions.assertEquals(List.of(), errorLines(trade.getTooltip()));
    }

    @Test
    public void testWorldCallsOverBudgetRestartAtMostSevenTimes() {
        Assertions.assertEquals(List.of(), errorLines(interpret(new HeavyCallsListing(7)).getTooltip()));
        Assertions.assertEquals(List.of("Unable to auto-detect some variants of this trade"), errorLines(interpret(new HeavyCallsListing(8)).getTooltip()));
    }

    @Test
    public void testListingCutAtPathLimit() {
        IDataNode trade = interpret(new BranchingListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        Assertions.assertEquals(List.of("Unable to auto-detect some variants of this trade", "Unknown additional predicate"), errorLines(trade.getTooltip()));
    }

    @Test
    public void testRegistriesOfTradersLevel() {
        ServerLevel level = mock(ServerLevel.class);
        IServerUtils utils = spy(UTILS);

        doReturn(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)).when(level).registryAccess();
        doReturn(level).when(utils).getServerLevel();

        IDataNode trade = TestTrades.interpret(utils, GET_OFFER, new RegistryListing(), TooltipNode.empty());

        Assertions.assertNotNull(trade);
        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testStaticFieldWrittenByListing() {
        IDataNode trade = interpret(new CountingListing());

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.constant(3));
        Assertions.assertEquals(0, CountingListing.calls);
    }

    @Test
    public void testListingRecordingTraderStateUnderNumberText() {
        IDataNode trade = interpret(new RerollingListing("apple"));

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(1, 3));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertTooltip(trade.getTooltip(), List.of(
                "Uses: 12",
                "XP: 5"
        ));
    }

    @Test
    public void testListingMappingOptionalCost() {
        IDataNode trade = interpret(new OptionalCostListing(Optional.of(new TradeCost(Items.BOOK, 1, 3))));
        IDataNode single = interpret(new OptionalCostListing(Optional.empty()));

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(trade, 1, Items.BOOK, NumberExpr.uniformInt(1, 3));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        assertSlot(single, 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(single, 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testListingBuildingCostFromStack() {
        IDataNode trade = interpret(new StackPriceListing(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.APPLE, 2)));

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.constant(5));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(2));
    }

    @Test
    public void testListingReadingOptionalStack() {
        IDataNode trade = interpret(new OptionalStackListing());

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.constant(1));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.uniformInt(1, 3));
    }

    @Test
    public void testListingBuildingOfferSubclass() {
        IDataNode trade = interpret(new SubclassOfferListing());

        assertSlot(trade, 0, Items.EMERALD, NumberExpr.uniformInt(2, 4));
        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testStackNamedByRandomText() {
        IDataNode trade = interpret(new TextNameListing(false));

        assertSlot(trade, 2, Items.PAPER, NumberExpr.constant(1));
        assertTooltip(slot(trade, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testStackNamedByTextFromWorld() {
        IDataNode trade = interpret(new TextNameListing(true));

        assertSlot(trade, 2, Items.PAPER, NumberExpr.constant(1));
        assertTooltip(slot(trade, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testStackChangedByWorldValue() {
        IDataNode trade = interpret(new NamedListing());

        assertSlot(trade, 2, Items.PAPER, NumberExpr.constant(1));
        assertTooltip(slot(trade, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testTagChangedByRandomValue() {
        IDataNode trade = interpret(new StarListing());
        ItemStack star = slot(trade, 2).getItem().left().orElseThrow();

        Assertions.assertSame(Items.FIREWORK_STAR, star.getItem());
        Assertions.assertNull(star.get(DataComponents.CUSTOM_DATA));
        assertTooltip(slot(trade, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testListTakingWorldValue() {
        IDataNode trade = interpret(new LoreListing());

        assertSlot(trade, 2, Items.PAPER, NumberExpr.constant(1));
        assertTooltip(slot(trade, 2).getTooltip(), List.of(
                "Random properties"
        ));
    }

    @Test
    public void testUncertainArrayIsNotRead() {
        Assertions.assertNull(TestTrades.interpret(UTILS, GET_OFFER, new StoredDrawListing(), TooltipNode.empty()));
    }

    @Test
    public void testNullCheckNarrowsChoice() {
        IDataNode trade = interpret(new SkippedPickListing());

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD), ((ListNode) ((ListNode) trade).nodes().get(2)).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
    }

    @Test
    public void testBranchNarrowsRandomNumber() {
        List<IDataNode> trades = ((ListNode) interpret(new SquareListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertSlot(trade(trades, Items.APPLE, 1), 0, Items.EMERALD, NumberExpr.constant(5));
        assertSlot(trade(trades, Items.APPLE, 2), 0, Items.EMERALD, NumberExpr.constant(9));
    }

    @Test
    public void testBranchNarrowsRandomNumberInStaticField() {
        List<IDataNode> trades = ((ListNode) interpret(new StoredNumberListing())).nodes();

        Assertions.assertEquals(2, trades.size());
        assertSlot(trade(trades, Items.APPLE, 1), 0, Items.EMERALD, NumberExpr.constant(5));
        assertSlot(trade(trades, Items.APPLE, 2), 0, Items.EMERALD, NumberExpr.constant(9));
        Assertions.assertEquals(0, StoredNumberListing.stored);
    }

    @Test
    public void testSwitchOnRandomChoice() {
        List<IDataNode> trades = ((ListNode) interpret(new SizeListing())).nodes();

        Assertions.assertEquals(3, trades.size());
        assertSlot(trade(trades, Items.APPLE), 0, Items.EMERALD, NumberExpr.constant(1));
        assertSlot(trade(trades, Items.BREAD), 0, Items.EMERALD, NumberExpr.constant(2));
        assertSlot(trade(trades, Items.CARROT), 0, Items.EMERALD, NumberExpr.constant(3));
    }

    @Test
    public void testBranchOnWideRandomRange() {
        List<IDataNode> trades = ((ListNode) Assertions.assertTimeout(Duration.ofSeconds(5), () -> interpret(new WideRangeListing()))).nodes();

        Assertions.assertEquals(2, trades.size());
        trade(trades, Items.APPLE);
        trade(trades, Items.BREAD);
    }

    @Test
    public void testRandomIndexSharedByTwoLists() {
        IDataNode trade = interpret(new ParallelListsListing());
        List<IDataNode> results = ((ListNode) ((ListNode) trade).nodes().get(2)).nodes();

        Assertions.assertEquals(List.of(Items.APPLE, Items.BREAD, Items.CARROT), results.stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
        Assertions.assertEquals(List.of("Red", "Brown", "Orange"), results.stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getHoverName().getString()).toList());
    }

    @Test
    public void testStringConcatenationWithChar() {
        assertSlot(interpret(new CharConcatListing()), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testTraderWithRandomSourceSubtype() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(LegacySourceTrader.class);

        Assertions.assertEquals(new NumberInterval(1, 2, true, true), ((TradeLevel.OfTrades) levels.get(1)).levelInfo().offers().bounds());
    }

    @Test
    public void testDecisionNarrowsChoiceInStaticField() {
        List<IDataNode> trades = ((ListNode) interpret(new StoredPickListing())).nodes();
        IDataNode cheap = trades.stream().filter((t) -> slot(t, 0).getCount().equals(NumberExpr.constant(1))).findFirst().orElseThrow();
        IDataNode expensive = trades.stream().filter((t) -> slot(t, 0).getCount().equals(NumberExpr.constant(2))).findFirst().orElseThrow();

        assertSlot(cheap, 2, Items.APPLE, NumberExpr.constant(1));
        Assertions.assertEquals(List.of(Items.BREAD, Items.CARROT), ((ListNode) ((ListNode) expensive).nodes().get(2)).nodes().stream()
                .map((n) -> ((ItemNode) n).getItem().left().orElseThrow().getItem()).toList());
        Assertions.assertNull(StoredPickListing.stored);
    }

    @Test
    public void testTraderWithOfferThatCannotBeShown() {
        BytecodeInterpreter.ComputedTrades trades = BytecodeInterpreter.getTradeLevels(UTILS, HeldOfferTrader.class);

        Assertions.assertTrue(trades.incomplete());

        List<IDataNode> nodes = ((TradeLevel.OfTrades) trades.levels().get(1)).trades().apply(UTILS);

        Assertions.assertEquals(2, nodes.size());
        assertSlot(nodes.getFirst(), 2, Items.APPLE, NumberExpr.constant(1));
        Assertions.assertInstanceOf(MissingNode.class, nodes.get(1));
    }

    @Test
    public void testListingWithVariantThatCannotBeShown() {
        IDataNode trade = interpret(new HeldItemListing());

        assertSlot(trade, 2, Items.APPLE, NumberExpr.constant(1));
        Assertions.assertEquals(List.of("Unable to auto-detect some variants of this trade"), errorLines(trade.getTooltip()));
    }

    @Test
    public void testTraderWithGroupPickedByRandomChoice() {
        Int2ObjectMap<TradeLevel> levels = computedLevels(ChoiceChanceTrader.class);
        TradeLevel.OfTrades level = (TradeLevel.OfTrades) levels.get(1);

        Assertions.assertEquals(listingItems(Traders.COMMON), levelItems(level));
        Assertions.assertEquals(0.25F, level.levelInfo().chance(), 1e-6);
    }

    @Test
    public void testLambdaInListChangedAfterInitialization() {
        if (ShiftedLambdas.LIST.size() == 2) {
            ShiftedLambdas.LIST.addFirst((world, trader, random) -> offerFor(Items.CARROT));
        }

        assertSlot(interpret(ShiftedLambdas.LIST.get(ShiftedLambdas.LIST.size() - 2)), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testLambdasReturningListsOfStacks() {
        assertSlot(interpret(new ListSupplierListing()), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testComparatorLambdas() {
        assertSlot(interpret(new ComparatorListing()), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testLambdasDrawingManyTimes() {
        assertSlot(interpret(ManyDraws.LISTINGS.getFirst()), 2, Items.APPLE, NumberExpr.constant(1));
    }

    @Test
    public void testLambdasHandingRandomSourceToRealCode() {
        assertSlot(interpret(RealCodeLambdas.LISTINGS.get(1)), 2, Items.BREAD, NumberExpr.constant(1));
    }

    @Test
    public void testLambdasReturningNullOnLowestDraws() {
        assertSlot(interpret(NullLambdas.LISTINGS.get(1)), 2, Items.BREAD, NumberExpr.constant(1));
    }

    private static IDataNode interpret(ItemListing listing) {
        IDataNode node = TestTrades.interpret(UTILS, GET_OFFER, listing, TooltipNode.empty());

        Assertions.assertNotNull(node);
        return node;
    }

    @NotNull
    private static IDataNode trade(List<IDataNode> trades, Item result) {
        return trades.stream().filter((t) -> slot(t, 2).getItem().left().orElseThrow().is(result)).findFirst().orElseThrow();
    }

    @NotNull
    private static IDataNode trade(List<IDataNode> trades, Item result, int count) {
        return trades.stream().filter((t) -> slot(t, 2).getItem().left().orElseThrow().is(result) && slot(t, 2).getCount().equals(NumberExpr.constant(count)))
                .findFirst().orElseThrow();
    }

    @NotNull
    private static List<IDataNode> levelTrades(TradeLevel level) {
        return ((TradeLevel.OfTrades) level).trades().apply(UTILS);
    }

    @Unmodifiable
    @NotNull
    private static List<Item> levelItems(TradeLevel level) {
        return levelTrades(level).stream().map((t) -> slot(t, 2).getItem().left().orElseThrow().getItem()).toList();
    }

    @Unmodifiable
    @NotNull
    private static List<Item> listingItems(ItemListing[] listings) {
        return java.util.Arrays.stream(listings).map((l) -> ((OfferListing) l).item()).toList();
    }

    @NotNull
    private static MerchantOffer offerFor(Item result) {
        return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(result), 12, 5, 0.05F);
    }

    @Unmodifiable
    @NotNull
    private static List<String> errorLines(TooltipNode tooltip) {
        Optional<TextColor> error = Optional.ofNullable(TooltipStyle.DEFAULT.error().getColor());

        return CoreTooltipUtils.toLines(tooltip, 0, true, TooltipStyle.DEFAULT, NumberOptions.DEFAULT).stream()
                .filter(TooltipLine.Text.class::isInstance).map((l) -> ((TooltipLine.Text) l).component())
                .filter((c) -> c.toFlatList().stream().anyMatch((p) -> Optional.ofNullable(p.getStyle().getColor()).equals(error)))
                .map((c) -> componentToPlainString(c).trim()).toList();
    }

    private static ItemNode slot(IDataNode trade, int index) {
        return (ItemNode) ((ListNode) trade).nodes().get(index);
    }

    @Unmodifiable
    @NotNull
    private static List<Map<ResourceKey<Enchantment>, Integer>> enchantments(IDataNode trade, int index) {
        return ((ListNode) ((ListNode) trade).nodes().get(index)).nodes().stream()
                .map((n) -> EnchantmentHelper.getEnchantmentsForCrafting(((ItemNode) n).getItem().left().orElseThrow()).entrySet().stream()
                        .collect(Collectors.toMap((e) -> e.getKey().unwrapKey().orElseThrow(), Object2IntMap.Entry::getIntValue))).toList();
    }

    @NotNull
    private static RegistryAccess withEnchantments(RegistryAccess registries) {
        MappedRegistry<Enchantment> enchantments = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());

        LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach((h) -> Registry.register(enchantments, h.key(), h.value()));
        enchantments.freeze();
        return new RegistryAccess.ImmutableRegistryAccess(Stream.concat(registries.registries().map(RegistryAccess.RegistryEntry::value), Stream.of(enchantments)).toList());
    }

    @NotNull
    private static Holder<Enchantment> enchantment(ResourceKey<Enchantment> key) {
        return LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
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

    private static final class RandomCountListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD, 2 + random.nextInt(3)), new ItemStack(Items.APPLE, random.nextIntBetweenInclusive(1, 4)), 12, 5, 0.05F);
        }
    }

    private interface EmeraldCount {
        default int emeralds(RandomSource random) {
            return 2 + random.nextInt(3);
        }
    }

    private static final class DefaultMethodListing implements ItemListing, EmeraldCount {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds(random)), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class PickListing implements ItemListing {
        private final List<Item> items = List.of(Items.APPLE, Items.BREAD, Items.CARROT);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(items.get(random.nextInt(items.size()))), 12, 5, 0.05F);
        }
    }

    private static final class TagPickListing implements ItemListing {
        private final List<Item> items = BuiltInRegistries.ITEM.stream().filter((i) -> i != Items.AIR).limit(300).toList();

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Item> values = new LinkedList<>(items);

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(values.get(random.nextInt(values.size()))), 12, 5, 0.05F);
        }
    }

    private static final TagKey<Item> DOORS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("ali_test", "doors"));
    private static final TagKey<Block> BLOCK_DOORS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("ali_test", "doors"));
    private static final TagKey<Block> BLOCK_TORCHES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("ali_test", "torches"));

    private static void bindDoorTags() {
        BuiltInRegistries.ITEM.prepareTagReload(new TagLoader.LoadResult<>(Registries.ITEM, Map.of(DOORS, List.<Holder<Item>>of(Items.OAK_DOOR.builtInRegistryHolder(),
                Items.SPRUCE_DOOR.builtInRegistryHolder(), Items.BIRCH_DOOR.builtInRegistryHolder())))).apply();
        BuiltInRegistries.BLOCK.prepareTagReload(new TagLoader.LoadResult<>(Registries.BLOCK, Map.of(BLOCK_DOORS, List.<Holder<Block>>of(Blocks.OAK_DOOR.builtInRegistryHolder(),
                Blocks.SPRUCE_DOOR.builtInRegistryHolder(), Blocks.BIRCH_DOOR.builtInRegistryHolder()),
                BLOCK_TORCHES, List.<Holder<Block>>of(Blocks.TORCH.builtInRegistryHolder(), Blocks.WALL_TORCH.builtInRegistryHolder(),
                Blocks.SOUL_TORCH.builtInRegistryHolder(), Blocks.WATER.builtInRegistryHolder())))).apply();
    }

    private record TagValuesListing(boolean withoutOak) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Item> values = new LinkedList<>();

            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(DOORS)) {
                if (!withoutOak || holder.value() != Items.OAK_DOOR) {
                    values.add(holder.value());
                }
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(values.get(random.nextInt(values.size()))), 12, 5, 0.05F);
        }
    }

    private static final class TagValuesFactoryListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Item> values = new LinkedList<>();

            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(DOORS)) {
                values.add(holder.value());
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), stack(values.get(random.nextInt(values.size())), 1), 12, 5, 0.05F);
        }

        private static ItemStack stack(ItemLike item, int count) {
            return new ItemStack(item, count);
        }
    }

    private record BlockTagValuesListing(TagKey<Block> tag) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Block> values = new LinkedList<>();

            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                values.add(holder.value());
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(values.get(random.nextInt(values.size()))), 12, 5, 0.05F);
        }
    }

    private static final class PotionPickListing implements ItemListing {
        private final List<Holder<Potion>> potions = List.of(Potions.NIGHT_VISION, Potions.INVISIBILITY, Potions.LEAPING);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Holder<Potion> potion = potions.get(random.nextInt(potions.size()));
            ItemStack stack = new ItemStack(Items.POTION);

            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
            return new MerchantOffer(new ItemCost(Items.EMERALD), stack, 12, 5, 0.05F);
        }
    }

    private static final class StackPickListing implements ItemListing {
        private final List<ItemStack> stacks = List.of(new ItemStack(Items.APPLE, 2), new ItemStack(Items.BREAD, 3));

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD), stacks.get(random.nextInt(stacks.size())), 12, 5, 0.05F);
        }
    }

    private static final class EnchantmentPickListing implements ItemListing {
        private final List<Holder<Enchantment>> enchantments = List.of(enchantment(Enchantments.SHARPNESS), enchantment(Enchantments.MENDING),
                enchantment(Enchantments.UNBREAKING));

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Holder<Enchantment> enchantment = enchantments.get(random.nextInt(enchantments.size()));
            ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel()));

            return new MerchantOffer(new ItemCost(Items.EMERALD), Optional.of(new ItemCost(Items.BOOK)), book, 3, 3, 0.2F);
        }
    }

    private static final class EnchantmentLevelListing implements ItemListing {
        private final Holder<Enchantment> enchantment = enchantment(Enchantments.UNBREAKING);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            int level = Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
            ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level));
            int price = 2 + random.nextInt(5 + level * 10) + 3 * level;

            if (price > 64) {
                price = 64;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD, price), Optional.of(new ItemCost(Items.BOOK)), book, 12, 1, 0.2F);
        }
    }

    private record PredicateListing(Supplier<Boolean> predicate) implements ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            if (!predicate.get()) {
                return null;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private record FlagSupplier(Boolean value) implements Supplier<Boolean> {
        @Override
        public Boolean get() {
            return value;
        }
    }

    private record PriceListing(IntUnaryOperator price, boolean random) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD, price.applyAsInt(this.random ? random.nextInt(3) : 2)), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class Prices extends ClassLoader {
        private static final String NAME = "com/yanny/ali/test/GeneratedPrices";
        private final byte[] bytes;

        private Prices(byte[] bytes) {
            super(BytecodeInterpreterTest.class.getClassLoader());
            this.bytes = bytes;
        }

        @NotNull
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

    private static final class NullablePickListing implements ItemListing {
        private final List<String> keys = List.of("apple", "none", "bread");
        private final Map<String, Item> items = Map.of("apple", Items.APPLE, "bread", Items.BREAD);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Item item = Optional.ofNullable(items.get(keys.get(random.nextInt(keys.size())))).orElse(Items.BARRIER);

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(item), 12, 5, 0.05F);
        }
    }

    private static final class RemovingPickListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Item> items = new ArrayList<>(List.of(Items.APPLE, Items.BREAD, Items.CARROT));
            Item picked = items.get(random.nextInt(items.size()));

            items.remove(picked);
            return new MerchantOffer(new ItemCost(Items.EMERALD, items.size()), new ItemStack(picked), 12, 5, 0.05F);
        }
    }

    private static final class BackupMapListing implements ItemListing {
        @SuppressWarnings("resource")
        @Nullable
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
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

            MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecorationTypes.OCEAN_MONUMENT);
            return new MerchantOffer(new ItemCost(Items.EMERALD), Optional.of(new ItemCost(Items.COMPASS)), map, 12, 5, 0.2F);
        }
    }

    private static final class DimensionListing implements ItemListing {
        @SuppressWarnings("resource")
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            if (trader.level().dimension() == Level.NETHER) {
                return offerFor(Items.APPLE);
            }
            if (trader.level().dimension().equals(Level.END)) {
                return offerFor(Items.BREAD);
            }

            return offerFor(Items.CARROT);
        }
    }

    private static final class NetherOnlyListing implements ItemListing {
        @SuppressWarnings("resource")
        @Nullable
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return Level.NETHER.equals(trader.level().dimension()) ? offerFor(Items.APPLE) : null;
        }
    }

    private static final class BiomeListing implements ItemListing {
        @SuppressWarnings("resource")
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(trader.level().getBiome(trader.blockPosition()).is(BiomeTags.IS_OCEAN) ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class VillagerTypeListing implements ItemListing {
        private static final Map<ResourceKey<VillagerType>, Item> ITEMS = Map.of(VillagerType.DESERT, Items.APPLE, VillagerType.SNOW, Items.BREAD);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(ITEMS.getOrDefault(((VillagerDataHolder) trader).getVillagerData().type().unwrapKey().orElseThrow(), Items.CARROT));
        }
    }

    private static final class HeightListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(trader.getY() < 0 ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class CoinListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(random.nextBoolean() ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class DiceListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return switch (random.nextInt(3)) {
                case 0 -> offerFor(Items.APPLE);
                case 1 -> offerFor(Items.BREAD);
                default -> offerFor(Items.CARROT);
            };
        }
    }

    private static final class SameCoinListing implements ItemListing {
        private static final Item HEADS = Items.APPLE;
        private static final Item TAILS = Items.APPLE;

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(random.nextFloat() < 0.5F ? HEADS : TAILS);
        }
    }

    private static final class Traders {
        private static final ItemListing[] COMMON = {new OfferListing(Items.APPLE), new OfferListing(Items.BREAD)};
        private static final ItemListing[] RARE = {new OfferListing(Items.CARROT)};
        private static final Int2ObjectMap<ItemListing[]> BY_LEVEL = new Int2ObjectOpenHashMap<>(Map.of(1, COMMON, 2, RARE));
    }

    private static void addListings(ServerLevel world, Entity trader, RandomSource random, MerchantOffers offers, ItemListing[] listings, int count) {
        List<ItemListing> pool = new ArrayList<>(List.of(listings));
        int added = 0;

        while (added < count && !pool.isEmpty()) {
            MerchantOffer offer = pool.remove(random.nextInt(pool.size())).getOffer(world, trader, random);

            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
    }

    private static Int2ObjectMap<TradeLevel> computedLevels(Class<?> trader) {
        return computedLevels(UTILS, trader);
    }

    private static Int2ObjectMap<TradeLevel> computedLevels(IServerUtils utils, Class<?> trader) {
        BytecodeInterpreter.ComputedTrades trades = BytecodeInterpreter.getTradeLevels(utils, trader);

        Assertions.assertNotNull(trades);
        Assertions.assertFalse(trades.incomplete());
        return trades.levels();
    }

    private static final ResourceKey<TradeSet> FIRST_SET = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("ali_test", "first"));
    private static final ResourceKey<TradeSet> SECOND_SET = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("ali_test", "second"));
    private static final ResourceKey<TradeSet> MISSING_SET = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("ali_test", "missing"));

    private static IServerUtils withTradeSets() {
        MappedRegistry<TradeSet> sets = new MappedRegistry<>(Registries.TRADE_SET, Lifecycle.stable());
        IServerUtils utils = spy(UTILS);

        Registry.register(sets, FIRST_SET, tradeSet(2, Items.APPLE, Items.BREAD, Items.CARROT));
        Registry.register(sets, SECOND_SET, tradeSet(1, Items.DIAMOND));
        sets.freeze();
        doReturn(HolderLookup.Provider.create(Stream.<HolderLookup.RegistryLookup<?>>concat(LOOKUP.listRegistries().filter((r) -> r.key() != Registries.TRADE_SET), Stream.of(sets))))
                .when(utils).lookupProvider();
        return utils;
    }

    private static TradeSet tradeSet(int amount, Item... results) {
        return new TradeSet(HolderSet.direct(Arrays.stream(results).<Holder<VillagerTrade>>map((i) -> Holder.direct(TestTrades.of(i))).toList()), ConstantValue.exactly(amount), false, Optional.empty());
    }

    private abstract static class TradeSetTrader extends AbstractVillager {
        private TradeSetTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addOffersFromTradeSet(world, getOffers(), FIRST_SET);
        }
    }

    private abstract static class LeveledTradeSetTrader extends Villager {
        private static final Int2ObjectMap<ResourceKey<TradeSet>> SETS = new Int2ObjectOpenHashMap<>(Map.of(1, FIRST_SET, 2, SECOND_SET));

        private LeveledTradeSetTrader(EntityType<? extends Villager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            ResourceKey<TradeSet> set = SETS.get(getVillagerData().level());

            if (set != null) {
                addOffersFromTradeSet(world, getOffers(), set);
            }
        }
    }

    private abstract static class TradeSetAndOfferTrader extends AbstractVillager {
        private TradeSetAndOfferTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addOffersFromTradeSet(world, getOffers(), FIRST_SET);
            getOffers().add(offerFor(Items.DIAMOND));
        }
    }

    private abstract static class ChanceTradeSetTrader extends AbstractVillager {
        private ChanceTradeSetTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            if (random.nextFloat() < 0.25F) {
                addOffersFromTradeSet(world, getOffers(), FIRST_SET);
            }
        }
    }

    private abstract static class MissingTradeSetTrader extends AbstractVillager {
        private MissingTradeSetTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addOffersFromTradeSet(world, getOffers(), MISSING_SET);
            getOffers().add(offerFor(Items.DIAMOND));
        }
    }

    private record PlainListing(Item item) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(item);
        }
    }

    private record OfferListing(Item item) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(item);
        }
    }

    private abstract static class OneLevelTrader extends AbstractVillager {
        private OneLevelTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addListings(world, this, random, getOffers(), Traders.COMMON, 2);
        }
    }

    private abstract static class TwoGroupTrader extends AbstractVillager {
        private TwoGroupTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            MerchantOffers offers = getOffers();

            addListings(world, this, random, offers, Traders.COMMON, 1);
            addListings(world, this, random, offers, Traders.RARE, 1);
        }
    }

    private abstract static class KeyedGroupTrader extends AbstractVillager {
        private KeyedGroupTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            MerchantOffers offers = getOffers();

            addListings(world, this, random, offers, Traders.BY_LEVEL.get(1), 2);
            addListings(world, this, random, offers, Traders.BY_LEVEL.get(2), 1);
        }
    }

    private abstract static class RandomIndexTrader extends AbstractVillager {
        private static final Int2ObjectMap<ItemListing[]> TRADES = new Int2ObjectOpenHashMap<>(Map.of(1, Traders.RARE, 2, Traders.COMMON));

        private RandomIndexTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            ItemListing[] listings1 = TRADES.get(1);
            ItemListing[] listings2 = TRADES.get(2);

            if (listings1 != null && listings2 != null) {
                MerchantOffers offers = getOffers();

                addListings(world, this, random, offers, listings1, 1);

                MerchantOffer offer = listings2[random.nextInt(listings2.length)].getOffer(world, this, random);

                if (offer != null) {
                    offers.add(offer);
                }
            }
        }
    }

    private abstract static class RegistryProfessionTrader extends AbstractVillager {
        private static final EntityDataAccessor<String> PROFESSION = SynchedEntityData.defineId(RegistryProfessionTrader.class, EntityDataSerializers.STRING);
        private static final MappedRegistry<TraderProfession> PROFESSIONS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("ali", "trader_profession")), Lifecycle.stable());

        static {
            Registry.register(PROFESSIONS, Identifier.fromNamespaceAndPath("ali", "farmer"), new FarmerProfession());
            Registry.register(PROFESSIONS, Identifier.fromNamespaceAndPath("ali", "smith"), new SmithProfession());
            PROFESSIONS.freeze();
        }

        private RegistryProfessionTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            if (getProfession() != null) {
                addListings(world, this, random, getOffers(), getProfession().getTrades(this), 5);
            }
        }

        private TraderProfession getProfession() {
            return PROFESSIONS.getValue(Identifier.parse(getEntityData().get(PROFESSION)));
        }
    }

    private abstract static class TraderProfession {
        abstract ItemListing[] getTrades(AbstractVillager trader);
    }

    private static final class FarmerProfession extends TraderProfession {
        @Override
        ItemListing[] getTrades(AbstractVillager trader) {
            return new ItemListing[]{new PlainListing(Items.APPLE), new PlainListing(Items.BREAD)};
        }
    }

    private static final class SmithProfession extends TraderProfession {
        @Override
        ItemListing[] getTrades(AbstractVillager trader) {
            return new ItemListing[]{new PlainListing(Items.IRON_INGOT)};
        }
    }

    private abstract static class ShuffledTradesTrader extends AbstractVillager {
        private static final List<SoldItem> TRADES = List.of(new SoldItem(Items.APPLE, false), new SoldItem(Items.BREAD, true), new SoldItem(Items.CARROT, false));

        private ShuffledTradesTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            List<Integer> indices = IntStream.range(0, TRADES.size()).boxed().collect(Collectors.toList());

            Collections.shuffle(indices);
            indices = indices.subList(0, Math.min(TRADES.size(), 2));

            for (Integer index : indices) {
                MerchantOffer offer = TRADES.get(index).create(this, getRandom());

                if (offer != null) {
                    getOffers().add(offer);
                }
            }
        }
    }

    private record SoldItem(Item item, boolean sometimes) {
        @Nullable
        MerchantOffer create(Entity trader, RandomSource random) {
            if (sometimes && random.nextBoolean()) {
                return null;
            }

            return new MatchingOffer(new ItemCost(Items.EMERALD, random.nextIntBetweenInclusive(2, 4)), Optional.empty(), new ItemStack(item), 12, 5, 0.05F);
        }
    }

    private abstract static class TwoStackTrader extends AbstractVillager {
        private TwoStackTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addListings(world, this, random, getOffers(), new ItemListing[]{simpleTrade(new ItemStack(Items.EMERALD, 10), new ItemStack(Items.APPLE), 10)}, 5);
        }

        private static ItemListing simpleTrade(ItemStack cost, ItemStack result, int uses) {
            return (world, trader, random) -> new MerchantOffer(new ItemCost(cost.getItem(), cost.getCount()), result.copy(), uses, 0, 0);
        }
    }

    private abstract static class ProfessionTrader extends Villager {
        private static final Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> TRADES = BuiltInRegistries.VILLAGER_PROFESSION.registryKeySet().stream()
                .filter((p) -> p != VillagerProfession.NONE && p != VillagerProfession.NITWIT)
                .collect(Collectors.toMap((p) -> p, (p) -> {
                    Int2ObjectMap<ItemListing[]> byLevel = new Int2ObjectOpenHashMap<>();

                    for (int level = 1; level <= 5; level++) {
                        byLevel.put(level, new ItemListing[]{new ProfessionListing(p, Items.APPLE), new ProfessionListing(p, Items.BREAD), new ProfessionListing(p, Items.CARROT)});
                    }

                    return byLevel;
                }));

        private ProfessionTrader(EntityType<? extends Villager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            VillagerData data = getVillagerData();
            ResourceKey<VillagerProfession> profession = data.profession().unwrapKey().orElse(null);

            if (profession == null) {
                return;
            }

            Int2ObjectMap<ItemListing[]> byLevel = TRADES.get(profession);

            if (byLevel == null || byLevel.isEmpty()) {
                return;
            }

            ItemListing[] listings = byLevel.get(data.level());

            if (listings != null) {
                addListings(world, this, random, getOffers(), listings, 2);
            }
        }
    }

    private record ProfessionListing(ResourceKey<VillagerProfession> profession, Item item) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(item);
        }
    }

    private abstract static class LeveledTrader extends AbstractVillager {
        @SuppressWarnings("unused")
        private int tradeLevel;

        private LeveledTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        private int getTradeLevel() {
            return tradeLevel;
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            ItemListing[] listings = Traders.BY_LEVEL.get(getTradeLevel());

            if (listings != null) {
                addListings(world, this, random, getOffers(), listings, 2);
            }
        }
    }

    private abstract static class RandomCountTrader extends AbstractVillager {
        private RandomCountTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addListings(world, this, random, getOffers(), Traders.COMMON, random.nextInt(2) + 1);
        }
    }

    private abstract static class OwnOffersTrader extends AbstractVillager {
        private OwnOffersTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @NotNull
        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            offers.add(offerFor(Items.APPLE));
            return offers;
        }
    }

    private abstract static class NullFilteringTrader extends AbstractVillager {
        private NullFilteringTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @NotNull
        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            offers.add(offerFor(Items.CARROT));
            offers.add(missingOffer());
            offers.add(Traders.COMMON[0].getOffer((ServerLevel) level(), this, random));
            offers.removeIf(Objects::isNull);
            return offers;
        }

        @Nullable
        private MerchantOffer missingOffer() {
            return null;
        }
    }

    private abstract static class CollectedListingsTrader extends AbstractVillager {
        private static final List<ItemListing> FILLER = List.of(new OfferListing(Items.APPLE), new OfferListing(Items.BREAD), new OfferListing(Items.CARROT));

        private CollectedListingsTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @NotNull
        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            offers.addAll(createRandomOffers(1, 2));
            return offers;
        }

        @NotNull
        private Collection<MerchantOffer> createRandomOffers(int min, int max) {
            Set<Integer> indices = com.google.common.collect.Sets.newHashSet();
            int count = random.nextIntBetweenInclusive(min, max);

            for (int i = 0; i < 10 && indices.size() < count; i++) {
                indices.add(random.nextInt(FILLER.size()));
            }

            Collection<MerchantOffer> offers = new ArrayList<>();

            for (Integer index : indices) {
                offers.add(FILLER.get(index).getOffer((ServerLevel) level(), this, random));
            }

            return offers;
        }
    }

    private abstract static class CollectedOffersTrader extends AbstractVillager {
        private static final List<MerchantOffer> FILLER = List.of(offerFor(Items.APPLE), offerFor(Items.BREAD), offerFor(Items.CARROT));

        private CollectedOffersTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @NotNull
        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            offers.addAll(createRandomOffers(1, 2));
            return offers;
        }

        @NotNull
        private Collection<MerchantOffer> createRandomOffers(int min, int max) {
            Set<Integer> indices = com.google.common.collect.Sets.newHashSet();
            int count = random.nextIntBetweenInclusive(min, max);

            for (int i = 0; i < 10 && indices.size() < count; i++) {
                indices.add(random.nextInt(FILLER.size()));
            }

            Collection<MerchantOffer> offers = new ArrayList<>();

            for (Integer index : indices) {
                offers.add(FILLER.get(index));
            }

            return offers;
        }
    }

    private abstract static class CollectionTrader extends AbstractVillager {
        private static final List<MerchantOffer> EXTRA = List.of(offerFor(Items.BREAD));

        private CollectionTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @NotNull
        @Override
        public MerchantOffers getOffers() {
            MerchantOffers offers = new MerchantOffers();

            offers.addAll(EXTRA);
            offers.addAll(new ArrayList<>());
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

        @NotNull
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

        private void addRandomOffers(MerchantOffers offers, ItemListing[] listings, int count) {
            ArrayList<ItemListing> remaining = com.google.common.collect.Lists.newArrayList(listings);
            int added = 0;

            while (added < count && !remaining.isEmpty()) {
                MerchantOffer offer = remaining.remove(random.nextInt(remaining.size())).getOffer((ServerLevel) level(), this, random);

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
        protected void updateTrades(ServerLevel world) {
            addTrades(world, getOffers(), Traders.BY_LEVEL.get(1), 1);
            addTrades(world, getOffers(), Traders.BY_LEVEL.get(2), 3);
        }

        private void addTrades(ServerLevel world, MerchantOffers offers, ItemListing[] listings, int count) {
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
                MerchantOffer offer = listings[index].getOffer(world, this, random);

                if (offer != null) {
                    offers.add(offer);
                }
            }
        }
    }

    private abstract static class ShuffleTrader extends AbstractVillager {
        private static final List<ItemListing> LISTINGS = List.of(Traders.COMMON);

        private ShuffleTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            List<Integer> indexes = java.util.stream.IntStream.range(0, LISTINGS.size()).boxed().collect(java.util.stream.Collectors.toList());

            java.util.Collections.shuffle(indexes);

            for (Integer index : indexes.subList(0, Math.min(LISTINGS.size(), 1))) {
                MerchantOffer offer = LISTINGS.get(index).getOffer(world, this, random);

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
        protected void updateTrades(ServerLevel world) {
            int roll = random.nextInt(3);

            if (roll == 0) {
                addListings(world, this, random, getOffers(), Traders.BY_LEVEL.get(1), 1);
            } else if (roll == 1) {
                addListings(world, this, random, getOffers(), Traders.BY_LEVEL.get(2), 1);
            }
        }
    }

    private abstract static class EmptyGroupTrader extends AbstractVillager {
        private static final Map<Rarity, List<ItemListing>> TRADES = Map.of(Rarity.COMMON, List.of(Traders.COMMON), Rarity.RARE, List.of());

        private EmptyGroupTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            for (Rarity rarity : Rarity.values()) {
                List<ItemListing> listings = TRADES.get(rarity);
                List<Integer> indexes = java.util.stream.IntStream.range(0, listings.size()).boxed().collect(java.util.stream.Collectors.toList());
                int count = 1 + random.nextInt(2);

                java.util.Collections.shuffle(indexes);

                for (Integer index : indexes.subList(0, Math.min(listings.size(), count))) {
                    MerchantOffer offer = listings.get(index).getOffer(world, this, random);

                    if (offer != null) {
                        getOffers().add(offer);
                    }
                }
            }
        }
    }

    private abstract static class ChanceTrader extends AbstractVillager {
        private static final Map<Rarity, List<ItemListing>> TRADES = Map.of(Rarity.COMMON, List.of(Traders.COMMON), Rarity.RARE, List.of(Traders.RARE));
        private static final Map<Rarity, Double> CHANCES = Map.of(Rarity.COMMON, 1.0, Rarity.RARE, 0.25);

        private ChanceTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            for (Rarity rarity : Rarity.values()) {
                double chance = CHANCES.get(rarity);

                if (chance < 1 && random.nextDouble() > chance) {
                    continue;
                }

                List<ItemListing> listings = TRADES.get(rarity);
                List<Integer> indexes = java.util.stream.IntStream.range(0, listings.size()).boxed().collect(java.util.stream.Collectors.toList());

                java.util.Collections.shuffle(indexes);

                for (Integer index : indexes.subList(0, Math.min(listings.size(), 2))) {
                    MerchantOffer offer = listings.get(index).getOffer(world, this, random);

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

        @NotNull
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

        @NotNull
        @Override
        public MerchantOffers getOffers() {
            if (offers == null) {
                offers = new MerchantOffers();
                offers.clear();
            }

            return offers;
        }

        @Override
        protected void updateTrades(ServerLevel world) {
        }

        @NotNull
        @Override
        public InteractionResult mobInteract(Player player, InteractionHand hand) {
            if (offers == null || offers.isEmpty()) {
                offers = generateOffers();
            }

            return super.mobInteract(player, hand);
        }

        @NotNull
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
        protected void updateTrades(ServerLevel world) {
            MerchantOffers offers = getOffers();

            for (int i = 0; i < 3; i++) {
                offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 1 + random.nextInt(3)), new ItemStack(Items.APPLE), 12, 5, 0.05F));
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
        protected void updateTrades(ServerLevel world) {
            if (getMainHandItem().isEmpty()) {
                addListings(world, this, random, getOffers(), Traders.COMMON, 2);
            } else {
                getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD, (int) random.nextLong()), new ItemStack(Items.APPLE), 12, 5, 0.05F));
            }
        }
    }

    private abstract static class UnknownTrader extends AbstractVillager {
        private UnknownTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD, (int) random.nextLong()), new ItemStack(Items.APPLE), 12, 5, 0.05F));
        }
    }

    private abstract static class RebuildingTrader extends AbstractVillager {
        private RebuildingTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            ItemListing apple = new PlainListing(Items.APPLE);

            getOffers().add(apple.getOffer(world, this, random));

            if (!getMainHandItem().isEmpty()) {
                getOffers().add(new PlainListing(Items.BREAD).getOffer(world, this, random));
            }
        }
    }

    private abstract static class NoTradesTrader extends AbstractVillager {
        private NoTradesTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
        }
    }

    private abstract static class IdleVillager extends Villager {
        private IdleVillager(EntityType<? extends Villager> type, Level level) {
            super(type, level);
        }
    }

    private static final class MutatingListing implements ItemListing {
        private final ItemStack price = new ItemStack(Items.EMERALD, 1);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            price.setCount(1 + random.nextInt(4));
            return new MerchantOffer(new ItemCost(Items.EMERALD, price.getCount()), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class EntityListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD, trader.getId()), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class Lambdas {
        static final ItemListing FIRST = (world, trader, random) ->
                new MerchantOffer(new ItemCost(Items.EMERALD, random.nextIntBetweenInclusive(1, 3)), new ItemStack(Items.APPLE), 1, 1, 0.05F);
        static final ItemListing SECOND = (world, trader, random) ->
                new MerchantOffer(new ItemCost(Items.EMERALD, random.nextIntBetweenInclusive(5, 9)), new ItemStack(Items.BREAD), 1, 1, 0.05F);
    }

    private static final class Suppliers {
        static Random random = new Random();
        static final Supplier<ItemStack> ARROWS = () -> new ItemStack(Items.ARROW, 16 + random.nextInt(16));
        static final Supplier<ItemStack> APPLE = () -> new ItemStack(Items.APPLE);
        static final Supplier<ItemStack> PICK = () -> new ItemStack(List.of(Items.APPLE, Items.BREAD).get(random.nextInt(2)));
    }

    private static final class PartlyRandomListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return random.nextBoolean() ? offerFor(Items.APPLE) : new MerchantOffer(new ItemCost(Items.EMERALD, (int) random.nextLong()), new ItemStack(Items.BREAD), 12, 5, 0.05F);
        }
    }

    private static final class RoomListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return hasRoom(trader) ? offerFor(Items.APPLE) : offerFor(Items.BREAD);
        }

        private static boolean hasRoom(Entity trader) {
            return new int[trader.getId()].length > 2;
        }
    }

    private static final class ManyChecksListing implements ItemListing {
        @Nullable
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            if (!isCalm(trader)) {
                return null;
            }

            return offerFor(trader.isInWater() ? Items.APPLE : trader.isOnFire() ? Items.BREAD : trader.isCrouching() ? Items.CARROT
                    : trader.isSprinting() ? Items.WHEAT : Items.POTATO);
        }

        private static boolean isCalm(Entity trader) {
            return !trader.isSilent() && !trader.isInLava() && !trader.isPassenger() && !trader.isVehicle() && !trader.isInvisible();
        }
    }

    private record HeavyCallsListing(int calls) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            if (calls > 0) {
                lookAround1(trader);
            }
            if (calls > 1) {
                lookAround2(trader);
            }
            if (calls > 2) {
                lookAround3(trader);
            }
            if (calls > 3) {
                lookAround4(trader);
            }
            if (calls > 4) {
                lookAround5(trader);
            }
            if (calls > 5) {
                lookAround6(trader);
            }
            if (calls > 6) {
                lookAround7(trader);
            }
            if (calls > 7) {
                lookAround8(trader);
            }

            return offerFor(Items.APPLE);
        }

        private static void lookAround1(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround2(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround3(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround4(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround5(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround6(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround7(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }

        private static void lookAround8(Entity trader) {
            for (int i = 0; i < 9; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }
        }
    }

    private static final class BranchingListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            for (int i = 0; i < 8; i++) {
                if (trader.distanceToSqr(i, 0, 0) < 1) {
                    trader.setGlowingTag(true);
                }
            }

            return offerFor(Items.APPLE);
        }
    }

    private static final class RegistryListing implements ItemListing {
        @SuppressWarnings("resource")
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Item item = trader.level().registryAccess().lookupOrThrow(Registries.ITEM).getValue(Identifier.fromNamespaceAndPath("minecraft", "apple"));

            return offerFor(Objects.requireNonNull(item));
        }
    }

    private static final class CountingListing implements ItemListing {
        private static int calls;

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            calls = 3;
            return new MerchantOffer(new ItemCost(Items.EMERALD, calls), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class NamedListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            ItemStack paper = new ItemStack(Items.PAPER);

            name(paper, trader);
            return new MerchantOffer(new ItemCost(Items.EMERALD), paper, 12, 5, 0.05F);
        }

        private static void name(ItemStack stack, Entity trader) {
            stack.set(DataComponents.CUSTOM_NAME, trader.getName());
        }
    }

    private record RerollingListing(String key) implements ItemListing {
        private static final Map<String, GenerateOffer> FUNCTIONS = new HashMap<>();

        static {
            FUNCTIONS.put("apple", (trader, player, random) ->
                    new MerchantOffer(new ItemCost(Items.EMERALD, 1 + random.nextInt(3)), new ItemStack(Items.APPLE), 12, 5, 0.05F));
        }

        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Player player = null;

            if (trader instanceof AbstractVillager villager && !villager.level().isClientSide()) {
                player = villager.getTradingPlayer();
                trader.addTag(String.valueOf(villager.getOffers().size()));
            }

            return FUNCTIONS.get(key).generateOffer(trader, player, random);
        }

        private interface GenerateOffer {
            MerchantOffer generateOffer(Entity trader, @Nullable Player player, RandomSource random);
        }
    }

    private record TradeCost(Item item, int min, int max) {
        ItemCost create(RandomSource random) {
            return new ItemCost(item, random.nextIntBetweenInclusive(min, max));
        }
    }

    private record OptionalCostListing(Optional<TradeCost> secondary) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MerchantOffer(new TradeCost(Items.EMERALD, 2, 4).create(random), secondary.map((c) -> c.create(random)), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private record StackPriceListing(ItemStack price, ItemStack forSale) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            ItemCost cost = new ItemCost(price.typeHolder(), price.getCount(), DataComponentExactPredicate.EMPTY, price);

            return new MerchantOffer(cost, Optional.empty(), forSale, 12, 5, 0.05F);
        }
    }

    private static final class OptionalStackListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Optional<ItemStack> sold = Optional.of(new ItemStack(Items.APPLE, random.nextIntBetweenInclusive(1, 3)));

            if (sold.isEmpty()) {
                return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.BREAD), 12, 5, 0.05F);
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), sold.get(), 12, 5, 0.05F);
        }
    }

    private static final class MatchingOffer extends MerchantOffer {
        MatchingOffer(ItemCost costA, Optional<ItemCost> costB, ItemStack result, int maxUses, int xp, float priceMultiplier) {
            super(costA, costB, result, maxUses, xp, priceMultiplier);
        }

        @Override
        public boolean satisfiedBy(@NotNull ItemStack playerOfferA, @NotNull ItemStack playerOfferB) {
            return false;
        }
    }

    private static final class SubclassOfferListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return new MatchingOffer(new ItemCost(Items.EMERALD, random.nextIntBetweenInclusive(2, 4)), Optional.empty(), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private record TextNameListing(boolean fromWorld) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            ItemStack paper = new ItemStack(Items.PAPER);

            paper.set(DataComponents.CUSTOM_NAME, Component.literal(fromWorld ? trader.getName().getString() : String.valueOf(random.nextInt(3))));
            return new MerchantOffer(new ItemCost(Items.EMERALD), paper, 12, 5, 0.05F);
        }
    }

    private static final class BeehiveListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            ItemStack stack = new ItemStack(Items.BEEHIVE);
            BeehiveBlockEntity hive = new BeehiveBlockEntity(BlockPos.ZERO, Blocks.BEEHIVE.defaultBlockState());

            for (int i = 0; i < 2; i++) {
                Bee bee = new Bee(EntityTypes.BEE, trader.level());

                hive.addOccupant(bee);
            }

            stack.applyComponents(hive.collectComponents());
            stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(BeehiveBlock.HONEY_LEVEL, 0));
            return new MerchantOffer(new ItemCost(Items.EMERALD, 2 + random.nextInt(3)), stack, 12, 5, 0.2F);
        }
    }

    private record LoggingListing(Logger logger) implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            logger.error("Found no structure for the map");
            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.APPLE), 12, 5, 0.05F);
        }
    }

    private static final class CopiedSelectionListing implements ItemListing {
        private final List<Item> items = List.of(Items.APPLE, Items.BREAD, Items.CARROT);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Item> shuffled = new ArrayList<>(items);

            Collections.shuffle(shuffled);

            List<Item> picked = List.copyOf(shuffled.subList(0, 2));

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(picked.isEmpty() ? Items.BARRIER : picked.get(0)), 12, 5, 0.05F);
        }
    }

    private static final class ReshuffledListing implements ItemListing {
        private final List<Item> items = List.of(Items.APPLE, Items.BREAD, Items.CARROT);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Item> shuffled = new ArrayList<>(items);

            Collections.shuffle(shuffled);
            Collections.shuffle(shuffled);
            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(shuffled.get(0)), 12, 5, 0.05F);
        }
    }

    private static final class ShearsListing implements ItemListing {
        private final Holder<Enchantment> unbreaking = enchantment(Enchantments.UNBREAKING);

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            ItemStack shears = new ItemStack(Items.SHEARS);
            int emeralds = 5;
            int level = random.nextDouble() < 0.1 ? 3 : random.nextInt(2) + 1;

            EnchantmentHelper.updateEnchantments(shears, (enchantments) -> enchantments.set(unbreaking, level));

            if (level > 1) {
                emeralds += level * level;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), shears, 12, 5, 0.2F);
        }
    }

    private static final class SquareListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            int level = random.nextInt(2) + 1;
            int emeralds = 5;
            ItemStack apples = new ItemStack(Items.APPLE, level);

            if (level > 1) {
                emeralds += level * level;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), apples, 12, 5, 0.05F);
        }
    }

    private static final class StarListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            CompoundTag explosion = new CompoundTag();
            ItemStack star = new ItemStack(Items.FIREWORK_STAR);

            explosion.putByte("Type", (byte) random.nextInt(4));
            CustomData.set(DataComponents.CUSTOM_DATA, star, explosion);
            return new MerchantOffer(new ItemCost(Items.EMERALD), star, 12, 5, 0.05F);
        }
    }

    private static final class LoreListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            List<Component> lore = new ArrayList<>();
            ItemStack paper = new ItemStack(Items.PAPER);

            lore.add(trader.getName());
            describe(paper, lore);
            return new MerchantOffer(new ItemCost(Items.EMERALD), paper, 12, 5, 0.05F);
        }

        private static void describe(ItemStack stack, List<Component> lore) {
            stack.set(DataComponents.CUSTOM_NAME, lore.get(0));
        }
    }

    private static final class StoredDrawListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            int[] draws = new int[1];

            draws[0] = random.nextInt(3);
            return offerFor(draws[0] == 0 ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class SkippedPickListing implements ItemListing {
        private static final List<Item> ITEMS = Arrays.asList(Items.APPLE, null, Items.BREAD);

        @Nullable
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Item item = ITEMS.get(random.nextInt(ITEMS.size()));

            if (item == null) {
                return null;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(item), 12, 5, 0.05F);
        }
    }

    private enum Size {
        SMALL,
        MEDIUM,
        LARGE,
    }

    private static final class SizeListing implements ItemListing {
        private static final Item[] RESULTS = {Items.APPLE, Items.BREAD, Items.CARROT};

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Size size = Size.values()[random.nextInt(Size.values().length)];
            int price = switch (size) {
                case SMALL -> 1;
                case MEDIUM -> 2;
                case LARGE -> 3;
            };

            return new MerchantOffer(new ItemCost(Items.EMERALD, price), new ItemStack(RESULTS[size.ordinal()]), 12, 5, 0.05F);
        }
    }

    private static final class WideRangeListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(random.nextInt(Integer.MAX_VALUE) < 1000 ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class ParallelListsListing implements ItemListing {
        private static final List<Item> ITEMS = List.of(Items.APPLE, Items.BREAD, Items.CARROT);
        private static final List<String> NAMES = List.of("Red", "Brown", "Orange");

        private static ItemStack named(Item item, String name) {
            ItemStack stack = new ItemStack(item);

            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            return stack;
        }

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            int index = random.nextInt(ITEMS.size());

            return new MerchantOffer(new ItemCost(Items.EMERALD), named(ITEMS.get(index), NAMES.get(index)), 12, 5, 0.05F);
        }
    }

    private static final class CharConcatListing implements ItemListing {
        private static final char[] SUFFIX = {'e'};

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:appl" + SUFFIX[0])));
        }
    }

    private abstract static class LegacySourceTrader extends AbstractVillager {
        private final LegacyRandomSource legacyRandom = new LegacyRandomSource(0);

        private LegacySourceTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            addListings(world, this, random, getOffers(), Traders.COMMON, legacyRandom.nextIntBetweenInclusive(1, 2));
        }
    }

    private static final class StoredNumberListing implements ItemListing {
        private static int stored;

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            int emeralds = 5;

            stored = random.nextInt(2) + 1;

            if (stored > 1) {
                emeralds += stored * stored;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), new ItemStack(Items.APPLE, stored), 12, 5, 0.05F);
        }
    }

    private static final class StoredPickListing implements ItemListing {
        private static final List<Item> ITEMS = List.of(Items.APPLE, Items.BREAD, Items.CARROT);
        private static Item stored;

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            Item item = ITEMS.get(random.nextInt(ITEMS.size()));

            stored = item;

            if (item == Items.APPLE) {
                return new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(stored), 12, 5, 0.05F);
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(stored), 12, 5, 0.05F);
        }
    }

    private abstract static class HeldOfferTrader extends AbstractVillager {
        @SuppressWarnings("unused")
        private MerchantOffer special;

        private HeldOfferTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            MerchantOffers offers = getOffers();

            offers.add(offerFor(Items.APPLE));
            offers.add(special);
        }
    }

    private static final class HeldItemListing implements ItemListing {
        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            if (random.nextBoolean()) {
                return offerFor(Items.APPLE);
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD), ((LivingEntity) trader).getMainHandItem(), 12, 5, 0.05F);
        }
    }

    private abstract static class ChoiceChanceTrader extends AbstractVillager {
        private static final List<ItemListing[]> POOLS = Arrays.asList(Traders.COMMON, null, null, null);

        private ChoiceChanceTrader(EntityType<? extends AbstractVillager> type, Level level) {
            super(type, level);
        }

        @Override
        protected void updateTrades(ServerLevel world) {
            ItemListing[] pool = POOLS.get(random.nextInt(POOLS.size()));

            if (pool != null) {
                addListings(world, this, random, getOffers(), pool, 1);
            }
        }
    }

    private static final class ShiftedLambdas {
        private static final List<ItemListing> LIST = new ArrayList<>();

        static {
            LIST.add((world, trader, random) -> offerFor(Items.APPLE));
            LIST.add((world, trader, random) -> offerFor(Items.BREAD));
        }
    }

    private static final class ListSupplierListing implements ItemListing {
        private static final List<Supplier<List<ItemStack>>> SUPPLIERS = List.of(() -> List.of(new ItemStack(Items.APPLE)), () -> List.of(new ItemStack(Items.BREAD)));

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(SUPPLIERS.get(0).get().get(0).getItem());
        }
    }

    private static final class ComparatorListing implements ItemListing {
        private static final List<Comparator<Integer>> ORDERS = List.of((a, b) -> Integer.signum(a - b), (a, b) -> Integer.signum(b - a));

        @NotNull
        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity trader, RandomSource random) {
            return offerFor(ORDERS.get(0).compare(1, 2) < 0 ? Items.APPLE : Items.BREAD);
        }
    }

    private static final class ManyDraws {
        private static final List<ItemListing> LISTINGS = List.of((world, trader, random) -> {
            for (int i = 0; i < 600; i++) {
                random.nextInt(2);
            }

            return offerFor(Items.APPLE);
        }, (world, trader, random) -> {
            for (int i = 0; i < 600; i++) {
                random.nextInt(2);
            }

            return offerFor(Items.BREAD);
        });
    }

    private static final class RealCodeLambdas {
        private static final List<ItemListing> LISTINGS = List.of((world, trader, random) -> {
            warmUp(random, 20);
            return offerFor(Items.APPLE);
        }, (world, trader, random) -> {
            warmUp(random, 20);
            return offerFor(Items.BREAD);
        });

        private static void warmUp(RandomSource random, int depth) {
            if (depth > 0) {
                warmUp(random, depth - 1);
            }
        }
    }

    private static final class NullLambdas {
        private static final List<ItemListing> LISTINGS = List.of(
                (world, trader, random) -> random.nextInt(2) == 1 ? offerFor(Items.APPLE) : null,
                (world, trader, random) -> random.nextInt(2) == 1 ? offerFor(Items.BREAD) : null
        );
    }
}
