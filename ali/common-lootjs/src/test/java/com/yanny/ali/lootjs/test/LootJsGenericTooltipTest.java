package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.core.filters.ItemFilterImpl;
import com.yanny.ali.lootjs.server.LootJsGenericTooltipUtils;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
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
        assertFilter(ItemFilter.NONE, "NONE");
        assertFilter(ItemFilter.ANY, "ANY");
        assertFilter(ItemFilter.EMPTY, "EMPTY");
        assertFilter(ItemFilter.ARMOR, "ARMOR");
        assertFilter(ItemFilter.EDIBLE, "EDIBLE");
        assertFilter(ItemFilter.DAMAGEABLE, "DAMAGEABLE");
        assertFilter(ItemFilter.DAMAGED, "DAMAGED");
        assertFilter(ItemFilter.ENCHANTED, "ENCHANTED");
        assertFilter(ItemFilter.BLOCK_ITEM, "BLOCK_ITEM");
    }

    @Test
    public void testItemFilterHasEnchantmentTooltip() {
        ItemFilterImpl.HasEnchantment filter = new ItemFilterImpl.HasEnchantment(
                new IdFilter.ByLocation(Identifier.withDefaultNamespace("fortune")),
                MinMaxBounds.Ints.between(2, 4),
                DataComponents.ENCHANTMENTS
        );

        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(
                "HAS_ENCHANTMENT",
                "  -> Filter:",
                "    -> minecraft:fortune",
                "  -> Levels: 2-4",
                "  -> Component: minecraft:enchantments"
        ));
    }

    @Test
    public void testItemFilterHasComponentTooltip() {
        ItemFilterImpl.HasComponent filter = new ItemFilterImpl.HasComponent(new DataComponentType<?>[]{
                DataComponents.ENCHANTMENTS,
                DataComponents.DAMAGE
        });

        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(
                "HAS_COMPONENT",
                "  -> Components:",
                "    -> minecraft:enchantments",
                "    -> minecraft:damage"
        ));
    }

    @Test
    public void testItemFilterEquipmentSlotTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.IsEquipmentSlot(EquipmentSlot.HEAD)).build(), List.of(
                "EQUIPMENT_SLOT",
                "  -> Slot: Head"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.IsEquipmentSlotGroup(EquipmentSlotGroup.ARMOR)).build(), List.of(
                "EQUIPMENT_SLOT_GROUP",
                "  -> Slot Group: Armor"
        ));
    }

    @Test
    public void testItemFilterByItemTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.ByItem(new ItemStack(Items.DIAMOND), true)).build(), List.of(
                "ITEM",
                "  -> Item:",
                "    -> Item: minecraft:diamond",
                "    -> Count: 1",
                "    -> Components:",
                "      -> minecraft:item_model",
                "        -> Value: minecraft:diamond",
                "      -> minecraft:item_name",
                "        -> Item Name: Diamond",
                "      -> minecraft:provides_trim_material",
                "        -> Value: minecraft:diamond",
                "  -> Check Components: true"
        ));
    }

    @Test
    public void testItemFilterByIngredientTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.ByIngredient(Ingredient.of(Items.DIAMOND, Items.EMERALD))).build(), List.of(
                "INGREDIENT",
                "  -> Item: minecraft:diamond",
                "  -> Item: minecraft:emerald"
        ));
    }

    @Test
    public void testItemFilterByTagTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.ByTag(ItemTags.PLANKS)).build(), List.of(
                "TAG",
                "  -> minecraft:planks"
        ));
    }

    @Test
    public void testItemFilterToolActionTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AnyOfToolAction(List.of(ItemAbilities.AXE_STRIP), (stack) -> true)).build(), List.of(
                "ANY_OF_TOOL_ACTION",
                "  -> Abilities:",
                "    -> axe_strip"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AllOfToolAction(List.of(ItemAbilities.SHOVEL_FLATTEN), (stack) -> true)).build(), List.of(
                "ALL_OF_TOOL_ACTION",
                "  -> Abilities:",
                "    -> shovel_flatten"
        ));
    }

    @Test
    public void testItemFilterCompositeTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.Not(ItemFilter.ARMOR)).build(), List.of(
                "NOT",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AllOf(new ItemFilter[]{ItemFilter.ARMOR, ItemFilter.DAMAGED}, ItemFilter.ANY)).build(), List.of(
                "ALL_OF",
                "  -> Filters:",
                "    -> ARMOR",
                "    -> DAMAGED"
        ));
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.AnyOf(new ItemFilter[]{ItemFilter.ARMOR, ItemFilter.DAMAGED}, ItemFilter.NONE)).build(), List.of(
                "ANY_OF",
                "  -> Filters:",
                "    -> ARMOR",
                "    -> DAMAGED"
        ));
    }

    @Test
    public void testItemFilterCustomTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, new ItemFilterImpl.Custom((stack) -> true, "my filter")).build(), List.of(
                "CUSTOM",
                "  -> Description: my filter"
        ));
    }

    @Test
    public void testItemFilterUnknownTooltip() {
        assertFilter((stack) -> true, "UNKNOWN");
    }

    @Test
    public void testIdFilterTooltip() {
        assertTooltip(LootJsGenericTooltipUtils.getIdFilterTooltip(UTILS, new IdFilter.ByLocation(Identifier.withDefaultNamespace("fortune"))).build(), List.of(
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
                new IdFilter.ByLocation(Identifier.withDefaultNamespace("fortune"))
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
        assertTooltip(LootJsGenericTooltipUtils.getItemAbilityTooltip(UTILS, ItemAbilities.AXE_STRIP).build(), List.of(
                "axe_strip"
        ));
    }

    private static class UnknownIdFilter implements IdFilter {
        @Override
        public boolean test(Identifier location) {
            return true;
        }
    }

    private static void assertFilter(ItemFilter filter, String expected) {
        assertTooltip(LootJsGenericTooltipUtils.getItemFilterTooltip(UTILS, filter).build(), List.of(expected));
    }
}
