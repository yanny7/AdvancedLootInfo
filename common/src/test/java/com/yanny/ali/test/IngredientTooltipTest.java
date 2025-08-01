package com.yanny.ali.test;

import com.yanny.ali.plugin.server.IngredientTooltipUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class IngredientTooltipTest {
    @Test
    public void ingredientTest() {
        assertTooltip(IngredientTooltipUtils.getIngredientTooltip(UTILS, Ingredient.of(Items.ACACIA_FENCE, Items.ACACIA_DOOR)), List.of(
                "Item: minecraft:acacia_fence",
                "Item: minecraft:acacia_door"
        ));
        assertTooltip(IngredientTooltipUtils.getIngredientTooltip(UTILS, Ingredient.of(Blocks.ACACIA_LOG)), List.of(
                "Item: minecraft:acacia_log"
        ));
    }
}
