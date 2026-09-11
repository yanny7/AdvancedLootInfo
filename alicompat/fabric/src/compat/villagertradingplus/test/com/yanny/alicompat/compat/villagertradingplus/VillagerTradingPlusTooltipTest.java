package com.yanny.alicompat.compat.villagertradingplus;

import com.google.gson.JsonObject;
import com.lion.villagertradingplus.tradeoffers.ConditionalTradeFactory;
import com.lion.villagertradingplus.tradeoffers.conditions.ParsedConditions;
import com.lion.villagertradingplus.tradeoffers.util.Ingredient;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ListNode;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class VillagerTradingPlusTooltipTest {
    private static final String TRADES = "com.lion.villagertradingplus.tradeoffers.trades.";

    @Test
    public void testBuyItemTradeOffer() {
        assertTooltip(tooltip(factory("JsonBuyItemTradeOffer$Factory", new ItemStack(Items.WHEAT, 5), new ItemStack(Items.EMERALD), 8, 4, 0.1F, 1)), List.of(
                "Uses: 8",
                "XP: 4",
                "Price Multiplier: 0.1"
        ));
    }

    @Test
    public void testSellItemTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellItemTradeOffer$Factory", new ItemStack(Items.BREAD, 3), new ItemStack(Items.EMERALD, 2), 7, 5, 0.15F, 2)), List.of(
                "Uses: 7",
                "XP: 5",
                "Price Multiplier: 0.15"
        ));
    }

    @Test
    public void testMultiInputTradeOffer() {
        assertTooltip(tooltip(factory("JsonMultiInputTradeOffer$Factory", new ItemStack(Items.EMERALD, 2), new ItemStack(Items.WHEAT, 4), new ItemStack(Items.BREAD), 6, 3, 0.2F, 1)), List.of(
                "Uses: 6",
                "XP: 3",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testProcessItemTradeOffer() {
        assertTooltip(tooltip(factory("JsonProcessItemTradeOffer$Factory", new ItemStack(Items.RAW_IRON, 4), new ItemStack(Items.EMERALD), new ItemStack(Items.IRON_INGOT, 4), 9, 6, 0.25F, 3)), List.of(
                "Uses: 9",
                "XP: 6",
                "Price Multiplier: 0.25"
        ));
    }

    @Test
    public void testBuyTaggedItemTradeOffer() {
        assertTooltip(tooltip(factory("JsonBuyTaggedItemTradeOffer$Factory", ingredient("minecraft:planks"), new ItemStack(Items.EMERALD), 5, 2, 0.1F, 1)), List.of(
                "Uses: 5",
                "XP: 2",
                "Price Multiplier: 0.1"
        ));
    }

    @Test
    public void testSellTaggedItemTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellTaggedItemTradeOffer$Factory", ingredient("minecraft:saplings"), new ItemStack(Items.EMERALD, 3), 4, 7, 0.15F, 2)), List.of(
                "Uses: 4",
                "XP: 7",
                "Price Multiplier: 0.15"
        ));
    }

    @Test
    public void testSellEnchantedBookTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellEnchantedBookTradeOffer$Factory", new ItemStack(Items.EMERALD, 10), 3, 12, 0.2F, 2)), List.of(
                "Uses: 3",
                "XP: 12",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSellSpecificEnchantedBookTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellSpecificEnchantedBookTradeOffer$Factory", new ItemStack(Items.EMERALD, 12), Enchantments.SHARPNESS, 2, 6, 15, 0.2F)), List.of(
                "Uses: 6",
                "XP: 15",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSellEnchantedBookFromListTradeOffer() {
        Object entry = construct(TRADES + "JsonSellEnchantedBookFromListTradeOffer$Entry", Enchantments.SHARPNESS, 1, 3, 5);

        assertTooltip(tooltip(factory("JsonSellEnchantedBookFromListTradeOffer$Factory", new ItemStack(Items.EMERALD, 8), List.of(entry), 1, 3, 4, 9, 2, 11, 0.2F, 2)), List.of(
                "Uses: 2",
                "XP: 11",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSellEnchantedToolTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellEnchantedToolTradeOffer$Factory", new ItemStack(Items.EMERALD, 14), new ItemStack(Items.DIAMOND_PICKAXE), 3, 10, 0.2F)), List.of(
                "Uses: 3",
                "XP: 10",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSellSpecificEnchantedToolTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellSpecificEnchantedToolTradeOffer$Factory", new ItemStack(Items.EMERALD, 16), new ItemStack(Items.DIAMOND_SWORD), Enchantments.FIRE_ASPECT, 2, 4, 13, 0.2F)), List.of(
                "Uses: 4",
                "XP: 13",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testSellPotionTradeOffer() {
        assertTooltip(tooltip(factory("JsonSellPotionTradeOffer$Factory", new ItemStack(Items.EMERALD, 4), new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.POTION), 5, 8, 0.15F)), List.of(
                "Uses: 5",
                "XP: 8",
                "Price Multiplier: 0.15"
        ));
    }

    @Test
    public void testSellStructureMapTradeOffer() {
        TagKey<Structure> destination = TagKey.create(Registries.STRUCTURE, new ResourceLocation("minecraft", "village"));

        assertTooltip(tooltip(factory("JsonSellStructureMapTradeOffer$Factory", new ItemStack(Items.EMERALD, 13), new ItemStack(Items.COMPASS), destination, "filled_map.village", 3, 9, 0.2F)), List.of(
                "Uses: 3",
                "XP: 9",
                "Price Multiplier: 0.2"
        ));
    }

    @Test
    public void testWeightedPoolTradeOffer() {
        VillagerTrades.ItemListing listing = factory("JsonSellItemTradeOffer$Factory", new ItemStack(Items.BREAD, 3), new ItemStack(Items.EMERALD, 2), 7, 5, 0.15F, 2);
        Object entry = construct(TRADES + "JsonWeightedPoolTradeOffer$Entry", 4, listing);

        IDataNode node = UTILS.getItemListing(UTILS, factory("JsonWeightedPoolTradeOffer$Factory", List.of(entry), 4), TooltipNode.empty());

        assertTooltip(((ListNode) node).nodes().get(0).getTooltip(), List.of(
                "Chance: 100%",
                "Uses: 7",
                "XP: 5",
                "Price Multiplier: 0.15"
        ));
    }

    @Test
    public void testConditionalTradeFactory() {
        VillagerTrades.ItemListing listing = factory("JsonSellItemTradeOffer$Factory", new ItemStack(Items.BREAD, 3), new ItemStack(Items.EMERALD, 2), 7, 5, 0.15F, 2);

        ParsedConditions.Entry entry = new ParsedConditions.Entry(Component.literal("Only on Sunday"), (e) -> true);

        assertTooltip(tooltip(new ConditionalTradeFactory(listing, new ParsedConditions(List.of(entry), false))), List.of(
                "All Of:",
                "  -> Only on Sunday",
                "Uses: 7",
                "XP: 5",
                "Price Multiplier: 0.15"
        ));
    }

    @NotNull
    private static TooltipNode tooltip(VillagerTrades.ItemListing listing) {
        return UTILS.getItemListing(UTILS, listing, TooltipNode.empty()).getTooltip();
    }

    @NotNull
    private static Ingredient ingredient(String tag) {
        JsonObject json = new JsonObject();

        json.addProperty("tag", tag);
        return Ingredient.fromJson(json);
    }

    @NotNull
    private static VillagerTrades.ItemListing factory(String name, Object... args) {
        return (VillagerTrades.ItemListing) construct(TRADES + name, args);
    }

    @NotNull
    private static Object construct(String className, Object... args) {
        try {
            Constructor<?> constructor = Class.forName(className).getDeclaredConstructors()[0];

            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + className, e);
        }
    }
}
