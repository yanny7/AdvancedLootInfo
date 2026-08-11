package com.yanny.ali.fabric.plugin;

import com.mojang.logging.LogUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.fabric.mixin.MixinCombinedIngredient;
import com.yanny.ali.fabric.mixin.MixinComponentsIngredient;
import com.yanny.ali.fabric.mixin.MixinCustomDataIngredient;
import com.yanny.ali.fabric.mixin.MixinDifferenceIngredient;
import com.yanny.ali.language.Lang;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CustomDataIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FabricIngredientTooltipUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static TooltipBuilder getCustomIngredientTooltip(IServerUtils utils, CustomIngredientImpl ingredient) {
        CustomIngredient i = ingredient.getCustomIngredient();

        if (i instanceof AnyIngredient anyIngredient) {
            return getCombinedTooltip(utils, anyIngredient).key(Lang.Branch.ANY);
        } else if (i instanceof AllIngredient allIngredient) {
            return getCombinedTooltip(utils, allIngredient).key(Lang.Branch.ALL);
        } else if (i instanceof DifferenceIngredient differenceIngredient) {
            MixinDifferenceIngredient accessor = (MixinDifferenceIngredient) differenceIngredient;

            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                    .add(utils.getValueTooltip(utils, accessor.getSubtracted()).build(Lang.Branch.SUBTRACTED))
            );
        } else if (i instanceof ComponentsIngredient componentsIngredient) {
            MixinComponentsIngredient accessor = (MixinComponentsIngredient) componentsIngredient;

            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                    .add(utils.getValueTooltip(utils, accessor.getComponents()).build(Lang.Branch.COMPONENTS))
            );
        } else if (i instanceof CustomDataIngredient customDataIngredient) {
            MixinCustomDataIngredient accessor = (MixinCustomDataIngredient) customDataIngredient;

            return TooltipBuilder.array((b) -> b
                    .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                    .add(utils.getValueTooltip(utils, accessor.getNbt()).build(Lang.Value.NBT))
            );
        } else if (i == null) {
            LOGGER.warn("NULL custom ingredient");
        } else {
            LOGGER.warn("Missing tooltip for fabric custom ingredient {}", i.getClass().getCanonicalName());
        }

        return TooltipBuilder.empty();
    }

    @NotNull
    private static TooltipBuilder getCombinedTooltip(IServerUtils utils, CustomIngredient ingredient) {
        List<Ingredient> ingredients = ((MixinCombinedIngredient) ingredient).getIngredients();

        return TooltipBuilder.array((b) -> ingredients.forEach((i) -> b.add(utils.getValueTooltip(utils, i))));
    }
}
