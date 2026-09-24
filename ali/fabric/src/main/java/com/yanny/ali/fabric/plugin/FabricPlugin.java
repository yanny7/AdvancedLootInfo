package com.yanny.ali.fabric.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AllIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.CustomDataIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
@AliEntrypoint
public class FabricPlugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "fabric";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerIngredientUnwrapper(Ingredient::getCustomIngredient);

        registry.registerValueTooltip(AnyIngredient.class, FabricIngredientTooltipUtils::getAnyIngredientTooltip);
        registry.registerValueTooltip(AllIngredient.class, FabricIngredientTooltipUtils::getAllIngredientTooltip);
        registry.registerValueTooltip(DifferenceIngredient.class, FabricIngredientTooltipUtils::getDifferenceIngredientTooltip);
        registry.registerValueTooltip(ComponentsIngredient.class, FabricIngredientTooltipUtils::getComponentsIngredientTooltip);
        registry.registerValueTooltip(CustomDataIngredient.class, FabricIngredientTooltipUtils::getCustomDataIngredientTooltip);
    }
}
