package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IIngredientTooltip;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

public class FluidContainerIngredientAccessor extends BaseAccessor<FluidContainerIngredient> implements IIngredientTooltip {
    @FieldAccessor
    private FluidIngredient fluidIngredient;

    @FieldAccessor
    private Ingredient display;

    public FluidContainerIngredientAccessor(FluidContainerIngredient parent) {
        super(parent);
    }

    @NotNull
    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, fluidIngredient.getFluids()).build(Lang.Branch.FLUID));
            b.add(utils.getIngredientTooltip(utils, display).build(MantleLang.Branch.CONTAINER));
        }, MantleLang.Ingredient.FLUID_CONTAINER);
    }
}
