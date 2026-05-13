package com.yanny.ali.fabric.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.fabric.mixin.MixinCombinedIngredient;
import com.yanny.ali.language.Lang;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@SuppressWarnings("UnstableApiUsage")
public class LootJsIngredientTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getCustomIngredientTooltip(IServerUtils utils, CustomIngredientImpl ingredient) {
        CustomIngredient i = ingredient.getCustomIngredient();

        if (i instanceof AnyIngredient anyIngredient) {
            MixinCombinedIngredient combinedIngredient = (MixinCombinedIngredient) anyIngredient;
            Ingredient[] ingredients = combinedIngredient.getIngredients();

            return TooltipBuilder.array((b) -> {
                for (Ingredient i2 : ingredients) {
                    b.add(utils.getValueTooltip(utils, i2));
                }
            }).key(Lang.Branch.ANY);
        } else if (i instanceof AllIngredient allIngredient) {
            MixinCombinedIngredient combinedIngredient = (MixinCombinedIngredient) allIngredient;
            Ingredient[] ingredients = combinedIngredient.getIngredients();

            return TooltipBuilder.array((b) -> {
                for (Ingredient i2 : ingredients) {
                    b.add(utils.getValueTooltip(utils, i2));
                }
            }).key(Lang.Branch.ALL);
        } else if (i == null) {
            LOGGER.warn("NULL custom ingredient");
            return TooltipBuilder.empty();
        } else {
            LOGGER.warn("Missing tooltip for fabric custom ingredient {}", i.getClass().getCanonicalName());
        }

        return TooltipBuilder.empty();
    }
}
