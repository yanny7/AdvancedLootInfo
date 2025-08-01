package com.yanny.ali.test;

import com.yanny.ali.plugin.server.IngredientTooltipUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class IngredientTooltipTest {
    @Test
    public void ingredientTest() {
        assertTooltip(IngredientTooltipUtils.getIngredientTooltip(UTILS, Ingredient.of(Items.ACACIA_FENCE, Items.ACACIA_DOOR)), List.of(
                "Item:",
                "  -> Item: minecraft:acacia_fence",
                "  -> Count: 1",
                "  -> Components:",
                "    -> minecraft:max_stack_size",
                "      -> Value: 64",
                "    -> minecraft:lore",
                "    -> minecraft:enchantments",
                "    -> minecraft:repair_cost",
                "      -> Value: 0",
                "    -> minecraft:attribute_modifiers",
                "      -> Show In Tooltip: true",
                "    -> minecraft:rarity",
                "      -> Rarity: COMMON",
                "Item:",
                "  -> Item: minecraft:acacia_door",
                "  -> Count: 1",
                "  -> Components:",
                "    -> minecraft:max_stack_size",
                "      -> Value: 64",
                "    -> minecraft:lore",
                "    -> minecraft:enchantments",
                "    -> minecraft:repair_cost",
                "      -> Value: 0",
                "    -> minecraft:attribute_modifiers",
                "      -> Show In Tooltip: true",
                "    -> minecraft:rarity",
                "      -> Rarity: COMMON"
        ));
        assertTooltip(IngredientTooltipUtils.getIngredientTooltip(UTILS, Ingredient.of(ItemTags.ACACIA_LOGS)), List.of(
                "Tag: minecraft:acacia_logs"
        ));
    }
}
