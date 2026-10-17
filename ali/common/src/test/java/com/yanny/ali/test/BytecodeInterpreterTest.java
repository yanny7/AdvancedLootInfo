package com.yanny.ali.test;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberInterval;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.ali.plugin.server.bytecode.BytecodeInterpreter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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
