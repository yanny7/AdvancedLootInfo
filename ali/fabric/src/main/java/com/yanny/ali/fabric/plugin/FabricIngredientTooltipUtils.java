package com.yanny.ali.fabric.plugin;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.fabric.mixin.MixinCombinedIngredient;
import com.yanny.ali.fabric.mixin.MixinDifferenceIngredient;
import com.yanny.ali.fabric.mixin.MixinNbtIngredient;
import com.yanny.ali.language.Lang;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.NbtIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

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
    public static TooltipBuilder getNbtIngredientTooltip(IServerUtils utils, NbtIngredient ingredient) {
        MixinNbtIngredient accessor = (MixinNbtIngredient) ingredient;

        return TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, accessor.getBase()).build(Lang.Branch.BASE))
                .add(utils.getValueTooltip(utils, accessor.getNbt()).build(Lang.Value.NBT))
                .add(utils.getValueTooltip(utils, accessor.isStrict()).build(Lang.Value.EXACT))
        );
    }

    @NotNull
    private static TooltipBuilder getCombinedTooltip(IServerUtils utils, MixinCombinedIngredient accessor) {
        Ingredient[] ingredients = accessor.getIngredients();

        return TooltipBuilder.array((b) -> {
            for (Ingredient i : ingredients) {
                b.add(utils.getValueTooltip(utils, i));
            }
        });
    }
}
