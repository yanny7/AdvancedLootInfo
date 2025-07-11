package com.yanny.ali.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.mixin.MixinCombinedIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@SuppressWarnings("UnstableApiUsage")
public class LootJsIngredientTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static ITooltipNode getCustomIngredientTooltip(IServerUtils utils, CustomIngredientImpl ingredient) {
        CustomIngredient i = ingredient.getCustomIngredient();

        if (i instanceof AnyIngredient anyIngredient) {
            MixinCombinedIngredient combinedIngredient = (MixinCombinedIngredient) anyIngredient;
            Ingredient[] ingredients = combinedIngredient.getIngredients();
            ITooltipNode tooltip = new TooltipNode(translatable("ali.property.branch.any"));

            for (Ingredient i2 : ingredients) {
                tooltip.add(utils.getIngredientTooltip(utils, i2));
            }

            return tooltip;
        } else if (i instanceof AllIngredient allIngredient) {
            MixinCombinedIngredient combinedIngredient = (MixinCombinedIngredient) allIngredient;
            Ingredient[] ingredients = combinedIngredient.getIngredients();
            ITooltipNode tooltip = new TooltipNode(translatable("ali.property.branch.all"));

            for (Ingredient i2 : ingredients) {
                tooltip.add(utils.getIngredientTooltip(utils, i2));
            }

            return tooltip;
        } else if (i == null) {
            LOGGER.warn("NULL custom ingredient");
            return TooltipNode.EMPTY;
        } else {
            LOGGER.warn("Missing tooltip for fabric custom ingredient {}", i.getClass().getCanonicalName());
        }

        return TooltipNode.EMPTY;
    }
}
