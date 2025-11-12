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
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:acacia_fence",
                "    -> Count: 1",
                "  -> Item:",
                "    -> Item: minecraft:acacia_door",
                "    -> Count: 1"
        ));
        assertTooltip(IngredientTooltipUtils.getIngredientTooltip(UTILS, Ingredient.of(ItemTags.ACACIA_LOGS)), List.of(
                "Items:",
                "  -> Tag: minecraft:acacia_logs"
        ));
    }
}
