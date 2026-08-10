package com.yanny.ali.fabric.mixin;

import net.fabricmc.fabric.impl.recipe.ingredient.builtin.DifferenceIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DifferenceIngredient.class)
public interface MixinDifferenceIngredient {
    @Accessor(value = "base", remap = false)
    Ingredient getBase();

    @Accessor(value = "subtracted", remap = false)
    Ingredient getSubtracted();
}
