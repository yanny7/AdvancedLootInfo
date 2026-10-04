package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.core.filters.ItemFilterImpl;
import com.yanny.ali.lootjs.server.LootJsGenericTooltipUtils;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.ItemAbilities;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsGenericTooltipTest {
    @Test
    public void testItemFilterConstantTooltip() {
        assertFilter(ItemFilter.NONE, "None");
        assertFilter(ItemFilter.ANY, "Any");
        assertFilter(ItemFilter.EMPTY, "Empty");
        assertFilter(ItemFilter.ARMOR, "Armor");
        assertFilter(ItemFilter.EDIBLE, "Edible");
        assertFilter(ItemFilter.DAMAGEABLE, "Damageable");
        assertFilter(ItemFilter.DAMAGED, "Damaged");
        assertFilter(ItemFilter.ENCHANTED, "Enchanted");
        assertFilter(ItemFilter.BLOCK_ITEM, "Block Item");
    }

    @Test
    public void testItemFilterHasEnchantmentTooltip() {
        ItemFilterImpl.HasEnchantment filter = new ItemFilterImpl.HasEnchantment(
                new IdFilter.ByLocation(ResourceLocation.withDefaultNamespace("fortune")),
                MinMaxBounds.Ints.between(2, 4),
                DataComponents.ENCHANTMENTS
        );

        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(
                "Has Enchantment",
                "  -> Filter:",
                "    -> minecraft:fortune",
                "  -> Levels: 2 to 4",
                "  -> Component: minecraft:enchantments"
        ));
    }

    @Test
    public void testItemFilterEquipmentSlotTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.IsEquipmentSlot(EquipmentSlot.HEAD)).build(), List.of(
                "Equipment Slot",
                "  -> Slot: Head"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.IsEquipmentSlotGroup(EquipmentSlotGroup.ARMOR)).build(), List.of(
                "Equipment Slot Group",
                "  -> Slot Group: Armor"
        ));
    }

    @Test
    public void testItemFilterByItemTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.ByItem(new ItemStack(Items.DIAMOND), true)).build(), List.of(
                "Item",
                "  -> Item:",
                "    -> Item: minecraft:diamond",
                "    -> Count: 1",
                "  -> Check Components: True"
        ));
    }

    @Test
    public void testItemFilterByIngredientTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.ByIngredient(Ingredient.of(Items.DIAMOND, Items.EMERALD))).build(), List.of(
                "Ingredient",
                "  -> Entry:",
                "    -> Item: minecraft:diamond",
                "    -> Count: 1",
                "  -> Entry:",
                "    -> Item: minecraft:emerald",
                "    -> Count: 1"
        ));
    }

    @Test
    public void testItemFilterByTagTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.ByTag(ItemTags.PLANKS)).build(), List.of(
                "Tag",
                "  -> minecraft:planks"
        ));
    }

    @Test
    public void testItemFilterToolActionTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AnyOfToolAction(List.of(ItemAbilities.AXE_DIG), (stack) -> true)).build(), List.of(
                "Any of Tool Actions",
                "  -> Abilities:",
                "    -> axe_dig"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AllOfToolAction(List.of(ItemAbilities.SHOVEL_DIG), (stack) -> true)).build(), List.of(
                "All of Tool Actions",
                "  -> Abilities:",
                "    -> shovel_dig"
        ));
    }

    @Test
    public void testItemFilterCompositeTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.Not(ItemFilter.ARMOR)).build(), List.of(
                "Not",
                "  -> Item Filter: Armor"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AllOf(new ItemFilter[]{ItemFilter.ARMOR, ItemFilter.DAMAGED}, ItemFilter.ANY)).build(), List.of(
                "All Of",
                "  -> Filters:",
                "    -> Armor",
                "    -> Damaged"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AnyOf(new ItemFilter[]{ItemFilter.ARMOR, ItemFilter.DAMAGED}, ItemFilter.NONE)).build(), List.of(
                "Any Of",
                "  -> Filters:",
                "    -> Armor",
                "    -> Damaged"
        ));
    }

    @Test
    public void testItemFilterCustomTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.Custom((stack) -> true, "my filter")).build(), List.of(
                "Custom",
                "  -> Description: my filter"
        ));
    }

    @Test
    public void testItemFilterUnknownTooltip() {
        assertFilter((stack) -> true, "Unknown");
    }

    @Test
    public void testIdFilterTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getIdFilterTooltip(UTILS, new IdFilter.ByLocation(ResourceLocation.withDefaultNamespace("fortune"))).build(), List.of(
                "minecraft:fortune"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getIdFilterTooltip(UTILS, new IdFilter.ByPattern(Pattern.compile("minecraft:.*"))).build(), List.of(
                "Pattern: minecraft:.*"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getIdFilterTooltip(UTILS, new IdFilter.ByMod("lootjs")).build(), List.of(
                "Mod: lootjs"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getIdFilterTooltip(UTILS, new IdFilter.Or(List.of(
                new IdFilter.ByMod("lootjs"),
                new IdFilter.ByLocation(ResourceLocation.withDefaultNamespace("fortune"))
        ))).build(), List.of(
                "Or:",
                "  -> Mod: lootjs",
                "  -> minecraft:fortune"
        ));
    }

    @Test
    public void testUnknownIdFilterDoesNotThrow() {
        assertTooltip(LootJsGenericTooltipUtils.getIdFilterTooltip(UTILS, new UnknownIdFilter()).build(), List.of(
                "Not implemented: [com.yanny.ali.lootjs.test.LootJsGenericTooltipTest$UnknownIdFilter]"
        ));
    }

    @Test
    public void testItemAbilityTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemAbilityTooltip(UTILS, ItemAbilities.AXE_DIG).build(), List.of(
                "axe_dig"
        ));
    }

    private static class UnknownIdFilter implements IdFilter {
        @Override
        public boolean test(ResourceLocation location) {
            return true;
        }
    }

    private static void assertFilter(ItemFilter filter, String expected) {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(expected));
    }
}
