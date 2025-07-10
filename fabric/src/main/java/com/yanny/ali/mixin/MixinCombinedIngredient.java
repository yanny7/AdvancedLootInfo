package com.yanny.ali.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = {"net/fabricmc/fabric/impl/recipe/ingredient/builtin/CombinedIngredient"})
public interface MixinCombinedIngredient {
    @Accessor(remap = false)
    Ingredient[] getIngredients();
}