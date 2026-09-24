package com.yanny.ali.fabric.plugin;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.fabric.mixin.MixinCombinedIngredient;
import com.yanny.ali.fabric.mixin.MixinComponentsIngredient;
import com.yanny.ali.fabric.mixin.MixinCustomDataIngredient;
import com.yanny.ali.fabric.mixin.MixinDifferenceIngredient;
import com.yanny.ali.language.Lang;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CustomDataIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class FabricIngredientTooltipUtils {
    @NotNull
    public static TooltipBuilder getAnyIngredientTooltip(IServerUtils utils, AnyIngredient ingredient) {
        return getCombinedTooltip(utils, (MixinCombinedIngredient) ingredient).key(Lang.Branch.ANY);
    }

    @NotNull
    public static TooltipBuilder getAllIngredientTooltip(IServerUtils utils, AllIngredient ingredient) {
        return getCombinedTooltip(utils, (MixinCombinedIngredient) ingredient).key(Lang.Branch.ALL);
    }

    @NotNull
    public static TooltipBuilder getDifferenceIngredientTooltip(IServerUtils utils, DifferenceIngredient ingredient) {
        MixinDifferenceIngredient accessor = (MixinDifferenceIngredient) ingredient;

        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                .add(utils.getValueTooltip(utils, accessor.getSubtracted()).build(Lang.Branch.SUBTRACTED))
        );
    }

    @NotNull
    public static TooltipBuilder getComponentsIngredientTooltip(IServerUtils utils, ComponentsIngredient ingredient) {
        MixinComponentsIngredient accessor = (MixinComponentsIngredient) ingredient;

        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                .add(utils.getValueTooltip(utils, accessor.getComponents()).build(Lang.Branch.COMPONENTS))
        );
    }

    @NotNull
    public static TooltipBuilder getCustomDataIngredientTooltip(IServerUtils utils, CustomDataIngredient ingredient) {
        MixinCustomDataIngredient accessor = (MixinCustomDataIngredient) ingredient;

        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                .add(utils.getValueTooltip(utils, accessor.getNbt()).build(Lang.Value.NBT))
        );
    }

    @NotNull
    private static TooltipBuilder getCombinedTooltip(IServerUtils utils, MixinCombinedIngredient accessor) {
        List<Ingredient> ingredients = accessor.getIngredients();

        return TooltipBuilder.array((b) -> ingredients.forEach((i) -> b.add(utils.getValueTooltip(utils, i))));
    }
}
