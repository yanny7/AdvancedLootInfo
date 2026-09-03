package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.filters.ItemFilter;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.almostreliable.lootjs.loot.condition.AnyStructure;
import com.yanny.ali.lootjs.server.LootJsGenericTooltipUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsGenericTooltipTest {
    @Test
    public void testItemFilterConstantTooltip() {
        assertFilter(ItemFilter.ALWAYS_FALSE, "ALWAYS_FALSE");
        assertFilter(ItemFilter.ALWAYS_TRUE, "ALWAYS_TRUE");
        assertFilter(ItemFilter.SWORD, "SWORD");
        assertFilter(ItemFilter.PICKAXE, "PICKAXE");
        assertFilter(ItemFilter.AXE, "AXE");
        assertFilter(ItemFilter.SHOVEL, "SHOVEL");
        assertFilter(ItemFilter.HOE, "HOE");
        assertFilter(ItemFilter.TOOL, "TOOL");
        assertFilter(ItemFilter.POTION, "POTION");
        assertFilter(ItemFilter.HAS_TIER, "HAS_TIER");
        assertFilter(ItemFilter.PROJECTILE_WEAPON, "PROJECTILE_WEAPON");
        assertFilter(ItemFilter.ARMOR, "ARMOR");
        assertFilter(ItemFilter.WEAPON, "WEAPON");
        assertFilter(ItemFilter.HEAD_ARMOR, "HEAD_ARMOR");
        assertFilter(ItemFilter.CHEST_ARMOR, "CHEST_ARMOR");
        assertFilter(ItemFilter.LEGS_ARMOR, "LEGS_ARMOR");
        assertFilter(ItemFilter.FEET_ARMOR, "FEET_ARMOR");
        assertFilter(ItemFilter.FOOD, "FOOD");
        assertFilter(ItemFilter.DAMAGEABLE, "DAMAGEABLE");
        assertFilter(ItemFilter.DAMAGED, "DAMAGED");
        assertFilter(ItemFilter.ENCHANTABLE, "ENCHANTABLE");
        assertFilter(ItemFilter.ENCHANTED, "ENCHANTED");
        assertFilter(ItemFilter.BLOCK, "BLOCK");
    }

    @Test
    public void testItemFilterHasEnchantmentByLocationTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, ItemFilter.hasEnchantment(
                new ResourceLocationFilter.ByLocation(new ResourceLocation("minecraft", "fortune")), 2, 4
        )).build(), List.of(
                "HAS_ENCHANTMENT",
                "  -> Enchantment: minecraft:fortune",
                "  -> Levels: 2 - 4"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, ItemFilter.hasEnchantment(
                new ResourceLocationFilter.ByLocation(new ResourceLocation("minecraft", "fortune")), 1, 5
        )).build(), List.of(
                "HAS_ENCHANTMENT",
                "  -> Enchantment: minecraft:fortune",
                "  -> Levels: 1 - 5"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, ItemFilter.hasEnchantment(
                new ResourceLocationFilter.ByLocation(new ResourceLocation("minecraft", "fortune"))
        )).build(), List.of(
                "HAS_ENCHANTMENT",
                "  -> Enchantment: minecraft:fortune"
        ));
    }

    @Test
    public void testItemFilterHasEnchantmentByPatternTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, ItemFilter.hasEnchantment(
                new ResourceLocationFilter.ByPattern(Pattern.compile("minecraft:.*")), 2, 5
        )).build(), List.of(
                "HAS_ENCHANTMENT",
                "  -> Enchantment: minecraft:.*",
                "  -> Levels: 2 - 5"
        ));
    }

    @Test
    public void testItemFilterIngredientTooltip() {
        Ingredient ingredient = Ingredient.of(Items.DIAMOND, Items.EMERALD);
        ItemFilter filter = ingredient::test;

        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(
                "INGREDIENT",
                "  -> Entry:",
                "    -> Item: minecraft:diamond",
                "    -> Count: 1",
                "  -> Entry:",
                "    -> Item: minecraft:emerald",
                "    -> Count: 1"
        ));
    }

    @Test
    public void testItemFilterUnknownTooltip() {
        Predicate<ItemStack> notAnItemFilter = (stack) -> true;

        assertFilter(notAnItemFilter, "UNKNOWN");
        assertFilter(ItemFilter.and(ItemFilter.SWORD, ItemFilter.DAMAGED), "UNKNOWN");
        assertFilter(Ingredient.EMPTY, "UNKNOWN");
    }

    @Test
    public void testStructureLocatorTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getStructureLocatorTooltip(UTILS, new AnyStructure.ById(BuiltinStructures.IGLOO)).build(), List.of(
                "minecraft:igloo"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getStructureLocatorTooltip(UTILS, new AnyStructure.ByTag(StructureTags.VILLAGE)).build(), List.of(
                "minecraft:village"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getStructureLocatorTooltip(UTILS, (registry, level, pos) -> null).build(), List.of());
    }

    private static void assertFilter(Predicate<ItemStack> filter, String expected) {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(expected));
    }
}
